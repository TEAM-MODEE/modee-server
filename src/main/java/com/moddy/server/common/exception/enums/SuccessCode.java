package com.moddy.server.common.exception.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessCode {


    SOCIAL_LOGIN_SUCCESS(HttpStatus.OK, "소셜 로그인 성공");

    private final HttpStatus httpStatus;
    private final String message;
}
