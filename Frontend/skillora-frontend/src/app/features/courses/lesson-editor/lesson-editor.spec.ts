import { vi } from 'vitest';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter, Router } from '@angular/router';
import { API_BASE_URL } from '../../../core/config/api.config';
import { LessonEditorComponent } from './lesson-editor';

describe('LessonEditorComponent', () => {
  let controller: HttpTestingController;
  let router: Router;
  let route: {
    snapshot: {
      data: Record<string, string>;
      paramMap: ReturnType<typeof convertToParamMap>;
      queryParamMap: ReturnType<typeof convertToParamMap>;
    };
  };

  beforeEach(async () => {
    route = {
      snapshot: {
        data: { target: 'course' },
        paramMap: convertToParamMap({ id: '7' }),
        queryParamMap: convertToParamMap({}),
      },
    };
    await TestBed.configureTestingModule({
      imports: [LessonEditorComponent],
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

  it('adds a lesson to the course', () => {
    const navigate = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    const fixture = TestBed.createComponent(LessonEditorComponent);
    fixture.detectChanges();
    fixture.componentInstance.form.setValue({
      title: 'Introduction',
      content: 'Bienvenue',
      position: 1,
    });

    fixture.componentInstance.submit();

    const request = controller.expectOne(`${API_BASE_URL}/courses/7/lessons`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({
      title: 'Introduction',
      content: 'Bienvenue',
      position: 1,
    });
    request.flush({ id: 3 });
    expect(navigate).toHaveBeenCalledWith(['/courses', 7]);
  });

  it('loads and updates an existing lesson', () => {
    route.snapshot.data = { target: 'lesson' };
    route.snapshot.paramMap = convertToParamMap({ id: '3' });
    route.snapshot.queryParamMap = convertToParamMap({ courseId: '7' });
    const navigate = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    const fixture = TestBed.createComponent(LessonEditorComponent);
    fixture.detectChanges();

    controller
      .expectOne(`${API_BASE_URL}/courses/7/lessons`)
      .flush([
        {
          id: 3,
          courseId: 7,
          title: 'Introduction',
          content: 'Bienvenue',
          position: 1,
          resources: [],
        },
      ]);
    expect(fixture.componentInstance.form.controls.title.value).toBe('Introduction');

    fixture.componentInstance.form.patchValue({ position: 2 });
    fixture.componentInstance.submit();

    const request = controller.expectOne(`${API_BASE_URL}/lessons/3`);
    expect(request.request.method).toBe('PUT');
    expect(request.request.body.position).toBe(2);
    request.flush({ id: 3, position: 2 });
    expect(navigate).toHaveBeenCalledWith(['/courses', 7]);
  });
});
