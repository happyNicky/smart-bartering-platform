package com.finalyear.liwatch.community_group_members;



import com.finalyear.liwatch.community_group_members.cg_enums.Role;
import com.finalyear.liwatch.community_group_members.cg_enums.Status;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "community_group_members")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityGroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "membership_id")
    private Long membershipId;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;




}
