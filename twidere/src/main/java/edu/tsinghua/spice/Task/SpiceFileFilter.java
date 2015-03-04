package edu.tsinghua.spice.Task;

import java.io.File;
import java.io.FileFilter;

/**
 * Created by Denny C. Ng on 2/21/15.
 */

public final class SpiceFileFilter implements FileFilter {

    @Override
    public boolean accept(final File file) {
        return file.isFile() && "spi".equalsIgnoreCase(getExtension(file));
    }

    static String getExtension(final File file) {
        final String name = file.getName();
        final int pos = name.lastIndexOf('.');
        if (pos == -1) return null;
        return name.substring(pos + 1);
    }
}