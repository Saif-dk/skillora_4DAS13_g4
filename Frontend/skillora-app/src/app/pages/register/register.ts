import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-register',
  imports: [FormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: '../login/login.scss',
})
export class Register {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly loading = signal(false);
  readonly errorMessage = signal('');

  form = { firstName: '', lastName: '', email: '', password: '', confirmPassword: '' };

  submit(): void {
    this.errorMessage.set('');

    if (this.form.password !== this.form.confirmPassword) {
      this.errorMessage.set('Les mots de passe ne correspondent pas.');
      return;
    }

    this.loading.set(true);
    this.auth
      .register({
        firstName: this.form.firstName,
        lastName: this.form.lastName,
        email: this.form.email,
        password: this.form.password,
      })
      .subscribe({
        next: () => this.router.navigate(['/dashboard']),
        error: (error: Error) => {
          this.errorMessage.set(error.message);
          this.loading.set(false);
        },
      });
  }
}
