package dev.yelpalekshitij.pubsubcsvimportservice.service

import dev.yelpalekshitij.pubsubcsvimportservice.config.KafkaConfig.Companion.TOPIC_FILE_UPLOADED
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
class KafkaProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun publishFileUploaded(jobId: UUID) {
        logger.info("Publishing file-uploaded event for job $jobId")
        kafkaTemplate.send(TOPIC_FILE_UPLOADED, jobId.toString())
    }
}
