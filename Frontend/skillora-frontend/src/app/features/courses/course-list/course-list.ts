import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CourseSummary } from '../../../core/models/course.models';
import { CourseService } from '../../../core/services/course.service';

@Component({
  selector: 'app-course-list',
  imports: [RouterLink],
  templateUrl: './course-list.html',
  styleUrl: './course-list.scss',
})
export class CourseListComponent {
  private readonly courseService = inject(CourseService);

  readonly rows = signal<CourseSummary[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly search = signal('');
  readonly status = signal('');
  readonly page = signal(0);
  readonly size = 10;
  readonly total = signal(0);

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);

    this.courseService
      .list({
        search: this.search() || undefined,
        status: this.status() || undefined,
        page: this.page(),
        size: this.size,
      })
      .subscribe({
        next: (response) => {
          this.rows.set(response.content);
          this.total.set(response.totalElements);
          this.loading.set(false);
        },
        error: () => {
          this.error.set('Impossible de charger les cours.');
          this.loading.set(false);
        },
      });
  }

  onSearch(value: string): void {
    this.search.set(value);
    this.page.set(0);
    this.load();
  }

  onStatus(value: string): void {
    this.status.set(value);
    this.page.set(0);
    this.load();
  }
}
