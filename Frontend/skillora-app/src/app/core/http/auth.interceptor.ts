import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { API_BASE_URL, COURSE_API_BASE_URL } from '../config/api.config';
import { TokenService } from '../services/token.service';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const token = inject(TokenService).get();
  const isSkilloraApi =
    request.url.startsWith(API_BASE_URL) || request.url.startsWith(COURSE_API_BASE_URL);

  if (!token || !isSkilloraApi) {
    return next(request);
  }

  return next(request.clone({ setHeaders: { Authorization: `Bearer ${token}` } }));
};
