import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Course, Lesson } from '../../../core/models/course.models';
import { CourseService } from '../../../core/services/course.service';

@Component({
  selector: 'app-course-detail',
  imports: [RouterLink],
  templateUrl: './course-detail.html',
  styleUrl: './course-detail.scss',
})
export class CourseDetailComponent {
  private readonly courseService = inject(CourseService);
  private readonly route = inject(ActivatedRoute);

  readonly course = signal<Course | null>(null);
  readonly lessons = signal<Lesson[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  constructor() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!Number.isInteger(id) || id <= 0) {
      this.error.set('Identifiant de cours invalide.');
      return;
    }

    this.loading.set(true);
    this.courseService.getById(id).subscribe({
      next: (course) => {
        this.course.set(course);
        this.loading.set(false);
      },
      error: () => this.setError('Impossible de charger le cours.'),
    });

    this.courseService.listLessons(id).subscribe({
      next: (lessons) => this.lessons.set(lessons),
      error: () => this.setError('Impossible de charger les leçons.'),
    });
  }

  private setError(message: string): void {
    this.loading.set(false);
    this.error.set(message);
  }
}
