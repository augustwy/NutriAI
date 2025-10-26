package com.nexon.nutriai.ports.output.repository;

import com.nexon.nutriai.domain.entity.AppUser;

import java.util.Optional;

public interface UserPort {

    AppUser findByOpenId(String openId);

    Optional<AppUser> findById(String phone);

    void save(AppUser appUser);
}
