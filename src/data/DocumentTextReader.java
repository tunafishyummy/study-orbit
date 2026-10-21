package data;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

// reads text from plain text and docx files without calling the api
public class DocumentTextReader {

    public boolean isLocalDocument(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".txt") || name.endsWith(".docx");
    }

    public String read(File file) throws IOException {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".txt")) {
            return Files.readString(file.toPath(), StandardCharsets.UTF_8);
        }
        if (name.endsWith(".docx")) {
            return readDocx(file);
        }
        throw new IOException("Only txt and docx files are read locally");
    }

    private String readDocx(File file) throws IOException {
        try (ZipInputStream zip = new ZipInputStream(new FileInputStream(file))) {
            ZipEntry entry = zip.getNextEntry();
            while (entry != null) {
                if ("word/document.xml".equals(entry.getName())) {
                    Document document = newDocumentBuilderFactory().newDocumentBuilder().parse(zip);
                    NodeList paragraphs = document.getElementsByTagName("w:p");
                    StringBuilder text = new StringBuilder();
                    for (int index = 0; index < paragraphs.getLength(); index++) {
                        text.append(paragraphs.item(index).getTextContent()).append("\n");
                    }
                    return text.toString().trim();
                }
                entry = zip.getNextEntry();
            }
        } catch (Exception exception) {
            throw new IOException("Could not read the docx file", exception);
        }
        throw new IOException("The docx file has no document body");
    }

    private DocumentBuilderFactory newDocumentBuilderFactory() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        return factory;
    }
}
