package com.redmine.converter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import javax.swing.text.html.HTML;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

public class RedmineToDevwikiConverter {

    private static final String DEVWIKI_URL = "https://devwiki.thomsonreuterslifesciences.com/pub/Main";

    public static void convert(Document document) {
        clearDocument(document);
        processHref(document);
        processImage(document);
        processHeaders(document);
    }

    private static void clearDocument(Document document) {
        document.getElementsByClass("wiki-anchor").remove();
        document.getElementsByTag(HTML.Tag.STYLE.toString()).remove();
        document.getElementsByTag(HTML.Tag.A.toString()).stream().filter(e -> e.hasAttr(HTML.Attribute.NAME.toString())).forEach(Node::remove);
    }

    private static void processImage(Document document) {
        String imagePath = String.join("/", DEVWIKI_URL, document.title());
        document.getElementsByTag(HTML.Tag.IMG.toString()).forEach(e -> {
            String imageName = e.attr(HTML.Attribute.SRC.toString());
            e.attr(HTML.Attribute.SRC.toString(), String.join("/", imagePath, imageName));
            e.attr(HTML.Attribute.ALT.toString(), imageName);
            e.attr(HTML.Attribute.TITLE.toString(), imageName);
        });
    }

    private static void processHref(Document document) {
        document.getElementsByTag(HTML.Tag.A.toString()).stream().filter(e ->
                e.attr(HTML.Attribute.HREF.toString()).equalsIgnoreCase(".html")
                        && e.hasClass("wiki-page")).forEach(e1 -> {
            e1.attr(HTML.Attribute.HREF.toString(), StringUtils.deleteWhitespace(e1.text().replaceAll("/", "")));
            e1.removeClass("wiki-page");
            e1.addClass("TMLlink");
        });
    }

    private static void processHeaders(Document document) {
        Arrays.asList(HTML.Tag.H1.toString(), HTML.Tag.H2.toString(), HTML.Tag.H3.toString()).forEach(header -> {
            Elements headers = document.getElementsByTag(header);
            headers.forEach(e -> {
                e.addClass("TML");
                if (header.equalsIgnoreCase(HTML.Tag.H1.toString())) {
                    e.addClass("notoc");
                } else if (header.equalsIgnoreCase(HTML.Tag.H2.toString())) {
                    Element line = new Element(HTML.Tag.HR.toString());
                    e.after(line);
                }
                e.children().forEach(e1 -> {
                    if (!e1.hasClass("TMLlink") && !e1.hasClass("external")) {
                        e1.remove();
                    }
                });
                if (e.children().size() == 0) {
                    addColorSpan(e);
                }
            });
        });
    }

    private static void addColorSpan(Element element) {
        Element span = new Element(HTML.Tag.SPAN.toString());
        span.addClass("WYSIWYG_COLOR");
        span.attr(HTML.Attribute.STYLE.toString(), span.attr("style") + "color: orange;");
        span.text(element.text());
        element.text(StringUtils.EMPTY);
        element.appendChild(span);
    }

    private static void writeToFile(String doc, String file) throws IOException {
        Path path = Paths.get(file);
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(doc);
        }
    }

    private static Document readFile(String file) throws IOException {
        String doc = IOUtils.toString(Files.newBufferedReader(Paths.get(file)));
        //System.out.println(doc);
        return Jsoup.parse(doc);
    }
}
