package com.finalyear.liwatch.userManagement.DTO;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserSummeryDto {
    private Long id;
    private String name;
    private String email;
}
