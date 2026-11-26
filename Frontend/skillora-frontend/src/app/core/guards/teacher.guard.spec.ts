import { Injector, runInInjectionContext } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import {
  ActivatedRouteSnapshot,
  Router,
  RouterStateSnapshot,
  UrlTree,
  provideRouter,
} from '@angular/router';
import { teacherGuard } from './teacher.guard';

describe('teacherGuard', () => {
  const route = {} as ActivatedRouteSnapshot;
  const state = {} as RouterStateSnapshot;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({ providers: [provideRouter([])] });
  });

  afterEach(() => localStorage.clear());

  function canActivate(): boolean | UrlTree {
    return runInInjectionContext(TestBed.inject(Injector), () => teacherGuard(route, state)) as
      boolean | UrlTree;
  }

  it('allows professors and admins', () => {
    localStorage.setItem(
      'skillora_token',
      `header.${btoa(JSON.stringify({ role: 'PROFESSOR' }))}.signature`,
    );
    expect(canActivate()).toBe(true);

    localStorage.setItem(
      'skillora_token',
      `header.${btoa(JSON.stringify({ role: 'ADMIN' }))}.signature`,
    );
    expect(canActivate()).toBe(true);
  });

  it('redirects students to the catalogue', () => {
    localStorage.setItem(
      'skillora_token',
      `header.${btoa(JSON.stringify({ role: 'STUDENT' }))}.signature`,
    );

    const result = canActivate();

    expect(result instanceof UrlTree).toBe(true);
    expect(TestBed.inject(Router).serializeUrl(result as UrlTree)).toBe('/courses');
  });
});
