package com.nexon.nutriai.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "app_user")
public class AppUser {
    @Id
    private String phone;

    @Column(length = 30)
    private String username;

    @Column(length = 60)
    private String password;
}
