package com.nexon.nutriai.dao.repository;

import com.nexon.nutriai.dao.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<AppUser, String> {

    AppUser findByOpenId(String openId);
}
