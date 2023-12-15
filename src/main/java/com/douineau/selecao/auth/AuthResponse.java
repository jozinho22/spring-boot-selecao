package com.douineau.selecao.auth;

import org.springframework.http.HttpStatus;

public class AuthResponse {

    private String token;

    private HttpStatus httpStatus;

    private String message;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public AuthResponse() {
    }

    public AuthResponse(String token, HttpStatus httpStatus, String message) {
        this.token = token;
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
