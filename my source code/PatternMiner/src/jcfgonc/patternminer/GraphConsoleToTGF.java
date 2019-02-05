package jcfgonc.patternminer;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.NoSuchFileException;
import java.util.Scanner;

import graph.GraphReadWrite;
import graph.StringGraph;

public class GraphConsoleToTGF {

	public static void main(String[] args) {
		@SuppressWarnings("resource")
		Scanner s = new Scanner(System.in);
		int counter = 0;
		while (true) {
			String nextLine = s.nextLine();
			// X0,field,X4; X5,field,X4; X3,field,X4; X2,field,X4; X7,field,X4; X6,field,X4; X4,atlocation,X1;
			String graphCSV = nextLine.replace("; ", "\r\n");
			graphCSV = graphCSV.replace(";", "\r\n");
			try {
				StringGraph graph = new StringGraph();
				StringReader sr = new StringReader(graphCSV);
				GraphReadWrite.readCSV(sr, graph);
				sr.close();

				String filename = counter + ".tgf";
				System.out.println("saving to " + filename);
				GraphReadWrite.writeTGF(filename, graph);
				counter++;
			} catch (NoSuchFileException e) {
			} catch (IOException e) {
			}
		}
		// s.close();
	}

}
