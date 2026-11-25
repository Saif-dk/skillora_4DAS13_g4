import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: '/courses' },
  {
    path: 'courses',
    loadComponent: () =>
      import('./features/courses/course-list/course-list').then((module) => module.CourseListComponent),
  },
  {
    path: 'courses/:id',
    loadComponent: () =>
      import('./features/courses/course-detail/course-detail').then((module) => module.CourseDetailComponent),
  },
  { path: '**', redirectTo: '/courses' },
];
