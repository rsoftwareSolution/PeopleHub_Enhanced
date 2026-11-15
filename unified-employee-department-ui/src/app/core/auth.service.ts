import {
  Injectable,
  computed,
  signal
} from '@angular/core';

import { HttpClient } from '@angular/common/http';

import {
  Observable,
  Subject,
  tap
} from 'rxjs';

import { API } from './api.config';

import {
  Role,
  TokenResponse
} from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly tokenKey = 'peoplehub_token';
  private readonly roleKey = 'peoplehub_role';

  private readonly logoutSubject = new Subject<void>();

  /**
   * Emits whenever the current user logs out.
   * Used by services such as InactivityService.
   */
  readonly loggedOut$ = this.logoutSubject.asObservable();

  /**
   * Current authentication token.
   */
  readonly token = signal<string>(
    this.getStoredValue(this.tokenKey) ?? ''
  );

  /**
   * Current authenticated user's role.
   */
  readonly role = signal<Role | null>(
    this.getStoredRole()
  );

  /**
   * Authentication state.
   */
  readonly isLoggedIn = computed(
    () => this.token().length > 0
  );

  /**
   * Administrator access state.
   */
  readonly isAdmin = computed(
    () => this.role() === 'ADMIN'
  );

  constructor(
    private readonly http: HttpClient
  ) {}

  // -----------------------------------------
  // Login
  // -----------------------------------------

  login(
    username: string,
    password: string
  ): Observable<TokenResponse> {

    return this.http.post<TokenResponse>(
      `${API.employee}/auth/token`,
      {
        username,
        password
      }
    ).pipe(
      tap(response => {

        if (!response?.accessToken) {
          throw new Error(
            'Authentication response does not contain an access token.'
          );
        }

        /*
         * DEMO ROLE MAPPING:
         * The current backend response model contains only
         * accessToken and tokenType.
         *
         * Therefore, this preserves the existing demo behavior.
         * Do not use this as the source of authorization truth
         * in a production application.
         */
        const role: Role =
          username.trim().toLowerCase() === 'admin'
            ? 'ADMIN'
            : 'USER';

        this.saveAuthentication(
          response.accessToken,
          role
        );
      })
    );
  }

  // -----------------------------------------
  // Store authentication
  // -----------------------------------------

  private saveAuthentication(
    accessToken: string,
    role: Role
  ): void {

    this.setStoredValue(
      this.tokenKey,
      accessToken
    );

    this.setStoredValue(
      this.roleKey,
      role
    );

    this.token.set(accessToken);
    this.role.set(role);
  }

  // -----------------------------------------
  // Logout
  // -----------------------------------------

  logout(): void {

    this.removeStoredValue(this.tokenKey);
    this.removeStoredValue(this.roleKey);

    this.token.set('');
    this.role.set(null);

    this.logoutSubject.next();
  }

  // -----------------------------------------
  // Local storage helpers
  // -----------------------------------------

  private getStoredValue(
    key: string
  ): string | null {

    try {
      if (typeof localStorage === 'undefined') {
        return null;
      }

      return localStorage.getItem(key);

    } catch {
      return null;
    }
  }

  private setStoredValue(
    key: string,
    value: string
  ): void {

    try {
      if (typeof localStorage !== 'undefined') {
        localStorage.setItem(key, value);
      }

    } catch {
      // Storage may be unavailable or blocked by the browser.
    }
  }

  private removeStoredValue(
    key: string
  ): void {

    try {
      if (typeof localStorage !== 'undefined') {
        localStorage.removeItem(key);
      }

    } catch {
      // Storage may be unavailable or blocked by the browser.
    }
  }

  // -----------------------------------------
  // Role validation
  // -----------------------------------------

  private getStoredRole(): Role | null {

    const storedRole = this.getStoredValue(this.roleKey);

    if (
      storedRole === 'ADMIN' ||
      storedRole === 'USER'
    ) {
      return storedRole;
    }

    return null;
  }
}
