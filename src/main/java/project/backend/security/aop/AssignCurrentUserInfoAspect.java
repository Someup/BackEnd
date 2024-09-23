package project.backend.security.aop;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import project.backend.common.error.CustomException;
import project.backend.common.error.ErrorCode;
import project.backend.security.oauth.KakaoUserDetails;

@Aspect
@Component
public class AssignCurrentUserInfoAspect {

    // @AssignCurrentUserInfo 가 있는 메서드 실행 전에 현재 유저의 ID를 CurrentUserInfo 객체에 할당
    @Before("@annotation(project.backend.security.aop.AssignCurrentUserInfo)")
    public void assignUserId(JoinPoint joinPoint) {
        Arrays.stream(joinPoint.getArgs())
                .forEach(arg -> getMethod(arg.getClass())
                        .ifPresent(
                                setUserId -> invokeMethod(arg, setUserId, getCurrentUserId())
                        )
                );
    }

    // Login 했을 경우 현재 유저의 ID를 CurrentUserInfo 객체에 할당
    // Login 하지 않았을 경우 CurrentUserInfo 객체에 null 할당
    @Before("@annotation(project.backend.security.aop.AssignOrNullCurrentUserInfo)")
    public void assignUserIdOrNull(JoinPoint joinPoint) {
        Arrays.stream(joinPoint.getArgs())
                .forEach(arg -> getMethod(arg.getClass())
                        .ifPresent(
                                setUserId -> invokeMethod(arg, setUserId, getCurrentUserIdOrNull())
                        )
                );
    }

    // arg 객체의 setUserId 메서드를 호출하여 현재 유저의 ID를 할당
    private void invokeMethod(Object arg, Method method, Long currentUserId) {
        try {
            method.invoke(arg, currentUserId);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    // arg 객체의 setUserId 메서드를 찾아 반환
    private Optional<Method> getMethod(Class<?> clazz) {
        try {
            return Optional.of(clazz.getMethod("setUserId", Long.class));
        } catch (NoSuchMethodException e) {
            return Optional.empty();
        }
    }

    // 현재 유저의 ID를 반환
    private Long getCurrentUserId() {
        return getCurrentUserIdCheck()
                .orElseThrow(RuntimeException::new);
    }

    // 현재 유저의 ID가 존재한다면 ID 반환, 없다면 null 반환
    private Long getCurrentUserIdOrNull() {
        return getCurrentUserIdCheckOrNull()
                .orElse(null);
    }

    private Optional<Long> getCurrentUserIdCheck() {
        // 현재 SecurityContext 에서 Authentication 객체를 가져옴
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            // 인증 정보가 없을 때 예외를 던짐
            throw new CustomException(ErrorCode.NONE_AUTHENTICATED);
        }

        Object principal = authentication.getPrincipal();

        // principal 이 UserDetails 타입인 경우에만 ID 반환
        if (principal instanceof KakaoUserDetails kakaoUserDetails) {
            return Optional.ofNullable(kakaoUserDetails.getId());
        }

        // principal 이 UserDetails 가 아닌 경우 예외를 던짐
        throw new CustomException(ErrorCode.NOT_AUTHENTICATED);
    }

    private Optional<Long> getCurrentUserIdCheckOrNull() {
        // 현재 SecurityContext 에서 Authentication 객체를 가져옴
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            // 인증 정보가 없을 때 예외를 던짐
            throw new CustomException(ErrorCode.NONE_AUTHENTICATED);
        }

        Object principal = authentication.getPrincipal();

        // principal 이 UserDetails 타입인 경우에만 ID 반환
        if (principal instanceof KakaoUserDetails kakaoUserDetails) {
            return Optional.ofNullable(kakaoUserDetails.getId());
        }

        return Optional.empty();
    }

}