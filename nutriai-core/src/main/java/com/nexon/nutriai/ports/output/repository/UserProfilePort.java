package com.nexon.nutriai.ports.output.repository;

import com.nexon.nutriai.domain.entity.UserProfile;

import java.util.Optional;

public interface UserProfilePort {
    void save(UserProfile userProfile);

    Optional<UserProfile> findById(String phone);
}
