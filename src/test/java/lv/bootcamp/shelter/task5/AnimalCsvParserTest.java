package lv.bootcamp.shelter.task5;

import lv.bootcamp.shelter.model.Animal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 - Task 5: Nested test classes for CSV parsing
 - <p>
 - Practice:
 - - @Nested to organize by scenario
 - - @DisplayName for readable output
 - - Testing Optional results (isPresent, isEmpty)
 - - Testing file I/O with temp files
 - <p>
 - Instructions:
 - Write tests for AnimalCsvParser. Use @Nested classes to group tests by scenario.
 - For file-based tests, use Files.createTempFile() and Files.writeString() to create test data.
 */
@DisplayName("AnimalCsvParser")
class AnimalCsvParserTest {

    private AnimalCsvParser parser;

    @BeforeEach
    void setUp() {
        parser = new AnimalCsvParser();
    }

    // PARSE ROW TESTS

    @Nested
    @DisplayName("When parsing valid rows")
    class ValidRows {

        @Test
        @DisplayName("parses a complete row into an Animal")
        void shouldParseCompleteRow() {
            Optional<Animal> result = parser.parseRow("Buddy,Dog,3,true,2026-01-15");

            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("Buddy");
            assertThat(result.get().getSpecies()).isEqualTo("Dog");
            assertThat(result.get().getAge()).isEqualTo(3);
            assertThat(result.get().isVaccinated()).isTrue();
            assertThat(result.get().getIntakeDate()).isEqualTo(LocalDate.of(2026, 1, 15));
        }

        @Test
        @DisplayName("trims whitespace from fields")
        void shouldTrimWhitespace() {
            Optional<Animal> result = parser.parseRow("  Buddy , Dog , 3 , true , 2026-01-15 ");

            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("Buddy");
            assertThat(result.get().getSpecies()).isEqualTo("Dog");
        }

        @Test
        @DisplayName("parses vaccinated=false correctly")
        void shouldParseFalseVaccination() {
            Optional<Animal> result = parser.parseRow("Buddy,Dog,3,false,2026-01-15");

            assertThat(result).isPresent();
            assertThat(result.get().isVaccinated()).isFalse();
        }
    }

    @Nested
    @DisplayName("When parsing malformed rows")
    class MalformedRows {

        @Test
        @DisplayName("returns empty for null input")
        void shouldReturnEmptyForNull() {
            Optional<Animal> result = parser.parseRow(null);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returns empty for blank input")
        void shouldReturnEmptyForBlank() {
            Optional<Animal> result = parser.parseRow("   ");
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returns empty when row has fewer than 5 fields")
        void shouldReturnEmptyForTooFewFields() {
            Optional<Animal> result = parser.parseRow("Buddy,Dog,3");
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returns empty when name is missing")
        void shouldReturnEmptyForMissingName() {
            Optional<Animal> result = parser.parseRow(",Dog,3,true,2026-01-15");
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returns empty when age is not a number")
        void shouldReturnEmptyForBadAge() {
            Optional<Animal> result = parser.parseRow("Buddy,Dog,old,true,2026-01-15");
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returns empty when age is negative")
        void shouldReturnEmptyForNegativeAge() {
            Optional<Animal> result = parser.parseRow("Buddy,Dog,-1,true,2026-01-15");
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returns empty when date is invalid")
        void shouldReturnEmptyForBadDate() {
            Optional<Animal> result = parser.parseRow("Buddy,Dog,3,true,not-a-date");
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("When handling edge cases")
    class EdgeCases {

        @Test
        @DisplayName("handles vaccinated field as any non-true string → false")
        void shouldTreatNonTrueAsFalse() {
            Optional<Animal> result = parser.parseRow("Buddy,Dog,3,maybe,2026-01-15");
            assertThat(result).isPresent();
            assertThat(result.get().isVaccinated()).isFalse();
        }

        @Test
        @DisplayName("handles age 0 as valid")
        void shouldAcceptAgeZero() {
            Optional<Animal> result = parser.parseRow("Buddy,Dog,0,true,2026-01-15");
            assertThat(result).isPresent();
            assertThat(result.get().getAge()).isEqualTo(0);
        }
    }

    // PARSE FILE TESTS

    @Nested
    @DisplayName("When parsing a CSV file")
    class ParseFile {

        @Test
        @DisplayName("parses valid rows from file")
        void shouldParseFile() throws IOException {
            String content = "name,species,age,vaccinated,intakeDate\n" +
                    "Buddy,Dog,3,true,2026-01-15\n" +
                    "Luna,Cat,2,true,2026-01-10\n";

            Path tempFile = Files.createTempFile("test-intake", ".csv");
            Files.writeString(tempFile, content, StandardCharsets.UTF_8);

            List<Animal> result = parser.parseFile(tempFile).animals();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getName()).isEqualTo("Buddy");

            Files.deleteIfExists(tempFile);
        }

        @Test
        @DisplayName("throws IOException for non-existent file")
        void shouldThrowForMissingFile() {
            Path missingPath = Path.of("does-not-exist.csv");
            assertThatThrownBy(() -> parser.parseFile(missingPath))
                    .isInstanceOf(IOException.class);
        }
    }
}
