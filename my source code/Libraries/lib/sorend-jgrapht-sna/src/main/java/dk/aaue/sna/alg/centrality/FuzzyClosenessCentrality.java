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
import fuzzy4j.aggregation.weighted.WeightedAggregation;
import fuzzy4j.aggregation.weighted.WeightedValue;
import fuzzy4j.sets.FuzzyFunction;
import org.jgrapht.Graph;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static fuzzy4j.util.FuzzyUtil.asArray;

/**
 * A closeness centrality measure, with a fuzzy distance measure in the graph.
 *
 * @param <V> The node type
 * @param <E> The edge type
 * @author Soren A. Davidsen <sda@es.aau.dk></sda@es.aau.dk>
 */
public class FuzzyClosenessCentrality<V, E> implements CentralityMeasure<V> {

    private Logger LOG = Logger.getLogger(FuzzyClosenessCentrality.class.getName());
    private Graph<V, E> graph;

    private FuzzyFunction pathMeasure = new FuzzyFunction() {
        @Override
        public double apply(double d) {
            if (Double.isInfinite(d))
                return 0.0;
            else
                return 1.0 / d; //- (d / diameter);
        }
    };
    private WeightedAggregation[] aggregations;

    private DijkstraForClosuresFactory<V, E> shortestPathFactory;
    private DijkstraForClosuresFactory<V, E> strongestPathFactory;

    private Map<V, Double>[] results;

    public FuzzyClosenessCentrality<V, E> withPathMeasure(FuzzyFunction pathMeasure) {
        this.pathMeasure = pathMeasure;
        return this;
    }

    public FuzzyClosenessCentrality<V, E> withAggregations(WeightedAggregation... aggregations) {
        this.aggregations = aggregations;
        return this;
    }

    public FuzzyClosenessCentrality(Graph<V, E> graph, WeightedAggregation aggregation) {
        this.graph = graph;
        this.aggregations = new WeightedAggregation[]{ aggregation };
        this.shortestPathFactory = DijkstraForClosuresFactory.<V, E>newShortestPath();
        this.strongestPathFactory = DijkstraForClosuresFactory.<V, E>newStrongestPath();
    }

    public CentralityResult<V> calculate() {

        double min_s = Double.POSITIVE_INFINITY;
        double max_s = Double.NEGATIVE_INFINITY;

        results = new HashMap[aggregations.length];
        for (int i = 0; i < results.length; i++)
            results[i] = new HashMap<V, Double>();

        Set<V> V = graph.vertexSet();

        for (V u : V) {

            List<WeightedValue> values = new ArrayList<WeightedValue>();

            DijkstraForClosures<V, E> shortestPathsU = shortestPathFactory.create(graph, u);

            //DijkstraForClosures<V, E> strongestPathsU = null;
            //if (graph instanceof WeightedGraph)
            // strongestPathsU = strongestPathFactory.create(graph, u);

            for (V v : V) {
                // skip reflexiveness
                if (u == v)
                    continue;

                // get length of the path
                Double distance = shortestPathsU.get(v);

                if (distance == null)
                    distance = Double.POSITIVE_INFINITY;

                double closeDistance = pathMeasure.apply(distance);

                // LOG.info(String.format("d, f(d) = (%.3f, %.3f)", distance, closeDistance));

                /*
                Double closeStrength = strongestPathsU.get(v);

                // no strength or 0.0 = no path
                if (closeStrength == null || closeStrength == 0.0)
                    continue;

                values.add(WeightedValue._(closeStrength, closeDistance));

                min_s = Math.min(min_s, closeStrength);
                max_s = Math.max(max_s, closeStrength);
                    */
                values.add(WeightedValue.w(1.0, closeDistance));

                min_s = Math.min(min_s, 1.0);
                max_s = Math.max(max_s, 1.0);
            }

            if (LOG.isLoggable(Level.FINE))
                LOG.fine("values(" + u + ") = [" + values.size() + "] " + values);

            WeightedValue[] arr = asArray(values);

            for (int k = 0; k < aggregations.length; k++) {
                if (arr.length == 0)
                    results[k].put(u, Double.NEGATIVE_INFINITY);
                else
                    results[k].put(u, aggregations[k].apply(arr));
            }
        }

        if (LOG.isLoggable(Level.FINE))
            LOG.fine("(min_s, max_s) = (" + min_s + ", " + max_s + ")");

        return new CentralityResult<V>(results[0], true);
    }

    public List<CentralityResult<V>> getAllResults() {
        List<CentralityResult<V>> r = new ArrayList();
        for (int i = 0;i < results.length; i++)
            r.add(new CentralityResult<V>(results[i], true));
        return r;
    }
}
