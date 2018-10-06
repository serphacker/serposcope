package com.serphacker.serposcope.scraper;

import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ResourceHelper {

    public static List<String> listResourceInDirectories(String[] directories) throws IOException {

        List<String> files = new ArrayList<>();

        for (String directory : directories) {
            String[] dirFiles = toString(directory)
                .split("\n");
            for (String dirFile : dirFiles) {
                files.add(directory + "/" + dirFile);
            }
        }

        return files;
    }

    public static String toString(String path) throws IOException {
        return new String(ByteStreams.toByteArray(ResourceHelper.class.getResourceAsStream(path)));
    }

}
