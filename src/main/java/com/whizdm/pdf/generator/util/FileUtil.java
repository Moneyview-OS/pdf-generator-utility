package com.whizdm.pdf.generator.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * @author satyampriyam
 * @since 17-October-2024
 **/
public class FileUtil {
    public static boolean exists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    public static void deleteFile(File file){
        Optional.ofNullable(file).ifPresent(d -> {
            if(d.isFile())
                d.delete();
        });
    }

    public static void deleteFile(String filePath) {
        if (!exists(filePath)) {
            return;
        }
        try {
            Files.delete(Paths.get(filePath));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static InputStream getInputStream(String filePath) throws IOException {
        return Files.newInputStream(Paths.get(filePath));
    }
    public static OutputStream getOutputStream(String filePath) throws IOException {
        return Files.newOutputStream(Paths.get(filePath));
    }
}