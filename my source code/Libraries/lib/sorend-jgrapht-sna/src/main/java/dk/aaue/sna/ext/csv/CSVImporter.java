/*
 * Copyright (c) 2012, Søren Atmakuri Davidsen
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package dk.aaue.sna.ext.csv;

import org.jgrapht.Graph;
import org.jgrapht.VertexFactory;
import org.jgrapht.WeightedGraph;
import org.jgrapht.generate.GraphGenerator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

/**
 * Importer for networks in CSV formatted files.
 *
 * @param <V> node type
 * @param <E> edge type
 */
public class CSVImporter<V, E> implements GraphGenerator<V, E, V> {

    public static <E> CSVImporter<String, E> createImporter(Reader reader, int srcPosition, int dstPosition, int weightPosition) throws IOException {
        return new CSVImporter<String, E>(new BufferedReader(reader),
                new NodeExtractors.StringNodeExtractor(srcPosition),
                new NodeExtractors.StringNodeExtractor(dstPosition),
                new WeightExtractor(weightPosition));
    }


    public static <V, E> CSVImporter<V, E> createImporter(Reader reader, NodeExtractor<V> srcExtractor, NodeExtractor<V> dstExtractor,WeightExtractor weightExtractor) throws IOException {
        BufferedReader br = new BufferedReader(reader);
        return new CSVImporter<V, E>(br, srcExtractor, dstExtractor, weightExtractor);
    }

    private BufferedReader reader;
    private String delimiter = ",";
    private NodeExtractor<V> srcExtractor;
    private NodeExtractor<V> dstExtractor;
    private WeightExtractor weightExtractor;

    public CSVImporter withDelimiter(String delimiter) {
        this.delimiter = delimiter;
        return this;
    }

    public CSVImporter(BufferedReader reader, NodeExtractor<V> srcExtractor, NodeExtractor<V> dstExtractor, WeightExtractor weightExtractor) {
        this.reader = reader;
        this.srcExtractor = srcExtractor;
        this.dstExtractor = dstExtractor;
        this.weightExtractor = weightExtractor;
    }

    public String last;

    @Override
    public void generateGraph(Graph<V, E> veGraph, VertexFactory<V> vVertexFactory, Map<String, V> stringVMap) {

        while (true) {

            String line = null;
            try {
                line = reader.readLine();
            }
            catch (IOException e) {
                throw new RuntimeException("Error reading graph from file: " + e.getMessage(), e);
            }

            if (line == null)
                break;

            // comments.
            if (line.startsWith(";") || line.startsWith("%") || line.startsWith("#"))
                continue;

            String[] split = line.split(delimiter);

            V srcNode = srcExtractor.createNode(split);
            V dstNode = dstExtractor.createNode(split);
            double weight = weightExtractor.createWeight(split);

            if (srcNode.equals(dstNode))
                continue;

            last = line;

            if (!veGraph.containsVertex(srcNode))
                veGraph.addVertex(srcNode);
            if (!veGraph.containsVertex(dstNode))
                veGraph.addVertex(dstNode);
            if (!veGraph.containsEdge(srcNode, dstNode)) {
                E edge = veGraph.addEdge(srcNode, dstNode);
                if (veGraph instanceof WeightedGraph)
                    ((WeightedGraph) veGraph).setEdgeWeight(edge, weight);
            }
        }
    }

    public void close() {
        try {
            reader.close();
        }
        catch (IOException e) {
            // swallow silently.
        }
    }
}