package com.redmine.converter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {

    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    public static List<Document> loadDocuments(String path) throws IOException {
        try (Stream<Path> documents = Files.walk(Paths.get(path))) {
            return documents.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".html"))
                    .map(Utils::parseToHtml).filter(Optional::isPresent)
                    .map(Optional::get).collect(Collectors.toList());
        } catch (IOException e) {
            LOGGER.error("Files not found:\n{}", e.toString());
            throw e;
        }
    }

    public static void writeDocuments(List<Document> documents, String packagePath) {
        preparedOutputDirectory(packagePath);
        documents.forEach(document -> {
            Path path = Paths.get(String.join("\\", packagePath, document.title() + ".html"));
            try {
                BufferedWriter writer = Files.newBufferedWriter(path);
                writer.write(document.body().html());
                writer.close();
            } catch (IOException e) {
                LOGGER.error("File wasn't wrote:\n{}", e.toString());
            }
        });
    }

    private static void preparedOutputDirectory(String packagePath) {
        Path outputPath = Paths.get(packagePath);
        try {
            if (Files.notExists(outputPath)) {
                Files.createDirectory(outputPath);
            } else {
                FileUtils.cleanDirectory(outputPath.toFile());
            }
        } catch (IOException e) {
            LOGGER.error("Creating/cleaning of output directory was failed:\n{}", e.toString());
        }
    }

    private static Optional<Document> parseToHtml(Path path) {
        try {
            return Optional.of(Jsoup.parse(path.toFile(), "UTF-8"));
        } catch (IOException e) {
            LOGGER.error("Parse failed:\n{}", e.toString());
            return Optional.empty();
        }
    }
}