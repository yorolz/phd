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

package dk.aaue.sna.alg.centrality;

import dk.aaue.sna.alg.DijkstraForClosures;
import dk.aaue.sna.alg.DijkstraForClosuresFactory;
import dk.aaue.sna.alg.FloydWarshallAllShortestPaths;
import org.jgrapht.Graph;
import org.jgrapht.alg.DijkstraShortestPath;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of Freeman's original farness(closeness) centrality. Uses floyd-warshall internally to calculate shortest paths.
 *
 * <p>
 * [1] Freeman, Linton, A set of measures of centrality based upon betweenness, Sociometry 40: 35–41, 1977.
 * </p>
 * @param <V> node type
 * @param <E> edge type
 */
public class FreemanClosenessCentrality<V, E> implements CentralityMeasure<V> {

	private Graph<V, E> graph;

	public FreemanClosenessCentrality(Graph<V, E> graph) {
        this.graph = graph;
	}

	public CentralityResult<V> calculate() {

		Map<V, Double> cc = new HashMap<V, Double>();

		Set<V> V = graph.vertexSet();

		for (V u : V) {

            DijkstraForClosures<V, E> sp = DijkstraForClosuresFactory.<V, E>newShortestPath().create(graph, u);

			double sum = 0.0;
			for (V v : V) {
				// skip reflexiveness
				if (u == v)
					continue;

				// get length of the path
                Double length = sp.get(v);
                if (length == null)
                    length = Double.POSITIVE_INFINITY;

                sum += length;

                if (Double.isInfinite(sum)) break; // no need to continue,
			}

            cc.put(u, (V.size() - 1) / sum);
		}

        return new CentralityResult<V>(cc, true);
	}

}
