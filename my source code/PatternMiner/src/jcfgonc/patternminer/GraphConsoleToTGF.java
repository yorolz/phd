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
		int status = 0;
		String graphID = null;
		while (true) {
			String nextLine = s.nextLine();
			switch (status) {
			// first type the graph ID
			case 0:
				graphID = nextLine.trim();
				break;
			case 1:
				// X0,field,X4; X5,field,X4; X3,field,X4; X2,field,X4; X7,field,X4; X6,field,X4; X4,atlocation,X1;
				String graphCSV = nextLine.replace("; ", "\r\n");
				try {
					StringGraph graph = new StringGraph();
					StringReader sr = new StringReader(graphCSV);
					GraphReadWrite.readCSV(sr, graph);
					sr.close();

					GraphReadWrite.writeTGF(graphID + ".tgf", graph);
				} catch (NoSuchFileException e) {
				} catch (IOException e) {
				}
				break;
			}
			status++;
			if (status > 1) {
				status = 0;
			}
		}
		// s.close();
	}

}
