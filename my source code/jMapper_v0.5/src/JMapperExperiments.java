import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

import structures.CSVWriter;

public class JMapperExperiments {
	public static final double TIMEOUT_SECONDS = 30 * 60;

	public static void main(String args[]) throws IOException, InterruptedException {
		for (int i = 0; i < 10; i++) {
			runMapperBatch();
		}
	}

	private static void runMapperBatch() throws InterruptedException, IOException {
		CSVWriter csvW = new CSVWriter();
		csvW.setHeader("tau", "bridge", "time", "pairs");

		String baseFolder = "..\\DomainSpotter\\generated inputspaces";
		String[] tauFolders = { "tau0.0", "tau0.01", "tau0.1", "tau0.25" };
		String[] bridges = { "beaver_rat", "big_board", "exercise_physiology", "flower_person", "hiram_ohio", "horror_fiction", "motorcycle_drag_race", "redwatch", "vascularity",
				"venography" };

		for (String tauFolder : tauFolders) {
			for (String bridge : bridges) {

				String filenameTemplate = baseFolder + "\\" + tauFolder + "\\" + tauFolder + "_" + bridge + "_is";

				// these are the guys which timeout
				if (filenameTemplate.contains("tau0.1_beaver_rat") || // ------
						filenameTemplate.contains("tau0.1_big_board") || // ---
						filenameTemplate.contains("tau0.1_vascularity") || // ---
						filenameTemplate.contains("tau0.25_beaver_rat") || // ---
						filenameTemplate.contains("tau0.25_big_board") || // ---
						filenameTemplate.contains("tau0.25_hiram_ohio") || // ---
						filenameTemplate.contains("tau0.25_vascularity") || false) {
					continue;
				} else {
					// continue;
				}

				String filename0 = filenameTemplate + "0.csv";
				String filename1 = filenameTemplate + "1.csv";

				runMapper(filename0, filename1, csvW);
				runMapper(filename1, filename0, csvW); // swap 1 to 0 and vice versa because this mapper is strange and gives different results

				System.gc();
				Thread.sleep(30000);

				System.out.println("-------------------------------------------------------");
			}
		}
		csvW.close();
	}

	/**
	 * (C) jcfgonc@gmail.com
	 * 
	 * @param tenorPath
	 * @param vehiclePath
	 * @param csvW
	 * @return
	 * @throws IOException
	 */
	private static ArrayList<Metaphor> runMapper(String tenorPath, String vehiclePath, CSVWriter csvW) throws IOException {
		// ..\\DomainSpotter\\generated inputspaces\\tau0.1\\tau0.1_beaver_rat_is0.csv
		String domain = new File(tenorPath).getName();
		domain = domain.substring(0, domain.indexOf(".csv"));
		// domain = tau0.1_beaver_rat_is0
		String tau = domain.substring(3, domain.indexOf('_'));
		String bridge = domain.substring(domain.indexOf('_') + 1, domain.length() - 4);

		// 3º SET THE PARAMETERS (if not defined the default value kept)
		jMapper jm = new jMapper();
		jm.setAnalogyTax(0.5);
		jm.setLowest_analogyTax(0.0);
		jm.setDepth(0);
		jm.setStrongers(false);

		// 4º SET THE TWO ONTOLOGIES
		jm.set_graphs(vehiclePath, tenorPath);

		// 5º SET THE TWO ONTOLOGIES
		System.out.println(jm.tenorDomain + " VS " + jm.vehicleDomain);
		Ticker ticker = new Ticker();
		ArrayList<Metaphor> metaphors = jm.runMapper();
		double dt = ticker.getTimeDeltaLastCall();
		System.out.printf("d(t)=%f\n", dt);

		// printMetaphors(metaphors);
		int pairs = metaphors.size();
		System.out.printf("Got %d concept pairs\n", pairs);

		// csvW.setHeader("tau", "bridge", "time", "pairs");
		csvW.addLine(tau, bridge, Double.toString(dt), Integer.toString(pairs));
		csvW.flush();

		return metaphors;
	}

	public static void mainOld(String args[]) throws FileNotFoundException {
		System.out.println(System.getProperty("user.dir"));

		// 1º READ SOURCE ONTOLOGY (TENOR)
		String tenorPath = "ontologies/classics/king_arthur.txt";
		String ste = readFile(tenorPath);
		ArrayList<String> te = arrangeString(ste);
		String domainTen = new StringTokenizer(tenorPath, ".", false).nextToken();

		// 2º READ TARGET ONTOLOGY (VEHICLE)
		String vehiclePath = "ontologies/classics/starwars.txt";
		String sve = readFile(vehiclePath);
		ArrayList<String> ve = arrangeString(sve);
		String domainVei = new StringTokenizer(vehiclePath, ".", false).nextToken();

		// 3º SET THE PARAMETERS (if not defined the default value kept)
		jMapper jm = new jMapper();
		jm.setAnalogyTax(0.5);
		jm.setLowest_analogyTax(0.3);
		jm.setDepth(3);
		jm.setStrongers(true);

		// 4º SET THE TWO ONTOLOGIES
		jm.set_graphs(ve, te);

		// 5º SET THE TWO ONTOLOGIES
		System.out.println(domainTen + " VS " + domainVei);
		ArrayList<Metaphor> metaphors = jm.runMapper();

		System.out.println("Final mappings (" + metaphors.size() + "):");
		printMetaphors(metaphors);
	}

	static void printMetaphors(ArrayList<Metaphor> a) {
		for (int i = 0; i < a.size(); i++)
			System.out.println(a.get(i));
	}

	static String readFile(String file) throws FileNotFoundException {
		String res = "";
		Scanner sc = null;

		sc = new Scanner(new File(file));

		while (sc.hasNextLine()) {
			res = res + sc.nextLine();
		}
		sc.close();
		return res;
	}

	static ArrayList<String> arrangeString(String input) {
		ArrayList<String> strings = new ArrayList<String>();

		String[] tokens = input.split("\\.");

		for (int i = 0; i < tokens.length; i++)
			strings.add(tokens[i]);

		return strings;
	}
}