import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CourseService } from '../../core/services/course.service';

@Component({
  selector: 'app-lessons',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './lessons.html',
  styleUrl: './lessons.scss',
})
export class Lessons {
  private readonly courseService = inject(CourseService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly editing = signal(false);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  private readonly courseId: number | null;
  private readonly lessonId: number | null;

  readonly form = new FormGroup({
    title: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(150)],
    }),
    content: new FormControl('', { nonNullable: true }),
    position: new FormControl(1, {
      nonNullable: true,
      validators: [Validators.required, Validators.min(1)],
    }),
  });

  constructor() {
    const snapshot = this.route.snapshot;
    const isLessonTarget = snapshot.data['target'] === 'lesson';
    const courseId = this.parseId(
      isLessonTarget ? snapshot.queryParamMap.get('courseId') : snapshot.paramMap.get('id'),
    );
    const lessonId = isLessonTarget ? this.parseId(snapshot.paramMap.get('id')) : null;

    this.courseId = courseId;
    this.lessonId = lessonId;
    this.editing.set(lessonId !== null);

    if (!courseId || (this.editing() && !lessonId)) {
      this.error.set('Identifiants de cours ou de leçon invalides.');
      return;
    }

    if (lessonId !== null) {
      this.loading.set(true);
      this.courseService.listLessons(courseId).subscribe({
        next: (lessons) => {
          const lesson = lessons.find((candidate) => candidate.id === lessonId);
          if (!lesson) {
            this.error.set('Leçon introuvable.');
            this.loading.set(false);
            return;
          }
          this.form.setValue({
            title: lesson.title,
            content: lesson.content ?? '',
            position: lesson.position,
          });
          this.loading.set(false);
        },
        error: () => {
          this.error.set('Impossible de charger la leçon.');
          this.loading.set(false);
        },
      });
    }
  }

  courseLink(): (string | number)[] {
    return this.courseId === null ? ['/courses'] : ['/courses', this.courseId];
  }

  submit(): void {
    if (this.form.invalid || !this.courseId) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.error.set(null);
    const request = this.form.getRawValue();
    const action =
      this.lessonId === null
        ? this.courseService.addLesson(this.courseId, request)
        : this.courseService.updateLesson(this.lessonId, request);

    action.subscribe({
      next: () => void this.router.navigate(['/courses', this.courseId]),
      error: () => {
        this.error.set('Impossible d’enregistrer la leçon.');
        this.loading.set(false);
      },
    });
  }

  private parseId(rawId: string | null): number | null {
    const id = Number(rawId);
    return rawId && Number.isInteger(id) && id > 0 ? id : null;
  }
}
