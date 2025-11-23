package dev.yelpalekshitij.pubsubcsvimportservice.service

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.UUID

@Service
class CSVParserService {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun parseCSV(file: File, jobId: UUID): List<String> {
        val parsingErrors = mutableListOf<String>()
        CSVParser.parse(
            file,
            StandardCharsets.UTF_8,
            CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .build()
        ).use { parser ->
            val headers = parser.headerNames
            logger.info("CSV Headers: $headers")

            parser.records.forEachIndexed { idx, record ->
                try {
                    val id = record.get("id")
                    val firstName = record.get("firstName")
                    val lastName = record.get("lastName")
                    val email = record.get("email")

                    logger.info("Parsed user with row ${idx + 1} in job $jobId: " +
                            "id=$id firstName=$firstName lastName=$lastName email=$email")

                } catch (ex: Exception) {
                    val msg = "Invalid row ${idx + 1} in job $jobId: ${ex.message}"
                    logger.warn(msg)
                    parsingErrors += msg
                }
            }
        }

        return parsingErrors
    }
}
