package com.finalyear.liwatch.community_group;

import com.finalyear.liwatch.Post.Post;
import com.finalyear.liwatch.Post.PostRepository;
import com.finalyear.liwatch.Post.PostResponseDto;
import com.finalyear.liwatch.Post.PostService;
import com.finalyear.liwatch.Post.enums.PostType;
import com.finalyear.liwatch.community_group_members.CommunityGroupMember;
import com.finalyear.liwatch.community_group_members.CommunityGroupMemberRepository;
import com.finalyear.liwatch.userManagement.model.User;
import com.finalyear.liwatch.userManagement.utils.classes.UserUtilService;
import org.springframework.data.domain.Sort;
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
public class GroupPostService {

    private final GroupPostRepository groupPostRepository;
    private final CommunityGroupRepository groupRepository;
    private final CommunityGroupMemberRepository memberRepository;
    private final PostRepository postRepository;
    private final PostService postService;
    private final UserUtilService userUtilService;

    public GroupPostService(GroupPostRepository groupPostRepository,
                            CommunityGroupRepository groupRepository,
                            CommunityGroupMemberRepository memberRepository,
                            PostRepository postRepository,
                            PostService postService,
                            UserUtilService userUtilService) {
        this.groupPostRepository = groupPostRepository;
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.postRepository = postRepository;
        this.postService = postService;
        this.userUtilService = userUtilService;
    }

    public GroupPostResponseDto sharePost(Long groupId, Long postId) {
        User currentUser = userUtilService.getCurrentlyAuthenticatedUser();
        CommunityGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found."));

        // If group is suspended, no actions are allowed
        if (group.getStatus() == CommunityGroup.Status.SUSPENDED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Group is suspended. Actions are blocked.");
        }

        // Must be an approved member
        CommunityGroupMember membership = memberRepository.findByGroupIdAndUserId(groupId, currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "You must be a member to share posts in this group."));
        if (membership.getStatus() != com.finalyear.liwatch.community_group_members.cg_enums.Status.APPROVED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Your membership is pending approval.");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found."));

        // Must own the listing
        if (!Objects.equals(post.getUser().getId(), currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only share your own listings.");
        }

        // Listing must be active
        if (post.getStatus() != com.finalyear.liwatch.Post.enums.Status.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only active listings can be shared.");
        }

        // Apply eligibility rules
        if ("ITEM_SWAP".equalsIgnoreCase(group.getGroupCategory())) {
            if (post.getPostType() != PostType.ITEM) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only Item listings can be posted in this group.");
            }
            if (group.getItemSubcategory() == null || !group.getItemSubcategory().equalsIgnoreCase(post.getCategory())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Listing category must match the group's subcategory: " + group.getItemSubcategory());
            }
        } else if ("SERVICE_SWAP".equalsIgnoreCase(group.getGroupCategory())) {
            if (post.getPostType() != PostType.SERVICE) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only Service listings can be posted in this group.");
            }
        }

        // Uniqueness check
        if (groupPostRepository.existsByGroupGroupIdAndPostPostId(groupId, postId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This listing is already shared in the group.");
        }

        GroupPost groupPost = GroupPost.builder()
                .group(group)
                .post(post)
                .sharedAt(LocalDateTime.now())
                .build();

        GroupPost saved = groupPostRepository.save(groupPost);
        return convertToDto(saved);
    }

    public List<GroupPostResponseDto> getGroupFeed(Long groupId, String search, String category, String location, String sortBy) {
        User currentUser = userUtilService.getCurrentlyAuthenticatedUser();
        CommunityGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found."));

        boolean isMember = memberRepository.existsByGroupIdAndUserId(groupId, currentUser.getId());
        boolean isOwner = Objects.equals(group.getOwnerUserId(), currentUser.getId());

        // Suspended group check: Non-members cannot access
        if (group.getStatus() == CommunityGroup.Status.SUSPENDED && !isMember && !isOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied. Group is suspended.");
        }
        if (group.getIsPrivate() && !isMember && !isOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied. Group is private.");
        }

        // Sort configuration
        Sort sort = Sort.by(Sort.Direction.DESC, "sharedAt"); // default newest first
        if ("oldest".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.ASC, "sharedAt");
        } else if ("activity".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.DESC, "post.createdAt");
        }

        List<GroupPost> posts = groupPostRepository.searchGroupPosts(groupId, search, category, location, sort);

        return posts.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public void removeGroupPost(Long groupId, Long postId) {
        User currentUser = userUtilService.getCurrentlyAuthenticatedUser();
        CommunityGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found."));

        // If group is suspended, no actions are allowed
        if (group.getStatus() == CommunityGroup.Status.SUSPENDED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Group is suspended. Actions are blocked.");
        }

        GroupPost gp = groupPostRepository.findByGroupGroupIdAndPostPostId(groupId, postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group post reference not found."));

        boolean isGroupOwner = Objects.equals(group.getOwnerUserId(), currentUser.getId());
        boolean isPostOwner = Objects.equals(gp.getPost().getUser().getId(), currentUser.getId());

        // Check if admin membership
        boolean isGroupAdmin = memberRepository.findByGroupIdAndUserId(groupId, currentUser.getId())
                .map(m -> m.getRole() == com.finalyear.liwatch.community_group_members.cg_enums.Role.ADMIN)
                .orElse(false);

        if (!isGroupOwner && !isPostOwner && !isGroupAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to delete this post.");
        }

        groupPostRepository.delete(gp);
    }

    public List<PostResponseDto> getEligibleListings(Long groupId) {
        User currentUser = userUtilService.getCurrentlyAuthenticatedUser();
        CommunityGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found."));

        // If group is suspended, actions are blocked
        if (group.getStatus() == CommunityGroup.Status.SUSPENDED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Group is suspended.");
        }

        // Fetch user's posts
        List<Post> userPosts = postRepository.findByUser(currentUser, org.springframework.data.domain.Pageable.unpaged()).getContent();

        // Filter active listings matching eligibility rules and not already shared
        return userPosts.stream()
                .filter(p -> p.getStatus() == com.finalyear.liwatch.Post.enums.Status.ACTIVE)
                .filter(p -> {
                    if ("ITEM_SWAP".equalsIgnoreCase(group.getGroupCategory())) {
                        return p.getPostType() == PostType.ITEM &&
                                group.getItemSubcategory() != null &&
                                group.getItemSubcategory().equalsIgnoreCase(p.getCategory());
                    } else if ("SERVICE_SWAP".equalsIgnoreCase(group.getGroupCategory())) {
                        return p.getPostType() == PostType.SERVICE;
                    }
                    return false;
                })
                .filter(p -> !groupPostRepository.existsByGroupGroupIdAndPostPostId(groupId, p.getPostId()))
                .map(postService::convertToDto)
                .collect(Collectors.toList());
    }

    private GroupPostResponseDto convertToDto(GroupPost gp) {
        Post post = gp.getPost();
        if (post instanceof org.hibernate.proxy.HibernateProxy) {
            post = (Post) ((org.hibernate.proxy.HibernateProxy) post).getHibernateLazyInitializer().getImplementation();
        }
        return GroupPostResponseDto.builder()
                .groupPostId(gp.getGroupPostId())
                .groupId(gp.getGroup().getGroupId())
                .sharedAt(gp.getSharedAt())
                .postDetails(postService.convertToDto(post))
                .build();
    }
}
