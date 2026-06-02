package com.finalyear.liwatch.community_group;

import com.finalyear.liwatch.community_group_members.cg_enums.Role;
import com.finalyear.liwatch.community_group_members.cg_enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupMemberResponseDto {
    private Long userId;
    private String fullName;
    private String email;
    private Role role;
    private Status status;
    private LocalDateTime joinedAt;
    private String badgeLabel;
    private java.math.BigDecimal averageRating;
}
