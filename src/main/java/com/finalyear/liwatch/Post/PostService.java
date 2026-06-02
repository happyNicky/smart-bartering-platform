package com.finalyear.liwatch.Post;

import com.finalyear.liwatch.Item.Item;
import com.finalyear.liwatch.Item.ItemRequestDto;
import com.finalyear.liwatch.Post.enums.PostType;
import com.finalyear.liwatch.Post.utils.PostUtilMethods;
import com.finalyear.liwatch.media.postMedia.PostMedia;
import com.finalyear.liwatch.media.postMedia.PostMediaDto;
import com.finalyear.liwatch.media.postMedia.PostMediaRepository;
import com.finalyear.liwatch.service.Service;
import com.finalyear.liwatch.service.ServiceRequestDto;
import com.finalyear.liwatch.userManagement.DTO.UserSummeryDto;
import com.finalyear.liwatch.userManagement.model.User;
import com.finalyear.liwatch.userManagement.repository.UserRepository;
import com.finalyear.liwatch.userManagement.service.UserService;
import com.finalyear.liwatch.userManagement.utils.classes.UserUtilService;
import com.finalyear.liwatch.userManagement.utils.enums.Role;
import com.finalyear.liwatch.community_group.CommunityGroup;
import com.finalyear.liwatch.community_group.CommunityGroupRepository;
import com.finalyear.liwatch.community_group_members.CommunityGroupMember;
import com.finalyear.liwatch.community_group_members.CommunityGroupMemberRepository;
import com.finalyear.liwatch.Notification.Notification;
import com.finalyear.liwatch.Notification.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import java.time.LocalDateTime;
import java.util.Objects;

import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import java.util.ArrayList;
import java.util.List;


@org.springframework.stereotype.Service
public class PostService {

   // injection
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostMediaRepository postMediaRepository;
    @Autowired
    private UserUtilService userUtilService;

    @Autowired
    private CommunityGroupRepository communityGroupRepository;

    @Autowired
    private CommunityGroupMemberRepository communityGroupMemberRepository;

    @Autowired
    private NotificationRepository notificationRepository;




