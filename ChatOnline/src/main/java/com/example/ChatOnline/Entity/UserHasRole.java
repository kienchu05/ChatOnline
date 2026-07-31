package com.example.ChatOnline.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_has_role")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class UserHasRole {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;
}
