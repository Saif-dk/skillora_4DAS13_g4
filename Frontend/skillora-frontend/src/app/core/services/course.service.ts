import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';
import { Course, CourseSummary, Lesson, PageResponse, Resource } from '../models/course.models';

@Injectable({ providedIn: 'root' })
export class CourseService {
  private readonly http = inject(HttpClient);

  list(params: { search?: string; status?: string; page?: number; size?: number } = {}): Observable<
    PageResponse<CourseSummary>
  > {
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

    return this.http.get<PageResponse<CourseSummary>>(`${API_BASE_URL}/courses`, { params: httpParams });
  }

  getById(id: number): Observable<Course> {
    return this.http.get<Course>(`${API_BASE_URL}/courses/${id}`);
  }

  listLessons(courseId: number): Observable<Lesson[]> {
    return this.http.get<Lesson[]>(`${API_BASE_URL}/courses/${courseId}/lessons`);
  }

  getResource(id: number): Observable<Resource> {
    return this.http.get<Resource>(`${API_BASE_URL}/resources/${id}`);
  }
}
