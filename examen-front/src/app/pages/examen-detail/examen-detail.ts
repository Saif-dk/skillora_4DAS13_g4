import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Examen, Question } from '../../models/examen';
import { ExamenService } from '../../services/examen';

@Component({
  selector: 'app-examen-detail',
  imports: [ReactiveFormsModule, RouterLink, DatePipe],
  template: `
    <a routerLink="/examens">← Retour</a>

    @if (examen(); as e) {
      <div class="card">
        <div style="display:flex; justify-content:space-between; align-items:center">
          <h2 style="margin:0">{{ e.titre }}</h2>
          <span class="badge" [class]="e.statut">{{ e.statut }}</span>
        </div>
        <p>{{ e.description }}</p>
        <small>
          📅 {{ e.dateExamen | date: 'dd/MM/yyyy HH:mm' }} · ⏱ {{ e.duree }} min ·
          🎯 Note totale : {{ e.noteTotale }} pts
        </small>
      </div>

      <h3>Questions ({{ questions().length }})</h3>
      @for (q of questions(); track q.id; let i = $index) {
        <div class="card">
          <strong>{{ i + 1 }}. {{ q.enonce }}</strong> <small>({{ q.points }} pt)</small>
          <ul>
            <li>A. {{ q.optionA }}</li>
            <li>B. {{ q.optionB }}</li>
            @if (q.optionC) { <li>C. {{ q.optionC }}</li> }
            @if (q.optionD) { <li>D. {{ q.optionD }}</li> }
          </ul>
          <small>✅ Bonne réponse : {{ q.bonneReponse }} · Type : {{ q.typeQuestion }}</small>
          @if (e.statut === 'BROUILLON') {
            <div class="actions">
              <button class="btn secondary" (click)="edit(q)">Modifier</button>
              <button class="btn danger" (click)="remove(q)">Supprimer</button>
            </div>
          }
        </div>
      } @empty {
        <p>Aucune question. Ajoute-en au moins une avant de publier.</p>
      }

      @if (e.statut === 'BROUILLON') {
        <div class="card">
          <h3>{{ editingId() ? 'Modifier la question' : 'Ajouter une question' }}</h3>
          <form [formGroup]="form" (ngSubmit)="saveQuestion()">
            <label>Énoncé</label>
            <textarea formControlName="enonce" rows="2"></textarea>

            <div class="row">
              <div>
                <label>Type</label>
                <select formControlName="typeQuestion">
                  <option value="QCM">QCM</option>
                  <option value="VRAI_FAUX">Vrai / Faux</option>
                </select>
              </div>
              <div>
                <label>Points</label>
                <input type="number" formControlName="points" />
              </div>
              <div>
                <label>Bonne réponse</label>
                <select formControlName="bonneReponse">
                  <option>A</option><option>B</option><option>C</option><option>D</option>
                </select>
              </div>
            </div>

            <div class="row">
              <div><label>Option A</label><input formControlName="optionA" /></div>
              <div><label>Option B</label><input formControlName="optionB" /></div>
            </div>
            <div class="row">
              <div><label>Option C</label><input formControlName="optionC" /></div>
              <div><label>Option D</label><input formControlName="optionD" /></div>
            </div>

            @if (error()) { <p class="error">{{ error() }}</p> }

            <div class="actions">
              <button class="btn" type="submit" [disabled]="form.invalid">
                {{ editingId() ? 'Mettre à jour' : 'Ajouter' }}
              </button>
              @if (editingId()) {
                <button class="btn secondary" type="button" (click)="reset()">Annuler</button>
              }
            </div>
          </form>
        </div>
      } @else {
        <p><em>Examen publié : les questions ne sont plus modifiables.</em></p>
      }
    }
  `,
})
export class ExamenDetail implements OnInit {
  private fb = inject(FormBuilder);
  private svc = inject(ExamenService);
  private id = inject(ActivatedRoute).snapshot.paramMap.get('id')!;

  examen = signal<Examen | null>(null);
  questions = signal<Question[]>([]);
  editingId = signal<string | null>(null);
  error = signal('');

  form = this.fb.nonNullable.group({
    enonce: ['', Validators.required],
    typeQuestion: ['QCM', Validators.required],
    optionA: ['', Validators.required],
    optionB: ['', Validators.required],
    optionC: [''],
    optionD: [''],
    bonneReponse: ['A', Validators.required],
    points: [1, [Validators.required, Validators.min(0.5)]],
  });

  ngOnInit() { this.load(); }

  load() {
    this.svc.get(this.id).subscribe((e) => this.examen.set(e));
    this.svc.getQuestions(this.id).subscribe((q) => this.questions.set(q));
  }

  saveQuestion() {
    const payload = this.form.getRawValue();
    const editing = this.editingId();
    const req = editing
      ? this.svc.updateQuestion(editing, payload)
      : this.svc.addQuestion(this.id, payload);
    req.subscribe({
      next: () => { this.reset(); this.load(); },
      error: (err) => this.error.set(err.error?.message ?? 'Erreur'),
    });
  }

  edit(q: Question) {
    this.editingId.set(q.id!);
    this.form.patchValue({
      enonce: q.enonce, typeQuestion: q.typeQuestion,
      optionA: q.optionA, optionB: q.optionB,
      optionC: q.optionC ?? '', optionD: q.optionD ?? '',
      bonneReponse: q.bonneReponse, points: q.points,
    });
    window.scrollTo({ top: document.body.scrollHeight, behavior: 'smooth' });
  }

  remove(q: Question) {
    if (confirm('Supprimer cette question ?')) {
      this.svc.deleteQuestion(q.id!).subscribe({ next: () => this.load() });
    }
  }

  reset() {
    this.editingId.set(null);
    this.error.set('');
    this.form.reset({ typeQuestion: 'QCM', bonneReponse: 'A', points: 1 });
  }
}