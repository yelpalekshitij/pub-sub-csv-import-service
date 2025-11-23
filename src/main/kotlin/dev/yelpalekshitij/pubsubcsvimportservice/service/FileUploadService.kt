package dev.yelpalekshitij.pubsubcsvimportservice.service

import dev.yelpalekshitij.pubsubcsvimportservice.dao.dto.FileUploadDto
import dev.yelpalekshitij.pubsubcsvimportservice.dao.model.FileUploadJob
import dev.yelpalekshitij.pubsubcsvimportservice.dao.model.type.FileUploadStatus
import dev.yelpalekshitij.pubsubcsvimportservice.dao.repository.FileUploadRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.time.Instant
import java.util.*

@Service
class FileUploadService(
    private val repo: FileUploadRepository,
    private val producer: KafkaProducer,
    @Value("\${file.upload-dir}") private val uploadDir: String
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun createJob(file: MultipartFile): FileUploadDto {
        logger.info("Received file to upload")
        val job = FileUploadJob()
        val path = saveTempFile(file, job.id)
        logger.debug("Saved at $path")
        repo.save(job.copy(filePath = path))
        logger.info("Created job ${job.id} for file $path")
        producer.publishFileUploaded(job.id)
        return job.toDto()
    }

    fun getJob(id: UUID): FileUploadDto = fetchJob(id).toDto()

    fun listJobs(): List<FileUploadDto> = repo.findAll().map { it.toDto() }

    fun markProcessing(id: UUID) {
        val j = fetchJob(id)
        j.status = FileUploadStatus.PROCESSING
        repo.save(j)
    }

    fun markCompleted(id: UUID, errors: List<String>) {
        val j = fetchJob(id)
        j.status = if (errors.isEmpty()) FileUploadStatus.COMPLETED else FileUploadStatus.FAILED
        j.completedAt = Instant.now()
        j.errors.addAll(errors)
        repo.save(j)
    }

    private fun fetchJob(id: UUID): FileUploadJob = repo.findById(id) ?: throw NoSuchElementException("Job $id not found")

    private fun saveTempFile(file: MultipartFile, id: UUID): String {
        return try {
            val dir = File(uploadDir)
            logger.debug("$uploadDir exists")
            if (!dir.exists()) {
                dir.mkdirs()
                logger.debug("$uploadDir created")
            }
            val filename = "${id}_${file.originalFilename}"
            val dest = File(dir, filename)
            file.transferTo(dest)
            dest.absolutePath
        } catch(e: Exception) {
            val message = "Error occurred saving file: ${e.message}"
            logger.error(message)
            throw RuntimeException(message)
        }
    }

    private fun FileUploadJob.toDto() = FileUploadDto(
        id = id,
        status = status,
        createdAt = createdAt,
        completedAt = completedAt,
        errors = errors
    )
}
