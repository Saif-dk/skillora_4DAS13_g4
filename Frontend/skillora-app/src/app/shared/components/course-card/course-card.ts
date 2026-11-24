import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CourseSummary } from '../../../core/models/course.models';
import { StatusBadge } from '../status-badge/status-badge';

@Component({
  selector: 'app-course-card',
  imports: [RouterLink, StatusBadge],
  templateUrl: './course-card.html',
  styleUrl: './course-card.scss',
})
export class CourseCard {
  @Input({ required: true }) course!: CourseSummary;

  get cover(): number {
    return (this.course.id % 6) + 1;
  }

  get coverIcon(): string {
    const icons = ['bi bi-code-slash', 'bi bi-braces', 'bi bi-palette', 'bi bi-server', 'bi bi-bar-chart', 'bi bi-kanban'];
    return icons[this.course.id % icons.length];
  }
}
