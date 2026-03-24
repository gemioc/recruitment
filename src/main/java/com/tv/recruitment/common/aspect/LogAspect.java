package com.tv.recruitment.common.aspect;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.tv.recruitment.common.annotation.Log;
import com.tv.recruitment.common.result.Result;
import com.tv.recruitment.common.utils.SecurityUtils;
import com.tv.recruitment.dto.response.LoginResponse;
import com.tv.recruitment.entity.OperationLog;
import com.tv.recruitment.mapper.OperationLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 操作日志切面
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private final OperationLogMapper operationLogMapper;

    /**
     * 处理正常返回
     */
    @AfterReturning(pointcut = "@annotation(logAnnotation)", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, Log logAnnotation, Object result) {
        handleLog(joinPoint, logAnnotation, result, null);
    }

    /**
     * 处理异常
     */
    @AfterThrowing(pointcut = "@annotation(logAnnotation)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Log logAnnotation, Exception e) {
        handleLog(joinPoint, logAnnotation, null, e);
    }

    /**
     * 统一处理日志记录
     */
    private void handleLog(JoinPoint joinPoint, Log logAnnotation, Object result, Exception e) {
        try {
            HttpServletRequest request = getRequest();

            OperationLog operationLog = new OperationLog();
            operationLog.setOperationType(logAnnotation.type());
            operationLog.setOperationDesc(logAnnotation.desc());
            operationLog.setOperationTime(LocalDateTime.now());

            // 设置请求信息
            if (request != null) {
                operationLog.setRequestMethod(request.getMethod());
                operationLog.setRequestUrl(request.getRequestURI());
                operationLog.setIpAddress(getClientIp(request));
            }

            // 设置用户信息
            setUserInfo(operationLog, logAnnotation, result);

            // 保存请求参数
            if (logAnnotation.saveRequest()) {
                String params = getRequestParams(joinPoint, logAnnotation.excludeParams());
                operationLog.setRequestParams(params);
            }

            // 保存响应结果
            if (logAnnotation.saveResponse() && result != null) {
                String responseStr = toJsonString(result);
                operationLog.setResponseResult(responseStr);
            }

            // 异常信息
            if (e != null) {
                operationLog.setResponseResult("异常: " + e.getMessage());
            }

            // 执行时间
            operationLog.setExecutionTime(0);

            // 异步保存
            saveLogAsync(operationLog);

        } catch (Exception ex) {
            log.error("记录操作日志异常: {}", ex.getMessage());
        }
    }

    /**
     * 设置用户信息
     */
    private void setUserInfo(OperationLog operationLog, Log logAnnotation, Object result) {
        // 登录操作特殊处理：从返回结果获取用户信息
        if ("LOGIN".equals(logAnnotation.type()) && result != null) {
            try {
                if (result instanceof Result<?> apiResult && apiResult.getData() instanceof LoginResponse loginResponse) {
                    if (loginResponse.getUserInfo() != null) {
                        operationLog.setUserId(loginResponse.getUserInfo().getId());
                        operationLog.setUserName(loginResponse.getUserInfo().getUsername());
                        return;
                    }
                }
            } catch (Exception ignored) {}
        }

        // 其他操作：从SecurityContext获取
        Long userId = SecurityUtils.getCurrentUserId();
        String username = SecurityUtils.getCurrentUsername();
        if (userId != null) {
            operationLog.setUserId(userId);
        }
        if (StrUtil.isNotBlank(username)) {
            operationLog.setUserName(username);
        }
    }

    /**
     * 异步保存日志
     */
    @Async
    public void saveLogAsync(OperationLog operationLog) {
        try {
            operationLogMapper.insert(operationLog);
        } catch (Exception e) {
            log.error("保存操作日志失败: {}", e.getMessage());
        }
    }

    /**
     * 获取请求对象
     */
    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    /**
     * 获取客户端真实IP
     * 支持Nginx等代理场景
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = null;

        // 优先从代理头获取真实IP
        String[] headerNames = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };

        for (String header : headerNames) {
            ip = request.getHeader(header);
            if (isValidIp(ip)) {
                // X-Forwarded-For可能包含多个IP，取第一个
                if ("X-Forwarded-For".equalsIgnoreCase(header) && ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                break;
            }
        }

        // 如果所有头都没获取到，使用RemoteAddr
        if (!isValidIp(ip)) {
            ip = request.getRemoteAddr();
        }

        // 本地访问返回127.0.0.1
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            ip = "127.0.0.1";
        }

        return ip;
    }

    /**
     * 判断IP是否有效
     */
    private boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip);
    }

    /**
     * 获取请求参数（过滤敏感字段）
     */
    private String getRequestParams(JoinPoint joinPoint, String[] excludeParams) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] paramNames = signature.getParameterNames();
            Object[] paramValues = joinPoint.getArgs();

            if (paramNames == null || paramNames.length == 0) {
                return "";
            }

            // 需要排除的敏感字段
            Set<String> excludeSet = new HashSet<>(Arrays.asList(excludeParams));

            Map<String, Object> params = new HashMap<>();
            for (int i = 0; i < paramNames.length; i++) {
                String paramName = paramNames[i];
                Object paramValue = paramValues[i];

                // 跳过不能序列化的对象
                if (paramValue instanceof ServletRequest
                        || paramValue instanceof ServletResponse
                        || paramValue instanceof MultipartFile) {
                    continue;
                }

                // 过滤敏感字段
                if (excludeSet.contains(paramName)) {
                    params.put(paramName, "******");
                } else {
                    params.put(paramName, paramValue);
                }
            }

            return toJsonString(params);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 对象转JSON字符串（限制长度）
     */
    private String toJsonString(Object obj) {
        try {
            String jsonStr = JSONUtil.toJsonStr(obj);
            if (jsonStr.length() > 2000) {
                jsonStr = jsonStr.substring(0, 2000) + "...";
            }
            return jsonStr;
        } catch (Exception e) {
            return "";
        }
    }
}