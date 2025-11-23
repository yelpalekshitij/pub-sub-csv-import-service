import {Component, ElementRef, inject, ViewChild} from '@angular/core';
import {FileUploadService} from '../services/file-upload.service';
import {CommonModule} from '@angular/common';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './home.html',
  styleUrl: './home.css'
})
export class HomeComponent {
  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

  private readonly fileUploadService = inject(FileUploadService);

  jobs = this.fileUploadService.jobs;
  selectedJob = this.fileUploadService.selectedJob;

  fileToUpload: File | null = null;

  loadFileJobs() {
    this.fileUploadService.getAllJobs();
  }

  loadFileJobById(id: string) {
    this.fileUploadService.getJobById(id);
  }

  onFileSelected(event: any) {
    this.fileToUpload = event.target.files[0] ?? null;
  }

  upload() {
    if (!this.fileToUpload) {
      return;
    }

    this.fileUploadService.uploadFile(this.fileToUpload).subscribe({
      next: () => {
        alert('Uploaded');
        this.fileToUpload = null;
        this.fileInput.nativeElement.value = '';
      },
      error: (err) => alert('Upload failed')
    });
  }
}
