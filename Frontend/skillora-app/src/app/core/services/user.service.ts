import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { API_BASE_URL } from '../config/api.config';
import { CreateUserPayload, UpdateUserPayload, UserDto } from '../models/user.models';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${API_BASE_URL}/users`;

  findAll() {
    return this.http.get<UserDto[]>(this.baseUrl);
  }

  findById(id: number) {
    return this.http.get<UserDto>(`${this.baseUrl}/${id}`);
  }

  create(payload: CreateUserPayload) {
    return this.http.post<UserDto>(this.baseUrl, payload);
  }

  update(id: number, payload: UpdateUserPayload) {
    return this.http.put<UserDto>(`${this.baseUrl}/${id}`, payload);
  }

  setEnabled(id: number, enabled: boolean) {
    return this.http.patch<UserDto>(`${this.baseUrl}/${id}/status`, null, { params: { enabled: String(enabled) } });
  }

  delete(id: number) {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
