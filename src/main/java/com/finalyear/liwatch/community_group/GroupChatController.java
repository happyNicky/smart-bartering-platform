package com.finalyear.liwatch.community_group;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupChatController {

    private final CommunityGroupService communityGroupService;
    private final SimpMessagingTemplate messagingTemplate;

    public GroupChatController(CommunityGroupService communityGroupService, SimpMessagingTemplate messagingTemplate) {
        this.communityGroupService = communityGroupService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * WebSocket endpoint to receive and broadcast group messages
     */
    @MessageMapping("/group.send")
    public void sendMessage(@Payload GroupMessageDto messageDto) {
        // Save the message using the service, which also validates the user's membership
        GroupChatMessage savedMessage = communityGroupService.saveGroupMessage(messageDto.getGroupId(), messageDto.getContent());

        // Broadcast the saved message to all subscribers of this group's topic
        messagingTemplate.convertAndSend("/group/" + messageDto.getGroupId(), savedMessage);
    }

    /**
     * REST endpoint to retrieve chat history for a group
     */
    @GetMapping("/{groupId}/chats")
    public ResponseEntity<List<GroupChatMessage>> getGroupChatHistory(@PathVariable Long groupId) {
        List<GroupChatMessage> history = communityGroupService.getGroupChatHistory(groupId);
        return ResponseEntity.ok(history);
    }
}
