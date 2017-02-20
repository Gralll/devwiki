package com.redmine.converter;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;

public class Main {

    private static String inputPath;
    private static String outputPath;

    public static void main(String[] args) throws IOException {
        validatePath(args);
        List<Document> documents = Utils.loadDocuments(inputPath);
        documents.forEach(RedmineToDevwikiConverter::convert);
        Utils.writeDocuments(documents, outputPath);
    }

    private static void validatePath(String... path) {
        inputPath = ArrayUtils.isNotEmpty(path) ? StringUtils.defaultIfBlank(path[0], "./") : "C:\\docs\\input";
        if (path.length > 1 && StringUtils.isNotBlank(path[1])) {
            outputPath = path[1];
        } else {
            outputPath = String.join("\\", Paths.get(inputPath).getParent().toString(), "outputHtml");
        }
    }
}