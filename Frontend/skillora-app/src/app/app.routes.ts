import { Routes } from '@angular/router';
import { DashboardLayout } from './core/layout/dashboard-layout/dashboard-layout';
import { authGuard, roleGuard } from './core/guards/auth.guard';
import { teacherGuard } from './core/guards/teacher.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  {
    path: 'login',
    title: 'Connexion — Skillora',
    loadComponent: () => import('./pages/login/login').then((m) => m.Login),
  },
  {
    path: 'register',
    title: 'Inscription — Skillora',
    loadComponent: () => import('./pages/register/register').then((m) => m.Register),
  },
  {
    path: '',
    component: DashboardLayout,
    canActivateChild: [authGuard],
    children: [
      {
        path: 'dashboard',
        title: 'Dashboard — Skillora',
        loadComponent: () => import('./pages/dashboard/dashboard').then((m) => m.Dashboard),
      },
      {
        path: 'courses',
        title: 'Gestion des cours — Skillora',
        loadComponent: () => import('./pages/courses/course-list/course-list').then((m) => m.CourseList),
      },
      {
        path: 'courses/new',
        title: 'Nouveau cours — Skillora',
        canActivate: [teacherGuard],
        loadComponent: () => import('./pages/courses/course-form/course-form').then((m) => m.CourseForm),
      },
      {
        path: 'courses/:id',
        title: 'Détails du cours — Skillora',
        loadComponent: () => import('./pages/courses/course-details/course-details').then((m) => m.CourseDetails),
      },
      {
        path: 'courses/:id/edit',
        title: 'Modifier le cours — Skillora',
        canActivate: [teacherGuard],
        loadComponent: () => import('./pages/courses/course-form/course-form').then((m) => m.CourseForm),
      },
      {
        path: 'courses/:id/lessons/new',
        title: 'Nouvelle leçon — Skillora',
        canActivate: [teacherGuard],
        data: { target: 'course' },
        loadComponent: () => import('./pages/lessons/lessons').then((m) => m.Lessons),
      },
      {
        path: 'courses/:id/resources/new',
        title: 'Nouvelle ressource — Skillora',
        canActivate: [teacherGuard],
        data: { target: 'course' },
        loadComponent: () => import('./pages/resources/resources').then((m) => m.Resources),
      },
      {
        path: 'lessons/:id/edit',
        title: 'Modifier la leçon — Skillora',
        canActivate: [teacherGuard],
        data: { target: 'lesson' },
        loadComponent: () => import('./pages/lessons/lessons').then((m) => m.Lessons),
      },
      {
        path: 'lessons/:id/resources/new',
        title: 'Nouvelle ressource de leçon — Skillora',
        canActivate: [teacherGuard],
        data: { target: 'lesson' },
        loadComponent: () => import('./pages/resources/resources').then((m) => m.Resources),
      },
      {
        path: 'catalog',
        title: 'Catalogue — Skillora',
        loadComponent: () => import('./pages/catalog/catalog').then((m) => m.Catalog),
      },
      {
        path: 'profile',
        title: 'Mon profil — Skillora',
        loadComponent: () => import('./pages/profile/profile').then((m) => m.Profile),
      },
      {
        path: 'users',
        title: 'Utilisateurs — Skillora',
        canActivate: [roleGuard(['ADMIN'])],
        loadComponent: () => import('./pages/users/user-list').then((m) => m.UserList),
      },
    ],
  },
  { path: '**', redirectTo: 'dashboard' },
];
