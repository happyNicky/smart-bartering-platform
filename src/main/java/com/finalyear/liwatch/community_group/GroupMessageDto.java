package com.finalyear.liwatch.community_group;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupMessageDto {
    private Long groupId;
    private String content;
}
