package com.finalyear.liwatch.userprofile;

import com.finalyear.liwatch.userManagement.DTO.UserSummeryDto;
import com.finalyear.liwatch.userprofile.enums.BadgeLevel;
import lombok.*;

import java.math.BigDecimal;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProfileResponseDto {
    private Long profileId;
    private String location;
    private String bio;
    private BigDecimal trustScore;
    private BadgeLevel badgeLevel;
    private String profileImage;
    private UserSummeryDto user;
}
