import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ResourceType } from '../../core/models/course.models';
import { CourseService } from '../../core/services/course.service';

@Component({
  selector: 'app-resources',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './resources.html',
  styleUrl: './resources.scss',
})
export class Resources {
  private readonly courseService = inject(CourseService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly types: ResourceType[] = ['PDF', 'VIDEO', 'DOCUMENT', 'LINK'];
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  private readonly targetId: number | null;
  private readonly courseId: number | null;
  readonly lessonTarget: boolean;

  readonly form = new FormGroup({
    name: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(200)],
    }),
    type: new FormControl<ResourceType>('PDF', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    url: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(500)],
    }),
    size: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(50)] }),
  });

  constructor() {
    const snapshot = this.route.snapshot;
    this.lessonTarget = snapshot.data['target'] === 'lesson';
    this.targetId = this.parseId(snapshot.paramMap.get('id'));
    this.courseId = this.parseId(
      this.lessonTarget ? snapshot.queryParamMap.get('courseId') : snapshot.paramMap.get('id'),
    );

    if (!this.targetId || !this.courseId) {
      this.error.set('Identifiants de cours ou de leçon invalides.');
    }
  }

  backLink(): (string | number)[] {
    return this.courseId === null ? ['/courses'] : ['/courses', this.courseId];
  }

  submit(): void {
    if (this.form.invalid || !this.targetId || !this.courseId) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.error.set(null);
    const fields = this.form.getRawValue();
    const request = this.lessonTarget
      ? { lessonId: this.targetId, ...fields }
      : { courseId: this.targetId, ...fields };
    const action = this.lessonTarget
      ? this.courseService.addResourceToLesson(this.targetId, request)
      : this.courseService.addResourceToCourse(this.targetId, request);

    action.subscribe({
      next: () => void this.router.navigate(this.backLink()),
      error: () => {
        this.error.set('Impossible d’enregistrer la ressource.');
        this.loading.set(false);
      },
    });
  }

  private parseId(rawId: string | null): number | null {
    const id = Number(rawId);
    return rawId && Number.isInteger(id) && id > 0 ? id : null;
  }
}
