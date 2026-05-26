package com.finalyear.liwatch.community_group;

import com.finalyear.liwatch.Notification.Notification;
import com.finalyear.liwatch.Notification.NotificationRepository;
import com.finalyear.liwatch.Post.Post;
import com.finalyear.liwatch.Post.PostRepository;
import com.finalyear.liwatch.Post.PostResponseDto;
import com.finalyear.liwatch.Post.utils.PostUtilMethods;
import com.finalyear.liwatch.media.postMedia.PostMediaDto;
import com.finalyear.liwatch.community_group_members.CommunityGroupMember;
import com.finalyear.liwatch.community_group_members.CommunityGroupMemberRepository;
import com.finalyear.liwatch.community_group_members.cg_enums.Role;
import com.finalyear.liwatch.community_group_members.cg_enums.Status;
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

@Service
public class CommunityGroupService {

    private final CommunityGroupRepository groupRepository;
    private final CommunityGroupMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final UserUtilService userUtilService;
    private final NotificationRepository notificationRepository;
    private final PostRepository postRepository;
    private final GroupChatMessageRepository groupChatMessageRepository;

    public CommunityGroupService(CommunityGroupRepository groupRepository,
                                 CommunityGroupMemberRepository memberRepository,
                                 UserRepository userRepository,
                                 UserUtilService userUtilService,
                                 NotificationRepository notificationRepository,
                                 PostRepository postRepository,
                                 GroupChatMessageRepository groupChatMessageRepository) {
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
        this.userUtilService = userUtilService;
        this.notificationRepository = notificationRepository;
        this.postRepository = postRepository;
        this.groupChatMessageRepository = groupChatMessageRepository;
    }

    public CommunityGroup createGroup(CommunityGroup group) {
        User currentUser = userUtilService.getCurrentlyAuthenticatedUser();

        // Rule 1: Only active, verified users can create groups
        if (!currentUser.isVerified() || currentUser.getStatus() != com.finalyear.liwatch.userManagement.utils.enums.Status.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only active, verified users may create community groups.");
        }

        // Rule 3: Group must have at least one of location or category set
        boolean hasLocation = group.getLocation() != null && !group.getLocation().trim().isEmpty();
        boolean hasCategory = group.getCategory() != null && !group.getCategory().trim().isEmpty();
        if (!hasLocation && !hasCategory) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group must define a location, a category, or both.");
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
        return groupRepository.searchGroups(CommunityGroup.Status.ACTIVE, location, category, q);
    }

    public CommunityGroup getGroupById(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found with id " + groupId));
    }

    public CommunityGroupMember joinGroup(Long groupId) {
        User currentUser = userUtilService.getCurrentlyAuthenticatedUser();

        // Verification check
        if (!currentUser.isVerified() || currentUser.getStatus() != com.finalyear.liwatch.userManagement.utils.enums.Status.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only active, verified users may join community groups.");
        }

        CommunityGroup group = getGroupById(groupId);
        if (group.getStatus() == CommunityGroup.Status.SUSPENDED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot join a suspended community group.");
        }

        // Enforce membership uniqueness
        if (memberRepository.existsByGroupIdAndUserId(groupId, currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You are already a member of this group, or your request is pending.");
        }

        CommunityGroupMember membership = CommunityGroupMember.builder()
                .groupId(groupId)
                .userId(currentUser.getId())
                .role(Role.MEMBER)
                .status(Status.APPROVED) // Immediate approval for simplicity/notification coherence
                .joinedAt(LocalDateTime.now())
                .build();

        CommunityGroupMember savedMembership = memberRepository.save(membership);

        // Notify Group Owner
        User owner = userRepository.findById(group.getOwnerUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Group owner not found."));

        Notification notification = Notification.builder()
                .userId(owner.getId())
                .emailAddress(owner.getEmail())
                .subject("New Group Member")
                .body("A new member joined your group " + group.getGroupName())
                .sentAt(LocalDateTime.now())
                .status(com.finalyear.liwatch.Notification.enum_notification.Status.PENDING)
                .type("GroupActivity")
                .build();

        notificationRepository.save(notification);

        return savedMembership;
    }

    public void removeMember(Long groupId, Long userId) {
        User currentUser = userUtilService.getCurrentlyAuthenticatedUser();
        CommunityGroup group = getGroupById(groupId);

        // Fetch performing user's membership to check permissions
        CommunityGroupMember actorMember = memberRepository.findByGroupIdAndUserId(groupId, currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied."));

        // Only group owner/admin can remove a member
        boolean isOwner = Objects.equals(group.getOwnerUserId(), currentUser.getId());
        boolean isAdmin = actorMember.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only group owners or admins can remove members.");
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
        if (targetUser != null) {
            Notification notification = Notification.builder()
                    .userId(targetUser.getId())
                    .emailAddress(targetUser.getEmail())
                    .subject("Removed from Group")
                    .body("You have been removed from group " + group.getGroupName())
                    .sentAt(LocalDateTime.now())
                    .status(com.finalyear.liwatch.Notification.enum_notification.Status.PENDING)
                    .type("GroupActivity")
                    .build();

            notificationRepository.save(notification);
        }
    }

    public CommunityGroupMember promoteMember(Long groupId, Long userId) {
        User currentUser = userUtilService.getCurrentlyAuthenticatedUser();
        CommunityGroup group = getGroupById(groupId);

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

        List<Post> posts = postRepository.findByGroupId(groupId);
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

    public GroupChatMessage saveGroupMessage(Long groupId, String content) {
        User currentUser = userUtilService.getCurrentlyAuthenticatedUser();

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
}
