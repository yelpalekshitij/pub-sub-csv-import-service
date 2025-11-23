package dev.yelpalekshitij.pubsubcsvimportservice.dao.repository

import dev.yelpalekshitij.pubsubcsvimportservice.dao.model.FileUploadJob
import org.springframework.stereotype.Repository
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Repository
class FileUploadRepository {
    private val store = ConcurrentHashMap<UUID, FileUploadJob>()

    fun save(job: FileUploadJob): FileUploadJob {
        store[job.id] = job
        return job
    }

    fun findById(id: UUID): FileUploadJob? = store[id]

    fun findAll(): List<FileUploadJob> = store.values.toList()
}
