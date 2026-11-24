import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';
import { TokenService } from './token.service';
import {
  AuthResponse,
  ChangePasswordPayload,
  LoginPayload,
  RegisterPayload,
  UpdateProfilePayload,
  UserDto,
  UserRole,
} from '../models/user.models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly tokenService = inject(TokenService);
  private readonly currentUser = signal<UserDto | null>(null);

  readonly token = this.tokenService.token;
  readonly user = this.currentUser.asReadonly();
  readonly isAuthenticated = computed(() => !!this.tokenService.get());
  readonly roles = computed<UserRole[]>(() => {
    const role = this.currentUser()?.role ?? this.readRoleFromToken();
    return role ? [role] : [];
  });

  constructor() {
    if (this.tokenService.get()) {
      this.loadProfile().subscribe({ error: () => this.clearSession() });
    }
  }

  login(payload: LoginPayload) {
    return this.http.post<AuthResponse>(`${API_BASE_URL}/auth/login`, payload).pipe(tap((response) => this.setSession(response)));
  }

  register(payload: RegisterPayload) {
    return this.http.post<AuthResponse>(`${API_BASE_URL}/auth/register`, payload).pipe(tap((response) => this.setSession(response)));
  }

  loadProfile() {
    return this.http.get<UserDto>(`${API_BASE_URL}/users/me`).pipe(tap((user) => this.currentUser.set(user)));
  }

  updateProfile(payload: UpdateProfilePayload) {
    return this.http.put<UserDto>(`${API_BASE_URL}/users/me`, payload).pipe(tap((user) => this.currentUser.set(user)));
  }

  changePassword(payload: ChangePasswordPayload) {
    return this.http.put<void>(`${API_BASE_URL}/users/me/password`, payload);
  }

  logout(): void {
    if (this.tokenService.get()) {
      this.http.post(`${API_BASE_URL}/auth/logout`, null).subscribe({ error: () => undefined });
    }
    this.clearSession();
    this.router.navigate(['/login']);
  }

  hasRole(role: UserRole): boolean {
    return this.roles().includes(role);
  }

  private setSession(response: AuthResponse): void {
    this.tokenService.set(response.token);
    this.currentUser.set(response.user);
  }

  private clearSession(): void {
    this.tokenService.clear();
    this.currentUser.set(null);
  }

  private readRoleFromToken(): UserRole | null {
    const role = this.tokenService.getRole();
    return role === 'ADMIN' || role === 'PROFESSOR' || role === 'STUDENT'
      ? (role as UserRole)
      : null;
  }
}
