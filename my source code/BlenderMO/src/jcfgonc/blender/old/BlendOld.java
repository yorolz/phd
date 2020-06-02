package jcfgonc.blender.old;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

//import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.util.FastMath;

import graph.GraphAlgorithms;
import graph.StringEdge;
import graph.StringGraph;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import jcfgonc.blender.logic.CompressionUtils;
import jcfgonc.blender.logic.LogicUtils;
import jcfgonc.blender.structures.ConceptPair;
import jcfgonc.blender.structures.Mapping;
import structures.MapOfSet;
import structures.ObjectIndex;

public class BlendOld {
	private static List<Mapping> analogies;
	private static StringGraph inputSpace;
	private static MapOfSet<String, StringEdge> nameSpaceEdges;
	private static StringGraph pegasusSpace;
	/**
	 * Mapping from namespace to contained concepts.
	 */
	private static MapOfSet<String, String> setOfNameSpaces;
	private static Set<ConceptPair<String>> allMappings;
	private static ArrayList<String> namespaces;
	private static ArrayList<ArrayList<HashMap<String, String>>> referenceFrameToSolutionToBindings;

	private static Set<String> intersectConcepts(Set<String> concepts0, Set<String> concepts1) {
		// used by genetic crossover
		HashSet<String> intersection = new HashSet<>();
		for (String concept0 : concepts0) {
			if (concepts1.contains(concept0)) {
				intersection.add(concept0);
			}
			if (concept0.contains("|")) {
				ArrayList<String> tokens = GraphAlgorithms.splitConceptWithBar(concept0);
				String concept00 = tokens.get(0);
				String concept01 = tokens.get(1);
				// reverse
				String tmp = concept01 + "|" + concept00;
				if (concepts1.contains(tmp)) {
					intersection.add(tmp);
				}
				if (concepts1.contains(concept00)) {
					intersection.add(concept0);
					// intersection.add(concept00);
				}
				if (concepts1.contains(concept01)) {
					intersection.add(concept0);
					// intersection.add(concept01);
				}
			}

		}
		return intersection;
	}

	HashMap<String, MutableInt> conceptCounter;
	double inputSpacesSizeStdDev = 0;
	int interSpaceEdges = 0;
	IntArrayList islands = new IntArrayList(0);
	private Set<ConceptPair<String>> mappings;
	private double novelty = 0;
	double oneLevelEntropy = 0;
	public StringGraph outputSpace;
	RandomGenerator randomGenerator;
	private HashMap<String, String> scoreMap;
	double topologyMean = 0;
	double topologyStdDev = 0;
	double twoLevelEntropy = 0;
	private double usefulness = 0;
	private double deltaFrameScore;
	private Object2DoubleOpenHashMap<String> edgeFrameScores;
	private Object2DoubleOpenHashMap<String> conceptFrameScores;
	private DoubleArrayList patternFramesScores;
	private double patternFrameScore = 0;
	private double compressionBreadth = 0;
	private double compressionDepth = 0;
	private double[] score;
	private double compression = 0;
	private double compressionPattern = 0;

	private DoubleArrayList compareEdgesOf(String concept, boolean incoming) {
		// get its edges
		Set<StringEdge> bcEdges;
		if (incoming)
			bcEdges = outputSpace.incomingEdgesOf(concept);
		else
			bcEdges = outputSpace.outgoingEdgesOf(concept);

		HashMap<String, Integer> osRelations = countRelations(bcEdges);
		HashMap<String, Integer> isRelations = new HashMap<>();

		// get matching node(s) in the input space
		if (concept.contains("|")) {
			ArrayList<String> tok = GraphAlgorithms.splitConceptWithBar(concept);
			if (incoming) {
				isRelations.putAll(countRelations(inputSpace.incomingEdgesOf(tok.get(0))));
				isRelations.putAll(countRelations(inputSpace.incomingEdgesOf(tok.get(1))));
			} else {
				isRelations.putAll(countRelations(inputSpace.outgoingEdgesOf(tok.get(0))));
				isRelations.putAll(countRelations(inputSpace.outgoingEdgesOf(tok.get(1))));
			}
		} else {
			if (incoming) {
				isRelations.putAll(countRelations(inputSpace.incomingEdgesOf(concept)));
			} else {
				isRelations.putAll(countRelations(inputSpace.outgoingEdgesOf(concept)));
			}
		}
		// count edges / relations and compare between the blend and the inputspace
		DoubleArrayList currentRatios = compareRelations(osRelations, isRelations);
		return currentRatios;
	}

