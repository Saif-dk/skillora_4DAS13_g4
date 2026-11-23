import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ExamenService } from '../../services/examen';

@Component({
  selector: 'app-examen-form',
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <div class="card">
      <h2>{{ id ? "Modifier l'examen" : 'Nouvel examen' }}</h2>
      <form [formGroup]="form" (ngSubmit)="save()">
        <label>Titre</label>
        <input formControlName="titre" />
        @if (form.controls.titre.touched && form.controls.titre.invalid) {
          <div class="error">Le titre est obligatoire</div>
        }

        <label>Description</label>
        <textarea formControlName="description" rows="3"></textarea>

        <div class="row">
          <div>
            <label>Date de l'examen</label>
            <input type="datetime-local" formControlName="dateExamen" />
          </div>
          <div>
            <label>Durée (minutes)</label>
            <input type="number" formControlName="duree" />
          </div>
        </div>

        <div class="row">
          <div>
            <label>ID du cours</label>
            <input formControlName="courseId" />
          </div>
          <div>
            <label>ID de l'enseignant</label>
            <input formControlName="teacherId" />
          </div>
        </div>

        @if (error()) { <p class="error">{{ error() }}</p> }

        <div class="actions">
          <button class="btn" type="submit" [disabled]="form.invalid">Enregistrer</button>
          <a class="btn secondary" routerLink="/examens">Annuler</a>
        </div>
      </form>
    </div>
  `,
})
export class ExamenForm implements OnInit {
  private fb = inject(FormBuilder);
  private svc = inject(ExamenService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  id = this.route.snapshot.paramMap.get('id');
  error = signal('');

  form = this.fb.nonNullable.group({
    titre: ['', Validators.required],
    description: [''],
    dateExamen: ['', Validators.required],
    duree: [60, [Validators.required, Validators.min(1)]],
    courseId: ['', Validators.required],
    teacherId: ['', Validators.required],
  });

  ngOnInit() {
    if (this.id) {
      this.svc.get(this.id).subscribe((e) =>
        this.form.patchValue({
          titre: e.titre,
          description: e.description ?? '',
          dateExamen: e.dateExamen.slice(0, 16),
          duree: e.duree,
          courseId: e.courseId,
          teacherId: e.teacherId,
        })
      );
    }
  }

  save() {
    const payload = this.form.getRawValue();
    const req = this.id ? this.svc.update(this.id, payload) : this.svc.create(payload);
    req.subscribe({
      next: () => this.router.navigate(['/examens']),
      error: (err) => this.error.set(err.error?.message ?? 'Erreur lors de l’enregistrement'),
    });
  }
}