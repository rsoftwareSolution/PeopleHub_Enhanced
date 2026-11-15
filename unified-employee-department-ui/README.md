# PeopleHub Angular Front End

Demo-ready Angular UI for the Employee Service (`8081`) and Department Service (`8082`).

## Features
- Login with `admin/admin123` or `user/user123`
- JWT stored in local storage and attached automatically by an HTTP interceptor
- ADMIN CRUD for employees and departments
- USER read-only experience with disabled write actions
- Employee and department search
- Employee-to-Department RestClient integration demo
- Responsive dashboard and API error notifications

## Runtime API configuration

Edit the `window.__PEOPLEHUB_CONFIG__` object in `src/index.html` before building to point to deployed Employee and Department API origins. The defaults target `localhost:8081` and `localhost:8082`.

## Run
```bash
fnm env --use-on-cd --shell powershell | Out-String | Invoke-Expression
npm ci
npm run build
npm start
```
Open `http://localhost:4200`.

Start Department Service on `8082` and Employee Service on `8081` first.

## Required backend CORS
Because Angular runs on port `4200`, allow `http://localhost:4200` in both Spring Boot services. A simple development configuration is:

```java
@Configuration
public class CorsConfig {
    @Bean
    WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                    .allowedOrigins("http://localhost:4200")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*");
            }
        };
    }
}
```

If the custom `JwtFilter` processes `OPTIONS`, add an OPTIONS bypass or allow it before JWT validation, otherwise browser CORS preflight requests can return 401.

## Configuration
API base URLs are in `src/app/core/api.config.ts`.

## Build and test

```bash
npm run build
npm test -- --watch=false
```
