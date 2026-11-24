import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../services/auth.service';

interface NavItem {
  label: string;
  icon: string;
  route?: string;
  action?: 'logout';
  exact?: boolean;
}

@Component({
  selector: 'app-sidebar',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss',
})
export class Sidebar {
  @Input() collapsed = false;
  @Input() mobileOpen = false;
  @Output() toggleCollapse = new EventEmitter<void>();
  @Output() closeMobile = new EventEmitter<void>();

  private readonly auth = inject(AuthService);

  get mainItems(): NavItem[] {
    const items: NavItem[] = [
      { label: 'Dashboard', icon: 'bi bi-grid-1x2', route: '/dashboard', exact: true },
      { label: 'Gestion des cours', icon: 'bi bi-journal-bookmark', route: '/courses' },
      { label: 'Catalogue', icon: 'bi bi-collection-play', route: '/catalog' },
    ];

    if (this.auth.hasRole('ADMIN')) {
      items.push({ label: 'Utilisateurs', icon: 'bi bi-people', route: '/users' });
    }

    return items;
  }

  get accountItems(): NavItem[] {
    const items: NavItem[] = [{ label: 'Mon profil', icon: 'bi bi-person-circle', route: '/profile' }];

    if (this.auth.isAuthenticated()) {
      items.push({ label: 'Déconnexion', icon: 'bi bi-box-arrow-right', action: 'logout' });
    } else {
      items.push({ label: 'Connexion', icon: 'bi bi-box-arrow-in-right', route: '/login' });
    }

    return items;
  }

  logout(): void {
    this.closeMobile.emit();
    this.auth.logout();
  }
}
