import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 * This class create mappings (metaphors) between two domains (ontologies). For instance, receiving as input the ontology of king arthur and starwars it states that: sword is the
 * light_saber of king_arthur with a strength of 0.75
 * 
 * @version: 0.5
 * @author Rui P. Costa (http://student.dei.uc.pt/~racosta)
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class jMapper {

	// Parameteres
	private double analogyTax = 0.5; // default: 0.5
	private double lowest_analogyTax = 0.3; // default: 0.3
	private int depth = 3; // default: 3
	private boolean print = false;
	private boolean strongers = true;
	private Graph tenor;
	private Graph vehicle;
	private Graph generic;
	public String tenorDomain;
	public String vehicleDomain;

	public void setAnalogyTax(double analogyTax) {
		this.analogyTax = analogyTax;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public void setLowest_analogyTax(double lowest_analogyTax) {
		this.lowest_analogyTax = lowest_analogyTax;
	}

	public void setPrint(boolean print) {
		this.print = print;
	}

	public void setStrongers(boolean strongers) {
		this.strongers = strongers;
	}

	/**
	 * After setting the ontologies this method have to be called in order to obtain the mappings. added a timer to prevent this method from hanging execution - jcfgonc@gmail.com
	 *
	 * @return ArrayList with the Metaphors
	 */
	ArrayList<Metaphor> runMapper() {
		ArrayList mappings = new ArrayList();
		ArrayList mappingsF = new ArrayList();
		Hashtable mappingsHash; // Used in recursive analogy
		ArrayList candidates;
		int it = 0;

		if (print)
			System.out.println(tenorDomain.toUpperCase() + " vs " + vehicleDomain.toUpperCase());

		while (true) {
			if (print)
				System.out.println(1);
			tenor.generatePrimaryCandidates(vehicle, generic);
			boolean timerExpired = tenor.isTimerExpired();
			if (timerExpired)
				break;

			// printAllCandidates(tenor);
			candidates = getAllCandidates(tenor);

			if (print)
				System.out.println(2);
			if (it == 0) {
				double i = 0.005;
				int j = 0;
				while (mappings.size() == 0 && i < analogyTax - lowest_analogyTax) {
					mappings = getMappingsLargerThanTaxAndX(candidates, tenor, vehicle, generic, analogyTax - i, depth - j);
					i *= 1.5;
					j = (int) (i * 10) % 10;
					if (print)
						System.out.printf("%f %d ~%d\n", i, j, mappings.size());
				}
			} else
				mappings = getMappingsLargerThanTaxAndX(candidates, tenor, vehicle, generic, analogyTax, depth);

			if (strongers)
				refineSearch(mappings);

			if (print)
				System.out.println(3);
			mappings = joinArrayLists(mappingsF, mappings, it);
			if (mappings.size() == 0)
				break;

			if (print)
				System.out.println("***** " + it + " recursive analogy ***** (" + mappings.size() + " new)");

			if (print)
				printArrayList(mappings);

			if (print)
				System.out.println(4);
			mappingsHash = buildMappingHastable(mappings);
			substituteByAnalogy(mappingsHash, tenor);

			if (print)
				System.out.println(5);
			it++;
		}
		if (mappingsF.size() > 0) {
			if (strongers)
				refineSearch(mappingsF);

			if (print)
				System.out.println("Final mappings (" + mappingsF.size() + "):");

			// refineSearchEquals(mappingsF); Comment if you want the cases with equal strength

			if (print)
				printArrayList_XisTheYofZ(mappingsF);
		}

		return convert2MetaphorArrayList(mappingsF);
	}

	/**
	 * Used in jMapper GUI
	 * 
	 * @param output
	 * @return ArrayList with the Mappings
	 */
	ArrayList<Mapping> run_mapper4GUI(jMapperGui output) {
		String s = "";
		ArrayList mappings = new ArrayList();
		ArrayList mappingsF = new ArrayList();
		Hashtable mappingsHash; // Use in recursive analogy
		ArrayList candidates;
		int it = 0;

		s += ("<p align=\"center\"><b><font color=\"red\">" + tenorDomain.toUpperCase() + " vs " + vehicleDomain.toUpperCase() + "</font></b></p>");

		while (true) {
			tenor.generatePrimaryCandidates(vehicle, generic);

			// printAllCandidates(tenor);
			candidates = getAllCandidates(tenor);

			// candidates=applyVealeRules(tenor, vehicle, generic); <-- Old way that does the same of getAllCandidates
			// eliminaReps(candidates); <-- This version becames very complex because its recursive, but this isnt essencial so I commented it!

			if (it == 0) {
				double i = 0.0;
				int j = 0;
				while (mappings.size() == 0 && i < analogyTax - lowest_analogyTax) {
					mappings = getMappingsLargerThanTaxAndX(candidates, tenor, vehicle, generic, analogyTax - i, depth - j);
					i += 0.05;
					j = (int) (i * 10) % 10;
				}
			} else
				mappings = getMappingsLargerThanTaxAndX(candidates, tenor, vehicle, generic, analogyTax, depth);

			if (strongers)
				refineSearch(mappings);

			mappings = joinArrayLists(mappingsF, mappings, it);

			if (mappings.size() == 0)
				break;

			s += "<BR><b>***** " + it + " recursive analogy ***** (" + mappings.size() + " new)</b><BR>";

			s += ArrayList2HTML(mappings);

			mappingsHash = buildMappingHastable(mappings);
			substituteByAnalogy(mappingsHash, tenor);

			it++;
		}
		if (mappingsF.size() > 0) {
			if (strongers)
				refineSearch(mappingsF);

			s += "<BR><b>Final mappings (" + mappingsF.size() + "):</b><BR>";

			// refineSearchEquals(mappingsF); // Comment if you want the cases with equal strength
			s += ArrayList_XisTheYofZ_2HTML(mappingsF);
		}

		output.appendReport(s);

		return mappingsF;
	}

	/**
	 * This is the first method that you need to call. It setup the ontologies needed.
	 * 
	 * @param genericInput,
	 *            Leave this as null (is not used at the moment)
	 * @param vehicleInput,
	 *            The target ontology
	 * @param tenorInput,
	 *            The source ontology
	 */
	public void set_graphs(ArrayList vehicleInput, ArrayList tenorInput) {
		tenor = new Graph();
		tenorDomain = tenor.loadGraph(tenorInput);
		// tenor.printGraph();

		vehicle = new Graph();
		vehicleDomain = vehicle.loadGraph(vehicleInput);
		// vehicle.printGraph();
	}

	public void set_graphs(String vehiclePath, String tenorPath) throws IOException {
		tenor = new Graph();
		tenorDomain = tenor.loadGraphCSV(vehiclePath);

		vehicle = new Graph();
		vehicleDomain = vehicle.loadGraphCSV(tenorPath);
	}

	/**
	 * join two ArrayLists into mappingsF
	 * 
	 * @param mappingsF
	 * @param mappings
	 * @param recursive_level
	 * @return ArrayList with the new mappings
	 */
	ArrayList joinArrayLists(ArrayList<Mapping> mappingsF, ArrayList<Mapping> mappings, int recursive_level) {
		boolean control = false;
		ArrayList<Mapping> v = new ArrayList<Mapping>();
		ArrayList aux = new ArrayList(); // To save the differences
		String tmp;
		int index;

		if (mappings.size() == 0)
			return v;
		else {
			for (int i = 0; i < mappingsF.size(); i++)
				aux.add(((Pair) ((Mapping) mappingsF.get(i)).maplist.get(0)).source.concept + "/" + ((Pair) ((Mapping) mappingsF.get(i)).maplist.get(0)).target.concept);

			for (int i = 0; i < mappings.size(); i++) {
				tmp = ((Pair) ((Mapping) mappings.get(i)).maplist.get(0)).source.concept + "/" + ((Pair) ((Mapping) mappings.get(i)).maplist.get(0)).target.concept;
				if ((index = aux.indexOf(tmp)) != -1) {
					control = true;
				}
				if (!control) {
					((Pair) mappings.get(i).maplist.get(0)).setRecursive_level(recursive_level);
					mappingsF.add(mappings.get(i));
					v.add(mappings.get(i));
					aux.add(tmp);
				} else {
					if (mappings.get(i).value > mappingsF.get(index).value) {
						((Pair) mappings.get(i).maplist.get(0)).setRecursive_level(((Pair) mappingsF.get(index).maplist.get(0)).getRecursive_level());
						mappingsF.remove(index);
						mappingsF.add(index, mappings.get(i));
					}
					control = false;
				}
			}
		}
		return v;
	}

	/**
	 * This method builds an ArrayList with the Mappings that passed the thredshold defined
	 * 
	 * @param candidates
	 * @param tenor
	 * @param vehicle
	 * @param generic
	 * @param tax
	 * @param x
	 * @return mappings found
	 */
	ArrayList getMappingsLargerThanTaxAndX(ArrayList candidates, Graph tenor, Graph vehicle, Graph generic, double tax, int x) {
		Mapping m;
		Hashtable mappings = new Hashtable();
		AuxMappings a;

		for (int i = 0; i < candidates.size(); i++) {
			m = getLargestSpread((Pair) candidates.get(i), tenor, vehicle, generic);

			double strength = m.value = ((m.maplist.size() - 1) / ((((Pair) candidates.get(i)).source.attributes + ((Pair) candidates.get(i)).target.attributes) / 2));
			// System.out.println("mapping strength: " + m + "\t" + strength);
			if (strength >= tax && m.maplist.size() > x) { // THRESHOLD
				// System.out.println(((Pair)candidates.get(i)).source.concept+" | "+((Pair)candidates.get(i)).target.concept+" >
				// "+(m.maplist.size()-1)+"/"+((((Pair)candidates.get(i)).source.attributes+"+"+((Pair)candidates.get(i)).target.attributes)+"/"+2));
				if (vehicle.getNode(((Pair) m.maplist.get(0)).source.concept) == null && tenor.getNode(((Pair) m.maplist.get(0)).target.concept) == null) {
					if ((a = (AuxMappings) mappings.get(((Pair) m.maplist.get(0)).source.concept)) != null) {
						// if(a.getMaxValue()<m.value){
						a.setMappings(new ArrayList());
						a.getMappings().add(m);
						a.setMaxValue(m.value);
						/*
						 * } else if(a.getMaxValue()==m.value) a.getMappings().add(m);
						 */
					} else
						mappings.put(((Pair) m.maplist.get(0)).source.concept, new AuxMappings(m, m.value));
				}
			}
		}

		return buildArrayListFromHashtable(mappings);
	}

	/*
	 * ArrayList getLargestsMappings(ArrayList candidates, Graph tenor, Graph vehicle, Graph generic) { Mapping m; ArrayList mappings = new ArrayList();
	 * 
	 * mappings.add(getLargestSpread((Pair)candidates.get(0), tenor, vehicle, generic)); //System.out.println(((Mapping)mappings.get(0)).toString());
	 * 
	 * for(int i=0; i<candidates.size(); i++) { m=getLargestSpread((Pair)candidates.get(i), tenor, vehicle, generic); //System.out.println(m.toString());
	 * if(m.maplist.size()>((Mapping)mappings.get(0)).maplist.size()){ mappings.clear(); mappings.add(m); } else if(m.maplist.size()==((Mapping)mappings.get(0)).maplist.size())
	 * mappings.add(m); } return mappings; }
	 * 
	 * ArrayList getMappingsLargerThanX(ArrayList candidates, Graph tenor, Graph vehicle, Graph generic, int x) { Mapping m; ArrayList mappings = new ArrayList();
	 * 
	 * for(int i=0; i<candidates.size(); i++) { m=getLargestSpread((Pair)candidates.get(i), tenor, vehicle, generic); //System.out.println(m.toString()); if(m.maplist.size()>x){
	 * mappings.add(m); } } return mappings; }
	 */

	ArrayList buildArrayListFromHashtable(Hashtable h) {
		Enumeration e = h.keys();
		AuxMappings a;
		ArrayList mappings = new ArrayList();
		ArrayList aux;

		while (e.hasMoreElements()) {
			a = (AuxMappings) h.get(e.nextElement());
			aux = a.getMappings();
			for (int i = 0; i < aux.size(); i++)
				mappings.add(aux.get(i));
		}

		return mappings;
	}

	/**
	 * To remove all the several analogies from a single concept
	 * 
	 * @param v
	 */
	void refineSearch(ArrayList v) {
		Pair p, p2;

		for (int i = 0; i < v.size() - 1; i++) {
			p = (Pair) ((Mapping) v.get(i)).maplist.get(0);
			for (int j = i + 1; j < v.size(); j++) {
				p2 = (Pair) ((Mapping) v.get(j)).maplist.get(0);
				if ((p.source.concept.equals(p2.source.concept) || p.target.concept.equals(p2.target.concept) || p.source.concept.equals(p2.target.concept)
						|| p.target.concept.equals(p2.source.concept)) && ((Mapping) v.get(i)).value != ((Mapping) v.get(j)).value) {
					// System.out.println(p.source.concept+"|"+p.target.concept+" - "+p2.source.concept+"|"+p2.target.concept);
					// System.out.println(((Mapping)v.get(i)).value+" - "+((Mapping)v.get(j)).value);
					if (((Mapping) v.get(i)).value > ((Mapping) v.get(j)).value)
						v.remove(j);
					else
						v.remove(i);
					refineSearch(v);
					break;
				}
			}
		}
	}

	/**
	 * Remove several analogies that has the same strength and lead to a equal concept
	 * 
	 * @param v
	 */
	void refineSearchEquals(ArrayList v) {
		Pair p, p2;

		for (int i = 0; i < v.size() - 1; i++) {
			p = (Pair) ((Mapping) v.get(i)).maplist.get(0);
			for (int j = i + 1; j < v.size(); j++) {
				p2 = (Pair) ((Mapping) v.get(j)).maplist.get(0);
				if ((p.source.concept.equals(p2.source.concept) || p.target.concept.equals(p2.target.concept) || p.source.concept.equals(p2.target.concept)
						|| p.target.concept.equals(p2.source.concept))) {
					// System.out.println(p.source.concept+"|"+p2.source.concept);
					if (((Mapping) v.get(i)).value > ((Mapping) v.get(j)).value)
						v.remove(j);
					else
						v.remove(i);
					refineSearchEquals(v);
					break;
				}
			}
		}
	}

	Mapping getLargestSpread(Pair initial, Graph tenor, Graph vehicle, Graph generic) {
		Mapping map = new Mapping();
		GraphNode source = initial.source, target = initial.target;
		Hashtable relSource = source.relations;
		Enumeration eSource = relSource.keys();
		String relS, tmp;
		StringTokenizer st;

		map.maplist.add(new Pair(source, target));

		while (eSource.hasMoreElements()) {
			relS = "" + eSource.nextElement();
			st = new StringTokenizer(relSource.get(relS) + "", ";");
			while (st.hasMoreElements())
				if (target.hasRelation(relS, tmp = st.nextToken()))
					map.maplist.add(new Pair(new GraphNode(tmp, source.domain), new GraphNode(tmp, target.domain)));
		}

		return map;
	}

	void substituteByAnalogy(Hashtable mapping, Graph tenor) {
		Enumeration tenorE = tenor.getEnumeration();
		GraphNode gn;

		while (tenorE.hasMoreElements()) {
			gn = (GraphNode) tenorE.nextElement();
			ArrayList gnDest = gn.getDestinations();
			for (int i = 0; i < gnDest.size(); i++) {
				if (mapping.containsKey("" + gnDest.get(i)))
					gn.substituteDest(gn.hasDest("" + gnDest.get(i)), "" + gnDest.get(i), "" + mapping.get("" + gnDest.get(i)));
			}
		}
	}

	/*
	 * IS NOT USED static ArrayList applyVealeRules(Graph tenor, Graph vehicle, Graph generic) { ArrayList v;
	 * 
	 * v=applyTriangulation(tenor, vehicle, generic);
	 * 
	 * return v;
	 * 
	 * }
	 * 
	 * IS NOT USED static ArrayList applyTriangulation(Graph tenor, Graph vehicle, Graph generic) { ArrayList v=new ArrayList(); Enumeration tenum=tenor.getEnumeration(); GraphNode
	 * tnode,vnode;
	 * 
	 * while(tenum.hasMoreElements()) { tnode=(GraphNode)tenum.nextElement();
	 * 
	 * if((vnode=vehicle.getNode(tnode.concept))!=null) v.add(new Pair(tnode, vnode));
	 * 
	 * for(int i=0; i<tnode.candidates.size(); i++) v.add(new Pair(tnode, (GraphNode)tnode.candidates.get(i)));
	 * 
	 * }
	 * 
	 * return v; }
	 */

	void printArrayList(ArrayList v) {
		for (int i = 0; i < v.size(); i++)
			System.out.println(v.get(i));
		System.out.println();

	}

	String ArrayList2HTML(ArrayList v) {
		String s = "";
		for (int i = 0; i < v.size(); i++)
			s += ((Mapping) v.get(i)).toHTML() + "<BR>";

		return s;
	}

	Hashtable buildMappingHastable(ArrayList v) {
		Hashtable h = new Hashtable();

		for (int i = 0; i < v.size(); i++)
			h.put(((Pair) ((Mapping) v.get(i)).maplist.get(0)).source.concept, ((Pair) ((Mapping) v.get(i)).maplist.get(0)).target.concept);

		return h;
	}

	void printAllCandidates(Graph g) {
		Enumeration e = g.getEnumeration();
		GraphNode node;
		while (e.hasMoreElements()) {
			node = (GraphNode) e.nextElement();
			for (int i = 0; i < node.candidates.size(); i++)
				System.out.println(node.concept + "/" + ((GraphNode) (node.candidates.get(i))).concept);
			// System.out.println();
		}

	}

	ArrayList getAllCandidates(Graph g) {
		ArrayList v = new ArrayList();
		Enumeration e = g.getEnumeration();
		GraphNode node;
		while (e.hasMoreElements()) {
			node = (GraphNode) e.nextElement();
			for (int i = 0; i < node.candidates.size(); i++)
				v.add(new Pair(node, ((GraphNode) (node.candidates.get(i)))));
		}
		return v;
	}

	void printArrayList_XisTheYofZ(ArrayList v) {
		for (int i = 0; i < v.size(); i++)
			System.out.println(((Mapping) v.get(i)).xIsTheYofZ() + " > " + ((Mapping) v.get(i)).value);
		System.out.println();

	}

	String ArrayList_XisTheYofZ_2HTML(ArrayList v) {
		String s = "";
		for (int i = 0; i < v.size(); i++)
			s += ((Mapping) v.get(i)).xIsTheYofZ() + " > " + ((Mapping) v.get(i)).value + "<BR>";
		return s;

	}

	/*
	 * Return an ArrayList with the Metaphors:
	 */
	ArrayList<Metaphor> convert2MetaphorArrayList(ArrayList v) {
		ArrayList<Metaphor> f = new ArrayList<Metaphor>();
		for (int i = 0; i < v.size(); i++)
			f.add(new Metaphor(((Pair) ((Mapping) v.get(i)).maplist.get(0)).source.concept, ((Pair) ((Mapping) v.get(i)).maplist.get(0)).target.concept, ((Mapping) v.get(i)).value,
					((Pair) ((Mapping) v.get(i)).maplist.get(0)).getRecursive_level()));
		return f;
	}

	/*
	 * Return a ArrayList with Strings with the following struct: king_arthur/luckskywalker/0.57
	 */
	ArrayList convert2ArrayList(ArrayList v) {
		ArrayList f = new ArrayList();
		for (int i = 0; i < v.size(); i++)
			f.add(((Pair) ((Mapping) v.get(i)).maplist.get(0)).source.concept + "/" + ((Pair) ((Mapping) v.get(i)).maplist.get(0)).target.concept + "/"
					+ ((Mapping) v.get(i)).value);
		return f;
	}

	void printHashtable_XisTheYofZ(Hashtable v) {
		Enumeration e = v.keys();
		while (e.hasMoreElements())
			System.out.println(((Mapping) e.nextElement()).xIsTheYofZ());
		System.out.println();

	}

	void printHashtable(Hashtable v) {
		Enumeration e = v.elements();
		for (int i = 0; i < v.size(); i++)
			System.out.println(e.nextElement());
		System.out.println();

	}

	/*
	 * void eliminaReps(ArrayList v) //Old version < Didnt work proprely { for(int i=0; i<v.size()-1;i++) for(int j=i+1; j<v.size();j++)
	 * if(((Pair)(v.get(i))).equals((Pair)v.get(j))) v.removeElementAt(j); }
	 */

	void eliminaReps(ArrayList v) {
		for (int i = 0; i < v.size() - 1; i++) {
			for (int j = i + 1; j < v.size(); j++) {
				if (((Pair) (v.get(i))).equals((Pair) v.get(j))) {
					v.remove(j);
					eliminaReps(v);
					break;
				}
			}
		}
	}

}

@SuppressWarnings({ "rawtypes", "unchecked" })
class Graph {
	private static final Charset CHARSET_UTF_8 = Charset.forName("UTF-8");
	private static final Charset CHARSET_Windows_1252 = Charset.forName("Windows-1252");
	Hashtable nodes = new Hashtable();
	String domain;
	private boolean timerExpired = false;

	public static int indexOf(char cref, char[] charBuffer, int from, int to) {
		for (int i = from; i < to; i++) {
			char c = charBuffer[i];
			if (cref == c) {
				return i;
			}
		}
		return -1;
	}

	public String loadGraphCSV(String filename) throws IOException {
		File file = new File(filename);
		String domain = file.getName();
		domain = domain.substring(0, domain.indexOf(".csv"));

		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		while (br.ready()) {
			String line = br.readLine();
			if (line == null)
				break;
			// ignore empty lines
			if (line.length() == 0)
				continue;

			byte ptext[] = line.getBytes(CHARSET_Windows_1252);
			String lineConverted = new String(ptext, CHARSET_UTF_8);

			String[] tokens = lineConverted.split(",");
			int ntokens = tokens.length;
			if (ntokens < 3)
				continue;
			String source = tokens[0];
			String relation = tokens[1];
			String target = tokens[2];

			GraphNode node = (GraphNode) nodes.get(source);
			if (node == null) {
				node = new GraphNode(source, domain);
				nodes.put(source, node);
			}

			node.addEdge(relation, target);
		}
		br.close();
		fr.close();
		return domain;
	}

	String loadGraph(ArrayList text) {
		StringTokenizer st;
		int N = text.size();
		String source, relation, target;
		GraphNode node;

		for (int i = 0; i < N; i++) {
			Object line = text.get(i);
			st = new StringTokenizer(line + "", "(, ).*");

			st.nextToken(); // get rid of rel
			domain = st.nextToken(); // domain name
			source = st.nextToken();
			relation = st.nextToken();
			target = st.nextToken();
			node = (GraphNode) nodes.get(source);
			// System.out.println("rel("+domain+", "+or+", "+rel+", "+dest+").");
			if (node == null) {
				node = new GraphNode(source, domain);
				nodes.put(source, node);
			}

			node.addEdge(relation, target);

		}

		return domain;
	}

	void printGraph() {
		Enumeration en = nodes.elements();
		System.out.println("\nConcept Map of " + domain);
		while (en.hasMoreElements())
			System.out.println(en.nextElement());

	}

	Enumeration getEnumeration() {
		return nodes.elements();

	}

	GraphNode getNode(String name) {
		return (GraphNode) nodes.get(name);
	}

	/**
	 * added a timer to prevent this method from hanging execution - jcfgonc@gmail.com
	 * 
	 * @param vehicle
	 * @param generic
	 */
	void generatePrimaryCandidates(Graph vehicle, Graph generic) {
		Ticker ticker = new Ticker();
		ArrayList<GraphNode> values = new ArrayList<>(nodes.values());
		Collections.shuffle(values); // shuffle because of timeout, allows different results when run multiple times
		for (GraphNode graphNode : values) {
			graphNode.findCandidates(vehicle, generic);
			double elapsedTime = ticker.getElapsedTime();
			if (elapsedTime > JMapperExperiments.TIMEOUT_SECONDS) {
				System.err.println("--- generatePrimaryCandidates() timeout! ");
				timerExpired = true;
				break;
			}
		}

		// Enumeration enu = nodes.elements();
		// while (enu.hasMoreElements()) {
		// GraphNode graphNode = (GraphNode) (enu.nextElement());
		// graphNode.findCandidates(vehicle, generic);
		// double elapsedTime = ticker.getElapsedTime();
		// if (elapsedTime > jMapperUsageExample.TIMEOUT_SECONDS) {
		// System.err.println("--- timeout!");
		// break;
		// }
		// }
	}

	public boolean isTimerExpired() {
		return timerExpired;
	}

}

@SuppressWarnings({ "rawtypes", "unchecked" })
class AuxMappings {
	private ArrayList mappings;
	private double maxValue;

	public AuxMappings(Mapping m, double v) {
		mappings = new ArrayList();
		mappings.add(m);
		maxValue = v;
	}

	public ArrayList getMappings() {
		return mappings;
	}

	public void setMappings(ArrayList mappings) {
		this.mappings = mappings;
	}

	public double getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
	}

}
