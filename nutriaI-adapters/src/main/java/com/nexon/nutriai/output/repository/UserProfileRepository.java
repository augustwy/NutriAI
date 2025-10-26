package com.nexon.nutriai.output.repository;

import com.nexon.nutriai.domain.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, String> {
}
