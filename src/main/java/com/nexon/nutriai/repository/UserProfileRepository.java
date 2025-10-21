package com.nexon.nutriai.repository;

import com.nexon.nutriai.repository.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, String> {
}
