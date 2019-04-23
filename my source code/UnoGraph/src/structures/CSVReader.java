package structures;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import graph.GraphAlgorithms;

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
	private boolean dataRead;

	public CSVReader(String columnSeparator, File file, boolean fileHasHeader) throws FileNotFoundException {
		this.columnSeparator = columnSeparator;
		this.file = file;
		this.fileHasHeader = fileHasHeader;
		this.dataRead = false;
		this.rows = null;
		this.header = null;
	}

	public CSVReader(String columnSeparator, String filename, boolean containsHeader) throws FileNotFoundException {
		this(columnSeparator, new File(filename), containsHeader);
	}

	public void close() throws IOException {
	}

	public void read() throws IOException {
		this.rows = new ArrayList<ArrayList<String>>();
		this.header = new ArrayList<>();
		boolean headRead = false;
		BufferedReader br = new BufferedReader(new FileReader(file), 1 << 16);
		while (br.ready()) {
			String line = br.readLine();
			String[] cells = line.split(columnSeparator + "+");
			ArrayList<String> asList = GraphAlgorithms.arrayToArrayList(cells);
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

	public ArrayList<String> getHeader() throws IOException {
		if (!dataRead) {
			read();
		}
		return header;
	}

	public ArrayList<ArrayList<String>> getRows() throws IOException {
		if (!dataRead) {
			read();
		}
		return rows;
	}

	public static CSVReader readCSV(String columnSeparator, File file, boolean fileHasHeader) throws IOException {
		CSVReader c = new CSVReader(columnSeparator, file, fileHasHeader);
		c.read();
		c.close();
		return c;
	}

	public int getNumberOfRows() {
		return rows.size();
	}

	public int getNumberOfColumns(int row) {
		return rows.get(row).size();
	}

	/**
	 * calls getNumberOfColumns(0)
	 * 
	 * @return
	 */
	public int getNumberOfColumns() {
		return rows.get(0).size();
	}

}
