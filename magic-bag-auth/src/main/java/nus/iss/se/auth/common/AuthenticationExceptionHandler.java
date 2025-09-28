package nus.iss.se.auth.common;

import lombok.extern.slf4j.Slf4j;
import nus.iss.se.common.Result;
import nus.iss.se.common.constant.ResultStatus;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class AuthenticationExceptionHandler {
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Result<Void>> handle(AuthenticationException e) {
        log.info("Authentication Exception: {}",ExceptionUtils.getStackTrace(e));
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Result.error(ResultStatus.FAIL, "Invalid Username or Password"));
    }
}
