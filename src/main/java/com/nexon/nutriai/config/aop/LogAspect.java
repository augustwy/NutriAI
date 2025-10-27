package com.nexon.nutriai.config.aop;

import com.nexon.nutriai.constant.annotaion.LogAnnotation;
import com.nexon.nutriai.repository.DialogueLogRepository;
import com.nexon.nutriai.repository.entity.DialogueLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private final DialogueLogRepository dialogueLogRepository;

    @Pointcut("@annotation(com.nexon.nutriai.constant.annotaion.LogAnnotation)")
    public void logPointcut() {
        // 定义切点
    }

    @Around("logPointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取方法签名和注解信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        LogAnnotation logAnnotation = method.getAnnotation(LogAnnotation.class);
        String description = logAnnotation.value();
        String methodName = method.getDeclaringClass().getName() + "." + method.getName();

        log.debug("========== 开始执行: {} ==========", description.isEmpty() ? methodName : description);

        // 步骤 3: 执行目标方法，并获取返回的 Mono
        // 注意：这里必须使用 Object 接收，因为返回值可能是 Mono, Flux 或其他类型
        Object result = joinPoint.proceed();

        // 步骤 4: 判断返回值是否是响应式类型，并添加切面逻辑
        if (result instanceof Mono) {
            Mono<?> monoResult = (Mono<?>) result;
            return monoResult
                    .doOnSuccess(success -> {
                        long endTime = System.currentTimeMillis();
                        log.debug("========== 执行成功: {}, 耗时: {}ms ==========", description.isEmpty() ? methodName : description, (endTime - startTime));
                    })
                    .doOnError(error -> {
                        long endTime = System.currentTimeMillis();
                        log.error("========== 执行失败: {}, 耗时: {}ms, 错误: {} ==========", description.isEmpty() ? methodName : description, (endTime - startTime), error.getMessage());
                    });
        }
        // 如果需要处理 Flux，逻辑类似
         else if (result instanceof Flux) {
             Flux<?> fluxResult = (Flux<?>) result;
            return fluxResult
                    .doOnComplete(() -> {
                        long endTime = System.currentTimeMillis();
                        log.debug("========== 执行成功: {}, 耗时: {}ms ==========", description.isEmpty() ? methodName : description, (endTime - startTime));
                    })
                    .doOnError(error -> {
                        long endTime = System.currentTimeMillis();
                        log.error("========== 执行失败: {}, 耗时: {}ms, 错误: {} ==========", description.isEmpty() ? methodName : description, (endTime - startTime), error.getMessage());
                    });
         }
        else {
            // 如果不是响应式类型，按传统方式处理（虽然 WebFlux 中很少见）
            long endTime = System.currentTimeMillis();
            log.debug("========== 执行完成 (非响应式): {}, 耗时: {}ms ==========", description.isEmpty() ? methodName : description, (endTime - startTime));
            return result;
        }
    }

    private void saveDialogueLog(String chatId, String phone, String question, String response) {
        DialogueLog dialogueLog = new DialogueLog();
        dialogueLog.setRequestId(chatId);
        dialogueLog.setPhone(phone);
        dialogueLog.setQuestion(question);
        dialogueLog.setAnswer(response);
        dialogueLogRepository.save(dialogueLog);
    }
}
