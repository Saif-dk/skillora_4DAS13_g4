import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { COURSE_API_BASE_URL } from '../config/api.config';
import {
  Course,
  CourseCreateRequest,
  CourseSummary,
  CourseUpdateRequest,
  Lesson,
  LessonCreateRequest,
  LessonUpdateRequest,
  PageResponse,
  Resource,
  ResourceCreateRequest,
} from '../models/course.models';

@Injectable({ providedIn: 'root' })
export class CourseService {
  private readonly http = inject(HttpClient);

  list(
    params: { search?: string; status?: string; page?: number; size?: number } = {},
  ): Observable<PageResponse<CourseSummary>> {
    let httpParams = new HttpParams();
    if (params.search !== undefined) {
      httpParams = httpParams.set('search', params.search);
    }
    if (params.status !== undefined) {
      httpParams = httpParams.set('status', params.status);
    }
    if (params.page !== undefined) {
      httpParams = httpParams.set('page', params.page);
    }
    if (params.size !== undefined) {
      httpParams = httpParams.set('size', params.size);
    }

    return this.http.get<PageResponse<CourseSummary>>(`${COURSE_API_BASE_URL}/courses`, {
      params: httpParams,
    });
  }

  getById(id: number): Observable<Course> {
    return this.http.get<Course>(`${COURSE_API_BASE_URL}/courses/${id}`);
  }

  listLessons(courseId: number): Observable<Lesson[]> {
    return this.http.get<Lesson[]>(`${COURSE_API_BASE_URL}/courses/${courseId}/lessons`);
  }

  getResource(id: number): Observable<Resource> {
    return this.http.get<Resource>(`${COURSE_API_BASE_URL}/resources/${id}`);
  }

  createCourse(request: CourseCreateRequest): Observable<Course> {
    return this.http.post<Course>(`${COURSE_API_BASE_URL}/courses`, request);
  }

  updateCourse(id: number, request: CourseUpdateRequest): Observable<Course> {
    return this.http.put<Course>(`${COURSE_API_BASE_URL}/courses/${id}`, request);
  }

  publishCourse(id: number): Observable<Course> {
    return this.http.patch<Course>(`${COURSE_API_BASE_URL}/courses/${id}/publish`, null);
  }

  unpublishCourse(id: number): Observable<Course> {
    return this.http.patch<Course>(`${COURSE_API_BASE_URL}/courses/${id}/unpublish`, null);
  }

  deleteCourse(id: number): Observable<void> {
    return this.http.delete<void>(`${COURSE_API_BASE_URL}/courses/${id}`);
  }

  addLesson(courseId: number, request: LessonCreateRequest): Observable<Lesson> {
    return this.http.post<Lesson>(`${COURSE_API_BASE_URL}/courses/${courseId}/lessons`, request);
  }

  updateLesson(lessonId: number, request: LessonUpdateRequest): Observable<Lesson> {
    return this.http.put<Lesson>(`${COURSE_API_BASE_URL}/lessons/${lessonId}`, request);
  }

  reorderLesson(lessonId: number, position: number): Observable<Lesson> {
    return this.http.patch<Lesson>(`${COURSE_API_BASE_URL}/lessons/${lessonId}/reorder`, null, {
      params: { position },
    });
  }

  deleteLesson(lessonId: number): Observable<void> {
    return this.http.delete<void>(`${COURSE_API_BASE_URL}/lessons/${lessonId}`);
  }

  addResourceToCourse(courseId: number, request: ResourceCreateRequest): Observable<Resource> {
    return this.http.post<Resource>(`${COURSE_API_BASE_URL}/courses/${courseId}/resources`, request);
  }

  addResourceToLesson(lessonId: number, request: ResourceCreateRequest): Observable<Resource> {
    return this.http.post<Resource>(`${COURSE_API_BASE_URL}/lessons/${lessonId}/resources`, request);
  }

  uploadResourceToCourse(courseId: number, formData: FormData): Observable<Resource> {
    return this.http.post<Resource>(`${COURSE_API_BASE_URL}/courses/${courseId}/resources/upload`, formData);
  }

  uploadResourceToLesson(lessonId: number, formData: FormData): Observable<Resource> {
    return this.http.post<Resource>(`${COURSE_API_BASE_URL}/lessons/${lessonId}/resources/upload`, formData);
  }

  deleteResource(id: number): Observable<void> {
    return this.http.delete<void>(`${COURSE_API_BASE_URL}/resources/${id}`);
  }
}
