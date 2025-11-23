package dev.yelpalekshitij.pubsubcsvimportservice.config

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class KafkaConfig {

    companion object {
        const val TOPIC_FILE_UPLOADED = "file-uploaded"
        const val GROUP_ID_FILE_PROCESSOR = "file-processor-group"
    }

    @Bean
    fun uploadedTopic(): NewTopic = NewTopic(TOPIC_FILE_UPLOADED, 1, 1)
}
