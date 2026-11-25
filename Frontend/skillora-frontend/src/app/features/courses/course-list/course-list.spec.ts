import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { API_BASE_URL } from '../../../core/config/api.config';
import { CourseListComponent } from './course-list';

describe('CourseListComponent', () => {
  let controller: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CourseListComponent],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
    controller = TestBed.inject(HttpTestingController);
  });

  afterEach(() => controller.verify());

  it('displays the courses returned by the API with links to details', () => {
    const fixture = TestBed.createComponent(CourseListComponent);

    controller.expectOne((request) => request.url === `${API_BASE_URL}/courses`).flush({
      content: [
        {
          id: 7,
          title: 'Angular avancé',
          category: 'Développement',
          level: 'AVANCE',
          status: 'PUBLISHED',
          price: 49.9,
          teacherId: 3,
          createdAt: '2026-11-25T10:00:00',
        },
      ],
      page: 0,
      size: 10,
      totalElements: 1,
      totalPages: 1,
    });
    fixture.detectChanges();

    const element = fixture.nativeElement as HTMLElement;
    expect(fixture.componentInstance.rows().length).toBe(1);
    expect(element.textContent).toContain('Angular avancé');
    expect(element.querySelector('a[href="/courses/7"]')).not.toBeNull();
  });

  it('shows an error state when the API fails', () => {
    const fixture = TestBed.createComponent(CourseListComponent);

    controller
      .expectOne((request) => request.url === `${API_BASE_URL}/courses`)
      .flush('error', { status: 500, statusText: 'Server Error' });
    fixture.detectChanges();

    const element = fixture.nativeElement as HTMLElement;
    expect(fixture.componentInstance.error()).toBe('Impossible de charger les cours.');
    expect(element.textContent).toContain('Impossible de charger les cours.');
  });

  it('reloads courses when the search input changes', () => {
    const fixture = TestBed.createComponent(CourseListComponent);
    controller.expectOne((candidate) => candidate.url === `${API_BASE_URL}/courses`).flush({
      content: [],
      page: 0,
      size: 10,
      totalElements: 0,
      totalPages: 0,
    });
    fixture.detectChanges();

    fixture.componentInstance.onSearch('angular');

    const request = controller.expectOne((candidate) => candidate.url === `${API_BASE_URL}/courses`);
    expect(request.request.params.get('search')).toBe('angular');
    request.flush({ content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 });
  });
});
