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
 * simple csv reader
 * 
 * @author jcfgonc@gmail.com
 *
 */
public class CSVReader {
	private BufferedReader br;
	private boolean containsHeader;
	private boolean readHeader;
	private List<String> header;
	private List<List<String>> rows;

	public CSVReader(File file, boolean containsHeader) throws FileNotFoundException {
		this.br = new BufferedReader(new FileReader(file));
		this.containsHeader = containsHeader;
		this.readHeader = false;
		this.rows = new ArrayList<List<String>>();
	}

	public CSVReader(String filename, boolean containsHeader) throws FileNotFoundException {
		this(new File(filename), containsHeader);
	}

	public void close() throws IOException {
		br.close();
	}

	public void read() throws IOException {
		while (br.ready()) {
			String line = br.readLine();
			String[] cells = line.split(",");
			List<String> asList = Arrays.asList(cells);
			if (containsHeader && !readHeader) {
				this.header = asList;
				readHeader = true;
			} else {
				this.rows.add(asList);
			}
		}
	}

	public List<String> getHeader() {
		return header;
	}

	public List<List<String>> getRows() {
		return rows;
	}

}
