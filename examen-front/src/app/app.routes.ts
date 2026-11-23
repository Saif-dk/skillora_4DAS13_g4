import { Routes } from '@angular/router';
import { ExamenList } from './pages/examen-list/examen-list';
import { ExamenForm } from './pages/examen-form/examen-form';
import { ExamenDetail } from './pages/examen-detail/examen-detail';

export const routes: Routes = [
  { path: '', redirectTo: 'examens', pathMatch: 'full' },
  { path: 'examens', component: ExamenList },
  { path: 'examens/nouveau', component: ExamenForm },
  { path: 'examens/:id/modifier', component: ExamenForm },
  { path: 'examens/:id', component: ExamenDetail },
];