	private DoubleArrayList compareRelations(HashMap<String, Integer> numerator, HashMap<String, Integer> denominator) {
		DoubleArrayList ratios = new DoubleArrayList();
		for (String dKey : denominator.keySet()) {
			double deCount = denominator.get(dKey).intValue();
			Integer numCount = numerator.get(dKey);
			double ratio;
			if (numCount != null) {
				int nv = numCount.intValue();
				if (nv < deCount)
					ratio = nv / deCount;
				else
					ratio = 1;
			} else
				ratio = 0;
			ratios.add(ratio);
		}
		return ratios;
	}

	private void countConceptNameSpace(HashMap<String, MutableInt> oneLevelCount, String source) {
		for (String namespace : namespaces) {
			Set<String> nset = setOfNameSpaces.get(namespace);
			if (nset.contains(source)) {
				MutableInt counter = oneLevelCount.get(namespace);
				if (counter == null) {
					counter = new MutableInt(0);
					oneLevelCount.put(namespace, counter);
				}
				counter.increment();
			}
		}
	}

	private HashMap<String, Integer> countRelations(Set<StringEdge> edges) {
		HashMap<String, Integer> counter = new HashMap<>();
		for (StringEdge edge : edges) {
			String relation = edge.getLabel();
			Integer relationCount = counter.get(relation);
			if (relationCount == null) {
				relationCount = Integer.valueOf(1);
				counter.put(relation, relationCount);
			} else {
				relationCount = Integer.valueOf(relationCount.intValue() + 1);
				counter.put(relation, relationCount);
			}
		}
		return counter;
	}

	private void createBlend() {
		outputSpace = new StringGraph();

		// iterate through a random amount of mappings
		if (!mappings.isEmpty()) {
			int numberOfMappings = randomGenerator.nextInt(mappings.size());
			if (numberOfMappings == 0) {
				numberOfMappings = 1;
			}
			Set<ConceptPair<String>> mappingSubSet = GraphAlgorithms.randomSubSet(mappings, numberOfMappings, randomGenerator);
			for (ConceptPair<String> mapping : mappingSubSet) {
				// create or extract a concept from a mapping
				String newConcept = LogicUtils.generateNewConcept(mapping, randomGenerator);
				String oldConcept0 = mapping.getLeftConcept();
				String oldConcept1 = mapping.getRightConcept();
				if (newConcept == null) {
					continue;
				}
				// get edges of the newconcept
				Set<StringEdge> edgesOf = edgesOfBlend(inputSpace, newConcept);
				int numberOfEdges = randomGenerator.nextInt(edgesOf.size());
				if (numberOfEdges == 0) {
					numberOfEdges = 1;
				}
				// insert a random amount of edges around the given concept
				Set<StringEdge> edgesOfSubSet = GraphAlgorithms.randomSubSet(edgesOf, numberOfEdges, randomGenerator);
				for (StringEdge edge : edgesOfSubSet) {
					if (edge.containsConcept(oldConcept0)) {
						StringEdge newEdge = edge.replaceSourceOrTarget(oldConcept0, newConcept);
						outputSpace.addEdge(newEdge);
					} else if (edge.containsConcept(oldConcept1)) {
						StringEdge newEdge = edge.replaceSourceOrTarget(oldConcept1, newConcept);
						outputSpace.addEdge(newEdge);
					}
				}
			}
		}
	}

	private Set<StringEdge> edgesOfBlend(StringGraph graph, String concept) {
		if (concept.contains("|")) {
			ArrayList<String> split = GraphAlgorithms.splitConceptWithBar(concept);
			String concept0 = split.get(0);
			String concept1 = split.get(1);
			HashSet<StringEdge> neighborhood = new HashSet<>();
			neighborhood.addAll(graph.edgesOf(concept0));
			neighborhood.addAll(graph.edgesOf(concept1));
			return neighborhood;
		} else {
			Set<StringEdge> neighborhood = graph.edgesOf(concept);
			return neighborhood;
		}
	}

