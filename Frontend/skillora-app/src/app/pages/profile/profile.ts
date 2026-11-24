import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { DemoModal } from '../../shared/components/modal/modal';

@Component({
  selector: 'app-profile',
  imports: [FormsModule, DemoModal],
  templateUrl: './profile.html',
  styleUrl: './profile.scss',
})
export class Profile {
  readonly auth = inject(AuthService);
  readonly user = this.auth.user;
  readonly editOpen = signal(false);
  readonly passwordOpen = signal(false);
  readonly saving = signal(false);
  readonly successMessage = signal('');
  readonly errorMessage = signal('');

  readonly memberSince = computed(() => {
    const createdAt = this.user()?.createdAt;
    return createdAt ? new Date(createdAt).toLocaleDateString('fr-FR') : '—';
  });

  form = { firstName: '', lastName: '', email: '' };
  passwordForm = { currentPassword: '', newPassword: '' };

  get initials(): string {
    const user = this.user();
    return user ? `${user.firstName[0]}${user.lastName[0]}`.toUpperCase() : '?';
  }

  openEdit(): void {
    const user = this.user();
    if (user) {
      this.form = { firstName: user.firstName, lastName: user.lastName, email: user.email };
    }
    this.errorMessage.set('');
    this.editOpen.set(true);
  }

  saveProfile(): void {
    this.saving.set(true);
    this.errorMessage.set('');

    this.auth.updateProfile(this.form).subscribe({
      next: () => {
        this.saving.set(false);
        this.editOpen.set(false);
        this.successMessage.set('Profil mis à jour avec succès.');
      },
      error: (error: Error) => {
        this.saving.set(false);
        this.errorMessage.set(error.message);
      },
    });
  }

  savePassword(): void {
    this.saving.set(true);
    this.errorMessage.set('');

    this.auth.changePassword(this.passwordForm).subscribe({
      next: () => {
        this.saving.set(false);
        this.passwordOpen.set(false);
        this.passwordForm = { currentPassword: '', newPassword: '' };
        this.successMessage.set('Mot de passe modifié avec succès.');
      },
      error: (error: Error) => {
        this.saving.set(false);
        this.errorMessage.set(error.message);
      },
    });
  }
}
