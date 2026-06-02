package com.finalyear.liwatch.cycleswap.service;

import com.finalyear.liwatch.Item.ItemRequestDto;
import com.finalyear.liwatch.Post.Post;
import com.finalyear.liwatch.Post.PostResponseDto;
import com.finalyear.liwatch.Post.PostService;
import com.finalyear.liwatch.Post.enums.PostType;
import com.finalyear.liwatch.cycleswap.dto.CreateCycleSwapRequestDto;
import com.finalyear.liwatch.cycleswap.dto.CycleSwapRequestResponseDto;
import com.finalyear.liwatch.cycleswap.model.CycleBarter;
import com.finalyear.liwatch.cycleswap.model.CycleNegotiation;
import com.finalyear.liwatch.cycleswap.model.CycleSwapRequest;
import com.finalyear.liwatch.cycleswap.repository.CycleBarterRepository;
import com.finalyear.liwatch.cycleswap.repository.CycleNegotiationRepository;
import com.finalyear.liwatch.cycleswap.repository.CycleSwapRequestRepository;
import com.finalyear.liwatch.directswap.request_enum.RequestStatus;
import com.finalyear.liwatch.media.postMedia.PostMediaDto;
import com.finalyear.liwatch.negotiation.negotiaition_enum.NegotiationStatus;
import com.finalyear.liwatch.service.ServiceRequestDto;
import com.finalyear.liwatch.userManagement.DTO.UserSummeryDto;
import com.finalyear.liwatch.userManagement.model.User;
import com.finalyear.liwatch.userManagement.service.UserService;
import com.finalyear.liwatch.userManagement.utils.classes.UserUtilService;
import com.finalyear.liwatch.Notification.NotificationService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CycleSwapRequestService {

    @Autowired
    private CycleSwapRequestRepository cycleSwapRequestRepository;

    @Autowired
    private CycleBarterRepository cycleBarterRepository;

    @Autowired
    private CycleNegotiationRepository cycleNegotiationRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PostService postService;

    @Autowired
    private UserUtilService userUtilService;

    @Autowired
    private NotificationService notificationService;

    public CycleSwapRequest getSwapRequest(Long id) {
        return cycleSwapRequestRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Cycle Swap Request not found!")
        );
    }

    private UserSummeryDto toUserDto(User user) {
        return UserSummeryDto.from(user);
    }

    private PostResponseDto toPostDto(Post post) {
        if (post == null) {
            return null;
        }

        UserSummeryDto userDto = UserSummeryDto.from(post.getUser());

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

    public CycleSwapRequestResponseDto toDto(CycleSwapRequest request) {
        Long barterId = request.getCycleBarter() != null ? request.getCycleBarter().getId() : null;

        return new CycleSwapRequestResponseDto(
                request.getId(),
                toUserDto(request.getInitiator()),
                toUserDto(request.getMiddleman()),
                toUserDto(request.getCloser()),
                toPostDto(request.getPostA()),
                toPostDto(request.getPostB()),
                toPostDto(request.getPostC()),
                request.isMiddlemanAccepted(),
                request.isCloserAccepted(),
                request.getStatus(),
                request.getCreatedAt(),
                barterId
        );
    }

    @Transactional
    public String makeRequest(CreateCycleSwapRequestDto dto) {
        User initiator = userUtilService.getCurrentlyAuthenticatedUser();
        User middleman = userService.getUser(dto.getMiddlemanId());
        User closer = userService.getUser(dto.getCloserId());

        if (initiator.getId().equals(middleman.getId()) || 
            initiator.getId().equals(closer.getId()) || 
            middleman.getId().equals(closer.getId())) {
            throw new RuntimeException("All three users in a cycle swap must be distinct");
        }
        
        Post postA = postService.getPostEntity(dto.getPostAId());
        Post postB = postService.getPostEntity(dto.getPostBId());
        Post postC = postService.getPostEntity(dto.getPostCId());

        CycleSwapRequest req = new CycleSwapRequest();
        req.setInitiator(initiator);
        req.setMiddleman(middleman);
        req.setCloser(closer);
        req.setPostA(postA);
        req.setPostB(postB);
        req.setPostC(postC);
        req.setStatus(RequestStatus.PENDING);
        req.setCreatedAt(LocalDateTime.now());
        req.setMiddlemanAccepted(false);
        req.setCloserAccepted(false);

        cycleSwapRequestRepository.save(req);

        // Notify middleman
        notificationService.createNotification(
                middleman.getId(), middleman.getEmail(),
                "New 3-Way Cycle Swap Request",
                initiator.getFullName() + " has initiated a cycle swap involving your '" + postB.getTitle() + "'.",
                "CYCLE_SWAP_REQUEST"
        );
        // Notify closer
        notificationService.createNotification(
                closer.getId(), closer.getEmail(),
                "New 3-Way Cycle Swap Request",
                initiator.getFullName() + " has initiated a cycle swap involving your '" + postC.getTitle() + "'.",
                "CYCLE_SWAP_REQUEST"
        );

        return "Cycle Swap Request has been sent!";
    }

    @Transactional
    public String acceptRequest(Long id) {
        User currentUser = userUtilService.getCurrentlyAuthenticatedUser();
        CycleSwapRequest req = getSwapRequest(id);

        if (req.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException("Request already processed");
        }

        boolean isMiddleman = req.getMiddleman().getId().equals(currentUser.getId());
        boolean isCloser = req.getCloser().getId().equals(currentUser.getId());

        if (!isMiddleman && !isCloser) {
            throw new RuntimeException("You are not allowed to accept this request");
        }

        if (isMiddleman) {
            req.setMiddlemanAccepted(true);
        }
        if (isCloser) {
            req.setCloserAccepted(true);
        }

        // Check if both accepted
        if (req.isMiddlemanAccepted() && req.isCloserAccepted()) {
            req.setStatus(RequestStatus.ACCEPTED);
            
            // Create Barter and Negotiation
            CycleBarter barter = new CycleBarter();
            barter.setCycleSwapRequest(req);
            barter.setCreatedAt(LocalDateTime.now());
            barter.setUserA(req.getInitiator());
            barter.setUserB(req.getMiddleman());
            barter.setUserC(req.getCloser());
            barter.setPostA(req.getPostA());
            barter.setPostB(req.getPostB());
            barter.setPostC(req.getPostC());
            cycleBarterRepository.save(barter);

            req.setCycleBarter(barter);

            CycleNegotiation negotiation = new CycleNegotiation();
            negotiation.setCycleBarter(barter);
            negotiation.setStatus(NegotiationStatus.PENDING);
            cycleNegotiationRepository.save(negotiation);

            notificationService.createNotification(
                    req.getInitiator().getId(), req.getInitiator().getEmail(),
                    "Cycle Swap Accepted!",
                    "Both parties have accepted your 3-way swap. You can now negotiate.",
                    "CYCLE_SWAP_ACCEPTED"
            );
        }

        cycleSwapRequestRepository.save(req);
        return "Cycle Swap Accepted";
    }

    @Transactional
    public String declineRequest(Long id) {
        User currentUser = userUtilService.getCurrentlyAuthenticatedUser();
        CycleSwapRequest req = getSwapRequest(id);

        boolean isMiddleman = req.getMiddleman().getId().equals(currentUser.getId());
        boolean isCloser = req.getCloser().getId().equals(currentUser.getId());

        if (!isMiddleman && !isCloser) {
            throw new RuntimeException("You are not allowed to decline this request");
        }
        if (req.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException("Request already processed");
        }

        req.setStatus(RequestStatus.DECLINED);
        cycleSwapRequestRepository.save(req);

        notificationService.createNotification(
                req.getInitiator().getId(), req.getInitiator().getEmail(),
                "Cycle Swap Declined",
                currentUser.getFullName() + " has declined the 3-way swap request.",
                "CYCLE_SWAP_DECLINED"
        );

        return "Request Declined";
    }

    @Transactional
    public String cancelRequest(Long id) {
        long senderID = userUtilService.getCurrentlyAuthenticatedUser().getId();
        CycleSwapRequest req = getSwapRequest(id);

        if (!req.getInitiator().getId().equals(senderID)) {
            throw new RuntimeException("You are not allowed to cancel this request");
        }
        if (req.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException("Request already processed");
        }

        req.setStatus(RequestStatus.CANCELED);
        cycleSwapRequestRepository.save(req);
        return "Request Canceled";
    }

    public List<CycleSwapRequestResponseDto> getMyRequests() {
        User user = userUtilService.getCurrentlyAuthenticatedUser();
        return cycleSwapRequestRepository.findByInitiatorOrMiddlemanOrCloser(user, user, user)
                .stream()
                .filter(request -> request.getStatus() == RequestStatus.PENDING)
                .map(this::toDto)
                .toList();
    }
}
