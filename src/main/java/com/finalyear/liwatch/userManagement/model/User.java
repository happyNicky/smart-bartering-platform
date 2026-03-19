package com.finalyear.liwatch.userManagement.model;

import com.finalyear.liwatch.barter.Barter;
import com.finalyear.liwatch.chat.Chat;
import com.finalyear.liwatch.directswap.DirectSwapRequest;
import com.finalyear.liwatch.userManagement.utils.enums.Role;
import com.finalyear.liwatch.userManagement.utils.enums.Status;
import com.finalyear.liwatch.userprofile.UserProfile;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Data

public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    private boolean isVerified = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    @Column(nullable = false)
    private boolean enabled=false;
    private String verificationToken;

    private LocalDateTime tokenExpiry;
    @OneToOne
    @JoinColumn(name = "user_profile_id")
    private UserProfile userProfile;

    // 🔹 Requests sent
    @OneToMany(mappedBy = "sender")
    private List<DirectSwapRequest> sentDirectRequests;

    // 🔹 Requests received
    @OneToMany(mappedBy = "receiver")
    private List<DirectSwapRequest> receivedDirectRequests;

    // 🔹 Messages sent
    @OneToMany(mappedBy = "sender")
    private List<Chat> messages;

    // 🔹 Barters as userA
    @OneToMany(mappedBy = "userA")
    private List<Barter> bartersAsUserA;

    // 🔹 Barters as userB
    @OneToMany(mappedBy = "userB")
    private List<Barter> bartersAsUserB;


}