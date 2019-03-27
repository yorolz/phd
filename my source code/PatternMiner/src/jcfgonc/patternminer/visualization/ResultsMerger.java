package jcfgonc.patternminer.visualization;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import graph.GraphReadWrite;
import graph.StringGraph;
import structures.CSVReader;
import structures.CSVWriter;

public class ResultsMerger {

	public static void main(String[] args) throws IOException {
		HashMap<String, StringGraph> hashcodes = new HashMap<>();
		ArrayList<ArrayList<String>> rows = new ArrayList<>();
		ArrayList<String> header = getHeader();
		rows.add(header);

		String[] filenames = { "mergedResults.csv" };
		for (String filename : filenames) {
			rows.addAll(processMatrix(hashcodes, CSVReader.readCSV("\t", filename, false)));
		}
		CSVWriter.writeCSV("merged.csv", "\t", rows);
		System.lineSeparator();
	}

	/**
	 * (from the matrix data) - removes even columns (their titles) and rows with previously existing hashcodes. Does not copy the hashcode column.
	 * 
	 * @param hashcodes
	 * @param rows
	 * @param inputRows
	 * @return
	 * @throws IOException
	 * @throws NoSuchFileException
	 */
	private static ArrayList<ArrayList<String>> processMatrix(HashMap<String, StringGraph> hashcodes, List<List<String>> inputRows) throws NoSuchFileException, IOException {
		ArrayList<ArrayList<String>> outPutRows = new ArrayList<>();
		boolean firstRow = true;
		for (List<String> inputRow : inputRows) {
			if (firstRow) {
				firstRow = false;
				continue;
			}
			// boolean evenColumn = true;
			ArrayList<String> outputRow = new ArrayList<>();
			int columnCounter = 0;
			String hashcode = null;
			StringGraph graph = null;
			for (String cell : inputRow) {
				// if (!evenColumn) {
				// }
				switch (columnCounter) {
				case 7:
					graph = GraphReadWrite.readCSVFromString(cell);
					String hash = new BigInteger(graph.accurateHashCode()).toString(16);
					hashcode = hash;
					outputRow.add(cell);
					break;
				// case 9:
				// hashcode = cell;
				// break;
				default:
					outputRow.add(cell);
					break;
				}
				columnCounter++;
				// evenColumn = !evenColumn;
			}
			if (hashcode != null) {
				StringGraph existingGraph = hashcodes.get(hashcode);
				if (existingGraph == null) {
					outPutRows.add(outputRow);
					hashcodes.put(hashcode, graph);
				} else {
					System.err.println("repeated hashcode:\t" + hashcode + "\t" + graph.toString() + "\t" + existingGraph);
				}
			}
		}
		return outPutRows;
	}

	private static ArrayList<String> getHeader() {
		ArrayList<String> header = new ArrayList<>();
		header.add("time");
		header.add("relationTypes");
		header.add("relationTypesStd");
		header.add("cycles");
		header.add("patternEdges");
		header.add("patternVertices");
		header.add("matches");
		header.add("query");
		header.add("pattern");
		// header.add("hashcode");
		return header;
	}

}
