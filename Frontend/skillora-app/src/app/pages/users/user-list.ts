import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { UserService } from '../../core/services/user.service';
import { TableActionEvent, TableColumn, DataTable } from '../../shared/components/data-table/data-table';
import { DemoModal } from '../../shared/components/modal/modal';
import { UserDto, UserRole } from '../../core/models/user.models';

interface UserRow {
  id: number;
  title: string;
  email: string;
  role: UserRole;
  statusText: string;
  createdAt: string;
  raw: UserDto;
}

@Component({
  selector: 'app-user-list',
  imports: [FormsModule, DataTable, DemoModal],
  templateUrl: './user-list.html',
  styleUrl: './user-list.scss',
})
export class UserList implements OnInit {
  private readonly userService = inject(UserService);
  readonly auth = inject(AuthService);

  readonly roles: UserRole[] = ['ADMIN', 'PROFESSOR', 'STUDENT'];
  readonly columns: TableColumn[] = [
    { key: 'title', label: 'Utilisateur' },
    { key: 'email', label: 'E-mail' },
    { key: 'role', label: 'Rôle', type: 'level' },
    { key: 'statusText', label: 'Statut' },
    { key: 'createdAt', label: 'Créé le' },
    { key: 'actions', label: 'Actions', type: 'actions' },
  ];

  readonly users = signal<UserRow[]>([]);
  readonly search = signal('');
  readonly loading = signal(false);
  readonly errorMessage = signal('');
  readonly successMessage = signal('');
  readonly formOpen = signal(false);
  readonly detailsOpen = signal(false);
  readonly deleteOpen = signal(false);
  readonly isEdit = signal(false);
  selectedUser: UserDto | null = null;

  readonly filteredUsers = computed(() => {
    const term = this.search().trim().toLowerCase();
    if (!term) return this.users();
    return this.users().filter((row) => row.title.toLowerCase().includes(term) || row.email.toLowerCase().includes(term) || row.role.toLowerCase().includes(term));
  });

  form = { firstName: '', lastName: '', email: '', password: '', role: 'STUDENT' as UserRole, enabled: true };

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.userService.findAll().subscribe({
      next: (users) => {
        this.users.set(users.map((user) => this.toRow(user)));
        this.loading.set(false);
      },
      error: (error: Error) => {
        this.errorMessage.set(error.message);
        this.loading.set(false);
      },
    });
  }

  openCreate(): void {
    this.isEdit.set(false);
    this.form = { firstName: '', lastName: '', email: '', password: '', role: 'STUDENT', enabled: true };
    this.errorMessage.set('');
    this.formOpen.set(true);
  }

  openEdit(user: UserDto): void {
    this.isEdit.set(true);
    this.form = {
      firstName: user.firstName,
      lastName: user.lastName,
      email: user.email,
      password: '',
      role: user.role,
      enabled: user.enabled,
    };
    this.errorMessage.set('');
    this.formOpen.set(true);
  }

  handleAction(event: TableActionEvent): void {
    const user = event.row.raw as UserDto;
    this.selectedUser = user;

    if (event.action === 'edit') {
      this.openEdit(user);
    } else if (event.action === 'delete') {
      this.deleteOpen.set(true);
    } else {
      this.detailsOpen.set(true);
    }
  }

  save(): void {
    this.errorMessage.set('');

    const request = {
      firstName: this.form.firstName,
      lastName: this.form.lastName,
      email: this.form.email,
      role: this.form.role,
      enabled: this.form.enabled,
    };

    const action = this.isEdit() && this.selectedUser
      ? this.userService.update(this.selectedUser.id, request)
      : this.userService.create({ ...request, password: this.form.password });

    action.subscribe({
      next: () => {
        this.formOpen.set(false);
        this.successMessage.set(this.isEdit() ? 'Utilisateur mis à jour.' : 'Utilisateur créé.');
        this.load();
      },
      error: (error: Error) => this.errorMessage.set(error.message),
    });
  }

  toggleStatus(user: UserDto): void {
    this.userService.setEnabled(user.id, !user.enabled).subscribe({
      next: () => {
        this.successMessage.set('Statut du compte mis à jour.');
        this.detailsOpen.set(false);
        this.load();
      },
      error: (error: Error) => this.errorMessage.set(error.message),
    });
  }

  deleteUser(): void {
    if (!this.selectedUser) return;

    this.userService.delete(this.selectedUser.id).subscribe({
      next: () => {
        this.deleteOpen.set(false);
        this.successMessage.set('Utilisateur supprimé.');
        this.load();
      },
      error: (error: Error) => this.errorMessage.set(error.message),
    });
  }

  private toRow(user: UserDto): UserRow {
    return {
      id: user.id,
      title: `${user.firstName} ${user.lastName}`,
      email: user.email,
      role: user.role,
      statusText: user.enabled ? 'Actif' : 'Inactif',
      createdAt: new Date(user.createdAt).toLocaleDateString('fr-FR'),
      raw: user,
    };
  }
}
