package dev.yelpalekshitij.pubsubcsvimportservice.dao.dto

import dev.yelpalekshitij.pubsubcsvimportservice.dao.model.type.FileUploadStatus
import java.time.Instant
import java.util.UUID

data class FileUploadDto(
    val id: UUID,
    val status: FileUploadStatus,
    val createdAt: Instant,
    var completedAt: Instant? = null,
    var errors: MutableList<String> = mutableListOf()
)
