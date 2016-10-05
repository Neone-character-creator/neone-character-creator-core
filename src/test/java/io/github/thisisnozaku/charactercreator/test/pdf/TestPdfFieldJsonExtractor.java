package io.github.thisisnozaku.charactercreator.test.pdf;

import io.github.thisisnozaku.pdfexporter.JsonFieldValueExtractor;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by Damien on 9/17/2016.
 */
public class TestPdfFieldJsonExtractor {

    @Test
    public void testFieldExtraction(){
        String json = "{\"name\":\"name\", \"age\":20}";
        JsonFieldValueExtractor extractor = new JsonFieldValueExtractor();
        Map<String, String> mappings = extractor.generateFieldMappings(json);
        assertEquals("name", mappings.get("name"));
        assertEquals("20", mappings.get("age"));
    }
}
