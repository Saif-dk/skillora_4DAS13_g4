import { Component, Input } from '@angular/core';
import { CourseStatus } from '../../../core/models/course.models';

@Component({
  selector: 'app-status-badge',
  templateUrl: './status-badge.html',
  styleUrl: './status-badge.scss',
})
export class StatusBadge {
  @Input({ required: true }) status!: CourseStatus;

  get label(): string {
    switch (this.status) {
      case 'PUBLISHED':
        return 'Publié';
      case 'DRAFT':
        return 'Brouillon';
      default:
        return 'Archivé';
    }
  }

  get badgeClass(): string {
    switch (this.status) {
      case 'PUBLISHED':
        return 'status-badge status-published';
      case 'DRAFT':
        return 'status-badge status-draft';
      default:
        return 'status-badge status-archived';
    }
  }

  get iconClass(): string {
    switch (this.status) {
      case 'PUBLISHED':
        return 'bi bi-check-circle-fill';
      case 'DRAFT':
        return 'bi bi-pencil-square';
      default:
        return 'bi bi-archive-fill';
    }
  }
}
