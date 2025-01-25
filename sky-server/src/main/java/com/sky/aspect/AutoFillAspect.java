package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Component
@Aspect
public class AutoFillAspect {

    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){}

    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) throws Exception {
        //获取被拦截的方法签名
        MethodSignature method =(MethodSignature) joinPoint.getSignature();
        //获取方法上的注解
        AutoFill autoFill=method.getMethod().getAnnotation(AutoFill.class);
        //获取方法的操作类型
        OperationType operationType = autoFill.value();
        //获取方法上的参数
        Object[] args = joinPoint.getArgs();
        if (args==null || args.length==0)return;
        Object entity= args[0];
        Class<?> entityClass = entity.getClass();
        //获取要填充的数据
        LocalDateTime now=LocalDateTime.now();
        Long currentId= BaseContext.getCurrentId();
        if (operationType==OperationType.INSERT){
            Method setCreateTime=entityClass.getMethod(AutoFillConstant.SET_CREATE_TIME,LocalDateTime.class);
            Method setCreateUser=entityClass.getMethod(AutoFillConstant.SET_CREATE_USER,Long.class);
            Method setUpdateTime=entityClass.getMethod(AutoFillConstant.SET_UPDATE_TIME,LocalDateTime.class);
            Method setUpdateUser=entityClass.getMethod(AutoFillConstant.SET_UPDATE_USER,Long.class);
            setCreateUser.invoke(entity,currentId);
            setUpdateUser.invoke(entity,currentId);
            setUpdateTime.invoke(entity,now);
            setCreateTime.invoke(entity,now);
        }else {
            Method setUpdateTime=entityClass.getMethod(AutoFillConstant.SET_UPDATE_TIME,LocalDateTime.class);
            Method setUpdateUser=entityClass.getMethod(AutoFillConstant.SET_UPDATE_USER,Long.class);
            setUpdateUser.invoke(entity,currentId);
            setUpdateTime.invoke(entity,now);
        }
    }
}
