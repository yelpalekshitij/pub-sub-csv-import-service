package dev.yelpalekshitij.pubsubcsvimportservice.service

import dev.yelpalekshitij.pubsubcsvimportservice.config.KafkaConfig.Companion.GROUP_ID_FILE_PROCESSOR
import dev.yelpalekshitij.pubsubcsvimportservice.config.KafkaConfig.Companion.TOPIC_FILE_UPLOADED
import dev.yelpalekshitij.pubsubcsvimportservice.dao.repository.FileUploadRepository
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.io.File
import java.util.*

@Component
class KafkaConsumer(
    private val repo: FileUploadRepository,
    private val fileUploadService: FileUploadService,
    private val csvParserService: CSVParserService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [TOPIC_FILE_UPLOADED], groupId = GROUP_ID_FILE_PROCESSOR)
    fun handleMessage(jobIdStr: String) {
        val jobId = UUID.fromString(jobIdStr)
        val job = repo.findById(jobId) ?: run {
            logger.error("Job $jobId not found in repository")
            return
        }

        logger.info("Worker received job ${job.id}; file=${job.filePath}")
        fileUploadService.markProcessing(jobId)

        val errors = mutableListOf<String>()

        try {
            val file = File(job.filePath)
            if (!file.exists()) {
                val msg = "File not found: ${job.filePath}"
                throw NoSuchElementException(msg)
            }

            val csvErrors = csvParserService.parseCSV(file, jobId)
            if (csvErrors.isNotEmpty()) {
                errors += csvErrors
            }

            fileUploadService.markCompleted(jobId, errors)
            logger.info("Job $jobId completed with ${errors.size} errors")
        } catch (ex: Exception) {
            val msg = "Error occurred processing job $jobId: ${ex.message}"
            logger.error(msg, ex)
            errors += msg
            fileUploadService.markCompleted(jobId, errors)
        }
    }
}
