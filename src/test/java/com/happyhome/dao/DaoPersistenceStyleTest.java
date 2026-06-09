package com.happyhome.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class DaoPersistenceStyleTest {

    @Test
    void productionDaosDoNotUseJdbcTemplate() throws IOException {
        Path daoDirectory = Path.of("src/main/java/com/happyhome/dao");

        try (var files = Files.walk(daoDirectory)) {
            assertThat(files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> fileContains(path, "JdbcTemplate"))
                    .map(Path::toString)
                    .toList())
                    .isEmpty();
        }
    }

    private boolean fileContains(Path path, String text) {
        try {
            return Files.readString(path).contains(text);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + path, e);
        }
    }
}
