package com.epam.digital.data.platform.report.util;

import java.io.File;
import com.epam.digital.data.platform.report.exception.NoFilesFoundException;

public class IOUtils {
    private IOUtils() {}

    public static File[] getFileList(File path) {
        File[] files = path.listFiles();

        if (files == null) {
            throw new NoFilesFoundException("No files found in " + path.getName());
        }

        return files;
    }
}
