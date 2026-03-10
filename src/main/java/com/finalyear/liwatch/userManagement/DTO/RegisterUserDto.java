package com.finalyear.liwatch.userManagement.DTO;

import lombok.Data;

@Data
public class RegisterUserDto {

    private String email;
    private String fullName;
    private String password;
}
