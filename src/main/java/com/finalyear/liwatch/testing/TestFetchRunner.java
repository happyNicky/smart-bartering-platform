package com.finalyear.liwatch.testing;

import com.finalyear.liwatch.directswap.DirectSwapRequest;
import com.finalyear.liwatch.negotiation.Negotiation;
import com.finalyear.liwatch.test_repositories.BarterRepository;
import com.finalyear.liwatch.test_repositories.ChatRepository;
import com.finalyear.liwatch.test_repositories.DirectSwapRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TestFetchRunner implements CommandLineRunner {

    private final DirectSwapRequestRepository requestRepo;
    private final BarterRepository barterRepo;
    private final ChatRepository chatRepo;

    @Override
    public void run(String... args) throws Exception {
        // ==============================
        //  Fetch a DirectSwapRequest fully
        // ==============================
        Long requestId = 1L; // adjust according to your test data
        requestRepo.findFullRequest(requestId).ifPresent(request -> {
            System.out.println("DirectSwapRequest #" + request.getId());
            System.out.println("Sender: " + request.getSender().getFullName());
            System.out.println("Receiver: " + request.getReceiver().getFullName());
            System.out.println("Offered Post: " + request.getOfferedPost().getTitle());
            System.out.println("Requested Post: " + request.getRequestedPost().getTitle());
            System.out.println("Status: " + request.getStatus());
        });

        // ==============================
        //  Fetch a Barter fully
        // ==============================
        Long barterId = 1L; // adjust according to your test data
        barterRepo.findFullBarter(barterId).ifPresent(barter -> {
            System.out.println("\nBarter #" + barter.getId());

            DirectSwapRequest req = barter.getSwapRequest();
            System.out.println("SwapRequest Status: " + req.getStatus());
            System.out.println("Sender: " + req.getSender().getFullName());
            System.out.println("Receiver: " + req.getReceiver().getFullName());
            System.out.println("Barter Status: " + barter.getStatus());

            // Fetch negotiation
            Negotiation neg = barter.getNegotiation();
            if (neg != null) {
                System.out.println("Negotiation Status: " + neg.getStatus());
                System.out.println("Fairness Score: " + neg.getFairnessScore());

                // Fetch chats
                chatRepo.findByNegotiation(neg.getId()).forEach(chat -> {
                    System.out.println(chat.getSentAt() + " - " +
                            chat.getSender().getFullName() + ": " + chat.getMessageText());
                });
            } else {
                System.out.println("No negotiation found");
            }
        });
    }
}
