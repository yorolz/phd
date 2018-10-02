package textual;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import graph.GraphAlgorithms;
import graph.GraphReadWrite;
import graph.StringEdge;
import graph.StringGraph;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class TextGenerator {
	public static String capitalize(final String word) {
		return Character.toUpperCase(word.charAt(0)) + word.substring(1);
	}

	public static boolean usePronoun(ArrayList<String> subjectHistory, String subject) {
		if (subjectHistory == null || subject == null) {
			throw new NullPointerException();
		}

		if (subjectHistory.isEmpty()) {
			return false;
		}

		int lastOccurrences = countNumberOfLastOccurrences(subjectHistory, subject);
		if (lastOccurrences == 0)
			return false;

		return (lastOccurrences % 2 == 0);
	}

	public static boolean subjectEqualToLast(ArrayList<String> subjectHistory, String subject) {
		if (subjectHistory == null || subject == null) {
			throw new NullPointerException();
		}

		if (subjectHistory.isEmpty()) {
			return false;
		}

		int historySize = subjectHistory.size();
		String lastSubject = subjectHistory.get(historySize - 1);
		if (lastSubject.equals(subject)) {
			return true;
		}
		return false;
	}

	public static int countNumberOfLastOccurrences(ArrayList<String> subjectHistory, String subject) {
		if (subjectHistory == null || subject == null) {
			throw new NullPointerException();
		}

		if (subjectHistory.isEmpty()) {
			return 0;
		}

		int size = subjectHistory.size();
		int counter = 0;
		for (int i = size - 1; i >= 0; i--) {
			String s = subjectHistory.get(i);
			if (!s.equals(subject)) {
				return counter;
			}
			counter++;
		}
		return size;
	}

	private static void describeEdge(StringEdge edge, String referenceConcept, Object2IntOpenHashMap<String> wordClasses, StringGraph graph, HashSet<StringEdge> closedSet,
			int followingRelations, boolean firstRelationGroup, RandomGenerator random, ArrayList<String> subjectHistory) {
		String relation = edge.getLabel().toLowerCase();
		String source = edge.getSource();
		String target = edge.getTarget();

		String sourceConcept = GraphAlgorithms.getConceptWithoutNamespace(source);
		String targetConcept = GraphAlgorithms.getConceptWithoutNamespace(target);
		sourceConcept = sourceConcept.replace('_', ' ');
		targetConcept = targetConcept.replace('_', ' ');

		switch (relation) {
		case "ability": {
			// if that ability has an associated purpose, use it also
			Set<StringEdge> propertyEdges = graph.incomingEdgesOf(target, "purpose");
			// we assume it only has one isa
			if (firstRelationGroup) {
				if (usePronoun(subjectHistory, sourceConcept)) { // last subject equal to current
					System.out.printf("It ");
				} else { // current subject is different than last
					System.out.printf("The %s ", sourceConcept);
				}
			}

			StringEdge propertyEdge = null;
			if (!propertyEdges.isEmpty()) {
				propertyEdge = propertyEdges.iterator().next();
			}
			if (propertyEdge == null || closedSet.contains(propertyEdge)) { // WITHOUT PROPERTY
				if (firstRelationGroup) {
					if (random.nextBoolean()) {
						System.out.printf("has the ability to %s", targetConcept);
					} else {
						System.out.printf("is able to %s", targetConcept);
					}
				} else {
					System.out.printf("%s", targetConcept);
				}
			} else { // WITH PROPERTY
				closedSet.add(propertyEdge); // obviously this is not to be described again
				String propertyConcept = GraphAlgorithms.getConceptWithoutNamespace(propertyEdge.getSource()).toLowerCase();
				System.out.printf("%s using its %s", targetConcept, propertyConcept);
			}

			if (followingRelations == 0) {
				System.out.printf(".");
			} else if (followingRelations == 1) {
				System.out.printf(" and");
			} else {
				System.out.printf(",");
			}

			subjectHistory.add(sourceConcept);
		}
			break;

		case "carrier": {
			String article = getArticle(targetConcept, true);
			System.out.printf("The %s carries %s %s.", sourceConcept, article, targetConcept);

			subjectHistory.add(sourceConcept);
		}
			break;

		case "color": {
			if (usePronoun(subjectHistory, sourceConcept)) { // last subject equal to current
				System.out.printf("It ");
			} else { // current subject is different than last
				System.out.printf("The %s ", sourceConcept);
			}
			System.out.printf("is %s.", targetConcept);

			subjectHistory.add(sourceConcept);
		}
			break;

		case "eat": {
			if (usePronoun(subjectHistory, sourceConcept)) { // last subject equal to current
				System.out.printf("It ");
			} else { // current subject is different than last
				System.out.printf("The %s ", sourceConcept);
			}
			System.out.printf("eats %s.", targetConcept);

			subjectHistory.add(sourceConcept);
		}
			break;

		case "existence": {
			// if that existence has an associated isa, use it also
			Set<StringEdge> isaEdges = graph.outgoingEdgesOf(target, "isa");
			StringEdge isaEdge = null;
			if (!isaEdges.isEmpty()) {
				isaEdge = isaEdges.iterator().next();
			}

			if (isaEdges.isEmpty() || closedSet.contains(isaEdge)) {
				if (firstRelationGroup) {
					if (usePronoun(subjectHistory, sourceConcept)) { // last subject equal to current
						System.out.printf("It exists in the %s", targetConcept);
					} else { // current subject is different than last
						System.out.printf("The %s exists in the %s", sourceConcept, targetConcept);
					}
				} else {
					System.out.printf("in the %s", targetConcept);
				}
			} else {
				// we assume it only has one isa
				String isaC = GraphAlgorithms.getConceptWithoutNamespace(isaEdge.getTarget());
				closedSet.add(isaEdge); // obviously this is not to be described again
				if (firstRelationGroup) {
					isaC = isaC.replace('_', ' ');
					if (usePronoun(subjectHistory, sourceConcept)) { // last subject equal to current
						System.out.printf("It exists in the %s (a %s)", targetConcept, isaC);
					} else { // current subject is different than last
						System.out.printf("The %s exists in the %s (a %s)", sourceConcept, targetConcept, isaC);
					}
				} else {
					System.out.printf("TODO: %s %s %s.", sourceConcept, targetConcept, isaC);
				}
			}
			if (followingRelations == 0) {
				System.out.printf(".");
			} else if (followingRelations == 1) {
				System.out.printf(" and");
			} else {
				System.out.printf(",");
			}

			subjectHistory.add(sourceConcept);
		}
			break;

		case "isa": {
			// if that isa has an associated isa, use it also
			Set<StringEdge> propertyEdges = graph.outgoingEdgesOf(target, "isa");
			StringEdge propertyEdge = null;
			
			if (firstRelationGroup) {
				if (usePronoun(subjectHistory, sourceConcept)) { // last subject equal to current
					System.out.printf("It is ");
				} else { // current subject is different than last
					System.out.printf("The %s is ", sourceConcept);
				}
			}
			System.out.printf("%s %s", getArticle(targetConcept, false), targetConcept);
			
			if (!propertyEdges.isEmpty()) {
				propertyEdge = propertyEdges.iterator().next();
			}

			if (propertyEdges.isEmpty() || closedSet.contains(propertyEdge)) {
				if (followingRelations == 0) {
					System.out.printf(".");
				} else if (followingRelations == 1) {
					System.out.printf(" and");
				} else {
					System.out.printf(",");
				}
			} else {
				// we assume it only has one isa
				String propertyConcept = GraphAlgorithms.getConceptWithoutNamespace(propertyEdge.getTarget());
				closedSet.add(propertyEdge); // obviously this is not to be described again
				if (followingRelations == 0) {
					System.out.printf(" and %s %s.", getArticle(propertyConcept, false), propertyConcept);
				} else if (followingRelations == 1) {
					System.out.printf(", %s %s and", getArticle(propertyConcept, false), propertyConcept);
				} else {
					System.out.printf(", %s %s,", getArticle(propertyConcept, false), propertyConcept);
				}
			}

			subjectHistory.add(sourceConcept);
		}
			break;

		case "material": {
			System.out.printf("The %s is made of %s.", sourceConcept, targetConcept);

			subjectHistory.add(sourceConcept);
		}
			break;

		case "purpose": {
			String preposition = getConceptPreposition(wordClasses, targetConcept);

			if (firstRelationGroup) {
				if (usePronoun(subjectHistory, sourceConcept)) { // last subject equal to current
					System.out.printf("It is used ");
				} else { // current subject is different than last
					System.out.printf("The %s is used ", sourceConcept);
				}
			}

			System.out.printf("%s %s", preposition, targetConcept);

			if (followingRelations == 0) {
				System.out.printf(".");
			} else if (followingRelations == 1) {
				System.out.printf(" and");
			} else {
				System.out.printf(",");
			}

			subjectHistory.add(sourceConcept);
		}
			break;

		case "adj": { // adjective / property of something
			if (firstRelationGroup) {
				String verb = "is";
				if (usePronoun(subjectHistory, sourceConcept)) { // last subject equal to current
					String pronoun = "it";
					System.out.printf("%s %s ", capitalize(pronoun), verb);
				} else { // current subject is different than last
					System.out.printf("The %s %s ", sourceConcept, verb);
				}
			}

			System.out.printf("%s", targetConcept);

			if (followingRelations == 0) {
				System.out.printf(".");
			} else if (followingRelations == 1) {
				System.out.printf(" and");
			} else {
				System.out.printf(",");
			}

			subjectHistory.add(sourceConcept);
		}
			break;

		case "pw": {
			// ESTE E O MELHOR TEMPLATE
			// if that pw has an associated quantity, use it also
			// we assume it only has one isa
			Set<StringEdge> propertyEdges0 = graph.outgoingEdgesOf(source, "quantity");
			Set<StringEdge> propertyEdges1 = graph.outgoingEdgesOf(source, "purpose");
			
			String subject = targetConcept;
			boolean usePronoun = usePronoun(subjectHistory, subject); // beginning of sentence
			
			if (firstRelationGroup) {
				if (usePronoun) { // last subject equal to current
					String pronoun = "it";
					System.out.printf("%s has ", capitalize(pronoun));
				} else { // current subject is different than last
					System.out.printf("The %s has ", subject);
				}
			} else {
				if (!subjectEqualToLast(subjectHistory, subject)) { // current subject is different than last
					System.out.printf("the %s has ", subject);
				}
			}

			StringEdge propertyEdge0 = null;
			StringEdge propertyEdge1 = null;
			if (!propertyEdges0.isEmpty()) {
				propertyEdge0 = propertyEdges0.iterator().next();
			}
			if (!propertyEdges1.isEmpty()) {
				propertyEdge1 = propertyEdges1.iterator().next();
			}

			boolean hasQuantity = (propertyEdge0 != null && !closedSet.contains(propertyEdge0)); // quantity
			boolean hasPurpose = (propertyEdge1 != null && !closedSet.contains(propertyEdge1)); // purpose
			boolean objectIsPlural = false;

			if (!hasQuantity) { // no explicit quantity
				System.out.printf("%s %s", getArticle(sourceConcept, false), sourceConcept);
			} else { // explicit quantity
				closedSet.add(propertyEdge0);
				String quantity = GraphAlgorithms.getConceptWithoutNamespace(propertyEdge0.getTarget()).toLowerCase();
				if (quantity.equals("1") || quantity.equals("one")) { // SINGULAR
					System.out.printf("one %s", sourceConcept);
				} else {
					String quantityIntegerText = integerToWord(quantity); // try number in textual description
					if (quantityIntegerText != null) {
						System.out.printf("%s %s", quantityIntegerText, plural(sourceConcept));
					} else {
						System.out.printf("%s %s", quantity, plural(sourceConcept));
					}
					objectIsPlural = true;
				}
			}

			if (hasPurpose) {
				closedSet.add(propertyEdge1);
				String purpose = GraphAlgorithms.getConceptWithoutNamespace(propertyEdge1.getTarget()).toLowerCase();
				String preposition = getConceptPreposition(wordClasses, purpose);
				String verbl;
				if (objectIsPlural) {
					verbl = "are";
				} else {
					verbl = "is";
				}
				// TODO: "it" to "he" "she" etc.
				System.out.printf(" which %s used %s %s", verbl, preposition, purpose);
			}

			if (followingRelations == 0) {
				System.out.printf(".");
			} else if (followingRelations == 1) {
				System.out.printf(" and");
			} else {
				System.out.printf(",");
			}

			subjectHistory.add(targetConcept);
		}
			break;

		case "quantity": {
			System.out.print("TODO:"); // dont like "they"
			// check who owns this quantity
			Set<StringEdge> pw = graph.outgoingEdgesOf(source, "pw");
			if (pw.isEmpty()) {
				System.out.printf("They have %s %s.", targetConcept, plural(sourceConcept));
			} else {
				// we assume this quantity only has one owner
				StringEdge pwEdge = pw.iterator().next();
				String ownerC = GraphAlgorithms.getConceptWithoutNamespace(pwEdge.getTarget());
				System.out.printf("%s have %s %s.", capitalize(plural(ownerC)), targetConcept, plural(sourceConcept));
				closedSet.add(pwEdge); // obviously this is not to be described again
			}

			subjectHistory.add("~~TODO~");
		}
			break;

		case "ride": {
			String subject = capitalize(plural(sourceConcept));
			if (usePronoun(subjectHistory, sourceConcept)) { // last subject equal to current
				System.out.printf("They ");
			} else { // current subject is different than last
				System.out.printf("%s ", subject);
			}
			System.out.printf("ride %s.", plural(targetConcept));

			subjectHistory.add(sourceConcept);
		}
			break;

		case "size": {
			if (usePronoun(subjectHistory, sourceConcept)) { // last subject equal to current
				System.out.printf("It ");
			} else { // current subject is different than last
				System.out.printf("The %s ", sourceConcept);
			}
			System.out.printf("is %s.", targetConcept);

			subjectHistory.add(sourceConcept);
		}
			break;

		case "sound": {
			if (usePronoun(subjectHistory, sourceConcept)) { // last subject equal to current
				System.out.printf("It ");
			} else { // current subject is different than last
				System.out.printf("The %s ", sourceConcept);
			}
			System.out.printf("%s.", plural(targetConcept));

			subjectHistory.add(sourceConcept);
		}
			break;

		case "taxonomicq": {
			if (usePronoun(subjectHistory, sourceConcept)) { // last subject equal to current
				System.out.printf("It ");
			} else { // current subject is different than last
				System.out.printf("The %s ", sourceConcept);
			}
			System.out.printf("is a %s.", targetConcept);

			subjectHistory.add(sourceConcept);
		}
			break;
		}
	}

	public static String getArticle(String concept, boolean definite) {
		if (definite) {
			return "the";
		} else {
			if (startsWithVowel(concept)) {
				return "an";
			} else {
				return "a";
			}
		}
	}

	private static ArrayList<StringEdge> filterEdges(HashSet<StringEdge> closedSet, ArrayList<StringEdge> set) {
		ArrayList<StringEdge> newset = new ArrayList<>();
		for (StringEdge e : set) {
			if (!closedSet.contains(e)) {
				newset.add(e);
			}
		}
		return newset;
	}

	private static String getConceptPreposition(Object2IntOpenHashMap<String> wordClasses, String targetConcept) {
		int tcClass = wordClasses.getInt(targetConcept);
		if (tcClass == WordClass.NOUN) {
			return "as";
		} else if (tcClass == WordClass.VERB) {
			return "to";
		}
		return "(preposition)";
	}

	public static String integerToWord(int number) {
		return EnglishNumberToWords.convert(number);
	}

	public static String integerToWord(String number) {
		try {
			int n = Integer.parseInt(number);
			return integerToWord(n);
		} catch (NumberFormatException e) {
		}
		return null; // not parsable
	}

	public static boolean isNumeric(String str) {
		try {
			Double.parseDouble(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	public static boolean isVowel(char firstChar) {
		switch (firstChar) {
		case 'a':
		case 'e':
		case 'i':
		case 'o':
		case 'u':
			return true;
		}
		return false;
	}

	public static void main(String[] args) throws IOException {
		new TextGenerator();
	}

	private static void moveEdgesToBeginning(ArrayList<StringEdge> edges, String edgeLabel) {
		ArrayList<StringEdge> tempEdges = new ArrayList<>();
		for (StringEdge edge : edges) {
			if (edge.getLabel().equalsIgnoreCase(edgeLabel)) {
				tempEdges.add(edge);
			}
		}
		edges.removeAll(tempEdges);
		edges.addAll(0, tempEdges);
	}

	public static String plural(final String word) {
		// TODO: when the word itself is already plural
		char lastChar = word.charAt(word.length() - 1);
		if (lastChar == 's') { // OOPS
			return word + "es";
		} else if (lastChar == 'y') {
			return word.substring(0, word.length() - 1) + "ies";
		} else {
			return word + "s";
		}
	}

	private void replaceEdgeLabels() {
		StringGraph newGraph = new StringGraph();
		for (StringEdge edge : graph.edgeSet()) {
			String label = edge.getLabel();
			String source = edge.getSource();
			String target = edge.getTarget();
			switch (label) {
			case "material": // kind of cheating but eh...
				String targetC = GraphAlgorithms.getConceptWithoutNamespace(target);
				edge = new StringEdge(source, "made of " + targetC, "adj");
				break;
			case "color":
			case "colour":
			case "size":
				edge = new StringEdge(source, target, "adj"); // adjective
				break;
			case "taxonomicq":
				edge = new StringEdge(source, target, "isa");
				break;
			case "motion_process":
				edge = new StringEdge(source, target, "ability");
				break;
			}
			newGraph.addEdge(edge);
		}
		graph = newGraph;
	}

	private static void sortEdges(ArrayList<StringEdge> edges) {
		if (edges.isEmpty()) {
			return;
		}
		edges.sort(new Comparator<StringEdge>() {
			@Override
			public int compare(StringEdge o1, StringEdge o2) {
				return o1.getLabel().compareTo(o2.getLabel());
			}
		});
		moveEdgesToBeginning(edges, "carrier");
		moveEdgesToBeginning(edges, "ride");
		moveEdgesToBeginning(edges, "sound");
		moveEdgesToBeginning(edges, "eat");
		moveEdgesToBeginning(edges, "purpose");
		moveEdgesToBeginning(edges, "ability");
		moveEdgesToBeginning(edges, "pw");
		moveEdgesToBeginning(edges, "existence");
		moveEdgesToBeginning(edges, "adj");
		moveEdgesToBeginning(edges, "taxonomicq");
		moveEdgesToBeginning(edges, "isa");
	}

	public static boolean startsWithVowel(String word) {
		char firstChar = word.toLowerCase().charAt(0);
		return isVowel(firstChar);
	}

	private HashSet<StringEdge> closedSet = new HashSet<>();

	/**
	 * edge to concept which expanded the edge
	 */
	private HashMap<StringEdge, String> edgeExploredFromConcept = new HashMap<>();

	private StringGraph graph;

	private Object2IntOpenHashMap<String> wordClasses;

	public TextGenerator() throws IOException {
		RandomGenerator random = new MersenneTwister(0);
		wordClasses = WordClass.readFile("wordclasses.txt");
		// graph = GraphReadWrite.readAutoDetect("C:\\Desktop\\bitbucket\\semantic graphs\\horse bird from francisco (original)\\horse_bird_from_book_with_namespaces.csv");
		graph = GraphReadWrite.readAutoDetect("C:\\Desktop\\bitbucket\\semantic graphs\\horse bird from francisco (original)\\bird_from_book.csv");
		replaceEdgeLabels(); // taxonomicq -> isa, etc.
		removeSymmetricRelations(); // ride /remove carrier

		// instead could be the most significant concept according to some principle
		String concept = GraphAlgorithms.getHighestDegreeVertex(graph.getVertexSet(), graph);

		ArrayList<StringEdge> openSet = visitConcept(concept);
		ArrayList<StringEdge> nextOpenSet = new ArrayList<>();

		ArrayList<String> subjectHistory = new ArrayList<>();

		do {
			for (int i = 0; i < openSet.size(); i++) {
				StringEdge edge = openSet.get(i);
				if (!closedSet.contains(edge)) {
					concept = edgeExploredFromConcept.get(edge);
					int followingRelations = countFollowingRelationsOfGroup(openSet, i);
					boolean first = firstRelationOfGroup(openSet, i);
					describeEdge(edge, concept, wordClasses, graph, closedSet, followingRelations, first, random, subjectHistory);
					System.out.printf(" ");
					// System.out.printf(" [%s] S:%s\n", edge.getLabel(), currentSubject.getValue());
					closedSet.add(edge);
					String opposingConcept = edge.getOppositeOf(concept);
					ArrayList<StringEdge> neighboringEdges = visitConcept(opposingConcept);
					nextOpenSet.addAll(neighboringEdges);
				}
			}
			System.out.println();
			ArrayList<StringEdge> filteredNewOpenSet = filterEdges(closedSet, nextOpenSet);
			openSet = filteredNewOpenSet;
		} while (!openSet.isEmpty());
		return;
	}

	private void removeSymmetricRelations() {
		ArrayList<StringEdge> remove = new ArrayList<>();
		for (StringEdge edge : graph.edgeSet()) {
			String source = edge.getSource();
			String target = edge.getTarget();
			String label = edge.getLabel();
			if (label.equals("ride")) {
				Set<StringEdge> symm = graph.getDirectedEdgesWithRelationEqualTo(target, source, "carrier"); // check symmetric carrier
				if (!symm.isEmpty()) { // riding implies carrying
					remove.addAll(symm);
				}
			}
		}
		for (StringEdge edgeToDelete : remove) {
			graph.removeEdge(edgeToDelete);
		}
	}

	private int countFollowingRelationsOfGroup(ArrayList<StringEdge> list, int i0) {
		StringEdge edge0 = list.get(i0);
		String label0 = edge0.getLabel();
		for (int i1 = i0 + 1; i1 < list.size(); i1++) {
			StringEdge edge1 = list.get(i1);
			String label1 = edge1.getLabel();
			if (!label1.equals(label0)) {
				return i1 - i0 - 1;
			}
		}
		return list.size() - i0 - 1;
	}

	private boolean firstRelationOfGroup(ArrayList<StringEdge> list, int i0) {
		if (i0 == 0) {
			return true;
		}
		StringEdge edge = list.get(i0);
		String label = edge.getLabel();

		StringEdge previousEdge = list.get(i0 - 1);
		String previousLabel = previousEdge.getLabel();

		boolean first = !previousLabel.equals(label);

		return first;
	}

	/**
	 * visits the concept, retrieving the sorted connected edges
	 * 
	 * @param graph
	 * @param edgeCameFromConcept
	 * @param exploredConcepts
	 * @param concept
	 * @return
	 */
	private ArrayList<StringEdge> visitConcept(String concept) {
		ArrayList<StringEdge> edgeList = new ArrayList<>(graph.edgesOf(concept));

		for (StringEdge edge : edgeList) {
			edgeExploredFromConcept.put(edge, concept);
		}

		sortEdges(edgeList);
		return edgeList;
	}
}
