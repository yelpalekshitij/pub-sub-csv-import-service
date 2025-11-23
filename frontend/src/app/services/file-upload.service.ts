import {computed, inject, Injectable, signal} from '@angular/core';
import {HttpClient} from '@angular/common/http';

export interface FileUploadResource {
  id: number;
  status: string;
  createdAt: string;
  completedAt: string;
  errors: string[];
}

@Injectable({
  providedIn: 'root',
})
export class FileUploadService {
  private readonly baseUrl = globalThis.location.hostname === 'localhost' ? 'http://localhost:8080' : 'http://backend:8080';
  private readonly api = `${this.baseUrl}/api/v1/files`;
  private readonly http = inject(HttpClient);

  private readonly jobsSignal = signal<FileUploadResource[]>([]);
  jobs = computed(() => this.jobsSignal());

  private readonly selectedJobSignal = signal<FileUploadResource | null>(null);
  selectedJob = computed(() => this.selectedJobSignal());

  getAllJobs() {
    this.http.get<FileUploadResource[]>(this.api).subscribe({
      next: (res) => this.jobsSignal.set(res)
    });
  }

  getJobById(id: string) {
    this.http.get<FileUploadResource>(`${this.api}/${id}`).subscribe({
      next: (job) => this.selectedJobSignal.set(job)
    });
  }

  uploadFile(file: File) {
    const form = new FormData();
    form.append('file', file);

    return this.http.post(this.api, form);
  }
}