	@SuppressWarnings("unused")
	public double[] getScore() {
		scoreMap.clear();

		// scoreNovelty();
		// scoreUsefulness();
		scoreFrames();

		// scoreTopology();
		scoreOneLevelEntropy();
		scoreTwoLevelEntropy();
		scoreInputSpaceRatio();
		// scoreBlendComponents();
		// scoreInterspaceEdges();
		// scoreNumberOfBlendConcepts();
		// scoreCompressionBreadth();
		// scoreCompressionDepth();
		// scoreCompressionRandom();
		// scoreCompressionPattern(true, 2);

		int i = 0;
		this.score = new double[1];

		int validNumberOfIslands = islands.size() > 1 ? islands.size() : 1;
		double horseConcepts = conceptFrameScores.getDouble("horse");
		double horseEdges = edgeFrameScores.getDouble("horse");
		double birdConcepts = conceptFrameScores.getDouble("bird");
		double birdEdges = edgeFrameScores.getDouble("bird");

		score[i++] = patternFrameScore * 10 + deltaFrameScore * 1 + oneLevelEntropy * 0.00 - twoLevelEntropy * 0.01
				+ outputSpace.numberOfVertices() * 0.00000001 - (validNumberOfIslands - 1) * 2 + interSpaceEdges * 0.0;

		// score[i++] = patternFrameScore * 3 + deltaFrameScore * 1 - compressionPattern * 0.01 + outputSpace.numberOfVertices() * 0.0001
		// - (validNumberOfIslands - 1) * 3;
		// score[i++] = oneLevelEntropy;
		// score[i++] = twoLevelEntropy;
		// // score[i++] = islands != null ? islands.size() : 0;
		// score[i++] = interSpaceEdges;
		// score[i++] = deltaFrameScore;
		// score[i++] = topologyMean;
		// score[i++] = topologyStdDev;
		return score;
	}

	private void scoreNumberOfBlendConcepts() {
		int blendConceptCount = 0;
		for (String concept : outputSpace.getVertexSet()) {
			boolean c = concept.contains("|");
			if (c) {
				blendConceptCount++;
			}
		}
		scoreMap.put("blendConceptCount", Double.toString(blendConceptCount));
	}

	public String getScoreForEntry(String key) {
		return scoreMap.get(key);
	}

	public Set<String> getScoreMapKeySet() {
		return scoreMap.keySet();
	}

	private double getTwoLevelTransitionsEntropy(boolean transitionOut0, boolean transitionOut1) {
		// two level counter
		HashMap<ImmutablePair<String, String>, MutableInt> twoLevelCount = new HashMap<>();
		Set<StringEdge> edgeSet = outputSpace.edgeSet();
		for (StringEdge currentEdge : edgeSet) {
			String cLabel = currentEdge.getLabel();

			String neigh;
			if (transitionOut0) {
				neigh = currentEdge.getTarget();
			} else {
				neigh = currentEdge.getSource();
			}

			Set<StringEdge> neighbs2;
			if (transitionOut1) {
				neighbs2 = outputSpace.outgoingEdgesOf(neigh);
			} else {
				neighbs2 = outputSpace.incomingEdgesOf(neigh);
			}

			for (StringEdge nextEdge : neighbs2) {
				if (nextEdge.equals(currentEdge))
					continue;

				String nLabel = nextEdge.getLabel();
				ImmutablePair<String, String> key = new ImmutablePair<String, String>(cLabel, nLabel);
				MutableInt counter = twoLevelCount.get(key);
				if (counter == null) {
					counter = new MutableInt(0);
					twoLevelCount.put(key, counter);
				}
				counter.increment();
			}
		}

		// one level counter
		HashMap<String, MutableInt> oneLevelCounter = new HashMap<>();
		double oneTotal = 0;
		for (ImmutablePair<String, String> transition : twoLevelCount.keySet()) {
			String left = transition.getLeft();
			MutableInt counter = oneLevelCounter.get(left);
			if (counter == null) {
				counter = new MutableInt(0);
				oneLevelCounter.put(left, counter);
			}
			int trans2count = twoLevelCount.get(transition).intValue();
			counter.add(trans2count);
			oneTotal += trans2count;
		}

		// calculate entropy
		// not sure if correctly calculated
		double totalEntropy = 0;
		for (ImmutablePair<String, String> transition : twoLevelCount.keySet()) {
			String left = transition.getLeft();
			double one = oneLevelCounter.get(left).intValue();
			double two = twoLevelCount.get(transition).intValue();
			double entropy = two / oneTotal * FastMath.log((one / oneTotal) / (two / oneTotal));
			totalEntropy += entropy;
		}
		return totalEntropy;
	}

	public double getUsefulness() {
		return usefulness;
	}

	public void initialize() {
		createBlend();
		// createBlend();
	}

