package structures;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * simple csv writer
 * 
 * @author jcfgonc@gmail.com
 *
 */
public class CSVWriter {

	public static String generateFilenameWithTimestamp() {
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		String filename = dateFormat.format(date);
		return filename;
	}

	private BufferedWriter bw;

	private boolean headerAllowed;

	private String filename;

	public CSVWriter() throws IOException {
		this(generateFilenameWithTimestamp() + ".csv");
	}

	public CSVWriter(String filename) throws IOException {
		this.headerAllowed = true;
		this.filename = filename;
		bw = new BufferedWriter(new FileWriter(filename));
	}

	public void addLine(List<String> columns) throws IOException {
		String[] array = (String[]) columns.toArray();
		addLine(array);
	}

	public void addLine(String... columns) throws IOException {
		writeLine(columns);
	}

	public void close() throws IOException {
		bw.close();
	}

	public void flush() throws IOException {
		bw.flush();
	}

	public void setHeader(List<String> header) throws IOException {
		String[] array = header.toArray(new String[header.size()]);
		setHeader(array);
	}

	public void setHeader(String... header) throws IOException {
		if (headerAllowed)
			writeLine(header);
		headerAllowed = false;
	}

	private void writeLine(String[] columns) throws IOException {
		headerAllowed = false;
		for (int i = 0; i < columns.length; i++) {
			String column = columns[i];
			bw.write(column);
			if (i < (columns.length - 1)) {
				bw.write(",");
			}
		}
		bw.newLine();
	}

	public String getFilename() {
		return filename;
	}

}
