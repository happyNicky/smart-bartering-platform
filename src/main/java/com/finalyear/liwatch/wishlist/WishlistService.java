package com.finalyear.liwatch.wishlist;

import com.finalyear.liwatch.Post.Post;
import com.finalyear.liwatch.Post.PostRepository;
import com.finalyear.liwatch.Post.PostResponseDto;
import com.finalyear.liwatch.Post.PostService;
import com.finalyear.liwatch.userManagement.model.User;
import com.finalyear.liwatch.userManagement.utils.classes.UserUtilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserUtilService userUtilService;

    @Autowired
    private PostService postService;

    public boolean toggleWishlist(Long postId) {
        User user = userUtilService.getCurrentlyAuthenticatedUser();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        if (wishlistRepository.existsByUserIdAndPostPostId(user.getId(), postId)) {
            wishlistRepository.deleteByUserIdAndPostPostId(user.getId(), postId);
            return false; // Removed
        } else {
            Wishlist wishlist = Wishlist.builder()
                    .userId(user.getId())
                    .post(post)
                    .build();
            wishlistRepository.save(wishlist);
            return true; // Added
        }
    }

    public boolean isInWishlist(Long postId) {
        try {
            User user = userUtilService.getCurrentlyAuthenticatedUser();
            return wishlistRepository.existsByUserIdAndPostPostId(user.getId(), postId);
        } catch (Exception e) {
            return false;
        }
    }

    public List<PostResponseDto> getUserWishlist() {
        User user = userUtilService.getCurrentlyAuthenticatedUser();
        List<Wishlist> wishlistItems = wishlistRepository.findByUserId(user.getId());
        return wishlistItems.stream()
                .map(item -> postService.convertToDto(item.getPost()))
                .collect(Collectors.toList());
    }
}