	private void scoreInputSpaceRatio() {
		this.conceptCounter = new HashMap<>();

		// iterate each edge present in the output/blend space
		// for each edge's vertex
		// check and count if coming from one input space and which one
		for (StringEdge edge : outputSpace.edgeSet()) {
			String source = edge.getSource();
			if (source.contains("|")) {
				ArrayList<String> tokens = GraphAlgorithms.splitConceptWithBar(source);
				countConceptNameSpace(conceptCounter, tokens.get(0));
				countConceptNameSpace(conceptCounter, tokens.get(1));
			} else {
				countConceptNameSpace(conceptCounter, source);
			}

			String target = edge.getTarget();
			if (target.contains("|")) {
				ArrayList<String> tokens = GraphAlgorithms.splitConceptWithBar(target);
				countConceptNameSpace(conceptCounter, tokens.get(0));
				countConceptNameSpace(conceptCounter, tokens.get(1));
			} else {
				countConceptNameSpace(conceptCounter, target);
			}
		}
		DoubleArrayList counts = new DoubleArrayList();
		for (String namespace : conceptCounter.keySet()) {
			counts.add(conceptCounter.get(namespace).doubleValue());
		}

		DescriptiveStatistics ds = new DescriptiveStatistics(counts.toDoubleArray());
		this.inputSpacesSizeStdDev = ds.getStandardDeviation();
		this.scoreMap.put("inputSpacesSizeStdDev", Double.toString(inputSpacesSizeStdDev));
	}

	private void scoreNovelty() {
		Object2IntOpenHashMap<String> missingNameSpaceEdges = new Object2IntOpenHashMap<>();
		// count edges from inputspaces that are missing in the blend
		Set<StringEdge> blendEdges = outputSpace.edgeSet();
		// for each namespace in the inputspaces
		for (String namespace : namespaces) {
			// iterate through all its edges
			Set<StringEdge> isEdges = BlendOld.nameSpaceEdges.get(namespace);
			// and check if each edge is present or missing in the blend
			int count = 0;
			for (StringEdge isEdge : isEdges) {
				boolean presentInBlend = blendEdges.contains(isEdge);
				if (!presentInBlend) {
					// missingNameSpaceEdges.addTo(namespace, 1);
					count++;
				}
			}
			missingNameSpaceEdges.put(namespace, count);
		}
		// count edges in the blend that are missing in each inputspace
		for (StringEdge bedge : blendEdges) {
			for (String namespace : namespaces) {
				Set<StringEdge> isEdges = BlendOld.nameSpaceEdges.get(namespace);
				boolean presentInIS = isEdges.contains(bedge);
				if (!presentInIS) {
					missingNameSpaceEdges.addTo(namespace, 1);
				}
			}
		}
		// get the minimum amount of missing edges
		int minimum = Integer.MAX_VALUE;
		for (String nameSpace : namespaces) {
			int count = missingNameSpaceEdges.getInt(nameSpace);
			if (count < minimum) {
				minimum = count;
			}
		}
		// do the calculus as described in francisco's phd, page 146
		double distance = (double) minimum / (double) blendEdges.size();
		double novelty = distance;
		if (novelty > 1) {
			novelty = 1;
		}
		this.novelty = novelty;
		scoreMap.put("novelty", Double.toString(novelty));
	}

	private void scoreOneLevelEntropy() {
		// this is independent of direction, as there is no reference (single state)
		HashMap<String, MutableInt> oneLevelCount = new HashMap<>();
		Set<StringEdge> edgeSet = outputSpace.edgeSet();
		int totalCount = 0;
		for (StringEdge currentEdge : edgeSet) {
			String relation = currentEdge.getLabel();

			MutableInt counter = oneLevelCount.get(relation);
			if (counter == null) {
				counter = new MutableInt(0);
				oneLevelCount.put(relation, counter);
			}
			counter.increment();
			totalCount++;
		}

		// calculate entropy
		double sum = 0;
		for (String relation : oneLevelCount.keySet()) {
			double relationCount = oneLevelCount.get(relation).doubleValue();
			double relationProbability = relationCount / totalCount;
			sum += relationProbability * FastMath.log(relationProbability);
		}
		this.oneLevelEntropy = -sum;
		this.scoreMap.put("entropy1d", Double.toString(oneLevelEntropy));
	}

	private void scoreFrames() {
		// check for pattern frames

		// calculate delta frames score

		// divago's concept and edge frames
		{
			// eh...
		}
	}

