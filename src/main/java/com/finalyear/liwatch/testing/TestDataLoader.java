//package com.finalyear.liwatch.testing;
//
//import com.finalyear.liwatch.Item.Item;
//import com.finalyear.liwatch.Item.itemenum.Condition;
//import com.finalyear.liwatch.Post.enums.ExchangeType;
//import com.finalyear.liwatch.barter.Barter;
//import com.finalyear.liwatch.barter.barterenum.Status;
//import com.finalyear.liwatch.chat.Chat;
//import com.finalyear.liwatch.directswap.DirectSwapRequest;
//import com.finalyear.liwatch.directswap.request_enum.RequestStatus;
//import com.finalyear.liwatch.negotiation.Negotiation;
//import com.finalyear.liwatch.negotiation.negotiaition_enum.NegotiationStatus;
//import com.finalyear.liwatch.test_repositories.*;
//import com.finalyear.liwatch.userManagement.model.User;
//import com.finalyear.liwatch.userManagement.repository.UserRepository;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.concurrent.atomic.AtomicInteger;
//
//
//@Configuration
//public class TestDataLoader {
//
//    @Bean
//    CommandLineRunner loadData(
//            UserRepository userRepo,
//            TestPostRepository postRepo,
//            DirectSwapRequestRepository requestRepo,
//            BarterRepository barterRepo,
//            NegotiationRepository negotiationRepo,
//            ChatRepository chatRepo
//    ) {
//        return args -> {
//
//            // ========================
//            //  USERS
//            // ========================
//            AtomicInteger counter = new AtomicInteger(0);
//            String uniqueNum = String.valueOf(counter.incrementAndGet());
//            User userA = new User();
//            userA.setEmail("a@test"+uniqueNum+".com");
//            userA.setFullName("User A");
//            userA.setPassword("123");
//            userA.setEnabled(true);
//
//            userA = userRepo.save(userA);
//
//            String uniqueNum2 = String.valueOf(counter.incrementAndGet());
//            User userB = new User();
//            userB.setEmail("b@test"+uniqueNum2+".com");
//            userB.setFullName("User B");
//            userB.setPassword("123");
//            userB.setEnabled(true);
//
//            userB = userRepo.save(userB);
//
//            // ========================
//            //  POSTS
//            // ========================
//            Item phone = Item.builder()
//                    .title("iPhone 11")
//                    .description("Good condition")
//                    .category("Electronics")
//                    .condition(Condition.NEW)
//                    .partialCashAllowed(true)
//                    .estimatedValue(BigDecimal.valueOf(1000.00))
//                    .exchangeType(ExchangeType.PERMANENT)
//                    .createdAt(LocalDateTime.now())
//                    .user(userA)
//                    .build();
//
//            Item laptop = Item.builder()
//                    .title("Dell Laptop")
//                    .description("8GB RAM")
//                    .category("Electronics")
//                    .exchangeType(ExchangeType.PERMANENT)
//                    .condition(Condition.NEW)
//                    .partialCashAllowed(true)
//                    .estimatedValue(BigDecimal.valueOf(1000.00))
//                    .createdAt(LocalDateTime.now())
//                    .user(userB)
//                    .build();
//
//            postRepo.save(phone);
//            postRepo.save(laptop);
//
//            // ========================
//            //  DIRECT SWAP REQUEST
//            // ========================
//            DirectSwapRequest request = requestRepo.save(
//                    DirectSwapRequest.builder()
//                            .sender(userA)
//                            .receiver(userB)
//                            .offeredPost(phone)
//                            .requestedPost(laptop)
//                            .status(RequestStatus.AGREED)
//                            .createdAt(LocalDateTime.now())
//                            .build()
//            );
//
//            // ========================
//            //  BARTER (ACCEPTED)
//            // ========================
//            Barter barter = barterRepo.save(
//                    Barter.builder()
//                            .swapRequest(request)
//                            .status(Status.AGREED)
//                            .createdAt(LocalDateTime.now())
//                            .build()
//            );
//
//            // ========================
//            //  NEGOTIATION
//            // ========================
//            Negotiation negotiation = negotiationRepo.save(
//                    Negotiation.builder()
//                            .barter(barter)
//                            .status(NegotiationStatus.PENDING)
//                            .fairnessScore(0.8)
//                            .build()
//            );
//
//            // ========================
//            //  CHAT MESSAGES
//            // ========================
//            chatRepo.save(Chat.builder()
//                    .negotiation(negotiation)
//                    .sender(userA)
//                    .messageText("Hi, want to swap?")
//                    .sentAt(LocalDateTime.now())
//                    .build());
//
//            chatRepo.save(Chat.builder()
//                    .negotiation(negotiation)
//                    .sender(userB)
//                    .messageText("Yes, but add cash?")
//                    .sentAt(LocalDateTime.now())
//                    .build());
//
//            System.out.println(" TEST DATA CREATED");
//        };
//    }
//}