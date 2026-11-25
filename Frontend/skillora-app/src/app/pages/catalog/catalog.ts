import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { CourseSummary } from '../../core/models/course.models';
import { CourseService } from '../../core/services/course.service';
import { CourseCard } from '../../shared/components/course-card/course-card';

@Component({
  selector: 'app-catalog',
  imports: [CourseCard],
  templateUrl: './catalog.html',
  styleUrl: './catalog.scss',
})
export class Catalog {
  private readonly courseService = inject(CourseService);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);

  readonly search = signal('');
  readonly level = signal('');
  readonly category = signal('');
  readonly courses = signal<CourseSummary[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  readonly categories = computed(() => [
    'Toutes les catégories',
    ...Array.from(new Set(this.courses().map((course) => course.category))).sort(),
  ]);

  readonly filteredCourses = computed(() => {
    const term = this.search().trim().toLowerCase();
    const selectedLevel = this.level();
    const selectedCategory = this.category();

    return this.courses().filter((course) => {
      const matchesTerm = !term || course.title.toLowerCase().includes(term);
      const matchesLevel = !selectedLevel || course.level === selectedLevel;
      const matchesCategory = !selectedCategory || course.category === selectedCategory;
      return matchesTerm && matchesLevel && matchesCategory;
    });
  });

  constructor() {
    this.route.queryParamMap
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((params) => this.search.set(params.get('search') ?? ''));
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.courseService.list({ status: 'PUBLISHED', page: 0, size: 100 }).subscribe({
      next: (response) => {
        this.courses.set(response.content);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Impossible de charger le catalogue.');
        this.loading.set(false);
      },
    });
  }

  setSearch(value: string): void {
    this.search.set(value);
  }

  setLevel(value: string): void {
    this.level.set(value);
  }

  setCategory(value: string): void {
    this.category.set(value === 'Toutes les catégories' ? '' : value);
  }

  reset(): void {
    this.search.set('');
    this.level.set('');
    this.category.set('');
  }
}
