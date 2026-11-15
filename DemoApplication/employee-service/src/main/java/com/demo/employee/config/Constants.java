
package com.demo.employee.config;

public final class Constants {

    private Constants() {
        // Prevent object creation
    }

    // API Endpoints
    public static final String EMPLOYEES = "/api/employees";
    public static final String TOKEN_ENDPOINT = "/token";
    public static final String AUTH_ENDPOINT = "/auth";
    public static final String SEARCH = "/search";
    public static final String ID = "/{id}";
    public static final String DEPARTMENT_COUNTS = "/department-counts";
    public static final String DEPARTMENT = "/department";
    public static final String DEPARTMENT_ID = "/{id}";
    public static final String DEPARTMENT_EMPLOYEES = "/{id}/employees";
    public static final String DEPARTMENT_COUNT = "/{id}/count";
    public static final String DEPARTMENT_DETAILS = "/{id}/details";

    // Credentials and Authentication
    public static final String USERNAME_STRING = "username";
    public static final String PASSWORD_STRING = "password";
    public static final String ACCESS_TOKEN = "accessToken";
    public static final String ADMIN_STRING = "admin";
    public static final String ADMIN = "ADMIN";
    public static final String TOKEN_TYPE = "tokenType";
    public static final String USER = "USER";
    public static final String BEARER = "Bearer";
    public static final String BEARER_PREFIX = BEARER + " ";
    public static final String AUTHORIZATION = "Authorization";
    public static final String INVALID_CREDENTIALS = "Invalid credentials";

    // Service Information
    public static final String TITLE = "Employee Service";

    // Request Parameters
    public static final String PARAM_Q = "q";
    public static final String PARAM_DEPARTMENT_ID = "departmentId";
    public static final String PARAM_PAGE = "page";
    public static final String PARAM_SIZE = "size";
    public static final String PARAM_SORT = "sort";

    // Pagination Defaults
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    public static final int MIN_PAGE_SIZE = 1;
    public static final int MAX_PAGE_SIZE = 100;

    // Annotation-compatible pagination defaults
    public static final String DEFAULT_PAGE_VALUE = "0";
    public static final String DEFAULT_SIZE_VALUE = "20";

    // Exception config
    public static final String CONFLICT_ERROR = "Conflict";

    // Sorting
    public static final String DEFAULT_SORT_FIELD = "name";
    public static final String DEFAULT_SORT_DIRECTION = "asc";
    public static final String DEFAULT_SORT = DEFAULT_SORT_FIELD + "," + DEFAULT_SORT_DIRECTION;
    public static final String SORT_ASC = "asc";
    public static final String SORT_DESC = "desc";

    public static final String SORT_NAME = "name";
    public static final String SORT_EMAIL = "email";
    public static final String SORT_ID = "id";
    public static final String SORT_DEPARTMENT_ID = "departmentId";

    // Response Map Keys
    public static final String EMPLOYEE_COUNT = "employeeCount";

    // Request Attributes
    public static final String AUTHENTICATED_USER = "authenticatedUser";

    // HTTP Methods
    public static final String HTTP_GET = "GET";
    public static final String HTTP_OPTIONS = "OPTIONS";

    // Security Paths
    public static final String SWAGGER_UI_PATH = "/swagger-ui";
    public static final String API_DOCS_PATH = "/v3/api-docs";
    public static final String ACTUATOR_HEALTH_PATH = "/actuator/health";
    public static final String ROLE = "role";

    // Security Error Messages
    public static final String BEARER_TOKEN_REQUIRED = "Bearer token required";
    public static final String INVALID_EXPIRED_JWT = "Invalid or expired JWT";
    public static final String UNSUPPORTED_ROLE = "Unsupported role";
    public static final String ADMIN_ROLE_REQUIRED =
            "Admin role required for write operations";

    // Validation Messages
    public static final String INVALID_PAGE_SIZE =
            "Invalid page or size (1-100).";
    public static final String UNSUPPORTED_SORT_FIELD =
            "Unsupported sort field.";
    public static final String INVALID_SORT_DIRECTION =
            "Sort direction must be asc or desc.";
}
