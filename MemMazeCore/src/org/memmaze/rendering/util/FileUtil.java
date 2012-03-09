package org.memmaze.rendering.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class FileUtil {
	
	public static String readStream(InputStream inputStream) throws IOException {
		final char[] buffer = new char[1024];
		StringBuilder out = new StringBuilder();
		Reader reader = new InputStreamReader(inputStream, "UTF-8");
		int read;
		do {
			read = reader.read(buffer, 0, buffer.length);
			if (read > 0) {
				out.append(buffer, 0, read);
			}
		} while (read >= 0);
		return out.toString();
	}

}
