import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { API_BASE_URL } from '../../../core/config/api.config';
import { CourseDetailComponent } from './course-detail';

describe('CourseDetailComponent', () => {
  let controller: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CourseDetailComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: convertToParamMap({ id: '42' }) } } },
      ],
    }).compileComponents();
    controller = TestBed.inject(HttpTestingController);
  });

  afterEach(() => controller.verify());

  it('displays the course and lessons returned by the API', () => {
    const fixture = TestBed.createComponent(CourseDetailComponent);

    controller.expectOne(`${API_BASE_URL}/courses/42`).flush({
      id: 42,
      title: 'Angular avancé',
      description: 'Approfondir Angular',
      category: 'Développement',
      level: 'AVANCE',
      status: 'PUBLISHED',
      price: 49.9,
      teacherId: 3,
      createdAt: '2026-11-25T10:00:00',
      updatedAt: '2026-11-25T10:00:00',
      lessons: [],
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
    controller.expectOne(`${API_BASE_URL}/courses/42/lessons`).flush([
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
    ]);
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

    controller
      .expectOne(`${API_BASE_URL}/courses/42`)
      .flush('error', { status: 500, statusText: 'Server Error' });
    controller.expectOne(`${API_BASE_URL}/courses/42/lessons`).flush([]);
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Impossible de charger le cours.');
  });
});
