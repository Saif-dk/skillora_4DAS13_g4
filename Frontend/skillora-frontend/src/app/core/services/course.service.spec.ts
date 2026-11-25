import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { API_BASE_URL } from '../config/api.config';
import { CourseService } from './course.service';

describe('CourseService', () => {
  let service: CourseService;
  let controller: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(CourseService);
    controller = TestBed.inject(HttpTestingController);
  });

  afterEach(() => controller.verify());

  it('lists courses with query parameters', () => {
    service.list({ search: 'angular', status: 'PUBLISHED', page: 2, size: 5 }).subscribe((response) => {
      expect(response.totalElements).toBe(1);
    });

    const request = controller.expectOne((candidate) => candidate.url === `${API_BASE_URL}/courses`);
    expect(request.request.method).toBe('GET');
    expect(request.request.params.get('search')).toBe('angular');
    expect(request.request.params.get('status')).toBe('PUBLISHED');
    expect(request.request.params.get('page')).toBe('2');
    expect(request.request.params.get('size')).toBe('5');
    request.flush({ content: [], page: 2, size: 5, totalElements: 1, totalPages: 1 });
  });

  it('gets a course by id', () => {
    service.getById(7).subscribe((course) => expect(course.id).toBe(7));

    const request = controller.expectOne(`${API_BASE_URL}/courses/7`);
    expect(request.request.method).toBe('GET');
    request.flush({ id: 7, title: 'Angular' });
  });

  it('lists the lessons of a course', () => {
    service.listLessons(7).subscribe((lessons) => expect(lessons.length).toBe(1));

    const request = controller.expectOne(`${API_BASE_URL}/courses/7/lessons`);
    expect(request.request.method).toBe('GET');
    request.flush([{ id: 1, courseId: 7, title: 'Introduction' }]);
  });

  it('gets a resource by id', () => {
    service.getResource(3).subscribe((resource) => expect(resource.name).toBe('Support PDF'));

    const request = controller.expectOne(`${API_BASE_URL}/resources/3`);
    expect(request.request.method).toBe('GET');
    request.flush({ id: 3, name: 'Support PDF' });
  });
});
