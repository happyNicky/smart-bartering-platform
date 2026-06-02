package com.finalyear.liwatch.community_group;

import com.finalyear.liwatch.Post.PostResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupPostController {

    private final GroupPostService groupPostService;

    public GroupPostController(GroupPostService groupPostService) {
        this.groupPostService = groupPostService;
    }

    // 1. Share a listing into a group
    @PostMapping("/{groupId}/posts")
    public ResponseEntity<GroupPostResponseDto> sharePost(
            @PathVariable Long groupId,
            @RequestParam Long postId) {
        GroupPostResponseDto shared = groupPostService.sharePost(groupId, postId);
        return ResponseEntity.status(HttpStatus.CREATED).body(shared);
    }

    // 2. Fetch feed (search, filter, sort)
    @GetMapping("/{groupId}/posts")
    public ResponseEntity<List<GroupPostResponseDto>> getGroupFeed(
            @PathVariable Long groupId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false, defaultValue = "newest") String sortBy) {
        List<GroupPostResponseDto> feed = groupPostService.getGroupFeed(groupId, search, category, location, sortBy);
        return ResponseEntity.ok(feed);
    }

    // 3. Remove a listing from a group
    @DeleteMapping("/{groupId}/posts/{postId}")
    public ResponseEntity<Void> removeGroupPost(
            @PathVariable Long groupId,
            @PathVariable Long postId) {
        groupPostService.removeGroupPost(groupId, postId);
        return ResponseEntity.noContent().build();
    }

    // 4. Get active listings eligible for sharing to this group
    @GetMapping("/{groupId}/eligible-listings")
    public ResponseEntity<List<PostResponseDto>> getEligibleListings(
            @PathVariable Long groupId) {
        List<PostResponseDto> listings = groupPostService.getEligibleListings(groupId);
        return ResponseEntity.ok(listings);
    }
}
