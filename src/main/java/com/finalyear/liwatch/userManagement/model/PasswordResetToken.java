package com.finalyear.liwatch.userManagement.model;


import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


import java.time.LocalDateTime;

@Entity
@Data
@Getter
@Setter
public class PasswordResetToken {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String token;

        @OneToOne
        private User user;

        private LocalDateTime expiryDate;

        public boolean isExpired() {
            return expiryDate.isBefore(LocalDateTime.now());
        }

        // getters & setters

}
