package com.nexon.nutriai.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "demo")
@Getter
@Setter
public class Demo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String username;

    @Column
    private String password;

    public Demo() {
    }

    public Demo(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public Demo(Long id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }
}
