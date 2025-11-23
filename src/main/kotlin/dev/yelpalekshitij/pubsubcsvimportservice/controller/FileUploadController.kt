package dev.yelpalekshitij.pubsubcsvimportservice.controller

import dev.yelpalekshitij.pubsubcsvimportservice.dao.dto.FileUploadDto
import dev.yelpalekshitij.pubsubcsvimportservice.service.FileUploadService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@RestController
@RequestMapping("/api/v1/files")
@CrossOrigin(origins = ["http://localhost:4200"])
class FileUploadController(
    private val service: FileUploadService
) {
    @PostMapping
    fun uploadFile(@RequestParam("file") file: MultipartFile): ResponseEntity<FileUploadDto> {
        val job = service.createJob(file)
        return ResponseEntity.accepted().body(job)
    }

    @GetMapping("/{id}")
    fun getJob(@PathVariable id: UUID): ResponseEntity<FileUploadDto> {
        val job = service.getJob(id)
        return ResponseEntity.ok(job)
    }

    @GetMapping
    fun listJobs() = ResponseEntity.ok(service.listJobs())
}
