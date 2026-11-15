import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';

import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);

  // Do not attach an existing token to the login request.
  const isLoginRequest = req.url.includes('/auth/token');

  const token = authService.token();

  // Attach the Bearer token to authenticated API requests.
  const authReq =
    token && !isLoginRequest && !req.headers.has('Authorization')
      ? req.clone({
          setHeaders: {
            Authorization: `Bearer ${token}`
          }
        })
      : req;

  return next(authReq).pipe(
    catchError((error) => {
      // Log out when the backend rejects an authenticated request.
      if (
        error.status === 401 &&
        !isLoginRequest &&
        authService.isLoggedIn()
      ) {
        authService.logout();
      }

      return throwError(() => error);
    })
  );
};
