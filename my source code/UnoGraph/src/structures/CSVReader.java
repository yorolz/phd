package structures;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple Custom (tab, csv or any other character) Separated Values reader.
 * 
 * @author jcfgonc@gmail.com
 *
 */
public class CSVReader {
	private boolean fileHasHeader;
	private List<String> header;
	private List<List<String>> rows;
	private final String columnSeparator;
	private final File file;
	private boolean dataRead;

	public CSVReader(String columnSeparator, File file, boolean fileHasHeader) throws FileNotFoundException {
		this.columnSeparator = columnSeparator;
		this.file = file;
		this.fileHasHeader = fileHasHeader;
		this.dataRead = false;
	}

	public CSVReader(String columnSeparator, String filename, boolean containsHeader) throws FileNotFoundException {
		this(columnSeparator, new File(filename), containsHeader);
	}

	public void close() throws IOException {
	}

	public void read() throws IOException {
		this.rows = new ArrayList<List<String>>();
		this.header = new ArrayList<>();
		boolean headRead = false;
		BufferedReader br = new BufferedReader(new FileReader(file));
		while (br.ready()) {
			String line = br.readLine();
			String[] cells = line.split(columnSeparator + "+");
			List<String> asList = Arrays.asList(cells);
			if (fileHasHeader && !headRead) {
				this.header = asList;
				headRead = true;
			} else {
				this.rows.add(asList);
			}
		}
		br.close();
		this.dataRead = true;
	}

	public List<String> getHeader() throws IOException {
		if (!dataRead) {
			read();
		}
		return header;
	}

	public List<List<String>> getRows() throws IOException {
		if (!dataRead) {
			read();
		}
		return rows;
	}

	public static List<List<String>> readCSV(String columnSeparator, String filename, boolean fileHasHeader) throws IOException {
		return readCSV(columnSeparator, new File(filename), fileHasHeader);
	}

	public static List<List<String>> readCSV(String columnSeparator, File file, boolean fileHasHeader) throws IOException {
		CSVReader c = new CSVReader(columnSeparator, file, fileHasHeader);
		c.read();
		c.close();
		List<List<String>> rows = new ArrayList<>();
		List<String> h = c.getHeader();
		if (h != null && !h.isEmpty()) {
			rows.add(h);
		}
		List<List<String>> r = c.getRows();
		if (r != null && !r.isEmpty()) {
			rows.addAll(r);
		}
		return rows;
	}

}
