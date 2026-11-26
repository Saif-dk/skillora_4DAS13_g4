import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { forkJoin, Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';
import { Course, Lesson } from '../../../core/models/course.models';
import { CourseService } from '../../../core/services/course.service';
import { TokenService } from '../../../core/services/token.service';

@Component({
  selector: 'app-course-detail',
  imports: [RouterLink],
  templateUrl: './course-detail.html',
  styleUrl: './course-detail.scss',
})
export class CourseDetailComponent {
  private readonly courseService = inject(CourseService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly tokenService = inject(TokenService);

  readonly course = signal<Course | null>(null);
  readonly lessons = signal<Lesson[]>([]);
  readonly loading = signal(false);
  readonly busy = signal(false);
  readonly error = signal<string | null>(null);
  readonly notice = signal<string | null>(null);
  readonly canManage = ['ADMIN', 'PROFESSOR'].includes(this.tokenService.getRole() ?? '');

  constructor() {
    this.load();
  }

  load(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!Number.isInteger(id) || id <= 0) {
      this.error.set('Identifiant de cours invalide.');
      return;
    }

    this.loading.set(true);
    this.error.set(null);
    forkJoin({
      course: this.courseService.getById(id),
      lessons: this.courseService.listLessons(id),
    }).subscribe({
      next: ({ course, lessons }) => {
        this.course.set(course);
        this.lessons.set(lessons);
        this.loading.set(false);
      },
      error: () => this.setError('Impossible de charger le cours.'),
    });
  }

  publish(): void {
    const id = this.course()?.id;
    if (id !== undefined) {
      this.run(this.courseService.publishCourse(id), 'Cours publié.');
    }
  }

  unpublish(): void {
    const id = this.course()?.id;
    if (id !== undefined) {
      this.run(this.courseService.unpublishCourse(id), 'Cours dépublié.');
    }
  }

  removeCourse(): void {
    const id = this.course()?.id;
    if (id !== undefined) {
      this.run(this.courseService.deleteCourse(id), 'Cours supprimé.', true);
    }
  }

  removeLesson(lessonId: number): void {
    this.run(this.courseService.deleteLesson(lessonId), 'Leçon supprimée.');
  }

  removeResource(resourceId: number): void {
    this.run(this.courseService.deleteResource(resourceId), 'Ressource supprimée.');
  }

  private run(action: Observable<unknown>, message: string, redirect = false): void {
    this.busy.set(true);
    this.error.set(null);
    this.notice.set(null);
    action.pipe(finalize(() => this.busy.set(false))).subscribe({
      next: () => {
        this.notice.set(message);
        if (redirect) {
          void this.router.navigate(['/courses']);
        } else {
          this.load();
        }
      },
      error: () => this.error.set('Impossible d’effectuer l’action.'),
    });
  }

  private setError(message: string): void {
    this.loading.set(false);
    this.error.set(message);
  }
}