    // create a single post
    // create a single post
    public PostResponseDto createPost(PostRequestDto postItem) {

        if (postItem.getPostType()== PostType.ITEM && postItem.getItem() == null) {
            throw new RuntimeException("Item data required");
        }

        if (postItem.getPostType()==PostType.SERVICE&& postItem.getService() == null) {
            throw new RuntimeException("Service data required");
        }
        //get currently authenticated user
        User user= userUtilService.getCurrentlyAuthenticatedUser();

        if (postItem.getGroupId() != null) {
            CommunityGroup group = communityGroupRepository.findById(postItem.getGroupId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));
            if (group.getStatus() != CommunityGroup.Status.ACTIVE) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot post listings to a suspended group.");
            }
            boolean isMember = communityGroupMemberRepository.findByGroupIdAndUserId(group.getGroupId(), user.getId())
                    .map(m -> m.getStatus() == com.finalyear.liwatch.community_group_members.cg_enums.Status.APPROVED)
                    .orElse(false);
            if (!isMember) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You must be an approved member of this community group to post listings.");
            }
        }

        // create a new post and set data from the post request dto
        Post post;


        if(postItem.getPostType()==PostType.ITEM)
        {
            //create item from request
            Item item= PostUtilMethods.createItemFromRequest(postItem);
            item.setUser(user);

            //set the item to the post
            post=item;

        } else {

            //create a service from the request
            Service service=PostUtilMethods.createServiceFromRequest(postItem);
            service.setUser(user);

            //set the service to the post
            post=service;
        }

        if (postItem.getIsVisible() != null) {
            post.setIsVisible(postItem.getIsVisible());
        }

        Post savedPost = postRepository.save(post);

        // Convert image URLs to PostMedia entities
        List<PostMedia> postMediaList= new ArrayList<>();
        List<PostMediaDto> postMediaDtosList= new ArrayList<>();

        for(PostMediaDto media : postItem.getPostImages())
        {
            PostMedia postMedia= new PostMedia();
            postMedia.setPost(savedPost);
            postMedia.setPostImageUrl(media.getPostImageUrl());
            postMedia= postMediaRepository.save(postMedia);
            postMediaList.add(postMedia);

            PostMediaDto postMediaDto= new PostMediaDto();
            postMediaDto.setPostImageUrl(postMedia.getPostImageUrl());
            postMediaDtosList.add(postMediaDto);
        }

        // add post media list to post and save
        savedPost.getPostImages().clear();
        savedPost.getPostImages().addAll(postMediaList);

        // create post response dto
        PostResponseDto prd= PostUtilMethods.getPostResponseDtoFromPost(user,savedPost,postMediaDtosList);
        if(prd.getPostType()==PostType.ITEM)
        {
            assert post instanceof Item;
            Item item=(Item)post;
            ItemRequestDto itemResponseDto=PostUtilMethods.createItemResponseDtoFromItem(item);
            prd.setItem(itemResponseDto);
        }
        else if(prd.getPostType() == PostType.SERVICE)
        {
            assert post instanceof Service;
            Service service=(Service) post;
            ServiceRequestDto serviceResponseDto= PostUtilMethods.createServiceResponseDtoFromService(service);

            prd.setService(serviceResponseDto);
        }
        postRepository.save(savedPost);

        if (savedPost.getGroupId() != null) {
            CommunityGroup group = communityGroupRepository.findById(savedPost.getGroupId()).orElse(null);
            if (group != null) {
                List<CommunityGroupMember> members = communityGroupMemberRepository.findByGroupId(savedPost.getGroupId());
                for (CommunityGroupMember member : members) {
                    if (member.getStatus() == com.finalyear.liwatch.community_group_members.cg_enums.Status.APPROVED && !Objects.equals(member.getUserId(), user.getId())) {
                        User targetUser = userRepository.findById(member.getUserId()).orElse(null);
                        if (targetUser != null) {
                            Notification notification = Notification.builder()
                                    .userId(targetUser.getId())
                                    .emailAddress(targetUser.getEmail())
                                    .subject("New Post in Group")
                                    .body("A new listing '" + savedPost.getTitle() + "' has been posted in group " + group.getGroupName())
                                    .sentAt(LocalDateTime.now())
                                    .status(com.finalyear.liwatch.Notification.enum_notification.Status.PENDING)
                                    .type("GroupActivity")
                                    .build();
                            notificationRepository.save(notification);
                        }
                    }
                }
            }
        }

        return prd;
    }


    // delete a single post with id
    public ResponseEntity<?> deletePost(Long id) {

       User user= userUtilService.getCurrentlyAuthenticatedUser();
       Post post=postRepository.getById(id);
       if(post.getUser()==user || user.getRole()== Role.ADMIN)
       {
           postRepository.deleteById(id);
           return ResponseEntity.status(HttpStatus.NO_CONTENT).body("post with id "+id+" is successfully deleted.");
       }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Action not allowed.");
    }


    // get a single post with id
    public PostResponseDto getPost(Long id) {

        Post post= postRepository.findById(id).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Post with id " + id + " not found"));

        if (Boolean.TRUE.equals(post.getIsGroupOnly())) {
            User currentUser = userUtilService.getCurrentlyAuthenticatedUser();
            boolean isMember = communityGroupMemberRepository.findByGroupIdAndUserId(post.getGroupId(), currentUser.getId())
                    .map(m -> m.getStatus() == com.finalyear.liwatch.community_group_members.cg_enums.Status.APPROVED)
                    .orElse(false);
            if (!isMember) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied. This post is scoped to group members only.");
            }
        }

        //create post media dto from post
        List<PostMediaDto> postMediaDtosList= new ArrayList<>();
        postMediaDtosList=createPostMediaDtoListFromPostMediaList(id,post);

        //get the authenticated user
        User user= post.getUser();
        if(post instanceof com.finalyear.liwatch.Item.Item)
        {
           if(post.getPostType() != PostType.ITEM) {
               post.setPostType(PostType.ITEM);
               postRepository.save(post);
           }
           PostResponseDto prd= PostUtilMethods.getPostResponseDtoFromPost(user,post,postMediaDtosList);
           ItemRequestDto itemResponseDto=PostUtilMethods.createItemResponseDtoFromItem((com.finalyear.liwatch.Item.Item)post);
           prd.setItem(itemResponseDto);
            return prd;
        }
        else if(post instanceof com.finalyear.liwatch.service.Service)
        {   
           if(post.getPostType() != PostType.SERVICE) {
               post.setPostType(PostType.SERVICE);
               postRepository.save(post);
           }
           PostResponseDto prd= PostUtilMethods.getPostResponseDtoFromPost(user,post,postMediaDtosList);
            ServiceRequestDto serviceResponseDto=PostUtilMethods.createServiceResponseDtoFromService((com.finalyear.liwatch.service.Service) post);
            prd.setService(serviceResponseDto);
            return prd;
        }
        else
        {
            return PostUtilMethods.getPostResponseDtoFromPost(user,post,postMediaDtosList);
        }


    }

    // converts post media list to post media dto list
    public List<PostMediaDto> createPostMediaDtoListFromPostMediaList(Long id, Post postItem)
    {
        List<PostMediaDto> postMediaDtosList= new ArrayList<>();

        List<PostMedia> postMedias= postItem.getPostImages();
        for(PostMedia media : postMedias)
        {
            PostMediaDto postMediaDto= new PostMediaDto();
            postMediaDto.setPostImageUrl(media.getPostImageUrl());
            postMediaDtosList.add(postMediaDto);
        }
        return postMediaDtosList;
    }

    // converts post media dto list to post media
    public List<PostMedia> createPostMediaListFromPostMediaDtoList(Long id, PostRequestDto newPost,Post oldPost)
    {
        List<PostMedia> postMediaList= new ArrayList<>();

        List<PostMediaDto> postMediaDtos= newPost.getPostImages();
        for(PostMediaDto media : postMediaDtos)
        {
            PostMedia postMedia= new PostMedia();
            postMedia.setPost(oldPost);
            postMedia.setPostImageUrl(media.getPostImageUrl());
            postMediaList.add(postMedia);
        }
        return postMediaList;
    }


    // get all posts with pagination
    public Page<PostResponseDto> getAllPosts(Pageable pageable) {
        Page<Post> postsPage = postRepository.findAllGlobal(pageable);

        // Convert entities to DTOs
        return postsPage.map(this::convertToDto);
    }

    // get posts by category
    public Page<PostResponseDto> getPostsByCategory(String category, Pageable pageable) {
        Page<Post> postsPage = postRepository.findByCategoryGlobal(category, pageable);
        return postsPage.map(this::convertToDto);
    }


    // converting post to post response dto
    public PostResponseDto convertToDto(Post post) {

        // Map PostMedia to PostMediaDto
        List<PostMediaDto> mediaDtos = post.getPostImages().stream()
                .map(media -> new PostMediaDto(media.getPostImageUrl()))
                .toList();

        // Map User to UserSummeryDto
        User user = post.getUser();
        UserSummeryDto userDto = UserSummeryDto.from(user);

        ItemRequestDto itemRequestDto= new ItemRequestDto();
        ServiceRequestDto serviceRequestDto=new ServiceRequestDto();
        if(post instanceof Item item)
        {

            itemRequestDto.setCondition(item.getCondition());
            itemRequestDto.setEstimatedValue(item.getEstimatedValue());
            PostResponseDto dto = new PostResponseDto(
                    post.getPostId(),
                    post.getTitle(),
                    post.getDescription(),
                    post.getCategory(),
                    post.getStatus(),
                    post.getPostType(),
                    post.getCreatedAt(),
                    mediaDtos,
                    userDto,
                    itemRequestDto,
                    post.getLocation(),
                    post.getLookingFor()
            );
            dto.setGroupId(post.getGroupId());
            dto.setIsGroupOnly(post.getIsGroupOnly());
            return dto;
        } else {
            Service service=(Service)post;
            serviceRequestDto.setServiceDuration(service.getServiceDuration());
            serviceRequestDto.setAvailability(service.getAvailability());
            serviceRequestDto.setSkillLevel(service.getSkillLevel());
            PostResponseDto dto = new PostResponseDto(
                    post.getPostId(),
                    post.getTitle(),
                    post.getDescription(),
                    post.getCategory(),
                    post.getStatus(),
                    post.getPostType(),
                    post.getCreatedAt(),
                    mediaDtos,
                    userDto,
                    serviceRequestDto,
                    post.getLocation(),
                    post.getLookingFor()
            );
            dto.setGroupId(post.getGroupId());
            dto.setIsGroupOnly(post.getIsGroupOnly());
            return dto;
        }

    }

    public PostResponseDto updatePost(Long id, PostRequestDto newPost) {

        // get old post with id
        Post oldPost = postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Post with id " + id + " is not found"
                ));

        // create new media list
        List<PostMedia> postMediaList =
                createPostMediaListFromPostMediaDtoList(id, newPost, oldPost);

        //  modify existing collection instead of replacing it
        oldPost.getPostImages().clear();

        for (PostMedia media : postMediaList) {
            media.setPost(oldPost);
            oldPost.getPostImages().add(media);
        }



        // update other fields
        oldPost.setTitle(newPost.getTitle());
        oldPost.setDescription(newPost.getDescription());
        oldPost.setCategory(newPost.getCategory());
        // Ensure postType is always in sync with the actual Java class to prevent ClassCastException
        oldPost.setPostType(oldPost instanceof com.finalyear.liwatch.Item.Item ? PostType.ITEM : PostType.SERVICE);
        oldPost.setLocation(newPost.getLocation());
        oldPost.setLookingFor(newPost.getLookingFor());


        // update the post item or service based on post type of new post
        if (oldPost instanceof com.finalyear.liwatch.Item.Item oldItem && newPost.getItem() != null) {
            oldItem.setCondition(newPost.getItem().getCondition());
            oldItem.setEstimatedValue(newPost.getItem().getEstimatedValue());
            oldItem.setPartialCashAllowed(newPost.getItem().getPartialCashAllowed());
        } else if (oldPost instanceof com.finalyear.liwatch.service.Service oldService && newPost.getService() != null) {
            oldService.setServiceDuration(newPost.getService().getServiceDuration());
            oldService.setSkillLevel(newPost.getService().getSkillLevel());
            oldService.setAvailability(newPost.getService().getAvailability());
        }

        Post updatedPost = postRepository.save(oldPost);

        return convertToDto(updatedPost);
    }


    // trail code
    // trail create post
