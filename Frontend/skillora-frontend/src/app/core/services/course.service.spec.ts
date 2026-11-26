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
    service
      .list({ search: 'angular', status: 'PUBLISHED', page: 2, size: 5 })
      .subscribe((response) => {
        expect(response.totalElements).toBe(1);
      });

    const request = controller.expectOne(
      (candidate) => candidate.url === `${API_BASE_URL}/courses`,
    );
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

  it('creates and updates a course', () => {
    const payload = {
      title: 'Angular',
      description: 'Cours complet',
      category: 'Développement',
      level: 'DEBUTANT',
      status: 'DRAFT' as const,
      price: 49.9,
    };

    service.createCourse(payload).subscribe((course) => expect(course.id).toBe(8));
    const create = controller.expectOne(`${API_BASE_URL}/courses`);
    expect(create.request.method).toBe('POST');
    expect(create.request.body).toEqual(payload);
    create.flush({ id: 8 });

    service.updateCourse(8, payload).subscribe((course) => expect(course.title).toBe('Angular'));
    const update = controller.expectOne(`${API_BASE_URL}/courses/8`);
    expect(update.request.method).toBe('PUT');
    expect(update.request.body).toEqual(payload);
    update.flush({ id: 8, title: 'Angular' });
  });

  it('publishes, unpublishes and deletes a course', () => {
    service.publishCourse(8).subscribe();
    const publish = controller.expectOne(`${API_BASE_URL}/courses/8/publish`);
    expect(publish.request.method).toBe('PATCH');
    publish.flush({ id: 8, status: 'PUBLISHED' });

    service.unpublishCourse(8).subscribe();
    const unpublish = controller.expectOne(`${API_BASE_URL}/courses/8/unpublish`);
    expect(unpublish.request.method).toBe('PATCH');
    unpublish.flush({ id: 8, status: 'DRAFT' });

    service.deleteCourse(8).subscribe();
    const remove = controller.expectOne(`${API_BASE_URL}/courses/8`);
    expect(remove.request.method).toBe('DELETE');
    remove.flush(null);
  });

  it('adds, updates, reorders and deletes lessons', () => {
    const payload = { title: 'Introduction', content: 'Bienvenue', position: 1 };

    service.addLesson(7, payload).subscribe((lesson) => expect(lesson.id).toBe(5));
    const add = controller.expectOne(`${API_BASE_URL}/courses/7/lessons`);
    expect(add.request.method).toBe('POST');
    expect(add.request.body).toEqual(payload);
    add.flush({ id: 5 });

    service.updateLesson(5, payload).subscribe();
    const update = controller.expectOne(`${API_BASE_URL}/lessons/5`);
    expect(update.request.method).toBe('PUT');
    expect(update.request.body).toEqual(payload);
    update.flush({ id: 5 });

    service.reorderLesson(5, 2).subscribe();
    const reorder = controller.expectOne(
      (candidate) => candidate.url === `${API_BASE_URL}/lessons/5/reorder`,
    );
    expect(reorder.request.method).toBe('PATCH');
    expect(reorder.request.params.get('position')).toBe('2');
    reorder.flush({ id: 5, position: 2 });

    service.deleteLesson(5).subscribe();
    const remove = controller.expectOne(`${API_BASE_URL}/lessons/5`);
    expect(remove.request.method).toBe('DELETE');
    remove.flush(null);
  });

  it('adds resources to a course or a lesson and deletes them', () => {
    const payload = {
      name: 'Support PDF',
      type: 'PDF' as const,
      url: 'https://example.com/a.pdf',
      size: '1 Mo',
    };

    service.addResourceToCourse(7, payload).subscribe((resource) => expect(resource.id).toBe(9));
    const addToCourse = controller.expectOne(`${API_BASE_URL}/courses/7/resources`);
    expect(addToCourse.request.method).toBe('POST');
    expect(addToCourse.request.body).toEqual(payload);
    addToCourse.flush({ id: 9 });

    service.addResourceToLesson(5, payload).subscribe((resource) => expect(resource.id).toBe(10));
    const addToLesson = controller.expectOne(`${API_BASE_URL}/lessons/5/resources`);
    expect(addToLesson.request.method).toBe('POST');
    expect(addToLesson.request.body).toEqual(payload);
    addToLesson.flush({ id: 10 });

    service.deleteResource(9).subscribe();
    const remove = controller.expectOne(`${API_BASE_URL}/resources/9`);
    expect(remove.request.method).toBe('DELETE');
    remove.flush(null);
  });
});
