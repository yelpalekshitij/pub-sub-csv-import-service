package dev.yelpalekshitij.pubsubcsvimportservice.dao.model

import dev.yelpalekshitij.pubsubcsvimportservice.dao.model.type.FileUploadStatus
import java.time.Instant
import java.util.*

data class FileUploadJob(
    val id: UUID = UUID.randomUUID(),
    var filePath: String? = null,
    var status: FileUploadStatus = FileUploadStatus.PENDING,
    val createdAt: Instant = Instant.now(),
    var completedAt: Instant? = null,
    var errors: MutableList<String> = mutableListOf()
)
