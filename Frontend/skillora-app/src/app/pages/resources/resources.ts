import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Observable } from 'rxjs';
import { Resource, ResourceType } from '../../core/models/course.models';
import { CourseService } from '../../core/services/course.service';

type ResourceSource = 'LINK' | 'UPLOAD';

@Component({
  selector: 'app-resources',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './resources.html',
  styleUrl: './resources.scss',
})
export class Resources {
  private readonly courseService = inject(CourseService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  readonly types: ResourceType[] = ['PDF', 'VIDEO', 'DOCUMENT', 'LINK'];
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly selectedFile = signal<File | null>(null);
  private readonly targetId: number | null;
  private readonly courseId: number | null;
  readonly lessonTarget: boolean;

  readonly form = new FormGroup({
    name: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(200)],
    }),
    type: new FormControl<ResourceType>('PDF', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    source: new FormControl<ResourceSource>('LINK', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    url: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(500)],
    }),
    size: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(50)] }),
  });

  constructor() {
    const snapshot = this.route.snapshot;
    this.lessonTarget = snapshot.data['target'] === 'lesson';
    this.targetId = this.parseId(snapshot.paramMap.get('id'));
    this.courseId = this.parseId(
      this.lessonTarget ? snapshot.queryParamMap.get('courseId') : snapshot.paramMap.get('id'),
    );

    if (!this.targetId || !this.courseId) {
      this.error.set('Identifiants de cours ou de leçon invalides.');
    }

    this.form.controls.source.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((source) => {
        const url = this.form.controls.url;
        if (source === 'UPLOAD') {
          url.clearValidators();
          url.setValue('');
        } else {
          url.setValidators([Validators.required, Validators.maxLength(500)]);
          this.selectedFile.set(null);
        }
        url.updateValueAndValidity();
      });
  }

  backLink(): (string | number)[] {
    return this.courseId === null ? ['/courses'] : ['/courses', this.courseId];
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    this.selectedFile.set(file);
    if (!file) {
      return;
    }

    if (!this.form.controls.name.value.trim()) {
      this.form.controls.name.setValue(file.name);
    }
    this.form.controls.size.setValue(this.formatBytes(file.size));
    const inferredType = this.typeFromFile(file.name);
    if (inferredType) {
      this.form.controls.type.setValue(inferredType);
    }
  }

  submit(): void {
    if (this.form.invalid || !this.targetId || !this.courseId) {
      this.form.markAllAsTouched();
      return;
    }

    const fields = this.form.getRawValue();
    const file = this.selectedFile();
    if (fields.source === 'UPLOAD' && !file) {
      this.error.set('Choisissez un fichier à importer.');
      return;
    }
    if (fields.source === 'LINK' && !fields.url.trim()) {
      this.form.controls.url.markAsTouched();
      return;
    }

    this.loading.set(true);
    this.error.set(null);
    const action =
      fields.source === 'UPLOAD' && file
        ? this.upload(file, fields.name, fields.type, fields.size)
        : this.createLink(fields.name, fields.type, fields.url, fields.size);

    action.subscribe({
      next: () => void this.router.navigate(this.backLink()),
      error: () => {
        this.error.set('Impossible d’enregistrer la ressource.');
        this.loading.set(false);
      },
    });
  }

  private upload(file: File, name: string, type: ResourceType, size: string): Observable<Resource> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('name', name);
    formData.append('type', type);
    formData.append('size', size || this.formatBytes(file.size));
    return this.lessonTarget
      ? this.courseService.uploadResourceToLesson(this.targetId!, formData)
      : this.courseService.uploadResourceToCourse(this.targetId!, formData);
  }

  private createLink(
    name: string,
    type: ResourceType,
    url: string,
    size: string,
  ): Observable<Resource> {
    const request = { name, type, url, size };
    return this.lessonTarget
      ? this.courseService.addResourceToLesson(this.targetId!, request)
      : this.courseService.addResourceToCourse(this.targetId!, request);
  }

  private typeFromFile(fileName: string): ResourceType | null {
    const extension = fileName.split('.').pop()?.toLowerCase() ?? '';
    if (extension === 'pdf') {
      return 'PDF';
    }
    if (['mp4', 'webm', 'mov', 'avi', 'mkv'].includes(extension)) {
      return 'VIDEO';
    }
    if (
      ['doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'txt', 'csv', 'zip', 'png', 'jpg', 'jpeg', 'gif'].includes(
        extension,
      )
    ) {
      return 'DOCUMENT';
    }
    return null;
  }

  private formatBytes(bytes: number): string {
    if (bytes < 1024) {
      return `${bytes} B`;
    }
    if (bytes < 1024 * 1024) {
      return `${(bytes / 1024).toFixed(1)} Ko`;
    }
    return `${(bytes / (1024 * 1024)).toFixed(1)} Mo`;
  }

  private parseId(rawId: string | null): number | null {
    const id = Number(rawId);
    return rawId && Number.isInteger(id) && id > 0 ? id : null;
  }
}
