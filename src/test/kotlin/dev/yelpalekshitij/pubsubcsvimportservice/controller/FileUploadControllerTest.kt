package dev.yelpalekshitij.pubsubcsvimportservice.controller

import com.ninjasquad.springmockk.MockkBean
import dev.yelpalekshitij.pubsubcsvimportservice.dao.dto.FileUploadDto
import dev.yelpalekshitij.pubsubcsvimportservice.dao.model.type.FileUploadStatus
import dev.yelpalekshitij.pubsubcsvimportservice.service.FileUploadService
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant
import java.util.UUID

@WebMvcTest(FileUploadController::class)
@AutoConfigureMockMvc
class FileUploadControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var service: FileUploadService

    @Test
    fun `POST uploadFile returns 202 and DTO`() {
        // given
        val file = MockMultipartFile(
            "file",
            "test.csv",
            "text/csv",
            "id,name\n1,Vijay".byteInputStream()
        )

        val dto = FileUploadDto(
            id = UUID.randomUUID(),
            status = FileUploadStatus.PENDING,
            createdAt = Instant.now(),
            completedAt = null,
            errors = mutableListOf()
        )

        every { service.createJob(any()) } returns dto

        // when/then
        mockMvc.perform(
            multipart("/api/v1/files")
                .file(file)
        )
            .andExpect(status().isAccepted)
            .andExpect(jsonPath("$.id").value(dto.id.toString()))
            .andExpect(jsonPath("$.status").value(FileUploadStatus.PENDING.name))

        verify { service.createJob(any()) }
    }

    @Test
    fun `GET getJob returns file job`() {
        // given
        val id = UUID.randomUUID()
        val dto = FileUploadDto(
            id = id,
            status = FileUploadStatus.PROCESSING,
            createdAt = Instant.now(),
            completedAt = null,
            errors = mutableListOf()
        )

        every { service.getJob(id) } returns dto

        // when/then
        mockMvc.perform(
            get("/api/v1/files/$id")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(id.toString()))
            .andExpect(jsonPath("$.status").value(FileUploadStatus.PROCESSING.name))

        verify { service.getJob(id) }
    }

    @Test
    fun `GET listJobs returns list of dtos`() {
        // given
        val dto = FileUploadDto(
            id = UUID.randomUUID(),
            status = FileUploadStatus.PENDING,
            createdAt = Instant.now(),
            completedAt = null,
            errors = mutableListOf()
        )

        every { service.listJobs() } returns listOf(dto)

        // when/then
        mockMvc.perform(get("/api/v1/files"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(dto.id.toString()))

        verify { service.listJobs() }
    }
}
