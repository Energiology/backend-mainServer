package com.example.backendmainserver.user.domain;

import com.example.backendmainserver.room.domain.Room;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(unique = true)
    private String loginId;

    private String password;

    private String nickname;

    private String email;

    private String phoneNumber;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    @OneToOne
    @JoinColumn(name = "room_id")
    private Room room;

    private String fcmToken;

    public void updateFcmToken(String fcmToken){
        this.fcmToken = fcmToken;
    }
}
