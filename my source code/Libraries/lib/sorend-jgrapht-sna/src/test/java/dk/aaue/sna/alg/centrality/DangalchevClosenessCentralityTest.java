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

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static junit.framework.Assert.assertEquals;

/**
 * @author Soren <soren@tanesha.net>
 */
public class DangalchevClosenessCentralityTest {

    SimpleGraph<String, DefaultEdge> G;

    @Before
    public void setup() {
        G = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
    }

    @Test
    public void checkPow() {

        // nothing here.
    }

    @Test
    public void testSimpleExample() {

        for (String v : Arrays.asList("A", "B", "C", "D", "E"))
            G.addVertex(v);

        G.addEdge("A", "B");
        G.addEdge("A", "C");
        G.addEdge("C", "B");
        G.addEdge("A", "D");
        // E is disconnected

        CentralityResult<String> result = new DangalchevClosenessCentrality<String, DefaultEdge>(G).calculate();

        assertEquals(1.50, result.get("A"), 0.00001);
        assertEquals(1.25, result.get("B"), 0.00001);
        assertEquals(1.25, result.get("C"), 0.00001);
        assertEquals(1.00, result.get("D"), 0.00001);
        assertEquals(0.00, result.get("E"), 0.00001);
    }
}
