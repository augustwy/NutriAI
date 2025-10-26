package com.nexon.nutriai.output.repository;

import com.nexon.nutriai.domain.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<AppUser, String> {

    AppUser findByOpenId(String openId);
}
