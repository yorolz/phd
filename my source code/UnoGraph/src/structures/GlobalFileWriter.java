package structures;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GlobalFileWriter {
	private static FileWriter fw = null;
	private static String extension = ".txt";

	private static String generateFilenameWithTimestamp() {
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		String filename = dateFormat.format(date);
		return filename;
	}

	public static void close() {
		if (fw != null) {
			try {
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			fw = null;
		}
	}

	public static synchronized void writeLine(String line) {
		writeLineUnsync(line);
	}

	public static void writeLineUnsync(String line) {
		try {
			if (fw == null) {
				createNewFile();
			}
			fw.write(line);
			fw.write("\r\n");
			fw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void setExtension(String ext) {
		GlobalFileWriter.extension = ext;
	}

	public static void createNewFile(String filename) {
		try {
			if (fw != null) {
				fw.close();
			}
			// may contain an extension
			if (filename.contains(".")) {
				fw = new FileWriter(filename);
			} else {
				fw = new FileWriter(filename + extension);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void createNewFile() {
		createNewFile(generateFilenameWithTimestamp());
	}

}
