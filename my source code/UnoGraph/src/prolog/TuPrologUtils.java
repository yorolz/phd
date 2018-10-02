package prolog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.math3.util.CombinatoricsUtils;

import alice.tuprolog.InvalidTheoryException;
import alice.tuprolog.Struct;
import alice.tuprolog.Term;
import alice.tuprolog.Theory;
import graph.StringEdge;
import graph.StringGraph;

public class TuPrologUtils {
	public static String correctStringProlog(String string) {
		boolean isVariable = string.matches("[A-Z]+.*");
		if (isVariable | string.equals("_")) {
			return string;
		}
		if (!string.startsWith("'")) {
			string = "'" + string;
		}
		if (!string.endsWith("'")) {
			string = string + "'";
		}
		return string;
	}

	public static String correctStringsForProlog(String line) {
		// get every sequence of text starting with a lowercase and not followed by (
		// ignore above if starting and ending with '
		int i = 0;
		// pw(horse/mane,horse/horse)
		HashMap<String, String> replacements = new HashMap<>();
		while (true) {
			int i0 = line.indexOf('(', i);
			if (i0 < 0)
				break;
			int i1 = line.indexOf(',', i0 + 1);
			if (i1 < 0)
				break;
			int i2 = line.indexOf(')', i1 + 1);
			if (i2 < 0)
				break;
			String tok0 = line.substring(i0 + 1, i1).trim();
			String tok1 = line.substring(i1 + 1, i2).trim();
			replacements.put(tok0, correctStringProlog(tok0));
			replacements.put(tok1, correctStringProlog(tok1));
			i = i2 + 1;
		}
		for (String source : replacements.keySet()) {
			String target = replacements.get(source);
			if (!target.equals(source)) {
				line = line.replace(source, target);
			}
		}
		return line;
	}

	public static Struct createClause(String relation, String source, String target) {
		Struct clause = new Struct(relation, new Struct(source), new Struct(target));
		return clause;
	}

	public static ArrayList<ArrayList<String>> readPatternFrames(String filename) throws Exception {
		ArrayList<ArrayList<String>> framesOfPredicates = new ArrayList<ArrayList<String>>();
		// read the file
		BufferedReader br = new BufferedReader(new FileReader(new File(filename)));
		while (br.ready()) {
			String completeSentence = "";
			while (br.ready()) {
				String line = br.readLine().trim();
				if (line.isEmpty())
					continue;
				if (line.startsWith("%"))
					continue;
				completeSentence += line;
				if (line.endsWith(".")) {
					break;
				}
			}
			if (completeSentence.isEmpty())
				continue;
			completeSentence = completeSentence.substring(0, completeSentence.indexOf('.'));
			ArrayList<String> predicates = splitQueryAsString(completeSentence);
			framesOfPredicates.add(predicates);
			// for (int i = predicates.size(); i >= 0; i--) {
			// Iterator<int[]> combinationsIterator = CombinatoricsUtils.combinationsIterator(predicates.size(), i);
			// while (combinationsIterator.hasNext()) {
			// int[] next = combinationsIterator.next();
			// for (int index : next) {
			// String pred = predicates.get(index);
			// }
			// System.currentTimeMillis();
			// }
			// }

			// completeSentence = correctStringsForProlog(completeSentence);
			// Term term = Term.createTerm(completeSentence);
			// patternFrames.add(term);
		}
		// store the precompiled structs and return them
		br.close();
		return framesOfPredicates;
	}

	public static ArrayList<Term> readClauseFile(String filename) throws Exception {
		ArrayList<Term> patternFrames = new ArrayList<>();
		// read the file
		BufferedReader br = new BufferedReader(new FileReader(new File(filename)));
		while (br.ready()) {
			String completeSentence = "";
			while (br.ready()) {
				String line = br.readLine().trim();
				if (line.isEmpty())
					continue;
				if (line.startsWith("%"))
					continue;
				completeSentence += line;
				if (line.endsWith(".")) {
					break;
				}
			}
			if (completeSentence.isEmpty())
				continue;
			completeSentence = completeSentence.substring(0, completeSentence.indexOf('.'));
			// completeSentence = correctStringsForProlog(completeSentence);
			Term term = Term.createTerm(completeSentence);
			patternFrames.add(term);
		}
		// store the precompiled structs and return them
		br.close();
		return patternFrames;
	}

	public static Theory createTheoryFromFile(String filename) throws IOException, InvalidTheoryException {
		BufferedReader br = new BufferedReader(new FileReader(filename));
		StringBuilder sb = new StringBuilder();
		String line = br.readLine();

		while (line != null) {
			line = line.trim();
			if (!line.startsWith("%") && !line.isEmpty()) {
				sb.append(line);
				sb.append(System.lineSeparator());
			}
			line = br.readLine();
		}
		String everything = sb.toString();
		Theory theory = new Theory(everything);
		br.close();
		return theory;
	}

	public static Theory createTheoryFromStringGraph(StringGraph graph) throws InvalidTheoryException {
		Struct clauses = new Struct();
		for (StringEdge edge : graph.edgeSet()) {
			String relation = edge.getLabel();
			String source = edge.getSource();
			String target = edge.getTarget();
			Struct clause = createClause(relation, source, target);
			clauses.append(clause);
		}
		Theory theory = new Theory(clauses);
		return theory;
	}

	public static String prepareStringForProlog(String str) {
		if (str.startsWith("'") && str.endsWith("'")) {
			return str;
		} else {
			if (str.startsWith("'"))
				return str + "'";
			if (str.endsWith("'"))
				return "'" + str;
			return "'" + str + "'";
		}
	}

	public static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

	private static ArrayList<String> splitQueryAsString(String completeSentence) {
		ArrayList<String> predicates = new ArrayList<>();
		String[] split = completeSentence.split("\\)");
		for (String token : split) {
			token = token.trim();
			int commai = token.indexOf(',');
			if (commai == 0) { // startswith
				token = token.substring(commai + 1).trim();
			}
			token = token + ")";
			predicates.add(token);
		}
		return predicates;
	}

	public static ArrayList<PatternFrameCombination> createPatternFrames(String filename) throws Exception {
		ArrayList<PatternFrameCombination> frames = new ArrayList<>();
		// list of frames (each frame is a list of ANDed predicates)
		ArrayList<ArrayList<String>> framesOfPredicates = readPatternFrames(filename);
		// for each frame
		for (ArrayList<String> frame : framesOfPredicates) {
			PatternFrameCombination pfc = new PatternFrameCombination();
			frames.add(pfc);
			// create combinations of predicates from the largest set of elements to the smallest
			int n = frame.size();
			for (int k = n; k > 0; k--) {
				Iterator<int[]> combinationsIterator = CombinatoricsUtils.combinationsIterator(n, k);
				while (combinationsIterator.hasNext()) {
					int[] comboIndices = combinationsIterator.next();
					// with the indices, create an AND composite as a string
					String clause = createFrameClause(frame, comboIndices);
					// compile the string as a prolog term
					Term term = Term.createTerm(clause);
					// store in a corresponding structure, within the specified level (k)
					pfc.addClause(term, k);
				}
			}
		}
		return frames;
	}

	private static String createFrameClause(ArrayList<String> frame, int[] comboIndices) {
		String clause = "";
		int n = comboIndices.length;
		for (int i = 0; i < n; i++) {
			int index = comboIndices[i];
			String predicate = frame.get(index);
			clause = clause + predicate;
			if (i < (n - 1)) {
				clause = clause + ",";
			}
		}
		// clause = clause + ".";
		return clause;
	}

}
