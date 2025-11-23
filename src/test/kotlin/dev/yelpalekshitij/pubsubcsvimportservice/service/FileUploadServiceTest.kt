package dev.yelpalekshitij.pubsubcsvimportservice.service

import dev.yelpalekshitij.pubsubcsvimportservice.dao.dto.FileUploadDto
import dev.yelpalekshitij.pubsubcsvimportservice.dao.model.FileUploadJob
import dev.yelpalekshitij.pubsubcsvimportservice.dao.model.type.FileUploadStatus
import dev.yelpalekshitij.pubsubcsvimportservice.dao.repository.FileUploadRepository
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Path
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FileUploadServiceMockKTest {

    private lateinit var repository: FileUploadRepository
    private lateinit var producer: KafkaProducer
    private lateinit var service: FileUploadService

    @TempDir
    lateinit var tempDir: Path

    @BeforeEach
    fun setup() {
        repository = mockk(relaxed = true)
        producer = mockk(relaxed = true)
        service = FileUploadService(repository, producer, tempDir.toString())
    }

    @Test
    fun `createJob saves file, creates job, and publishes event`() {
        // given
        val multipartFile = mockk<MultipartFile>()
        every { multipartFile.originalFilename } returns "test.csv"
        every { multipartFile.transferTo(any<File>()) } answers {
            val f = firstArg<File>()
            f.writeText("id,firstName,lastName,email\n1,John,Doe,john@example.com")
        }

        // when
        val dto: FileUploadDto = service.createJob(multipartFile)

        // then
        assertNotNull(dto.id)
        assertEquals(FileUploadStatus.PENDING, dto.status)
        verify { repository.save(any<FileUploadJob>()) }
        verify { producer.publishFileUploaded(any<UUID>()) }
        val savedFile = File(tempDir.toFile(), dto.id.toString() + "_test.csv")
        assertTrue(savedFile.exists())
    }

    @Test
    fun `getJob returns correct DTO`() {
        // given
        val job = FileUploadJob(filePath = "test")
        every { repository.findById(job.id) } returns job

        // when
        val dto = service.getJob(job.id)

        // then
        assertEquals(job.id, dto.id)
    }

    @Test
    fun `getJob throws exception if job not found`() {
        // given
        val missingId = UUID.randomUUID()
        every { repository.findById(missingId) } returns null

        // then
        assertThrows<NoSuchElementException> { service.getJob(missingId) }
    }

    @Test
    fun `listJobs returns all jobs`() {
        // given
        val job1 = FileUploadJob(filePath = "f1")
        val job2 = FileUploadJob(filePath = "f2")
        every { repository.findAll() } returns listOf(job1, job2)

        // when
        val jobs = service.listJobs()

        // then
        assertEquals(2, jobs.size)
    }

    @Test
    fun `markProcessing updates status`() {
        // given
        val job = FileUploadJob(filePath = "test")
        every { repository.findById(job.id) } returns job

        // when
        service.markProcessing(job.id)

        // then
        assertEquals(FileUploadStatus.PROCESSING, job.status)
        verify { repository.save(job) }
    }

    @Test
    fun `markCompleted sets COMPLETED when no errors`() {
        // given
        val job = FileUploadJob(filePath = "test")
        every { repository.findById(job.id) } returns job

        // when
        service.markCompleted(job.id, emptyList())

        // then
        assertEquals(FileUploadStatus.COMPLETED, job.status)
        assertNotNull(job.completedAt)
        verify { repository.save(job) }
    }

    @Test
    fun `markCompleted sets FAILED when errors exist`() {
        // given
        val job = FileUploadJob(filePath = "test")
        val errors = listOf("Row 1 invalid")
        every { repository.findById(job.id) } returns job

        // when
        service.markCompleted(job.id, errors)

        // then
        assertEquals(FileUploadStatus.FAILED, job.status)
        assertEquals(errors, job.errors)
        assertNotNull(job.completedAt)
        verify { repository.save(job) }
    }
}

