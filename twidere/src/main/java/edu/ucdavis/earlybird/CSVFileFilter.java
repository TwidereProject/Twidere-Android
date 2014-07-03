package edu.ucdavis.earlybird;

import java.io.File;
import java.io.FileFilter;

public final class CSVFileFilter implements FileFilter {

	@Override
	public boolean accept(final File file) {
		return file.isFile() && "csv".equalsIgnoreCase(getExtension(file));
	}

	static String getExtension(final File file) {
		final String name = file.getName();
		final int pos = name.lastIndexOf('.');
		if (pos == -1) return null;
		return name.substring(pos + 1);
	}
}
