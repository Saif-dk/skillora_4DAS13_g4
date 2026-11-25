import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { API_BASE_URL } from '../config/api.config';
import { authInterceptor } from './auth.interceptor';

describe('authInterceptor', () => {
  let http: HttpClient;
  let controller: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(withInterceptors([authInterceptor])), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpClient);
    controller = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    localStorage.clear();
    controller.verify();
  });

  it('adds the bearer token to API requests', () => {
    localStorage.setItem('skillora_token', 'token-123');

    http.get(`${API_BASE_URL}/courses`).subscribe();

    const request = controller.expectOne(`${API_BASE_URL}/courses`);
    expect(request.request.headers.get('Authorization')).toBe('Bearer token-123');
    request.flush({});
  });

  it('does not add the bearer token outside the API or without a token', () => {
    http.get(`${API_BASE_URL}/courses`).subscribe();
    const apiRequest = controller.expectOne(`${API_BASE_URL}/courses`);
    expect(apiRequest.request.headers.has('Authorization')).toBe(false);
    apiRequest.flush({});

    localStorage.setItem('skillora_token', 'token-123');
    http.get('https://example.com/data').subscribe();
    const externalRequest = controller.expectOne('https://example.com/data');
    expect(externalRequest.request.headers.has('Authorization')).toBe(false);
    externalRequest.flush({});
  });
});
