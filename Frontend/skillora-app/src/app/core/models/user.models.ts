export type UserRole = 'ADMIN' | 'PROFESSOR' | 'STUDENT';

export interface UserDto {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  role: UserRole;
  enabled: boolean;
  createdAt: string;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  expiresIn: number;
  user: UserDto;
}

export interface LoginPayload {
  email: string;
  password: string;
}

export interface RegisterPayload {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
}

export interface UpdateProfilePayload {
  firstName: string;
  lastName: string;
  email: string;
}

export interface ChangePasswordPayload {
  currentPassword: string;
  newPassword: string;
}

export interface CreateUserPayload {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  role: UserRole;
  enabled: boolean;
}

export interface UpdateUserPayload {
  firstName: string;
  lastName: string;
  email: string;
  role: UserRole;
  enabled: boolean;
}

export interface ApiError {
  status: number;
  error?: string;
  message?: string;
  validationErrors?: Record<string, string>;
}
