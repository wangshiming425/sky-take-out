package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
public class AutoFillAspect {
    @Pointcut("execution(* com.sky.service.*.*(..))&& @annotation(com.sky.annotation.AutoFill)")
    public void pointcut() {
    }


    @Around("pointcut()")
    public Object autoFill(ProceedingJoinPoint pjp) throws Throwable {
        log.info("开始公共字段的填充");

        MethodSignature signature = (MethodSignature) pjp.getSignature();
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        OperationType type = autoFill.value();

        Object[] args = pjp.getArgs();
        if (args == null || args.length == 0) {
            return pjp.proceed();
        }

        Object entity = args[0];
        LocalDateTime now = LocalDateTime.now();
        Long empId = BaseContext.getCurrentId();

        log.info("操作类型: {}, 当前用户ID: {}", type, empId);

        if (type == OperationType.INSERT) {
            setFieldValue(entity, "createTime", now);
            setFieldValue(entity, "updateTime", now);
            setFieldValue(entity, "createUser", empId);
            setFieldValue(entity, "updateUser", empId);
        } else if (type == OperationType.UPDATE) {
            setFieldValue(entity, "updateTime", now);
            setFieldValue(entity, "updateUser", empId);
        }

        return pjp.proceed();
    }

    private void setFieldValue(Object obj, String fieldName, Object value) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
            log.debug("字段 {} 已设置为 {}", fieldName, value);
        } catch (NoSuchFieldException e) {
            log.warn("类 {} 中不存在字段 {}", obj.getClass().getName(), fieldName);
        } catch (IllegalAccessException e) {
            log.error("设置字段 {} 失败", fieldName, e);
        }
    }
}
