package com.nexon.nutriai.repository;

import com.nexon.nutriai.repository.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<AppUser, String> {

    AppUser findByOpenId(String openId);
}
