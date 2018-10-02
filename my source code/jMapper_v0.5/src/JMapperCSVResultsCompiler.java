import java.io.File;
import java.io.IOException;
import java.util.List;

import structures.CSVReader;
import structures.CSVWriter;

/**
 * this merges identical row (tau and bridge) in a single row, because of the way I did things
 * 
 * @author CK
 *
 */
public class JMapperCSVResultsCompiler {

	public static void main(String[] args) throws Exception {
		new JMapperCSVResultsCompiler("csv_results").compile();
	}

	private String path;

	public JMapperCSVResultsCompiler(String path) {
		this.path = path;
	}

	public void compile() throws IOException {

		File[] files = new File(path).listFiles();

		for (File file : files) {
			if (file.isFile()) {
				String filename = file.getName();
				if (filename.endsWith(".csv")) {
					CSVReader csvReader = new CSVReader(file, true);
					csvReader.read();
					List<String> header = csvReader.getHeader();

					CSVWriter csvWriter = new CSVWriter("compiled_" + file.getName());
					csvWriter.setHeader(header);

					List<List<String>> rows = csvReader.getRows();
					for (int line = 0; line < rows.size() - 1; line++) {
						List<String> row_i0 = rows.get(line);
						List<String> row_i1 = rows.get(line + 1);

						String tau0 = row_i0.get(0);
						String tau1 = row_i1.get(0);

						String bridge0 = row_i0.get(1);
						String bridge1 = row_i1.get(1);

						// rows must be related to the same tau & bridge
						if (tau0.equals(tau1) && bridge0.equals(bridge1)) {
							double time0 = Double.parseDouble(row_i0.get(2));
							double time1 = Double.parseDouble(row_i1.get(2));

							int pairs0 = Integer.parseInt(row_i0.get(3));
							int pairs1 = Integer.parseInt(row_i1.get(3));

							// sum the execution time and get the maximum number of pairs
							int pairs = Math.max(pairs0, pairs1);
							double time = time0 + time1;

							csvWriter.addLine(tau0, bridge0, Double.toString(time), Integer.toString(pairs));

							// advance one additional line
							line++;
						} else {
							// just copy the line
							csvWriter.addLine(row_i0);
						}
					}
					csvWriter.close();
					System.lineSeparator();
				}
			}
		}
	}

}
