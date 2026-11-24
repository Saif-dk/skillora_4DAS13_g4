export type CourseLevel = 'Débutant' | 'Intermédiaire' | 'Avancé';

export type CourseStatus = 'Brouillon' | 'Publié' | 'Archivé';

export interface Course {
  id: number;
  title: string;
  description: string;
  level: CourseLevel;
  teacher: string;
  status: CourseStatus;
  createdAt: string;
  lessons: number;
  students: number;
  cover: number;
  category: string;
}

export interface Lesson {
  id: number;
  courseId: number;
  title: string;
  position: number;
  resourcesCount: number;
  duration: string;
}

export type ResourceType = 'PDF' | 'VIDEO' | 'DOCUMENT' | 'LINK';

export interface Resource {
  id: number;
  lessonId: number;
  name: string;
  type: ResourceType;
  size: string;
}

export interface ActivityItem {
  id: number;
  icon: string;
  title: string;
  detail: string;
  time: string;
  tone: 'blue' | 'green' | 'amber' | 'violet';
}
