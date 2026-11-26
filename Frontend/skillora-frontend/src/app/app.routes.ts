import { Routes } from '@angular/router';
import { teacherGuard } from './core/guards/teacher.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: '/courses' },
  {
    path: 'courses',
    loadComponent: () =>
      import('./features/courses/course-list/course-list').then(
        (module) => module.CourseListComponent,
      ),
  },
  {
    path: 'courses/new',
    canActivate: [teacherGuard],
    loadComponent: () =>
      import('./features/courses/course-editor/course-editor').then(
        (module) => module.CourseEditorComponent,
      ),
  },
  {
    path: 'courses/:id',
    loadComponent: () =>
      import('./features/courses/course-detail/course-detail').then(
        (module) => module.CourseDetailComponent,
      ),
  },
  {
    path: 'courses/:id/edit',
    canActivate: [teacherGuard],
    loadComponent: () =>
      import('./features/courses/course-editor/course-editor').then(
        (module) => module.CourseEditorComponent,
      ),
  },
  {
    path: 'courses/:id/lessons/new',
    canActivate: [teacherGuard],
    data: { target: 'course' },
    loadComponent: () =>
      import('./features/courses/lesson-editor/lesson-editor').then(
        (module) => module.LessonEditorComponent,
      ),
  },
  {
    path: 'courses/:id/resources/new',
    canActivate: [teacherGuard],
    data: { target: 'course' },
    loadComponent: () =>
      import('./features/courses/resource-editor/resource-editor').then(
        (module) => module.ResourceEditorComponent,
      ),
  },
  {
    path: 'lessons/:id/edit',
    canActivate: [teacherGuard],
    data: { target: 'lesson' },
    loadComponent: () =>
      import('./features/courses/lesson-editor/lesson-editor').then(
        (module) => module.LessonEditorComponent,
      ),
  },
  {
    path: 'lessons/:id/resources/new',
    canActivate: [teacherGuard],
    data: { target: 'lesson' },
    loadComponent: () =>
      import('./features/courses/resource-editor/resource-editor').then(
        (module) => module.ResourceEditorComponent,
      ),
  },
  { path: '**', redirectTo: '/courses' },
];
