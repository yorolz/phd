package structures;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GlobalFileWriter {
	private static FileWriter fw;

	private static String generateFilenameWithTimestamp() {
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		String filename = dateFormat.format(date);
		return filename;
	}

	public static void close() throws IOException {
		if (fw != null) {
			fw.close();
		}
	}

	public static synchronized void writeLine(String line)  {
		try {
			if (fw == null) {
				fw = new FileWriter(generateFilenameWithTimestamp() + ".txt");
			}
			fw.write(line);
			fw.write("\r\n");
			fw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
