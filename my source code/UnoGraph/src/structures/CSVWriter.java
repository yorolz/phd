package structures;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

/**
 * A simple Custom (tab, csv or any other character) Separated Values writer.
 * 
 * @author jcfgonc@gmail.com
 *
 */
public class CSVWriter {

	private static String generateFilenameWithTimestamp() {
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		String filename = dateFormat.format(date);
		return filename;
	}

	private final BufferedWriter bw;
	private final String columnSeparator;
	private boolean headerWritten;
	private final String filename;

	public CSVWriter(String columnSeparator) throws IOException {
		this(generateFilenameWithTimestamp() + ".csv", columnSeparator);
	}

	public CSVWriter(String filename, String columnSeparator) throws IOException {
		this.headerWritten = false;
		this.filename = filename;
		this.columnSeparator = columnSeparator;
		bw = new BufferedWriter(new FileWriter(filename));
	}

	public void close() throws IOException {
		flush();
		bw.close();
	}

	public void flush() throws IOException {
		bw.flush();
	}

	public String getFilename() {
		return filename;
	}

	public void writeHeader(Collection<String> header) throws IOException {
		if (!headerWritten) {
			writeLine(header);
		}
		headerWritten = true;
	}

	public void writeLine(Collection<String> columns) throws IOException {
		headerWritten = true; // forcibly, can't go back now
		Iterator<String> iterator = columns.iterator();
		while (iterator.hasNext()) {
			String column = iterator.next();
			bw.write(column);
			if (iterator.hasNext()) {
				bw.write(columnSeparator);
			}
		}
		bw.newLine();
	}

	public void writeLines(Collection<Collection<String>> rows) throws IOException {
		for (Collection<String> row : rows) {
			writeLine(row);
		}
	}

	public void writeLines(ArrayList<ArrayList<String>> rows) throws IOException {
		for (ArrayList<String> row : rows) {
			writeLine(row);
		}
	}

	public static void writeCSV(String filename, String columnSeparator, ArrayList<ArrayList<String>> rows) throws IOException {
		CSVWriter csv = new CSVWriter(filename, columnSeparator);
		csv.writeLines(rows);
		csv.close();
	}

}
