package com.finalyear.liwatch.directswap.directswap_managment;

import com.finalyear.liwatch.Item.ItemRequestDto;
import com.finalyear.liwatch.Post.Post;
import com.finalyear.liwatch.Post.PostResponseDto;
import com.finalyear.liwatch.Post.PostService;
import com.finalyear.liwatch.Post.enums.PostType;
import com.finalyear.liwatch.Post.enums.Status;
import com.finalyear.liwatch.barter.Barter;
import com.finalyear.liwatch.barter.barter_managment.BarterService;
import com.finalyear.liwatch.directswap.DirectSwapRequest;
import com.finalyear.liwatch.directswap.dto.CreateDirectSwapRequestDto;
import com.finalyear.liwatch.directswap.dto.DirectSwapRequestResponseDto;
import com.finalyear.liwatch.directswap.request_enum.RequestStatus;
import com.finalyear.liwatch.media.postMedia.PostMediaDto;
import com.finalyear.liwatch.service.ServiceRequestDto;
import com.finalyear.liwatch.userManagement.DTO.UserSummeryDto;
import com.finalyear.liwatch.userManagement.model.User;
import com.finalyear.liwatch.userManagement.service.UserService;
import com.finalyear.liwatch.userManagement.utils.classes.UserUtilService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DirectSwapRequestService {

    @Autowired
    private DirectSwapRequestRepository directSwapRequestRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PostService postService;

    @Autowired
    private BarterService barterService;

    @Autowired
    private UserUtilService userUtilService;

    public DirectSwapRequest getSwapRequest(Long id) {
        return directSwapRequestRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Swap Request not found!")
        );
    }

    private UserSummeryDto toUserDto(User user) {
        if (user == null) {
            return null;
        }
        return new UserSummeryDto(
                user.getId(),
                user.getFullName(),
                user.getEmail()
        );
    }

    private PostResponseDto toPostDto(Post post) {
        if (post == null) {
            return null;
        }

        UserSummeryDto userDto = post.getUser() != null
                ? new UserSummeryDto(
                post.getUser().getId(),
                post.getUser().getFullName(),
                post.getUser().getEmail()
        )
                : null;

        List<PostMediaDto> mediaDtos = post.getPostImages() == null
                ? List.of()
                : post.getPostImages().stream()
                  .map(media -> new PostMediaDto(media.getPostImageUrl()))
                  .toList();

        ItemRequestDto itemRequestDto = null;
        ServiceRequestDto serviceRequestDto = null;

        if (post.getPostType() == PostType.ITEM) {
            itemRequestDto = new ItemRequestDto();
        } else if (post.getPostType() == PostType.SERVICE) {
            serviceRequestDto = new ServiceRequestDto();
        }

        PostResponseDto dto = new PostResponseDto(
                post.getPostId(),
                post.getTitle(),
                post.getDescription(),
                post.getCategory(),
                post.getExchangeType(),
                post.getStatus(),
                post.getPostType(),
                post.getCreatedAt(),
                mediaDtos,
                userDto,
                itemRequestDto,
                post.getLocation(),
                post.getLookingFor()
        );

        if (serviceRequestDto != null) {
            dto.setService(serviceRequestDto);
        }

        return dto;
    }

    private DirectSwapRequestResponseDto toDto(DirectSwapRequest request) {
        Long barterId = request.getBarter() != null ? request.getBarter().getId() : null;

        return new DirectSwapRequestResponseDto(
                request.getId(),
                toUserDto(request.getRequestSender()),
                toUserDto(request.getRequestReceiver()),
                toPostDto(request.getOfferedPost()),
                toPostDto(request.getRequestedPost()),
                request.getStatus(),
                request.getCreatedAt(),
                barterId
        );
    }

    public String makeRequest(CreateDirectSwapRequestDto dto) {
        User requestSender = userUtilService.getCurrentlyAuthenticatedUser();
        User requestReceiver = userService.getUser(dto.getReceiverId());
        Post offeredPost = postService.getPostEntity(dto.getOfferedPostId());
        Post requestedPost = postService.getPostEntity(dto.getRequestedPostId());

        DirectSwapRequest directSwapRequest = new DirectSwapRequest();
        directSwapRequest.setRequestSender(requestSender);
        directSwapRequest.setRequestReceiver(requestReceiver);
        directSwapRequest.setOfferedPost(offeredPost);
        directSwapRequest.setRequestedPost(requestedPost);
        directSwapRequest.setStatus(RequestStatus.PENDING);
        directSwapRequest.setCreatedAt(LocalDateTime.now());

        directSwapRequestRepository.save(directSwapRequest);
        return "Request has been sent!";
    }

    @Transactional
    public String acceptRequest(Long id) {
        long accepterID = userUtilService.getCurrentlyAuthenticatedUser().getId();
        DirectSwapRequest swapRequest = getSwapRequest(id);

        if (!swapRequest.getRequestReceiver().getId().equals(accepterID)) {
            throw new RuntimeException("You are not allowed to accept this request");
        }
        if (swapRequest.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException("Request already processed");
        }
        if (swapRequest.getRequestSender().getId().equals(swapRequest.getRequestReceiver().getId())) {
            throw new RuntimeException("You cannot accept your own request");
        }

        swapRequest.setStatus(RequestStatus.ACCEPTED);
        Barter barter = barterService.createBarter(swapRequest);
        swapRequest.setBarter(barter);
        directSwapRequestRepository.save(swapRequest);

        return "Request Accept";
    }

    public boolean checkRequestMade(long userId, long postId) {
        userUtilService.checkUser(userId);
        User user = userService.getUser(userId);
        Post post = postService.getPostEntity(postId);
        return directSwapRequestRepository.existsByRequestSenderAndRequestedPost(user, post);
    }

    public List<DirectSwapRequestResponseDto> getMyRequestForSpecificPost(long userId, long postId) {
        userUtilService.checkUser(userId);
        User user = userService.getUser(userId);
        Post post = postService.getPostEntity(postId);

        return directSwapRequestRepository.findByRequestReceiverAndRequestedPost(user, post)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<DirectSwapRequestResponseDto> getAllMyRequest(long userId) {
        userUtilService.checkUser(userId);
        User user = userService.getUser(userId);

        return directSwapRequestRepository.findByRequestReceiver(user)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<DirectSwapRequestResponseDto> getAllRequestSentAndReceived(long userId) {
        userUtilService.checkUser(userId);
        User user = userService.getUser(userId);

        return directSwapRequestRepository.findByRequestReceiverOrRequestSender(user, user)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public DirectSwapRequestResponseDto getRequestById(Long id) {
        return toDto(getSwapRequest(id));
    }

    public List<DirectSwapRequestResponseDto> getMyRequests() {
        User user = userUtilService.getCurrentlyAuthenticatedUser();

        return directSwapRequestRepository.findByRequestReceiverOrRequestSender(user,null)
                .stream()
                .filter(request -> request.getStatus() == RequestStatus.PENDING)
                .map(this::toDto)
                .toList();
    }
}