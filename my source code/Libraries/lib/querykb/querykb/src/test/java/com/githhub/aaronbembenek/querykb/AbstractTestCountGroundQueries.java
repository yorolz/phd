package com.githhub.aaronbembenek.querykb;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import com.githhub.aaronbembenek.querykb.parse.ParseException;

public abstract class AbstractTestCountGroundQueries extends AbstractTestCount {

	public AbstractTestCountGroundQueries() {
		super("p(a, b). p(b, c). p(c, c).");
	}

	@Test
	public void testEmptyQuery() throws IOException, ParseException {
		assertTrue(query(":-.") == 1);
	}
	
	@Test
	public void testSingletonTrueQuery() throws IOException, ParseException {
		assertTrue(query(":- p(a, b).") == 1);
	}
	
	@Test
	public void testSingletonFalseQuery() throws IOException, ParseException {
		assertTrue(query(":- p(a, c).") == 0);
	}
	
	@Test
	public void testSizeTwoTrueQuery1() throws IOException, ParseException {
		assertTrue(query(":- p(a, b), p(b, c).") == 1);
	}
	
	@Test
	public void testSizeTwoTrueQuery2() throws IOException, ParseException {
		assertTrue(query(":- p(c, c), p(b, c).") == 1);
	}
	
	@Test
	public void testSizeTwoTrueQuery3() throws IOException, ParseException {
		assertTrue(query(":- p(a, b), p(a, b).") == 1);
	}
	
	@Test
	public void testSizeTwoFalseQuery1() throws IOException, ParseException {
		assertTrue(query(":- p(a, c), p(b, c).") == 0);
	}
	
	@Test
	public void testSizeTwoFalseQuery2() throws IOException, ParseException {
		assertTrue(query(":- p(a, b), p(c, b).") == 0);
	}
	
	@Test
	public void testSizeThreeTrueQuery1() throws IOException, ParseException {
		assertTrue(query(":- p(a, b), p(b, c), p(c, c).") == 1);
	}
	
	@Test
	public void testSizeThreeTrueQuery2() throws IOException, ParseException {
		assertTrue(query(":- p(b, c), p(c, c), p(a, b).") == 1);
	}
	
	@Test
	public void testSizeThreeTrueQuery3() throws IOException, ParseException {
		assertTrue(query(":- p(a, b), p(b, c), p(a, b).") == 1);
	}
	
	@Test
	public void testSizeThreeFalseQuery1() throws IOException, ParseException {
		assertTrue(query(":- p(b, a), p(b, c), p(c, c).") == 0);
	}
	
	@Test
	public void testSizeThreeFalseQuery2() throws IOException, ParseException {
		assertTrue(query(":- p(a, b), p(c, b), p(c, c).") == 0);
	}
	
	@Test
	public void testSizeThreeFalseQuery3() throws IOException, ParseException {
		assertTrue(query(":- p(a, b), p(b, c), p(c, b).") == 0);
	}
	
	@Test
	public void testQueryWithUnknownConstant() throws IOException, ParseException {
		assertTrue(query(":- p(a, d).") == 0);
	}
}
