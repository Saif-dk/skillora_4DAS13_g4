import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  imports: [FormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly loading = signal(false);
  readonly errorMessage = signal('');

  form = { email: '', password: '' };

  constructor() {
    if (this.auth.isAuthenticated()) {
      this.router.navigate(['/dashboard']);
    }
  }

  submit(): void {
    this.errorMessage.set('');
    this.loading.set(true);

    this.auth.login(this.form).subscribe({
      next: () => this.router.navigateByUrl(this.returnUrl()),
      error: (error: Error) => {
        this.errorMessage.set(error.message);
        this.loading.set(false);
      },
    });
  }

  private returnUrl(): string {
    return this.route.snapshot.queryParamMap.get('returnUrl') || '/dashboard';
  }
}
