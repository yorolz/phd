package com.githhub.aaronbembenek.querykb;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import com.githhub.aaronbembenek.querykb.parse.ParseException;

public abstract class AbstractTestCountNonGroundQueries extends AbstractTestCount {

	public AbstractTestCountNonGroundQueries() {
		super("p(a, b). p(a, c). p(b, c). p(c, c).");
	}

	@Test
	public void testSingletonQueryWithTwoUnboundVar() throws IOException, ParseException {
		assertTrue(query(":- p(X, Y).") == 4);
	}

	@Test
	public void testSingletonQueryWithOneUnboundVar1() throws IOException, ParseException {
		assertTrue(query(":- p(X, b).") == 1);
	}

	@Test
	public void testSingletonQueryWithOneUnboundVar2() throws IOException, ParseException {
		assertTrue(query(":- p(a, X).") == 2);
	}

	@Test
	public void testSingletonQueryWithOneUnboundVar3() throws IOException, ParseException {
		assertTrue(query(":- p(X, c).") == 3);
	}

	@Test
	public void testSingletonQueryWithOneUnboundVar4() throws IOException, ParseException {
		assertTrue(query(":- p(X, a).") == 0);
	}

	@Test
	public void testSingletonQueryWithMatchingVar() throws IOException, ParseException {
		assertTrue(query(":- p(X, X).") == 1);
	}

	@Test
	public void testTwoQuery1() throws IOException, ParseException {
		assertTrue(query(":- p(X, Y), p(Z, W).") == 16);
	}

	@Test
	public void testTwoQuery2() throws IOException, ParseException {
		assertTrue(query(":- p(X, Y), p(Y, Z).") == 4);
	}

	@Test
	public void testTwoQuery3() throws IOException, ParseException {
		assertTrue(query(":- p(X, Y), p(X, Y).") == 4);
	}

	@Test
	public void testTwoQuery4() throws IOException, ParseException {
		assertTrue(query(":- p(X, Y), p(Y, X).") == 1);
	}

	@Test
	public void testTwoQuery5() throws IOException, ParseException {
		assertTrue(query(":- p(X, X), p(Z, Z).") == 1);
	}

	@Test
	public void testThreeQuery1() throws IOException, ParseException {
		assertTrue(query(":- p(X, Y), p(Z, W), p(U, V).") == 64);
	}

	@Test
	public void testThreeQuery2() throws IOException, ParseException {
		assertTrue(query(":- p(X, Y), p(Y, W), p(U, V).") == 16);
	}

	@Test
	public void testThreeQuery3() throws IOException, ParseException {
		assertTrue(query(":- p(X, Y), p(Y, W), p(Y, V).") == 4);
	}

	@Test
	public void testThreeQuery4() throws IOException, ParseException {
		assertTrue(query(":- p(X, a), p(Y, W), p(Y, V).") == 0);
	}

	@Test
	public void testThreeQuery5() throws IOException, ParseException {
		assertTrue(query(":- p(X, Y), p(Y, a), p(Y, V).") == 0);
	}

	@Test
	public void testThreeQuery6() throws IOException, ParseException {
		assertTrue(query(":- p(X, Y), p(Y, W), p(Y, a).") == 0);
	}
	
	@Test
	public void testThreeQuery7() throws IOException, ParseException {
		assertTrue(query(":- p(X1,X2), p(X1,X3), p(X4,X3).") == 14);
	}

	@Test
	public void testEightQuery1() throws IOException, ParseException {
		assertTrue(query(":- p(X,Y), p(X,Z), p(Z,X), p(Y,X), p(X,W), p(W,X), p(Y,W), p(Y,W).") == 1);
	}

}
