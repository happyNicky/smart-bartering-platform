package com.finalyear.liwatch.community_group;

import com.finalyear.liwatch.Notification.Notification;
import com.finalyear.liwatch.Notification.NotificationService;
import com.finalyear.liwatch.Post.Post;
import com.finalyear.liwatch.Post.PostRepository;
import com.finalyear.liwatch.Post.PostResponseDto;
import com.finalyear.liwatch.Post.utils.PostUtilMethods;
import com.finalyear.liwatch.media.postMedia.PostMediaDto;
import com.finalyear.liwatch.community_group_members.CommunityGroupMember;
import com.finalyear.liwatch.community_group_members.CommunityGroupMemberRepository;
import com.finalyear.liwatch.community_group_members.cg_enums.Role;
import com.finalyear.liwatch.community_group_members.cg_enums.Status;
import com.finalyear.liwatch.rating.service.RatingService;
import com.finalyear.liwatch.trust.dto.TrustResponseDto;
import com.finalyear.liwatch.userManagement.model.User;
import com.finalyear.liwatch.userManagement.repository.UserRepository;
import com.finalyear.liwatch.userManagement.utils.classes.UserUtilService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CommunityGroupService {

    private final CommunityGroupRepository groupRepository;
    private final CommunityGroupMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final UserUtilService userUtilService;
    private final NotificationService notificationService;
    private final PostRepository postRepository;
    private final GroupChatMessageRepository groupChatMessageRepository;
    private final RatingService ratingService;

    public CommunityGroupService(CommunityGroupRepository groupRepository,
                                 CommunityGroupMemberRepository memberRepository,
                                 UserRepository userRepository,
                                 UserUtilService userUtilService,
                                 NotificationService notificationService,
                                 PostRepository postRepository,
                                 GroupChatMessageRepository groupChatMessageRepository,
                                 RatingService ratingService) {
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
        this.userUtilService = userUtilService;
        this.notificationService = notificationService;
        this.postRepository = postRepository;
        this.groupChatMessageRepository = groupChatMessageRepository;
        this.ratingService = ratingService;
    }

    public CommunityGroup createGroup(CommunityGroup group) {
        User currentUser = userUtilService.getCurrentlyAuthenticatedUser();

        // Rule 1: Only active, enabled users can create groups
        if (!currentUser.isEnabled() || currentUser.getStatus() != com.finalyear.liwatch.userManagement.utils.enums.Status.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only active, enabled users may create community groups.");
        }

        // Validation Rule: Unique Group Name
        if (group.getGroupName() == null || group.getGroupName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group name is required.");
        }
        if (groupRepository.existsByGroupName(group.getGroupName().trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group name must be unique.");
        }

        // Validation Rule: General Category must be selected
        if (group.getGroupCategory() == null || group.getGroupCategory().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "General Category is required.");
        }

        String cat = group.getGroupCategory().trim().toUpperCase();
        if (!cat.equals("ITEM_SWAP") && !cat.equals("SERVICE_SWAP")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid General Category. Must be Item Swap Group or Service Swap Group.");
        }
        group.setGroupCategory(cat);

        // Validation Rule: Subcategory for Item groups
        if (cat.equals("ITEM_SWAP")) {
            if (group.getItemSubcategory() == null || group.getItemSubcategory().trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item Subcategory is required for Item Swap groups.");
            }
            String sub = group.getItemSubcategory().trim();
            List<String> validSubs = List.of(
                    "Electronics & Tech",
                    "Vehicles & Parts",
                    "Home & Furniture",
                    "Clothing & Fashion",
                    "Books & Education",
                    "Sports & Outdoors",
                    "Tools & Equipment"
            );
            boolean isValid = validSubs.stream().anyMatch(s -> s.equalsIgnoreCase(sub));
            if (!isValid) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Item Subcategory. Value must come from the standard list and cannot be 'Other Items' or null/empty.");
            }
        } else {
            // Service group has no subcategories
            group.setItemSubcategory(null);
        }

        // Populate compatibility columns
        if (group.getCategory() == null) {
            group.setCategory(cat.equals("ITEM_SWAP") ? group.getItemSubcategory() : "Service");
        }
        if (group.getLocation() == null) {
            group.setLocation("Global");
        }
        if (group.getIsPrivate() == null) {
            group.setIsPrivate(false);
        }

        group.setOwnerUserId(currentUser.getId());
        group.setCreatedAt(LocalDateTime.now());
        group.setStatus(CommunityGroup.Status.ACTIVE);

        CommunityGroup savedGroup = groupRepository.save(group);

        // Creator automatically becomes group owner with role ADMIN (status APPROVED)
        CommunityGroupMember creatorMember = CommunityGroupMember.builder()
                .groupId(savedGroup.getGroupId())
                .userId(currentUser.getId())
                .role(Role.ADMIN)
                .status(Status.APPROVED)
                .joinedAt(LocalDateTime.now())
                .build();

        memberRepository.save(creatorMember);

        return savedGroup;
    }

    public List<CommunityGroup> searchGroups(String location, String category, String q) {
        User currentUser = userUtilService.getCurrentlyAuthenticatedUser();
        List<CommunityGroup> active = groupRepository.searchGroups(CommunityGroup.Status.ACTIVE, currentUser.getId(), location, category, q);
        List<CommunityGroup> suspended = groupRepository.searchGroups(CommunityGroup.Status.SUSPENDED, currentUser.getId(), location, category, q);

        List<CommunityGroup> result = new ArrayList<>(active);
        for (CommunityGroup group : suspended) {
            if (memberRepository.existsByGroupIdAndUserId(group.getGroupId(), currentUser.getId())
                    || Objects.equals(group.getOwnerUserId(), currentUser.getId())) {
                result.add(group);
            }
        }

        for (CommunityGroup group : result) {
            group.setMemberCount(memberRepository.countByGroupId(group.getGroupId()));
            calculateAndSetGroupAverageRating(group);
        }

        return result;
    }

    public CommunityGroup getGroupById(Long groupId) {
        CommunityGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found with id " + groupId));
        
        if (group.getStatus() == CommunityGroup.Status.SUSPENDED) {
            User currentUser = userUtilService.getCurrentlyAuthenticatedUser();
            boolean isMember = memberRepository.existsByGroupIdAndUserId(groupId, currentUser.getId());
            if (!isMember && !Objects.equals(group.getOwnerUserId(), currentUser.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied. Group is suspended.");
            }
        }
        
        group.setMemberCount(memberRepository.countByGroupId(groupId));
        calculateAndSetGroupAverageRating(group);
        return group;
    }

    private void calculateAndSetGroupAverageRating(CommunityGroup group) {
        List<CommunityGroupMember> members = memberRepository.findByGroupId(group.getGroupId());
        if (members.isEmpty()) {
            group.setGroupAverageRating(0.0);
            return;
        }
        double totalRating = 0;
        int count = 0;
        for (CommunityGroupMember m : members) {
            try {
                TrustResponseDto trust = ratingService.getTrustForUser(m.getUserId());
                if (trust != null && trust.getAverageRating() != null) {
                    totalRating += trust.getAverageRating().doubleValue();
                    count++;
                }
            } catch (Exception e) {
                // Ignore missing profiles or rating errors for individual users
            }
        }
        group.setGroupAverageRating(count > 0 ? totalRating / count : 0.0);
    }

    public CommunityGroupMember joinGroup(Long groupId) {
        User currentUser = userUtilService.getCurrentlyAuthenticatedUser();

        // Verification check
        if (!currentUser.isEnabled() || currentUser.getStatus() != com.finalyear.liwatch.userManagement.utils.enums.Status.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only active, enabled users may join community groups.");
        }

        CommunityGroup group = getGroupById(groupId);
        if (group.getStatus() == CommunityGroup.Status.SUSPENDED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot join a suspended community group.");
        }

        if (Boolean.TRUE.equals(group.getIsPrivate())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot join a private community group directly.");
        }

        // Enforce membership uniqueness
        if (memberRepository.existsByGroupIdAndUserId(groupId, currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You are already a member of this group, or your request is pending.");
        }

        CommunityGroupMember membership = CommunityGroupMember.builder()
                .groupId(groupId)
                .userId(currentUser.getId())
                .role(Role.MEMBER)
                .status(Status.PENDING)
                .joinedAt(LocalDateTime.now())
                .build();

        CommunityGroupMember savedMembership = memberRepository.save(membership);

        // Notify Group Owner
        User owner = userRepository.findById(group.getOwnerUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Group owner not found."));

        notificationService.createNotification(
                owner.getId(),
                owner.getEmail(),
                "New Group Join Request",
                "A user has requested to join your group " + group.getGroupName(),
                "GroupActivity"
        );

        return savedMembership;
    }

    public void removeMember(Long groupId, Long userId) {
        User currentUser = userUtilService.getCurrentlyAuthenticatedUser();
        CommunityGroup group = getGroupById(groupId);

        if (group.getStatus() == CommunityGroup.Status.SUSPENDED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Group is suspended. Actions are blocked.");
        }

        // Fetch performing user's membership to check permissions
        CommunityGroupMember actorMember = memberRepository.findByGroupIdAndUserId(groupId, currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied."));

        // Only group owner/admin can remove a member, EXCEPT when a member is leaving themselves
        boolean isOwner = Objects.equals(group.getOwnerUserId(), currentUser.getId());
        boolean isAdmin = actorMember.getRole() == Role.ADMIN;
        boolean isSelf = Objects.equals(userId, currentUser.getId());
        if (!isOwner && !isAdmin && !isSelf) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only group owners, admins, or the members themselves can remove membership.");
        }

        // Find the member to be removed
        CommunityGroupMember targetMember = memberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found in this group."));

        // Prevent removing the owner
        if (Objects.equals(group.getOwnerUserId(), userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The group owner cannot be removed.");
        }

        memberRepository.delete(targetMember);

        // Notify the removed user
        User targetUser = userRepository.findById(userId).orElse(null);
        if (targetUser != null && !isSelf) {
            notificationService.createNotification(
                    targetUser.getId(),
                    targetUser.getEmail(),
                    "Removed from Group",
                    "You have been removed from group " + group.getGroupName(),
                    "GroupActivity"
            );
        }
    }

    public CommunityGroupMember promoteMember(Long groupId, Long userId) {
        User currentUser = userUtilService.getCurrentlyAuthenticatedUser();
        CommunityGroup group = getGroupById(groupId);

        if (group.getStatus() == CommunityGroup.Status.SUSPENDED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Group is suspended. Actions are blocked.");
        }

        // Rule 2: Only group owner can promote members (one owner per group)
        if (!Objects.equals(group.getOwnerUserId(), currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the group owner can promote members to Admin.");
        }

        CommunityGroupMember targetMember = memberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found in this group."));

        targetMember.setRole(Role.ADMIN);
        targetMember.setStatus(Status.APPROVED); // Auto-approve if they were pending
        return memberRepository.save(targetMember);
    }

    public CommunityGroupMember demoteMember(Long groupId, Long userId) {
        User currentUser = userUtilService.getCurrentlyAuthenticatedUser();
        CommunityGroup group = getGroupById(groupId);

        if (group.getStatus() == CommunityGroup.Status.SUSPENDED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Group is suspended. Actions are blocked.");
        }

        if (!Objects.equals(group.getOwnerUserId(), currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the group owner can demote Admins.");
        }

        if (Objects.equals(group.getOwnerUserId(), userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The group owner cannot be demoted.");
        }

        CommunityGroupMember targetMember = memberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found in this group."));

        if (targetMember.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not an Admin.");
        }

        targetMember.setRole(Role.MEMBER);
        return memberRepository.save(targetMember);
    }

    public CommunityGroup updateGroupStatus(Long groupId, String statusStr) {
        User currentUser = userUtilService.getCurrentlyAuthenticatedUser();
        CommunityGroup group = getGroupById(groupId);

        // Fetch performing user's membership to check permissions
        CommunityGroupMember actorMember = memberRepository.findByGroupIdAndUserId(groupId, currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied."));

        boolean isOwner = Objects.equals(group.getOwnerUserId(), currentUser.getId());
        boolean isAdmin = actorMember.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only group owners or admins can modify group status.");
        }

        try {
            CommunityGroup.Status newStatus = CommunityGroup.Status.valueOf(statusStr.toUpperCase());
            group.setStatus(newStatus);
            return groupRepository.save(group);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status value: " + statusStr);
        }
    }

    public List<PostResponseDto> getGroupListings(Long groupId) {
        User currentUser = userUtilService.getCurrentlyAuthenticatedUser();

        // Must be a member to see group listings
        CommunityGroupMember member = memberRepository.findByGroupIdAndUserId(groupId, currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "You must be a member of the group to view its listings."));

        if (member.getStatus() != Status.APPROVED) {
             throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Your membership is pending approval.");
        }

        List<Post> posts = postRepository.findByGroupIdAndStatus(groupId, com.finalyear.liwatch.Post.enums.Status.ACTIVE);
        List<PostResponseDto> responseDtos = new ArrayList<>();

        for (Post post : posts) {
            // Map PostMedia
            List<PostMediaDto> mediaDtos = post.getPostImages().stream()
                    .map(media -> new PostMediaDto(media.getPostImageUrl()))
                    .toList();

            User user = post.getUser();
            PostResponseDto dto = PostUtilMethods.getPostResponseDtoFromPost(user, post, mediaDtos);
            dto.setGroupId(post.getGroupId());
            dto.setIsGroupOnly(post.getIsGroupOnly());

            if (post instanceof com.finalyear.liwatch.Item.Item item) {
                dto.setItem(PostUtilMethods.createItemResponseDtoFromItem(item));
            } else if (post instanceof com.finalyear.liwatch.service.Service service) {
                dto.setService(PostUtilMethods.createServiceResponseDtoFromService(service));
            }

            responseDtos.add(dto);
        }

        return responseDtos;
    }

    public List<GroupChatMessage> getGroupChatHistory(Long groupId) {
        User currentUser = userUtilService.getCurrentlyAuthenticatedUser();

        CommunityGroupMember member = memberRepository.findByGroupIdAndUserId(groupId, currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "You must be a member of the group to view its chat history."));

        if (member.getStatus() != Status.APPROVED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Your membership is pending approval.");
        }

        return groupChatMessageRepository.findByGroupIdOrderBySentAtAsc(groupId);
    }

    public GroupChatMessage saveGroupMessage(Long groupId, Long senderId, String content) {
        User currentUser = userRepository.findById(senderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));

        CommunityGroupMember member = memberRepository.findByGroupIdAndUserId(groupId, currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "You must be a member of the group to send messages."));

        if (member.getStatus() != Status.APPROVED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Your membership is pending approval.");
        }

        GroupChatMessage message = GroupChatMessage.builder()
                .groupId(groupId)
                .sender(currentUser)
                .messageText(content)
                .sentAt(LocalDateTime.now())
                .build();

        return groupChatMessageRepository.save(message);
    }

    public List<GroupMemberResponseDto> getGroupMembers(Long groupId) {
        User currentUser = userUtilService.getCurrentlyAuthenticatedUser();
        CommunityGroup group = getGroupById(groupId);
        
        boolean isMember = memberRepository.existsByGroupIdAndUserId(groupId, currentUser.getId());
        boolean isOwner = Objects.equals(group.getOwnerUserId(), currentUser.getId());
        
        if (group.getIsPrivate() && !isMember && !isOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You must be a member of this private group to view its members.");
        }

        List<CommunityGroupMember> memberships = memberRepository.findByGroupId(groupId).stream()
                .filter(m -> m.getStatus() == Status.APPROVED || m.getUserId().equals(currentUser.getId()))
                .collect(Collectors.toList());
        List<GroupMemberResponseDto> memberDtos = new java.util.ArrayList<>();
        for (CommunityGroupMember m : memberships) {
            User user = userRepository.findById(m.getUserId()).orElse(null);
            if (user != null) {
                String badgeLabel = "Level 1";
                java.math.BigDecimal avgRating = java.math.BigDecimal.ZERO;
                try {
                    TrustResponseDto trust = ratingService.getTrustForUser(user.getId());
                    if (trust != null && trust.getBadgeLevel() != null) {
                        badgeLabel = trust.getBadgeLevel().getLevel();
                        avgRating = trust.getAverageRating();
                    }
                } catch (Exception e) {
                    // Ignore profile not found
                }

                memberDtos.add(new GroupMemberResponseDto(
                        user.getId(),
                        user.getFullName(),
                        user.getEmail(),
                        m.getRole(),
                        m.getStatus(),
                        m.getJoinedAt(),
                        badgeLabel,
                        avgRating
                ));
            }
        }
        return memberDtos;
    }

    public List<GroupMemberResponseDto> getPendingJoinRequests(Long groupId) {
        User currentUser = userUtilService.getCurrentlyAuthenticatedUser();
        CommunityGroup group = getGroupById(groupId);

        CommunityGroupMember actorMember = memberRepository.findByGroupIdAndUserId(groupId, currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied."));

        boolean isOwner = Objects.equals(group.getOwnerUserId(), currentUser.getId());
        boolean isAdmin = actorMember.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only group owners or admins can view join requests.");
        }

        List<CommunityGroupMember> memberships = memberRepository.findByGroupId(groupId).stream()
                .filter(m -> m.getStatus() == Status.PENDING)
                .collect(Collectors.toList());

        List<GroupMemberResponseDto> memberDtos = new java.util.ArrayList<>();
        for (CommunityGroupMember m : memberships) {
            User user = userRepository.findById(m.getUserId()).orElse(null);
            if (user != null) {
                String badgeLabel = "Level 1";
                java.math.BigDecimal avgRating = java.math.BigDecimal.ZERO;
                try {
                    TrustResponseDto trust = ratingService.getTrustForUser(user.getId());
                    if (trust != null && trust.getBadgeLevel() != null) {
                        badgeLabel = trust.getBadgeLevel().getLevel();
                        avgRating = trust.getAverageRating();
                    }
                } catch (Exception e) {
                }

                memberDtos.add(new GroupMemberResponseDto(
                        user.getId(),
                        user.getFullName(),
                        user.getEmail(),
                        m.getRole(),
                        m.getStatus(),
                        m.getJoinedAt(),
                        badgeLabel,
                        avgRating
                ));
            }
        }
        return memberDtos;
    }

    public CommunityGroupMember approveJoinRequest(Long groupId, Long userId) {
        User currentUser = userUtilService.getCurrentlyAuthenticatedUser();
        CommunityGroup group = getGroupById(groupId);

        CommunityGroupMember actorMember = memberRepository.findByGroupIdAndUserId(groupId, currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied."));

        boolean isOwner = Objects.equals(group.getOwnerUserId(), currentUser.getId());
        boolean isAdmin = actorMember.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only group owners or admins can approve join requests.");
        }

        CommunityGroupMember targetMember = memberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Join request not found."));

        if (targetMember.getStatus() != Status.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not in pending state.");
        }

        targetMember.setStatus(Status.APPROVED);
        CommunityGroupMember saved = memberRepository.save(targetMember);

        User targetUser = userRepository.findById(userId).orElse(null);
        if (targetUser != null) {
            notificationService.createNotification(
                    targetUser.getId(),
                    targetUser.getEmail(),
                    "Group Join Request Approved",
                    "Your request to join " + group.getGroupName() + " has been approved.",
                    "GroupActivity"
            );
        }
        return saved;
    }

    public void rejectJoinRequest(Long groupId, Long userId) {
        User currentUser = userUtilService.getCurrentlyAuthenticatedUser();
        CommunityGroup group = getGroupById(groupId);

        CommunityGroupMember actorMember = memberRepository.findByGroupIdAndUserId(groupId, currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied."));

        boolean isOwner = Objects.equals(group.getOwnerUserId(), currentUser.getId());
        boolean isAdmin = actorMember.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only group owners or admins can reject join requests.");
        }

        CommunityGroupMember targetMember = memberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Join request not found."));

        if (targetMember.getStatus() != Status.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not in pending state.");
        }

        memberRepository.delete(targetMember);
    }

    public void deleteGroup(Long groupId) {
        User currentUser = userUtilService.getCurrentlyAuthenticatedUser();
        CommunityGroup group = getGroupById(groupId);

        if (group.getStatus() == CommunityGroup.Status.SUSPENDED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Group is suspended. Actions are blocked.");
        }

        if (!Objects.equals(group.getOwnerUserId(), currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the group creator can delete this group.");
        }

        List<CommunityGroupMember> members = memberRepository.findByGroupId(groupId);
        memberRepository.deleteAll(members);

        groupRepository.delete(group);
    }

    public CommunityGroup updateGroupDetails(Long groupId, String newName, String newCoverImage) {
        User currentUser = userUtilService.getCurrentlyAuthenticatedUser();
        CommunityGroup group = getGroupById(groupId);

        if (group.getStatus() == CommunityGroup.Status.SUSPENDED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Group is suspended. Actions are blocked.");
        }

        CommunityGroupMember actorMember = memberRepository.findByGroupIdAndUserId(groupId, currentUser.getId())
                .orElse(null);

        boolean isOwner = Objects.equals(group.getOwnerUserId(), currentUser.getId());
        boolean isAdmin = actorMember != null && actorMember.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only Owner or Admin can update group details.");
        }

        if (newName != null && !newName.trim().isEmpty() && !newName.trim().equals(group.getGroupName())) {
            if (groupRepository.existsByGroupName(newName.trim())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A group with this name already exists.");
            }
            group.setGroupName(newName.trim());
        }

        if (newCoverImage != null) {
            group.setCoverImageUrl(newCoverImage);
        }

        return groupRepository.save(group);
    }
}

