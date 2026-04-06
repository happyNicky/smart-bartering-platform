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
public class ProfileRequestDto {

    private String location;
    private String bio;
    private String profileImage;
}
