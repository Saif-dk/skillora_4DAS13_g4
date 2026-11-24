import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class TokenService {
  private readonly tokenKey = 'skillora_token';
  private readonly tokenSignal = signal<string | null>(localStorage.getItem(this.tokenKey));

  readonly token = this.tokenSignal.asReadonly();

  get(): string | null {
    return this.tokenSignal();
  }

  getRole(): string | null {
    const token = this.get();
    if (!token) {
      return null;
    }

    const parts = token.split('.');
    if (parts.length < 2 || !parts[1]) {
      return null;
    }

    try {
      const base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/');
      const padded = base64.padEnd(Math.ceil(base64.length / 4) * 4, '=');
      const payload = JSON.parse(atob(padded)) as { role?: unknown };
      return typeof payload.role === 'string'
        ? payload.role.replace(/^ROLE_/, '').toUpperCase()
        : null;
    } catch {
      return null;
    }
  }

  set(token: string): void {
    localStorage.setItem(this.tokenKey, token);
    this.tokenSignal.set(token);
  }

  clear(): void {
    localStorage.removeItem(this.tokenKey);
    this.tokenSignal.set(null);
  }
}