	/**
	 * Returns a list (per frame) where each element are the differences (in normalized ratios - higher ratio = higher difference) between the
	 * reference and the blend
	 * 
	 * @param frameToSolutionToBindings
	 * @return
	 */
	private ArrayList<DoubleArrayList> compareDeltaFrameBindings(ArrayList<ArrayList<HashMap<String, String>>> frameToSolutionToBindings) {
		ArrayList<DoubleArrayList> blendDeltaNovelty = new ArrayList<>();
		// for each frame
		for (int frameIndex = 0; frameIndex < referenceFrameToSolutionToBindings.size(); frameIndex++) {
			DoubleArrayList currentFrameBlendDeltaNovelty = new DoubleArrayList();
			blendDeltaNovelty.add(currentFrameBlendDeltaNovelty);
			// frame in reference
			ArrayList<HashMap<String, String>> frameR = referenceFrameToSolutionToBindings.get(frameIndex);
			// frame in blend
			ArrayList<HashMap<String, String>> frameB = frameToSolutionToBindings.get(frameIndex);

			// for each solution
			for (int solutionR = 0; solutionR < frameR.size(); solutionR++) {
				for (int solutionB = 0; solutionB < frameB.size(); solutionB++) {
					HashMap<String, String> bindingsR = frameR.get(solutionR);
					HashMap<String, String> bindingsB = frameB.get(solutionB);
					// evaluate novelty in blend by comparing the vars/bindings
					double bindingsB_Novelty = compareBindingsNovelty(bindingsR, bindingsB);
					currentFrameBlendDeltaNovelty.add(bindingsB_Novelty);
				}
			}
		}
		return blendDeltaNovelty;
	}

	private double compareBindingsNovelty(HashMap<String, String> bindingsR, HashMap<String, String> bindingsB) {
		int difCounter = 0;
		// vars in both bindings are supposed to be equal (if existing in the blend)
		Set<String> varsR = bindingsR.keySet();
		Set<String> varsB = bindingsB.keySet();
		Set<String> vars = GraphAlgorithms.union(varsB, varsB);
		for (String var : vars) {
			// check if the reference (inputspace) contains the var
			boolean varsRcontains = varsR.contains(var);
			// check if the blend contains the var
			boolean varsBcontains = varsB.contains(var);
			// if the reference does not have the var and the blend has it, the blend has something new
			if (!varsRcontains && varsBcontains) {
				difCounter++;
				// System.out.println("INPUTSPACE DOES NOT HAVE VAR DEFINED");
			} else if (varsRcontains && varsBcontains) {
				String valueR = bindingsR.get(var);
				String valueB = bindingsB.get(var);
				if (!valueR.equals(valueB)) {
					difCounter++;
				}
			}
		}
		int sizeVarsR = varsR.size();
		double ratio;
		if (sizeVarsR == 0) {
			ratio = 1;
		} else {
			ratio = (double) difCounter / sizeVarsR;
		}
		return ratio;
	}

	private void scoreTopology() {
		Set<String> outputSpaceConcepts = outputSpace.getVertexSet();
		DoubleArrayList ratios = new DoubleArrayList();
		if (outputSpaceConcepts.isEmpty()) {
			ratios.add(0);
		} else {
			// for each concept in the blend
			for (String concept : outputSpaceConcepts) {
				DoubleArrayList incomingRatios = compareEdgesOf(concept, true);
				DoubleArrayList outgoingRatios = compareEdgesOf(concept, false);
				ratios.addAll(incomingRatios);
				ratios.addAll(outgoingRatios);
			}
		}

		DescriptiveStatistics ds = new DescriptiveStatistics(ratios.toDoubleArray());
		this.topologyMean = ds.getMean();
		this.topologyStdDev = ds.getStandardDeviation();
		this.scoreMap.put("topologyMean", Double.toString(topologyMean));
		this.scoreMap.put("topologyStdDev", Double.toString(topologyStdDev));
	}

	private void scoreTwoLevelEntropy() {
		double entropy00 = getTwoLevelTransitionsEntropy(false, false);
		double entropy01 = getTwoLevelTransitionsEntropy(false, true);
		double entropy10 = getTwoLevelTransitionsEntropy(true, false);
		double entropy11 = getTwoLevelTransitionsEntropy(true, true);

		this.twoLevelEntropy = entropy00 + entropy01 + entropy10 + entropy11;
		this.scoreMap.put("entropy2d", Double.toString(twoLevelEntropy));
	}

