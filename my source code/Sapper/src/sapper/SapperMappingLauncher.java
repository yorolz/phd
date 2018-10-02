package sapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import graph.GraphAlgorithms;
import graph.GraphReadWrite;
import graph.StringGraph;

public class SapperMappingLauncher {

	public static void main(String[] args) throws Exception {
		// Well44497b random = new Well44497b(System.nanoTime());
		StringGraph graph = new StringGraph();
		GraphReadWrite.readTGF("C:\\Desktop\\bitbucket\\semantic graphs\\composer_general.tgf", graph);

		Set<DormantBridge> db = runSapperRules(graph);
		// rateDormantBridges(db, graph);
	}

	private static void rateDormantBridges(Set<DormantBridge> dbset, StringGraph graph) {
		for (DormantBridge db : dbset) {
			String concept0 = db.getConcept0();
			String concept1 = db.getConcept1();
			int hops = GraphAlgorithms.getDistance(concept0, concept1, graph);
			System.out.println(concept0 + "\t" + concept1 + "\t" + hops);
		}
	}

	private static Set<DormantBridge> runSapperRules(StringGraph graph) throws InterruptedException {
		Set<DormantBridge> oldDormantBridges = new HashSet<>(); // list of known DB, iterated version, indexed by the threads
		HashSet<DormantBridge> newDormantBridges = SapperMappingRules.applyTriangulationRule(graph); 
		// run this while there are new discovered BN (#new dormant bridges >0)
		do {
			// copy the newly discovered DB into the list of known DB
			oldDormantBridges.addAll(newDormantBridges);
			newDormantBridges.clear();
			List<DormantBridge> oldDBasList = new ArrayList<>(oldDormantBridges);
			SapperMappingRules.applySquaringRule(graph, newDormantBridges, oldDBasList);
		} while (newDormantBridges.size() > 0);
		oldDormantBridges.addAll(newDormantBridges);
		newDormantBridges.clear();
		return oldDormantBridges;
	}
}
