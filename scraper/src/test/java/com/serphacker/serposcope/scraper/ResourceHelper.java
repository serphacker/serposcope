package com.serphacker.serposcope.scraper;

import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ResourceHelper {

    public static List<String> listResourceDir(Collection<String> resourceDirectories) throws IOException {

        List<String> files = new ArrayList<>();

        for (String resourceDir : resourceDirectories) {
            String[] resourceFiles = readResourceAsString(resourceDir).split("\n");
            for (String resourceFile : resourceFiles) {
                files.add(resourceDir + "/" + resourceFile);
            }
        }

        return files;
    }

    public static String readResourceAsString(String resourcePath) throws IOException {
        return new String(ByteStreams.toByteArray(ResourceHelper.class.getResourceAsStream(resourcePath)));
    }

}
