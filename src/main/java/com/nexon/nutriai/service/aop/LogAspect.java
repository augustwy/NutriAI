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

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private final LoginLogRepository loginLogRepository;
    private final DialogueSessionRepository dialogueSessionRepository;
    private final DialogueDetailRepository dialogueDetailRepository;
    private final SingleRequestLogRepository singleRequestLogRepository;

    @Pointcut("@annotation(com.nexon.nutriai.constant.annotaion.LogAnnotation)")
    public void logPointcut() {
        // 定义切点
    }

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

    private void handleLoginFailure(ProceedingJoinPoint joinPoint, String methodName, String description, String errorMessage) {
        log.error("========== 登录执行失败: {}, 错误: {} ==========", description.isEmpty() ? methodName : description, errorMessage);
        saveLoginLog(joinPoint, false, errorMessage, getLoginType(methodName));
    }

    private String getLoginType(String methodName) {
        if (methodName.contains("signInWithOpenId")) {
            return "WECHAT";
        } else if (methodName.contains("signIn")) {
            return "PASSWORD";
        }
        return "UNKNOWN";
    }

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

    private void executeAsync(Runnable task) {
        Mono.fromRunnable(task)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

    private String extractUsernameFromArgs(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof String str && str.matches("^1[3-9]\\d{9}$")) {
                return str;
            }
        }
        return "unknown";
    }
}
