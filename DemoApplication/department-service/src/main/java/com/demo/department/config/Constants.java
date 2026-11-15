package com.demo.department.config;

public class Constants {

    private Constants() {
        // Prevent object creation
    }

    // Cred
    public static final String USERNAME_STRING = "username";
    public static final String PASSWORD_STRING = "password";
    public static final String ACCESS_TOKEN = "accessToken";
    public static final String ADMIN_STRING = "admin";
    public static final String ADMIN = "ADMIN";
    public static final String TOKEN_TYPE = "tokenType";
    public static final String USER = "USER";
    public static final String BEARER = "Bearer";
    public static final String INVALID_CREDENTIALS = "Invalid credentials";

    public static final String TITLE = "Department Service";

    // API Endpoints
    public static final String DEPARTMENTS = "/api/departments";
    public static final String WITH_EMPLOYEE_COUNTS = "/with-employee-counts";
    public static final String SEARCH = "/search";
    public static final String ID = "/{id}";
    public static final String TOKEN_ENDPOINT = "/token";
    public static final String AUTH_ENDPOINT = "/auth";

    // Request Parameters
    public static final String AUTHORIZATION = "Authorization";
    public static final String PARAM_Q = "q";
    public static final String PARAM_PAGE = "page";
    public static final String PARAM_SIZE = "size";
    public static final String PARAM_SORT = "sort";

    // Pagination Defaults
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    public static final int MIN_PAGE_SIZE = 1;
    public static final int MAX_PAGE_SIZE = 100;

    // Sorting
    public static final String DEFAULT_SORT_FIELD = "name";
    public static final String DEFAULT_SORT_DIRECTION = "asc";
    public static final String SORT_ASC = "asc";
    public static final String SORT_DESC = "desc";

    public static final String SORT_NAME = "name";
    public static final String SORT_LOCATION = "location";
    public static final String SORT_ID = "id";

    // Request Body Map Keys
    public static final String DEPARTMENT_ID = "departmentId";
    public static final String EMPLOYEE_COUNT = "employeeCount";

    // Default Values
    public static final long DEFAULT_EMPLOYEE_COUNT = 0L;

    // Validation Messages
    public static final String INVALID_PAGE_SIZE = "Invalid page or size (1-100).";

    public static final String UNSUPPORTED_SORT_FIELD = "Unsupported sort field.";

    public static final String INVALID_SORT_DIRECTION = "Sort direction must be asc or desc.";

    // Business Error Messages
    public static final String DEPARTMENT_DELETE_ERROR = "Department cannot be deleted while employees are assigned to it.";

    // Exception config
    public static final String CONFLICT_ERROR = "Conflict";

    // Security Paths
    public static final String SWAGGER_UI_PATH = "/swagger-ui";
    public static final String API_DOCS_PATH = "/v3/api-docs";
    public static final String ACTUATOR_HEALTH_PATH = "/actuator/health";
    public static final String ROLE = "role";

    // Security Request Attributes
    public static final String AUTHENTICATED_USER = "authenticatedUser";

    // Security Error Messages
    public static final String BEARER_TOKEN_REQUIRED = "Bearer token required";
    public static final String INVALID_EXPIRED_JWT = "Invalid or expired JWT";
    public static final String UNSUPPORTED_ROLE = "Unsupported role";
    public static final String ADMIN_ROLE_REQUIRED = "Admin role required for write operations";

    // Bearer Token
    public static final String BEARER_PREFIX = BEARER + " ";
    public static final int BEARER_PREFIX_LENGTH = 7;

    // HTTP Status Codes
    public static final int HTTP_UNAUTHORIZED = 401;
    public static final int HTTP_FORBIDDEN = 403;
}
