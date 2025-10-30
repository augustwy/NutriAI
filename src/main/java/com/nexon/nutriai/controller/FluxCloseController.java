package com.nexon.nutriai.controller;

import com.nexon.nutriai.service.aop.SubscriptionTrackingAspect;
import com.nexon.nutriai.pojo.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/web/")
@RequiredArgsConstructor
public class FluxCloseController {

    private final SubscriptionTrackingAspect subscriptionTracker;

    /**
     * 停止流
     *
     * @param key
     * @param streamId
     * @return
     */
    @PostMapping("/cancel/{key}")
    public BaseResponse<Void> cancelRecommendations(@PathVariable String key, String streamId) {
        if (streamId == null) {
            subscriptionTracker.getActiveStreamIds(key);
            return BaseResponse.success();
        }
        subscriptionTracker.cancelSingleSubscription(key, streamId);
        return BaseResponse.success();
    }
}
