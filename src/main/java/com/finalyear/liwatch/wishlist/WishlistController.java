package com.finalyear.liwatch.wishlist;

import com.finalyear.liwatch.Post.PostResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @PostMapping("/toggle/{postId}")
    public ResponseEntity<Map<String, Boolean>> toggleWishlist(@PathVariable Long postId) {
        boolean added = wishlistService.toggleWishlist(postId);
        return ResponseEntity.ok(Map.of("inWishlist", added));
    }

    @GetMapping("/status/{postId}")
    public ResponseEntity<Map<String, Boolean>> getWishlistStatus(@PathVariable Long postId) {
        boolean inWishlist = wishlistService.isInWishlist(postId);
        return ResponseEntity.ok(Map.of("inWishlist", inWishlist));
    }

    @GetMapping("/my-wishlist")
    public ResponseEntity<List<PostResponseDto>> getMyWishlist() {
        List<PostResponseDto> wishlist = wishlistService.getUserWishlist();
        return ResponseEntity.ok(wishlist);
    }
}
