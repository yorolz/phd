package structures;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * A simple Custom (tab, csv or any other character) Separated Values reader.
 * 
 * @author jcfgonc@gmail.com
 *
 */
public class CSVReader {
	private boolean fileHasHeader;
	private ArrayList<String> header;
	private ArrayList<ArrayList<String>> rows;
	private final String columnSeparator;
	private final File file;

	public CSVReader(String columnSeparator, File file, boolean fileHasHeader) throws FileNotFoundException {
		this.columnSeparator = columnSeparator;
		this.file = file;
		this.fileHasHeader = fileHasHeader;
		this.rows = new ArrayList<ArrayList<String>>();
	}

	public CSVReader(String columnSeparator, String filename, boolean containsHeader) throws FileNotFoundException {
		this(columnSeparator, new File(filename), containsHeader);
	}

	public void close() throws IOException {
	}

	public void read() throws IOException {
		boolean headRead = false;
		BufferedReader br = new BufferedReader(new FileReader(file));
		while (br.ready()) {
			String line = br.readLine();
			String[] cells = line.split(columnSeparator);
			ArrayList<String> asList = toArrayList(cells);
			if (fileHasHeader && !headRead) {
				this.header = asList;
				headRead = true;
			} else {
				this.rows.add(asList);
			}
		}
		br.close();
	}

	private ArrayList<String> toArrayList(String[] cells) {
		ArrayList<String> asList = new ArrayList<String>(cells.length);
		for (String element : cells) {
			asList.add(element);
		}
		return asList;
	}

	public ArrayList<String> getHeader() {
		return header;
	}

	public ArrayList<ArrayList<String>> getRows() {
		return rows;
	}

}
