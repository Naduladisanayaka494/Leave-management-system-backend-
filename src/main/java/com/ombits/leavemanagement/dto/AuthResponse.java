package com.ombits.leavemanagement.dto;

public class AuthResponse {
    private String token;
    private String tokenType = "Bearer";
    private UserDto user;
    private EmployeeDto employee;

    public AuthResponse() {
    }

    public AuthResponse(String token, UserDto user, EmployeeDto employee) {
        this.token = token;
        this.tokenType = "Bearer";
        this.user = user;
        this.employee = employee;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public UserDto getUser() {
        return user;
    }

    public void setUser(UserDto user) {
        this.user = user;
    }

    public EmployeeDto getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeDto employee) {
        this.employee = employee;
    }
}
