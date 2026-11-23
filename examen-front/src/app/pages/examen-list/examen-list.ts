import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Examen } from '../../models/examen';
import { ExamenService } from '../../services/examen';

@Component({
  selector: 'app-examen-list',
  imports: [RouterLink, DatePipe],
  template: `
    <h2>Examens</h2>
    @if (error()) { <p class="error">{{ error() }}</p> }

    @for (e of examens(); track e.id) {
      <div class="card">
        <div style="display:flex; justify-content:space-between; align-items:center">
          <h3 style="margin:0">{{ e.titre }}</h3>
          <span class="badge" [class]="e.statut">{{ e.statut }}</span>
        </div>
        <p>{{ e.description }}</p>
        <small>
          📅 {{ e.dateExamen | date: 'dd/MM/yyyy HH:mm' }} · ⏱ {{ e.duree }} min ·
          🎯 {{ e.noteTotale }} pts · Cours : {{ e.courseId }} · Enseignant : {{ e.teacherId }}
        </small>
        <div class="actions">
          <a class="btn" [routerLink]="['/examens', e.id]">Questions</a>
          @if (e.statut === 'BROUILLON') {
            <a class="btn secondary" [routerLink]="['/examens', e.id, 'modifier']">Modifier</a>
            <button class="btn success" (click)="publier(e)">Publier</button>
          }
          <button class="btn danger" (click)="supprimer(e)">Supprimer</button>
        </div>
      </div>
    } @empty {
      <p>Aucun examen pour le moment.</p>
    }
  `,
})
export class ExamenList implements OnInit {
  private svc = inject(ExamenService);
  examens = signal<Examen[]>([]);
  error = signal('');

  ngOnInit() { this.load(); }

  load() {
    this.svc.getAll().subscribe({
      next: (d) => this.examens.set(d),
      error: () => this.error.set('Impossible de joindre le backend (port 8082).'),
    });
  }

  publier(e: Examen) {
    this.svc.publier(e.id!).subscribe({
      next: () => this.load(),
      error: (err) => alert(err.error?.message ?? 'Erreur lors de la publication'),
    });
  }

  supprimer(e: Examen) {
    if (confirm(`Supprimer l'examen "${e.titre}" et ses questions ?`)) {
      this.svc.delete(e.id!).subscribe({ next: () => this.load() });
    }
  }
}