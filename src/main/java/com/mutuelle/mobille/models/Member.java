package com.mutuelle.mobille.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "members")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Member {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firsname;
    private String phone;
    private String lastname;
    private String registrationFreeToPaid;

    @Column(name = "avatar", nullable = true)
    private String avatar;

    @Column(name = "is_active")
    private Boolean isActive=true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt ;

    @Column(name = "created_up", nullable = false )
    private LocalDateTime createdUp ;
}
