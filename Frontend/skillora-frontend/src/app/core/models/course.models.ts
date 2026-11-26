export type CourseStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';

export type ResourceType = 'PDF' | 'VIDEO' | 'DOCUMENT' | 'LINK';

export interface CourseSummary {
  id: number;
  title: string;
  category: string;
  level: string;
  status: CourseStatus;
  price: number;
  teacherId: number;
  createdAt: string;
}

export interface Resource {
  id: number;
  courseId: number;
  lessonId: number | null;
  name: string;
  type: ResourceType;
  url: string;
  size: string;
  createdAt: string;
}

export interface Lesson {
  id: number;
  courseId: number;
  title: string;
  content: string;
  position: number;
  createdAt: string;
  updatedAt: string;
  resources: Resource[];
}

export interface Course extends CourseSummary {
  description: string;
  updatedAt: string;
  lessons: Lesson[];
  resources: Resource[];
}

export interface CourseCreateRequest {
  title: string;
  description: string;
  category: string;
  level: string;
  status: CourseStatus;
  price: number;
}

export interface CourseUpdateRequest {
  title: string;
  description: string;
  category: string;
  level: string;
  status: CourseStatus;
  price: number;
}

export interface LessonCreateRequest {
  title: string;
  content: string;
  position: number;
}

export interface LessonUpdateRequest {
  title: string;
  content: string;
  position: number;
}

export interface ResourceCreateRequest {
  courseId?: number;
  lessonId?: number;
  name: string;
  type: ResourceType;
  url: string;
  size: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