	private void scoreUsefulness() {
//well... this one is going to be problematic
	}

	private static final ByteArrayList intToByteArrayList(int value) {
		ByteArrayList arr = new ByteArrayList();
		arr.add((byte) (value >>> 24));
		arr.add((byte) (value >>> 16));
		arr.add((byte) (value >>> 8));
		arr.add((byte) (value));
		return arr;
	}

	private static int getNumberRequiredBytes(int toHoldValue) {
		if (toHoldValue < 256) {
			return 1;
		} else if (toHoldValue >= 256 && toHoldValue < 16384) {
			return 2;
		} else if (toHoldValue >= 16384 && toHoldValue < 16777216) {
			return 3;
		} else {
			return 4;
		}
	}

	void scoreCompressionRandom() {
		try {
			final int numberOfVertices = outputSpace.numberOfVertices();
			final int numberOfBytes = getNumberRequiredBytes(numberOfVertices);
			ByteArrayList bufferSource = new ByteArrayList();
			ByteArrayList bufferRelation = new ByteArrayList();
			ByteArrayList bufferTarget = new ByteArrayList();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			{
				BufferedOutputStream bos = new BufferedOutputStream(baos);
				Deflater def = new Deflater(9, true);
				DeflaterOutputStream compressedOS = new DeflaterOutputStream(bos, def);
				// BZip2CompressorOutputStream compressedOS = new BZip2CompressorOutputStream(bos);

				ArrayList<StringEdge> blendEdges = new ArrayList<>(outputSpace.edgeSet());
				// GraphAlgorithms.shuffleArrayList(blendEdges, randomGenerator);
				blendEdges.sort(null);

				ObjectIndex<String> relationLabels = new ObjectIndex<>();
				ObjectIndex<String> concepts = new ObjectIndex<>();
				for (StringEdge edge : blendEdges) {
					String relation = edge.getLabel();
					String source = edge.getSource();
					String target = edge.getTarget();
					int sourceId = concepts.addObject(source);
					int relationId = relationLabels.addObject(relation);
					int targetId = concepts.addObject(target);

					switch (numberOfBytes) {
					case 1:
						bufferSource.add((byte) sourceId);
						bufferRelation.add((byte) relationId);
						bufferTarget.add((byte) targetId);
						break;
					case 2:
					case 3:
					case 4:
						bufferSource.addAll(intToByteArrayList(sourceId));
						bufferRelation.addAll(intToByteArrayList(relationId));
						bufferTarget.addAll(intToByteArrayList(targetId));
						break;
					default:
						bufferSource.addAll(new ByteArrayList(source.getBytes()));
						bufferRelation.addAll(new ByteArrayList(relation.getBytes()));
						bufferTarget.addAll(new ByteArrayList(target.getBytes()));
						break;
					}
				}
				// compressedOS.write(bufferSource.toByteArray());
				// compressedOS.write(bufferTarget.toByteArray());
				def.finish();
				compressedOS.write(bufferRelation.toByteArray());
				compressedOS.finish();
				compressedOS.close();
				bos.close();
				def.end();
			}
			baos.close();
			int size = baos.size();

			this.compression = (double) size;
			scoreMap.put("compression", Double.toString(compression));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	void scoreCompressionBreadth() {
		IntArrayList expansion = CompressionUtils.iterativeExpansionBuilder(outputSpace, true, randomGenerator);
		int size = CompressionUtils.scoreCompression(expansion);
		this.compressionBreadth = (double) size;
		scoreMap.put("compressionBreadth", Double.toString(compressionBreadth));
	}

	void scoreCompressionDepth() {
		IntArrayList expansion = CompressionUtils.iterativeExpansionBuilder(outputSpace, false, randomGenerator);
		int size = CompressionUtils.scoreCompression(expansion);
		this.compressionDepth = (double) size;
		scoreMap.put("compressionDepth", Double.toString(compressionDepth));
	}

	void scoreCompressionPattern(boolean breadth, int minPatternSize) {
		IntArrayList expansion = CompressionUtils.iterativeExpansionBuilder(outputSpace, breadth, randomGenerator);
		IntArrayList compressed = CompressionUtils.patternCompressor(expansion, minPatternSize);
		this.compressionPattern = (double) compressed.size();
		scoreMap.put("compressionPattern", Double.toString(compressionPattern));
	}

}
