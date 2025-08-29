package com.gigigenie.domain.member.entity;

import com.gigigenie.domain.member.enums.MemberRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id", nullable = false)
    private Integer memberId;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(length = 255)
    private String password;

    @Column(nullable = false, length = 20)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private MemberRole role;

    private String provider;
    private String providerId;

    @Column(name = "join_date", nullable = false)
    @Builder.Default
    private LocalDateTime joinDate = LocalDateTime.now();

    public Member updateName(String name) {
        this.name = name;
        return this;
    }

    public Member updateOAuth2Info(String provider, String providerId) {
        this.provider = provider;
        this.providerId = providerId;
        return this;
    }
}
