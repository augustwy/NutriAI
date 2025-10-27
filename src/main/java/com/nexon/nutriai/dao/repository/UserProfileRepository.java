package com.nexon.nutriai.dao.repository;

import com.nexon.nutriai.dao.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, String> {
}
