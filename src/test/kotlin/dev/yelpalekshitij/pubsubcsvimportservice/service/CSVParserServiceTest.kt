package dev.yelpalekshitij.pubsubcsvimportservice.service

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import java.io.File
import java.io.FileWriter
import java.nio.file.Files
import java.util.*

class CSVParserServiceTest {

    private lateinit var parserService: CSVParserService

    @BeforeEach
    fun setup() {
        parserService = CSVParserService()
    }

    @Test
    fun `parseCSV with valid CSV returns no errors`() {
        // given
        val csvContent = """
            id,firstName,lastName,email
            1,John,Doe,john.doe@example.com
            2,Jane,Doe,jane.doe@example.com
        """.trimIndent()

        val file = createTempCSV(csvContent)
        val jobId = UUID.randomUUID()

        // when
        val errors = parserService.parseCSV(file, jobId)

        // then
        assertTrue(errors.isEmpty(), "Expected no parsing errors")
    }

    @Test
    fun `parseCSV with invalid row returns errors`() {
        // given
        val csvContent = """
            id,firstName,lastName,email
            1,John,Doe,john.doe@example.com
            2,Jane,Doe
        """.trimIndent() // Missing email in second row

        val file = createTempCSV(csvContent)
        val jobId = UUID.randomUUID()

        // when
        val errors = parserService.parseCSV(file, jobId)

        // then
        assertEquals(1, errors.size, "Expected 1 parsing error")
        assertTrue(errors[0].contains("Invalid row 2"), "Error message should mention row 2")
    }

    @Test
    fun `parseCSV with empty CSV returns empty errors`() {
        // given
        val csvContent = ""
        val file = createTempCSV(csvContent)
        val jobId = UUID.randomUUID()

        // when
        val errors = parserService.parseCSV(file, jobId)

        // then
        assertTrue(errors.isEmpty(), "Empty CSV should return no parsing errors (no rows to parse)")
    }

    @Test
    fun `parseCSV with missing headers throws exception`() {
        // given
        val csvContent = """
            1,John,Doe,john.doe@example.com
            2,Jane,Doe,jane.doe@example.com
        """.trimIndent() // No header row

        val file = createTempCSV(csvContent)
        val jobId = UUID.randomUUID()

        // when
        val errors = parserService.parseCSV(file, jobId)

        // then
        assertEquals(1, errors.size) // Only the second row triggers an error
        assertTrue(errors[0].contains("Invalid row 1"))
    }

    private fun createTempCSV(contents: String): File {
        val tempFile = Files.createTempFile("test", ".csv").toFile()
        tempFile.writeText(contents)
        tempFile.deleteOnExit()
        return tempFile
    }
}

