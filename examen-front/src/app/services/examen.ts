import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Examen, Question } from '../models/examen';

@Injectable({ providedIn: 'root' })
export class ExamenService {
  private http = inject(HttpClient);
  private api = 'http://localhost:8082/api';

  // ---- Examens ----
  getAll(): Observable<Examen[]> {
    return this.http.get<Examen[]>(`${this.api}/examens`);
  }
  get(id: string): Observable<Examen> {
    return this.http.get<Examen>(`${this.api}/examens/${id}`);
  }
  create(e: Examen): Observable<Examen> {
    return this.http.post<Examen>(`${this.api}/examens`, e);
  }
  update(id: string, e: Examen): Observable<Examen> {
    return this.http.put<Examen>(`${this.api}/examens/${id}`, e);
  }
  publier(id: string): Observable<Examen> {
    return this.http.patch<Examen>(`${this.api}/examens/${id}/publier`, {});
  }
  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.api}/examens/${id}`);
  }

  // ---- Questions ----
  getQuestions(examenId: string): Observable<Question[]> {
    return this.http.get<Question[]>(`${this.api}/examens/${examenId}/questions`);
  }
  addQuestion(examenId: string, q: Question): Observable<Question> {
    return this.http.post<Question>(`${this.api}/examens/${examenId}/questions`, q);
  }
  updateQuestion(id: string, q: Question): Observable<Question> {
    return this.http.put<Question>(`${this.api}/questions/${id}`, q);
  }
  deleteQuestion(id: string): Observable<void> {
    return this.http.delete<void>(`${this.api}/questions/${id}`);
  }
}