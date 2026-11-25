import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { TokenService } from '../services/token.service';

export const teacherGuard: CanActivateFn = () => {
  const role = inject(TokenService).getRole();

  if (role === 'ADMIN' || role === 'PROFESSOR') {
    return true;
  }

  return inject(Router).createUrlTree(['/courses']);
};