//    public PostResponseDto createPost(List<MultipartFile> images, String postJson) {
//
//        // 1. Upload images safely
//        List<String> postImages;
//        try {
//            postImages = cloudinaryService.uploadMultiple(images, "posts");
//        } catch (IOException e) {
//            throw new RuntimeException("Failed to upload images to Cloudinary", e);
//        }
//
//        // 2. Convert image URLs to DTOs
//        List<PostMediaDto> postMediaDtoList = new ArrayList<>();
//        for (String postImage : postImages) {
//            PostMediaDto postMediaDto = new PostMediaDto();
//            postMediaDto.setPostImageUrl(postImage);
//            postMediaDtoList.add(postMediaDto);
//        }
//
//        // 3. Parse JSON safely
//        ObjectMapper objectMapper = new ObjectMapper();
//        PostRequestDto postItem;
//        try {
//            postItem = objectMapper.readValue(postJson, PostRequestDto.class);
//        } catch (Exception e) {
//            throw new RuntimeException("Invalid post JSON format", e);
//        }
//
//        postItem.setPostImages(postMediaDtoList);
//
//        // 4. Validation
//        if (postItem.getPostType() == PostType.ITEM && postItem.getItem() == null) {
//            throw new RuntimeException("Item data required");
//        }
//
//        if (postItem.getPostType() == PostType.SERVICE && postItem.getService() == null) {
//            throw new RuntimeException("Service data required");
//        }
//
//        // 5. Get authenticated user
//        User user = userUtilMethods.getCurrentlyAuthenticatedUser();
//
//        // 6. Create post
//        Post post;
//
//        if (postItem.getPostType() == PostType.ITEM) {
//            Item item = PostUtilMethods.createItemFromRequest(postItem);
//            item.setUser(user);
//            post = item;
//        } else {
//            Service service = PostUtilMethods.createServiceFromRequest(postItem);
//            service.setUser(user);
//            post = service;
//        }
//
//        // 7. Save post
//        Post savedPost = postRepository.save(post);
//
//        // 8. Save media
//        List<PostMedia> postMediaList = new ArrayList<>();
//        List<PostMediaDto> postMediaDtosList = new ArrayList<>();
//
//        for (PostMediaDto media : postItem.getPostImages()) {
//            PostMedia postMedia = new PostMedia();
//            postMedia.setPost(savedPost);
//            postMedia.setPostImageUrl(media.getPostImageUrl());
//
//            postMedia = postMediaRepository.save(postMedia);
//            postMediaList.add(postMedia);
//
//            PostMediaDto dto = new PostMediaDto();
//            dto.setPostImageUrl(postMedia.getPostImageUrl());
//            postMediaDtosList.add(dto);
//        }
//
//        // 9. Attach media to post
//        savedPost.getPostImages().clear();
//        savedPost.getPostImages().addAll(postMediaList);
//
//        // 10. Build response
//        PostResponseDto prd = PostUtilMethods.getPostResponseDtoFromPost(
//                user, savedPost, postMediaDtosList
//        );
//
//        if (prd.getPostType() == PostType.ITEM) {
//            assert post instanceof Item;
//            Item item = (Item) post;
//            ItemRequestDto itemResponseDto = PostUtilMethods.createItemResponseDtoFromItem(item);
//            prd.setItem(itemResponseDto);
//        } else if (prd.getPostType() == PostType.SERVICE) {
//            assert post instanceof Service;
//            Service service = (Service) post;
//            ServiceRequestDto serviceResponseDto =
//                    PostUtilMethods.createServiceResponseDtoFromService(service);
//            prd.setService(serviceResponseDto);
//        }
//
//        postRepository.save(savedPost);
//
//        return prd;
//    }


    public Post getPostEntity(Long id){
        return postRepository.findById(id).orElseThrow(
                ()-> new RuntimeException("Post not found!")
        );
    }

    public Page<PostResponseDto> getAllUserPost(Pageable pageable, Long userId) {

        User user= userRepository.findById(userId).orElseThrow(()-> new RuntimeException("something went wrong!"));

        Page<Post> postsPage = postRepository.findByUser(user,pageable);

        // Convert entities to DTOs
        return postsPage.map(this::convertToDto);

    }
}
