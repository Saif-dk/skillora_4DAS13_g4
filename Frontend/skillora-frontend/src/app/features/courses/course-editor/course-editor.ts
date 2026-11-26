import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CourseStatus } from '../../../core/models/course.models';
import { CourseService } from '../../../core/services/course.service';

@Component({
  selector: 'app-course-editor',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './course-editor.html',
  styleUrl: './course-editor.scss',
})
export class CourseEditorComponent {
  private readonly courseService = inject(CourseService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly statuses: CourseStatus[] = ['DRAFT', 'PUBLISHED', 'ARCHIVED'];
  readonly levels = ['DEBUTANT', 'INTERMEDIAIRE', 'AVANCE'];
  readonly editing = signal(false);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  private readonly courseId: number | null;

  readonly form = new FormGroup({
    title: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(150)],
    }),
    description: new FormControl('', { nonNullable: true }),
    category: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(100)] }),
    level: new FormControl('DEBUTANT', {
      nonNullable: true,
      validators: [Validators.maxLength(30)],
    }),
    status: new FormControl<CourseStatus>('DRAFT', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    price: new FormControl(0, {
      nonNullable: true,
      validators: [Validators.required, Validators.min(0)],
    }),
  });

  constructor() {
    const rawId = this.route.snapshot.paramMap.get('id');
    const id = Number(rawId);
    if (rawId && (!Number.isInteger(id) || id <= 0)) {
      this.error.set('Identifiant de cours invalide.');
      this.courseId = null;
      return;
    }

    this.courseId = rawId ? id : null;
    this.editing.set(this.courseId !== null);

    if (this.courseId !== null) {
      this.loading.set(true);
      this.courseService.getById(this.courseId).subscribe({
        next: (course) => {
          this.form.setValue({
            title: course.title,
            description: course.description ?? '',
            category: course.category ?? '',
            level: course.level ?? 'DEBUTANT',
            status: course.status,
            price: course.price,
          });
          this.loading.set(false);
        },
        error: () => {
          this.error.set('Impossible de charger le cours.');
          this.loading.set(false);
        },
      });
    }
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.error.set(null);
    const request = this.form.getRawValue();
    const action =
      this.courseId === null
        ? this.courseService.createCourse(request)
        : this.courseService.updateCourse(this.courseId, request);

    action.subscribe({
      next: (course) => void this.router.navigate(['/courses', course.id]),
      error: () => {
        this.error.set('Impossible d’enregistrer le cours.');
        this.loading.set(false);
      },
    });
  }
}
