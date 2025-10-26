package com.nexon.nutriai.ports.output.repository;

import com.nexon.nutriai.domain.entity.UserProfileLog;

public interface UserProfileLogPort {
    void save(UserProfileLog userProfileLog);
}
