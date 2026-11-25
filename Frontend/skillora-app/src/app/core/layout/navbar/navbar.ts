import { Component, DestroyRef, EventEmitter, OnInit, Output, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { NavigationEnd, Router, RouterLink } from '@angular/router';
import { filter } from 'rxjs';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-navbar',
  imports: [RouterLink, FormsModule],
  templateUrl: './navbar.html',
  styleUrl: './navbar.scss',
})
export class Navbar implements OnInit {
  @Output() toggleMobile = new EventEmitter<void>();

  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  readonly auth = inject(AuthService);

  pageTitle = 'Dashboard';
  searchTerm = '';

  private readonly titles: Record<string, string> = {
    '/dashboard': 'Dashboard',
    '/courses': 'Gestion des cours',
    '/courses/new': 'Nouveau cours',
    '/catalog': 'Catalogue des cours',
    '/profile': 'Mon profil',
    '/users': 'Gestion des utilisateurs',
  };

  get initials(): string {
    const user = this.auth.user();
    return user ? `${user.firstName[0]}${user.lastName[0]}`.toUpperCase() : '??';
  }

  ngOnInit(): void {
    this.updateTitle();
    this.router.events
      .pipe(
        filter((event): event is NavigationEnd => event instanceof NavigationEnd),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(() => this.updateTitle());
  }

  search(): void {
    const term = this.searchTerm.trim();
    const role = this.auth.user()?.role;
    const target = role === 'ADMIN' || role === 'PROFESSOR' ? '/courses' : '/catalog';

    void this.router.navigate([target], {
      queryParams: term ? { search: term } : {},
    });
  }

  private updateTitle(): void {
    const url = this.router.url.split('?')[0];

    if (/^\/courses\/\d+\/edit$/.test(url)) {
      this.pageTitle = 'Modifier le cours';
    } else if (/^\/courses\/\d+\/lessons\/new$/.test(url)) {
      this.pageTitle = 'Nouvelle leçon';
    } else if (/^\/courses\/\d+\/resources\/new$/.test(url)) {
      this.pageTitle = 'Nouvelle ressource';
    } else if (/^\/lessons\/\d+\/edit$/.test(url)) {
      this.pageTitle = 'Modifier la leçon';
    } else if (/^\/lessons\/\d+\/resources\/new$/.test(url)) {
      this.pageTitle = 'Nouvelle ressource de leçon';
    } else if (/^\/courses\/\d+$/.test(url)) {
      this.pageTitle = 'Détails du cours';
    } else {
      this.pageTitle = this.titles[url] ?? 'Skillora';
    }
  }
}
