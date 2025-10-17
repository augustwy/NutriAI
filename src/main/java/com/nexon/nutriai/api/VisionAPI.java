package com.nexon.nutriai.api;

import com.nexon.nutriai.pojo.FoodIdentification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface VisionAPI {

    FoodIdentification identifyFood(MultipartFile image);
}
