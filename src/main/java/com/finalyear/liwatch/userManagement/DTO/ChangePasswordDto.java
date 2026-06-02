package com.finalyear.liwatch.userManagement.DTO;

import lombok.Data;

@Data
public class ChangePasswordDto {
    private String currentPassword;
    private String newPassword;
}
