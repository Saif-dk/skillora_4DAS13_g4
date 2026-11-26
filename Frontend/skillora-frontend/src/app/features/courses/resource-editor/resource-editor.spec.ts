import { vi } from 'vitest';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter, Router } from '@angular/router';
import { API_BASE_URL } from '../../../core/config/api.config';
import { ResourceEditorComponent } from './resource-editor';

describe('ResourceEditorComponent', () => {
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
      imports: [ResourceEditorComponent],
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

  it('adds a resource to a course', () => {
    const navigate = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    const fixture = TestBed.createComponent(ResourceEditorComponent);
    fixture.detectChanges();
    fixture.componentInstance.form.setValue({
      name: 'Support PDF',
      type: 'PDF',
      url: 'https://example.com/support.pdf',
      size: '1 Mo',
    });

    fixture.componentInstance.submit();

    const request = controller.expectOne(`${API_BASE_URL}/courses/7/resources`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({
      courseId: 7,
      name: 'Support PDF',
      type: 'PDF',
      url: 'https://example.com/support.pdf',
      size: '1 Mo',
    });
    request.flush({ id: 4 });
    expect(navigate).toHaveBeenCalledWith(['/courses', 7]);
  });

  it('adds a resource to a lesson', () => {
    route.snapshot.data = { target: 'lesson' };
    route.snapshot.paramMap = convertToParamMap({ id: '3' });
    route.snapshot.queryParamMap = convertToParamMap({ courseId: '7' });
    const navigate = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    const fixture = TestBed.createComponent(ResourceEditorComponent);
    fixture.detectChanges();
    fixture.componentInstance.form.setValue({
      name: 'VidÃ©o',
      type: 'VIDEO',
      url: 'https://example.com/video',
      size: '25 Mo',
    });

    fixture.componentInstance.submit();

    const request = controller.expectOne(`${API_BASE_URL}/lessons/3/resources`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body.lessonId).toBe(3);
    expect(request.request.body.courseId).toBeUndefined();
    request.flush({ id: 5 });
    expect(navigate).toHaveBeenCalledWith(['/courses', 7]);
  });
});
