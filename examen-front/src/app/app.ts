import { Component } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink],
  template: `
    <header class="nav">
      <a routerLink="/examens" class="brand">E-Learning · Gestion d'examen</a>
      <a routerLink="/examens/nouveau" class="btn">+ Nouvel examen</a>
    </header>
    <main class="container">
      <router-outlet />
    </main>
  `,
})
export class App {}