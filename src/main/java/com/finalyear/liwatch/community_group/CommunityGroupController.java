package com.finalyear.liwatch.community_group;

import com.finalyear.liwatch.Post.PostResponseDto;
import com.finalyear.liwatch.community_group_members.CommunityGroupMember;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class CommunityGroupController {

    private final CommunityGroupService groupService;

    public CommunityGroupController(CommunityGroupService groupService) {
        this.groupService = groupService;
    }

    // 1. Create a new community group
    @PostMapping
    public ResponseEntity<CommunityGroup> createGroup(@RequestBody CommunityGroup group) {
        CommunityGroup createdGroup = groupService.createGroup(group);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdGroup);
    }

    // 2. List/search all active groups
    @GetMapping
    public ResponseEntity<List<CommunityGroup>> searchGroups(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String q) {
        List<CommunityGroup> groups = groupService.searchGroups(location, category, q);
        return ResponseEntity.ok(groups);
    }

    // 3. Get details of a specific group
    @GetMapping("/{group_id}")
    public ResponseEntity<CommunityGroup> getGroupDetails(@PathVariable("group_id") Long groupId) {
        CommunityGroup group = groupService.getGroupById(groupId);
        return ResponseEntity.ok(group);
    }

    // 4. Authenticated user joins a group
    @PostMapping("/{group_id}/join")
    public ResponseEntity<CommunityGroupMember> joinGroup(@PathVariable("group_id") Long groupId) {
        CommunityGroupMember membership = groupService.joinGroup(groupId);
        return ResponseEntity.status(HttpStatus.CREATED).body(membership);
    }

    // 5. Owner/admin removes a member
    @DeleteMapping("/{group_id}/members/{user_id}")
    public ResponseEntity<Void> removeMember(
            @PathVariable("group_id") Long groupId,
            @PathVariable("user_id") Long userId) {
        groupService.removeMember(groupId, userId);
        return ResponseEntity.noContent().build();
    }

    // 6. Owner promotes member to Admin / approves request
    @PatchMapping("/{group_id}/members/{user_id}")
    public ResponseEntity<CommunityGroupMember> promoteMember(
            @PathVariable("group_id") Long groupId,
            @PathVariable("user_id") Long userId) {
        CommunityGroupMember updatedMember = groupService.promoteMember(groupId, userId);
        return ResponseEntity.ok(updatedMember);
    }

    // 7. Owner/admin suspends or reactivates a group
    @PatchMapping("/{group_id}/status")
    public ResponseEntity<CommunityGroup> updateGroupStatus(
            @PathVariable("group_id") Long groupId,
            @RequestParam("status") String status) {
        CommunityGroup updatedGroup = groupService.updateGroupStatus(groupId, status);
        return ResponseEntity.ok(updatedGroup);
    }

    // 8. Get all posts scoped to this group
    @GetMapping("/{group_id}/listings")
    public ResponseEntity<List<PostResponseDto>> getGroupListings(@PathVariable("group_id") Long groupId) {
        List<PostResponseDto> listings = groupService.getGroupListings(groupId);
        return ResponseEntity.ok(listings);
    }
}
