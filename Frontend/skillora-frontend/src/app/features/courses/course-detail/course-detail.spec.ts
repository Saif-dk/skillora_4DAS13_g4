import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { API_BASE_URL } from '../../../core/config/api.config';
import { CourseDetailComponent } from './course-detail';

function tokenFor(role: string): string {
  const payload = btoa(JSON.stringify({ role }))
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=+$/, '');
  return `header.${payload}.signature`;
}

describe('CourseDetailComponent', () => {
  let controller: HttpTestingController;

  const course = {
    id: 42,
    title: 'Angular avancé',
    description: 'Approfondir Angular',
    category: 'Développement',
    level: 'AVANCE',
    status: 'DRAFT',
    price: 49.9,
    teacherId: 3,
    createdAt: '2026-11-25T10:00:00',
    updatedAt: '2026-11-25T10:00:00',
    lessons: [],
    resources: [],
  };

  const lessons = [
    {
      id: 5,
      courseId: 42,
      title: 'Introduction',
      content: 'Première leçon',
      position: 1,
      createdAt: '2026-11-25T10:00:00',
      updatedAt: '2026-11-25T10:00:00',
      resources: [],
    },
  ];

  beforeEach(async () => {
    localStorage.clear();
    localStorage.setItem('skillora_token', tokenFor('PROFESSOR'));
    await TestBed.configureTestingModule({
      imports: [CourseDetailComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap({ id: '42' }) } },
        },
      ],
    }).compileComponents();
    controller = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    controller.verify();
    localStorage.clear();
  });

  function flushInitialLoad(lessonList: unknown[] = []): void {
    controller.expectOne(`${API_BASE_URL}/courses/42`).flush(course);
    controller.expectOne(`${API_BASE_URL}/courses/42/lessons`).flush(lessonList);
  }

  it('displays the course and lessons returned by the API', () => {
    const fixture = TestBed.createComponent(CourseDetailComponent);

    controller.expectOne(`${API_BASE_URL}/courses/42`).flush({
      ...course,
      status: 'PUBLISHED',
      resources: [
        {
          id: 9,
          courseId: 42,
          lessonId: null,
          name: 'Support PDF',
          type: 'PDF',
          url: 'https://example.com/support.pdf',
          size: '1MB',
          createdAt: '2026-11-25T10:00:00',
        },
      ],
    });
    controller.expectOne(`${API_BASE_URL}/courses/42/lessons`).flush(lessons);
    fixture.detectChanges();

    const element = fixture.nativeElement as HTMLElement;
    expect(fixture.componentInstance.course()?.title).toBe('Angular avancé');
    expect(fixture.componentInstance.lessons().length).toBe(1);
    expect(element.textContent).toContain('Angular avancé');
    expect(element.textContent).toContain('Introduction');
    expect(element.textContent).toContain('Support PDF');
  });

  it('shows an error state when the API fails', () => {
    const fixture = TestBed.createComponent(CourseDetailComponent);

    controller.expectOne(`${API_BASE_URL}/courses/42/lessons`).flush([]);
    controller
      .expectOne(`${API_BASE_URL}/courses/42`)
      .flush('error', { status: 500, statusText: 'Server Error' });
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain(
      'Impossible de charger le cours.',
    );
  });

  it('hides management actions for students', () => {
    localStorage.setItem('skillora_token', tokenFor('STUDENT'));
    const fixture = TestBed.createComponent(CourseDetailComponent);
    flushInitialLoad(lessons);
    fixture.detectChanges();

    expect(
      (fixture.nativeElement as HTMLElement).querySelector('[data-testid="course-actions"]'),
    ).toBeNull();
    expect(
      (fixture.nativeElement as HTMLElement).querySelector('[data-testid="delete-lesson"]'),
    ).toBeNull();
  });

  it('publishes the course through the API and reloads the page', () => {
    const fixture = TestBed.createComponent(CourseDetailComponent);
    flushInitialLoad();
    fixture.detectChanges();

    (
      (fixture.nativeElement as HTMLElement).querySelector(
        '[data-testid="publish-course"]',
      ) as HTMLButtonElement
    ).click();
    const request = controller.expectOne(`${API_BASE_URL}/courses/42/publish`);
    expect(request.request.method).toBe('PATCH');
    request.flush({ ...course, status: 'PUBLISHED' });
    flushInitialLoad();
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Cours publié.');
    expect(fixture.componentInstance.course()?.status).toBe('DRAFT');
  });

  it('deletes a lesson through the API and reloads the page', () => {
    const fixture = TestBed.createComponent(CourseDetailComponent);
    flushInitialLoad(lessons);
    fixture.detectChanges();

    (
      (fixture.nativeElement as HTMLElement).querySelector(
        '[data-testid="delete-lesson"]',
      ) as HTMLButtonElement
    ).click();
    const request = controller.expectOne(`${API_BASE_URL}/lessons/5`);
    expect(request.request.method).toBe('DELETE');
    request.flush(null);
    flushInitialLoad();
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Leçon supprimée.');
    expect(fixture.componentInstance.lessons().length).toBe(0);
  });
});
