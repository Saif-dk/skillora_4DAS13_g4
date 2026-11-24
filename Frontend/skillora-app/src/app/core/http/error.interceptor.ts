import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';
import { ApiError } from '../models/user.models';

export const errorInterceptor: HttpInterceptorFn = (request, next) =>
  next(request).pipe(
    catchError((error: HttpErrorResponse) => {
      const body = error.error as ApiError | null;
      const validationMessage = body?.validationErrors ? Object.values(body.validationErrors)[0] : undefined;
      const message = validationMessage ?? body?.message ?? 'Une erreur est survenue. Veuillez réessayer.';
      return throwError(() => new Error(message));
    }),
  );
