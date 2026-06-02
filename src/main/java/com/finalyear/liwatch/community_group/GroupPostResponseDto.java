package com.finalyear.liwatch.community_group;

import com.finalyear.liwatch.Post.PostResponseDto;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupPostResponseDto {
    private Long groupPostId;
    private Long groupId;
    private LocalDateTime sharedAt;
    private PostResponseDto postDetails;
}
