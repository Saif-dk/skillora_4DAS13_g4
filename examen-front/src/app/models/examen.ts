export type StatutExamen = 'BROUILLON' | 'PUBLIE';

export interface Examen {
  id?: string;
  titre: string;
  description?: string;
  dateExamen: string;
  duree: number;
  noteTotale?: number;
  statut?: StatutExamen;
  courseId: string;
  teacherId: string;
}

export interface Question {
  id?: string;
  enonce: string;
  typeQuestion: string;
  optionA: string;
  optionB: string;
  optionC?: string;
  optionD?: string;
  bonneReponse: string;
  points: number;
  examenId?: string;
}