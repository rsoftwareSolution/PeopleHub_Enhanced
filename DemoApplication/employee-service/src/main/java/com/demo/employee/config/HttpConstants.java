package com.demo.employee.config;

public final class HttpConstants {

    private HttpConstants() {
    }

    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String PUT = "PUT";
    public static final String PATCH = "PATCH";
    public static final String DELETE = "DELETE";
    public static final String OPTIONS = "OPTIONS";

    public static final String[] ALLOWED_METHODS = {
            GET, POST, PUT, PATCH, DELETE, OPTIONS
    };
}