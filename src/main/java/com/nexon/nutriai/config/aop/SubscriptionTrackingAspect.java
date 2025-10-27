package com.nexon.nutriai.config.aop;

import com.nexon.nutriai.constant.annotaion.TrackSubscription;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
@Slf4j
public class SubscriptionTrackingAspect {

    private final Map<String, Map<String, Disposable>> subscriptions = new ConcurrentHashMap<>();

    @Around("@annotation(trackSubscription)")
    public Object trackFluxSubscription(ProceedingJoinPoint joinPoint, TrackSubscription trackSubscription) throws Throwable {
        Object result = joinPoint.proceed();

        if (result instanceof Flux<?> flux) {
            String subscriptionKey = generateSubscriptionKey(joinPoint, trackSubscription);
            String streamId = generateStreamId(joinPoint, trackSubscription);

            return flux.doOnSubscribe(subscription -> {
                Disposable disposable = subscription::cancel;
                // 记录订阅，使用嵌套Map存储具体每个流实例
                subscriptions.computeIfAbsent(subscriptionKey, k -> new ConcurrentHashMap<>())
                        .put(streamId, disposable);
            }).doFinally(_ -> {
                // 清理特定流的订阅记录
                Map<String, Disposable> streams = subscriptions.get(subscriptionKey);
                if (streams != null) {
                    Disposable removed = streams.remove(streamId);
                    if (removed != null && !removed.isDisposed()) {
                        removed.dispose();
                    }
                    // 如果该key下没有更多流，则清理整个key
                    if (streams.isEmpty()) {
                        subscriptions.remove(subscriptionKey);
                    }
                }
            });
        }

        return result;
    }

    private String generateStreamId(ProceedingJoinPoint joinPoint, TrackSubscription annotation) {
        // 通过参数名称获取
        if (!annotation.streamIdParamName().isEmpty()) {
            String paramName = annotation.streamIdParamName();
            Object[] args = joinPoint.getArgs();
            String[] parameterNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();

            for (int i = 0; i < parameterNames.length; i++) {
                if (paramName.equals(parameterNames[i]) && i < args.length) {
                    return String.valueOf(args[i]);
                }
            }
        }

        // 默认生成UUID
        return UUID.randomUUID().toString();
    }

    // 关闭指定key下的特定流
    public boolean cancelSingleSubscription(String key, String streamId) {
        Map<String, Disposable> streams = subscriptions.get(key);
        if (streams != null) {
            Disposable disposable = streams.remove(streamId);
            if (disposable != null) {
                if (!disposable.isDisposed()) {
                    disposable.dispose();
                }
                // 如果该key下没有更多流，则清理整个key
                if (streams.isEmpty()) {
                    subscriptions.remove(key);
                }
                return true;
            }
        }
        return false;
    }

    // 获取指定key下的所有流ID
    public Set<String> getActiveStreamIds(String key) {
        Map<String, Disposable> streams = subscriptions.get(key);
        if (streams != null) {
            return new HashSet<>(streams.keySet());
        }
        return Collections.emptySet();
    }

    private String generateSubscriptionKey(ProceedingJoinPoint joinPoint, TrackSubscription annotation) {
        String baseKey = annotation.value().isEmpty() ?
                joinPoint.getSignature().toShortString() :
                annotation.value();
        return baseKey;
    }
}