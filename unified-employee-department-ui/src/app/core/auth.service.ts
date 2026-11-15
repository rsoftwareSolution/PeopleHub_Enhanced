import { Injectable, computed, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { API } from './api.config';
import { Role, TokenResponse } from '../models/models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly tokenKey = 'peoplehub_token';
  private readonly roleKey = 'peoplehub_role';
  readonly token = signal(localStorage.getItem(this.tokenKey) ?? '');
  readonly role = signal<Role | null>((localStorage.getItem(this.roleKey) as Role | null));
  readonly isLoggedIn = computed(() => !!this.token());
  readonly isAdmin = computed(() => this.role() === 'ADMIN');

  constructor(private http: HttpClient) {}

  login(username: string, password: string): Observable<TokenResponse> {
    return this.http.post<TokenResponse>(`${API.employee}/auth/token`, { username, password }).pipe(
      tap(response => {
        const role: Role = username === 'admin' ? 'ADMIN' : 'USER';
        localStorage.setItem(this.tokenKey, response.accessToken);
        localStorage.setItem(this.roleKey, role);
        this.token.set(response.accessToken);
        this.role.set(role);
      })
    );
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.roleKey);
    this.token.set('');
    this.role.set(null);
  }
}
