package com.nexon.nutriai.service.aop;

import com.nexon.nutriai.constant.annotaion.LogAnnotation;
import com.nexon.nutriai.repository.DialogueDetailRepository;
import com.nexon.nutriai.repository.DialogueSessionRepository;
import com.nexon.nutriai.repository.LoginLogRepository;
import com.nexon.nutriai.repository.SingleRequestLogRepository;
import com.nexon.nutriai.repository.entity.DialogueDetail;
import com.nexon.nutriai.repository.entity.DialogueSession;
import com.nexon.nutriai.repository.entity.LoginLog;
import com.nexon.nutriai.repository.entity.SingleRequestLog;
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
import reactor.core.scheduler.Schedulers;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 日志切面类
 * 
 * 使用AOP技术对带有@LogAnnotation注解的方法进行日志记录。
 * 支持普通请求、登录请求和对话请求三种类型的日志记录。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private final LoginLogRepository loginLogRepository;
    private final DialogueSessionRepository dialogueSessionRepository;
    private final DialogueDetailRepository dialogueDetailRepository;
    private final SingleRequestLogRepository singleRequestLogRepository;

    /**
     * 定义切点
     * 
     * 匹配所有带有@LogAnnotation注解的方法。
     */
    @Pointcut("@annotation(com.nexon.nutriai.constant.annotaion.LogAnnotation)")
    public void logPointcut() {
        // 定义切点
    }

    /**
     * 环绕通知
     * 
     * 对目标方法进行环绕增强，记录方法执行时间、结果等信息。
     * 根据请求类型分发到不同的处理方法。
     * 
     * @param joinPoint 连接点
     * @return 目标方法的返回值
     * @throws Throwable 可能抛出的异常
     */
    @Around("logPointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        LogAnnotation logAnnotation = method.getAnnotation(LogAnnotation.class);
        String description = logAnnotation.value();
        String methodName = method.getDeclaringClass().getName() + "." + method.getName();
        LogAnnotation.RequestType requestType = logAnnotation.requestType();

        log.debug("========== 开始执行: {} ==========", description.isEmpty() ? methodName : description);

        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Exception e) {
            if (requestType == LogAnnotation.RequestType.LOGIN) {
                handleLoginFailure(joinPoint, methodName, description, e.getMessage());
            }
            throw e;
        }

        return switch (requestType) {
            case LOGIN -> handleLoginRequest(joinPoint, result, methodName, description, startTime);
            case DIALOGUE -> handleDialogueRequest(joinPoint, result, methodName, description, startTime);
            default -> handleNormalRequest(joinPoint, result, methodName, description, startTime);
        };
    }

    /**
     * 处理登录请求日志
     * 
     * @param joinPoint 连接点
     * @param result 方法执行结果
     * @param methodName 方法名
     * @param description 方法描述
     * @param startTime 开始时间
     * @return 处理后的结果
     */
    private Object handleLoginRequest(ProceedingJoinPoint joinPoint, Object result, String methodName, String description, long startTime) {
        if (result instanceof Mono<?> monoResult) {
            return monoResult
                    .doOnSuccess(success -> {
                        long endTime = System.currentTimeMillis();
                        log.debug("========== 登录执行成功: {}, 耗时: {}ms ==========", description.isEmpty() ? methodName : description, (endTime - startTime));
                        saveLoginLog(joinPoint, true, null, getLoginType(methodName));
                    })
                    .doOnError(error -> {
                        long endTime = System.currentTimeMillis();
                        log.error("========== 登录执行失败: {}, 耗时: {}ms, 错误: {} ==========", description.isEmpty() ? methodName : description, (endTime - startTime), error.getMessage());
                        saveLoginLog(joinPoint, false, error.getMessage(), getLoginType(methodName));
                    });
        } else {
            long endTime = System.currentTimeMillis();
            log.debug("========== 登录执行完成: {}, 耗时: {}ms ==========", description.isEmpty() ? methodName : description, (endTime - startTime));
            saveLoginLog(joinPoint, result != null, null, getLoginType(methodName));
            return result;
        }
    }

    /**
     * 处理登录失败情况
     * 
     * @param joinPoint 连接点
     * @param methodName 方法名
     * @param description 方法描述
     * @param errorMessage 错误信息
     */
    private void handleLoginFailure(ProceedingJoinPoint joinPoint, String methodName, String description, String errorMessage) {
        log.error("========== 登录执行失败: {}, 错误: {} ==========", description.isEmpty() ? methodName : description, errorMessage);
        saveLoginLog(joinPoint, false, errorMessage, getLoginType(methodName));
    }

    /**
     * 获取登录类型
     * 
     * 根据方法名判断登录类型（微信登录或密码登录）。
     * 
     * @param methodName 方法名
     * @return 登录类型
     */
    private String getLoginType(String methodName) {
        if (methodName.contains("signInWithOpenId")) {
            return "WECHAT";
        } else if (methodName.contains("signIn")) {
            return "PASSWORD";
        }
        return "UNKNOWN";
    }

    /**
     * 处理对话请求日志
     * 
     * @param joinPoint 连接点
     * @param result 方法执行结果
     * @param methodName 方法名
     * @param description 方法描述
     * @param startTime 开始时间
     * @return 处理后的结果
     */
    private Object handleDialogueRequest(ProceedingJoinPoint joinPoint, Object result, String methodName, String description, long startTime) {
        String chatId = null;
        String phone = null;
        String question = null;

        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof com.nexon.nutriai.pojo.request.BaseRequest baseRequest) {
                phone = baseRequest.getPhone();
                chatId = baseRequest.getChatId();
            }
        }

        for (Object arg : args) {
            if (arg instanceof String && !arg.equals(chatId)) {
                question = (String) arg;
                break;
            }
        }

        if (result instanceof Flux<?> fluxResult) {
            StringBuilder responseBuilder = new StringBuilder();

            String finalChatId = chatId;
            String finalPhone = phone;
            String finalQuestion = question;
            return fluxResult
                    .doOnNext(data -> {
                        if (data instanceof String) {
                            responseBuilder.append((String) data);
                        }
                    })
                    .doOnComplete(() -> {
                        long endTime = System.currentTimeMillis();
                        log.debug("========== 对话执行成功: {}, 耗时: {}ms ==========", description.isEmpty() ? methodName : description, (endTime - startTime));

                        if (finalChatId != null && finalPhone != null && finalQuestion != null) {
                            saveDialogueLog(finalChatId, finalPhone, finalQuestion, responseBuilder.toString(), methodName);
                        }
                    })
                    .doOnError(error -> {
                        long endTime = System.currentTimeMillis();
                        log.error("========== 对话执行失败: {}, 耗时: {}ms, 错误: {} ==========", description.isEmpty() ? methodName : description, (endTime - startTime), error.getMessage());
                    });
        } else if (result instanceof Mono<?> monoResult) {
            return monoResult
                    .doOnSuccess(success -> {
                        long endTime = System.currentTimeMillis();
                        log.debug("========== 对话执行成功: {}, 耗时: {}ms ==========", description.isEmpty() ? methodName : description, (endTime - startTime));
                    })
                    .doOnError(error -> {
                        long endTime = System.currentTimeMillis();
                        log.error("========== 对话执行失败: {}, 耗时: {}ms, 错误: {} ==========", description.isEmpty() ? methodName : description, (endTime - startTime), error.getMessage());
                    });
        } else {
            long endTime = System.currentTimeMillis();
            log.debug("========== 对话执行完成 (非响应式): {}, 耗时: {}ms ==========", description.isEmpty() ? methodName : description, (endTime - startTime));
            return result;
        }
    }

    /**
     * 处理普通请求日志
     * 
     * @param joinPoint 连接点
     * @param result 方法执行结果
     * @param methodName 方法名
     * @param description 方法描述
     * @param startTime 开始时间
     * @return 处理后的结果
     */
    private Object handleNormalRequest(ProceedingJoinPoint joinPoint, Object result, String methodName, String description, long startTime) {
        if (result instanceof Mono<?> monoResult) {
            return monoResult
                    .doOnSuccess(success -> {
                        long endTime = System.currentTimeMillis();
                        log.debug("========== 普通请求执行成功: {}, 耗时: {}ms ==========", description.isEmpty() ? methodName : description, (endTime - startTime));
                        saveSingleRequestLog(methodName, joinPoint.getArgs(), success, null, (int)(endTime - startTime), true);
                    })
                    .doOnError(error -> {
                        long endTime = System.currentTimeMillis();
                        log.error("========== 普通请求执行失败: {}, 耗时: {}ms, 错误: {} ==========", description.isEmpty() ? methodName : description, (endTime - startTime), error.getMessage());
                        saveSingleRequestLog(methodName, joinPoint.getArgs(), null, error.getMessage(), (int)(endTime - startTime), false);
                    });
        } else if (result instanceof Flux<?> fluxResult) {
            StringBuilder responseBuilder = new StringBuilder();

            return fluxResult
                    .doOnNext(data -> {
                        if (data != null) {
                            responseBuilder.append(data);
                        }
                    })
                    .doOnComplete(() -> {
                        long endTime = System.currentTimeMillis();
                        log.debug("========== 普通请求执行成功: {}, 耗时: {}ms ==========", description.isEmpty() ? methodName : description, (endTime - startTime));
                        saveSingleRequestLog(methodName, joinPoint.getArgs(), responseBuilder.toString(), null, (int)(endTime - startTime), true);
                    })
                    .doOnError(error -> {
                        long endTime = System.currentTimeMillis();
                        log.error("========== 普通请求执行失败: {}, 耗时: {}ms, 错误: {} ==========", description.isEmpty() ? methodName : description, (endTime - startTime), error.getMessage());
                        saveSingleRequestLog(methodName, joinPoint.getArgs(), null, error.getMessage(), (int)(endTime - startTime), false);
                    });
        } else {
            long endTime = System.currentTimeMillis();
            log.debug("========== 普通请求执行完成: {}, 耗时: {}ms ==========", description.isEmpty() ? methodName : description, (endTime - startTime));
            saveSingleRequestLog(methodName, joinPoint.getArgs(), result, null, (int)(endTime - startTime), true);
            return result;
        }
    }

    /**
     * 保存对话日志
     * 
     * @param chatId 会话ID
     * @param phone 用户手机号
     * @param question 用户问题
     * @param response AI回答
     * @param methodName 方法名
     */
    private void saveDialogueLog(String chatId, String phone, String question, String response, String methodName) {
        executeAsync(() -> {
            try {
                DialogueSession session = dialogueSessionRepository.findBySessionId(chatId)
                        .orElseGet(() -> {
                            DialogueSession newSession = new DialogueSession();
                            newSession.setSessionId(chatId);
                            newSession.setPhone(phone);
                            newSession.setTitle(question.length() > 50 ? question.substring(0, 50) + "..." : question);
                            newSession.setMethodName(methodName);
                            return newSession;
                        });
                if (session.getMethodName() == null) {
                    session.setMethodName(methodName);
                }
                dialogueSessionRepository.save(session);

                int maxSequence = dialogueDetailRepository.findBySessionIdOrderBySequence(chatId)
                        .stream()
                        .map(DialogueDetail::getSequence)
                        .max(Integer::compareTo)
                        .orElse(0);

                DialogueDetail userDetail = new DialogueDetail();
                userDetail.setSessionId(chatId);
                userDetail.setMessageType(1);
                userDetail.setContent(question);
                userDetail.setSequence(maxSequence + 1);
                dialogueDetailRepository.save(userDetail);

                DialogueDetail aiDetail = new DialogueDetail();
                aiDetail.setSessionId(chatId);
                aiDetail.setMessageType(2);
                aiDetail.setContent(response);
                aiDetail.setSequence(maxSequence + 2);
                dialogueDetailRepository.save(aiDetail);
            } catch (Exception e) {
                log.error("保存对话日志失败: chatId={}, methodName={}, error={}", chatId, methodName, e.getMessage(), e);
            }
        });
    }

    /**
     * 保存单次请求日志
     * 
     * @param methodName 方法名
     * @param args 方法参数
     * @param result 方法执行结果
     * @param errorMessage 错误信息
     * @param executeTime 执行时间
     * @param success 是否成功
     */
    private void saveSingleRequestLog(String methodName, Object[] args, Object result, String errorMessage, int executeTime, boolean success) {
        executeAsync(() -> {
            try {
                SingleRequestLog log = new SingleRequestLog();
                log.setMethodName(methodName);
                log.setRequestParams(Arrays.toString(args));
                log.setResponseData(result != null ? result.toString() : null);
                log.setErrorMessage(errorMessage);
                log.setExecuteTime(executeTime);
                log.setSuccess(success ? 1 : 0);
                singleRequestLogRepository.save(log);
            } catch (Exception e) {
                log.error("保存单次请求日志失败: methodName={}, error={}", methodName, e.getMessage());
            }
        });
    }

    /**
     * 保存登录日志
     * 
     * @param joinPoint 连接点
     * @param success 是否成功
     * @param failureReason 失败原因
     * @param loginType 登录类型
     */
    private void saveLoginLog(ProceedingJoinPoint joinPoint, boolean success, String failureReason, String loginType) {
        executeAsync(() -> {
            try {
                LoginLog loginLog = new LoginLog();
                String username = extractUsernameFromArgs(joinPoint.getArgs());
                loginLog.setUsername(username);
                loginLog.setStatus(success ? 1 : 0);
                loginLog.setFailureReason(failureReason);
                loginLog.setLoginType(loginType);
                loginLogRepository.save(loginLog);
            } catch (Exception e) {
                log.error("保存登录日志失败: error={}", e.getMessage(), e);
            }
        });
    }

    /**
     * 异步执行任务
     * 
     * 使用boundedElastic线程池异步执行任务，避免阻塞主线程。
     * 
     * @param task 要执行的任务
     */
    private void executeAsync(Runnable task) {
        Mono.fromRunnable(task)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

    /**
     * 从参数中提取用户名
     * 
     * @param args 参数数组
     * @return 用户名（手机号）
     */
    private String extractUsernameFromArgs(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof String str && str.matches("^1[3-9]\\d{9}$")) {
                return str;
            }
        }
        return "unknown";
    }
}