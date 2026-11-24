import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { CourseService } from '../../core/services/course.service';
import { UserService } from '../../core/services/user.service';
import { CourseSummary } from '../../core/models/course.models';
import { StatCard } from '../../shared/components/stat-card/stat-card';
import { StatusBadge } from '../../shared/components/status-badge/status-badge';

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink, StatCard, StatusBadge],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard {
  private readonly auth = inject(AuthService);
  private readonly courseService = inject(CourseService);
  private readonly userService = inject(UserService);

  readonly displayName = computed(() => this.auth.user()?.firstName ?? 'Utilisateur');
  readonly courses = signal<CourseSummary[]>([]);
  readonly userCount = signal(0);
  readonly lessonCount = signal(0);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  readonly publishedCount = computed(
    () => this.courses().filter((course) => course.status === 'PUBLISHED').length,
  );
  readonly publicationRate = computed(() => {
    const total = this.courses().length;
    return total === 0 ? 0 : Math.round((this.publishedCount() / total) * 100);
  });
  readonly popularCourses = computed(() => [...this.courses()].reverse().slice(0, 4));
  readonly latestCourses = computed(() => [...this.courses()].slice(0, 5));
  readonly activities = computed(() =>
    this.courses().slice(0, 4).map((course, index) => ({
      id: course.id,
      icon: index % 2 === 0 ? 'bi-journal-plus' : 'bi-check2-circle',
      title: index % 2 === 0 ? 'Cours ajouté' : 'Cours enregistré',
      detail: `${course.title} · ${course.category}`,
      time: course.createdAt,
      tone: (['blue', 'green', 'violet', 'amber'] as const)[index % 4],
    })),
  );

  readonly learningTools = [
    { icon: 'bi bi-play-circle', title: 'Vidéos pédagogiques', text: 'Des leçons courtes et claires, à revoir à tout moment.' },
    { icon: 'bi bi-patch-question', title: 'Quiz interactifs', text: 'Vérifiez vos acquis à la fin de chaque module.' },
    { icon: 'bi bi-award', title: 'Certifications', text: 'Valorisez vos compétences à la fin de votre parcours.' },
  ];

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);

    this.courseService.list({ page: 0, size: 100 }).subscribe({
      next: (response) => {
        this.courses.set(response.content);
        this.loading.set(false);
        this.loadLessonCount(response.content);
      },
      error: () => {
        this.error.set('Impossible de charger les données du tableau de bord.');
        this.loading.set(false);
      },
    });

    if (this.auth.hasRole('ADMIN')) {
      this.userService.findAll().subscribe({
        next: (users) => this.userCount.set(users.length),
        error: () => this.userCount.set(0),
      });
    } else {
      this.userCount.set(1);
    }
  }

  coverFor(id: number): number {
    return (id % 6) + 1;
  }

  private loadLessonCount(courses: CourseSummary[]): void {
    if (courses.length === 0) {
      this.lessonCount.set(0);
      return;
    }

    forkJoin(courses.map((course) => this.courseService.getById(course.id))).subscribe({
      next: (details) =>
        this.lessonCount.set(details.reduce((total, course) => total + course.lessons.length, 0)),
      error: () => this.lessonCount.set(0),
    });
  }
}
