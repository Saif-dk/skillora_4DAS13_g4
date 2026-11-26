import { vi } from 'vitest';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter, Router } from '@angular/router';
import { API_BASE_URL } from '../../../core/config/api.config';
import { CourseEditorComponent } from './course-editor';

describe('CourseEditorComponent', () => {
  let controller: HttpTestingController;
  let router: Router;
  let route: { snapshot: { paramMap: ReturnType<typeof convertToParamMap> } };

  beforeEach(async () => {
    route = { snapshot: { paramMap: convertToParamMap({}) } };
    await TestBed.configureTestingModule({
      imports: [CourseEditorComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: ActivatedRoute, useValue: route },
      ],
    }).compileComponents();
    controller = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
  });

  afterEach(() => controller.verify());

  it('creates a course with the API and opens its detail page', () => {
    const navigate = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    const fixture = TestBed.createComponent(CourseEditorComponent);
    fixture.detectChanges();
    fixture.componentInstance.form.setValue({
      title: 'Angular',
      description: 'Cours complet',
      category: 'DÃ©veloppement',
      level: 'DEBUTANT',
      status: 'DRAFT',
      price: 49.9,
    });

    fixture.componentInstance.submit();

    const request = controller.expectOne(`${API_BASE_URL}/courses`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({
      title: 'Angular',
      description: 'Cours complet',
      category: 'DÃ©veloppement',
      level: 'DEBUTANT',
      status: 'DRAFT',
      price: 49.9,
    });
    request.flush({ id: 8, title: 'Angular' });

    expect(navigate).toHaveBeenCalledWith(['/courses', 8]);
  });

  it('loads an existing course and sends an update', () => {
    route.snapshot.paramMap = convertToParamMap({ id: '12' });
    const navigate = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    const fixture = TestBed.createComponent(CourseEditorComponent);
    fixture.detectChanges();

    controller.expectOne(`${API_BASE_URL}/courses/12`).flush({
      id: 12,
      title: 'Angular avancÃ©',
      description: 'Approfondir',
      category: 'DÃ©veloppement',
      level: 'AVANCE',
      status: 'PUBLISHED',
      price: 79,
    });
    expect(fixture.componentInstance.form.controls.title.value).toBe('Angular avancÃ©');

    fixture.componentInstance.form.patchValue({ price: 89 });
    fixture.componentInstance.submit();

    const request = controller.expectOne(`${API_BASE_URL}/courses/12`);
    expect(request.request.method).toBe('PUT');
    expect(request.request.body.price).toBe(89);
    request.flush({ id: 12, title: 'Angular avancÃ©', price: 89 });
    expect(navigate).toHaveBeenCalledWith(['/courses', 12]);
  });

  it('does not call the API when the form is invalid', () => {
    const fixture = TestBed.createComponent(CourseEditorComponent);
    fixture.detectChanges();

    fixture.componentInstance.submit();

    controller.expectNone(`${API_BASE_URL}/courses`);
    expect(fixture.componentInstance.form.controls.title.touched).toBe(true);
  });
});
