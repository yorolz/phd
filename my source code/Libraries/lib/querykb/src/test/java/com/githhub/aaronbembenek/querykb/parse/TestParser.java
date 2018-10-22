package com.githhub.aaronbembenek.querykb.parse;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;

import com.githhub.aaronbembenek.querykb.parse.ParseException;
import com.githhub.aaronbembenek.querykb.parse.Parser;

public class TestParser {
	
	private Tokenizer makeTokenizer(String s) {
		return new Tokenizer(new StringReader(s));
	}
	
	public void testParseConstant(String constant) {
		try {
			Tokenizer t = makeTokenizer(constant);
			String s = Parser.parseConstant(t);
			assertTrue(s.equals(constant));
			assertTrue(Parser.isConstant(s));
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	public void testParseNonConstant(String nonConstant) {
		try {
			Tokenizer t = makeTokenizer(nonConstant);
			Parser.parseConstant(t);
			fail(nonConstant + " parsed as a constant");
		} catch (ParseException e) {

		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testParseConstant01() {
		testParseConstant("foo");
	}

	@Test
	public void testParseConstant02() {
		testParseConstant("fOO");
	}

	@Test
	public void testParseConstant03() {
		testParseConstant("f__");
	}

	@Test
	public void testParseConstant04() {
		testParseConstant("42");
	}

	@Test
	public void testParseNonConstant01() {
		testParseNonConstant("");
	}

	@Test
	public void testParseNonConstant02() {
		testParseNonConstant("X");
	}

	@Test
	public void testParseNonConstant03() {
		testParseNonConstant("_");
	}

	public void testParseVariable(String variable) {
		try {
			Tokenizer t = makeTokenizer(variable);
			String s = Parser.parseVariable(t);
			assertTrue(s.equals(variable));
			assertTrue(Parser.isVariable(s));
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	public void testParseNonVariable(String nonVariable) {
		try {
			Tokenizer t = makeTokenizer(nonVariable);
			Parser.parseVariable(t);
			fail(nonVariable + " parsed as a variable");
		} catch (ParseException e) {

		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testParseVariable01() {
		testParseVariable("Foo");
	}

	@Test
	public void testParseVariable02() {
		testParseVariable("FOO");
	}

	@Test
	public void testParseVariable03() {
		testParseVariable("F__");
	}

	@Test
	public void testParseVariable04() {
		testParseVariable("F42");
	}

	@Test
	public void testParseVariable05() {
		testParseVariable("_");
	}

	@Test
	public void testParseNonVariable01() {
		testParseNonVariable("");
	}

	@Test
	public void testParseNonVariable02() {
		testParseNonVariable("x");
	}

	@Test
	public void testParseNonVariable03() {
		testParseNonVariable("x_");
	}

	@Test
	public void testParseNonVariable04() {
		testParseNonVariable("xX");
	}

	private void consumeCommaSeparatedTriple(Tokenizer t) throws IOException, ParseException {
		for (int i = 0; i < 2; ++i) {
			Parser.parseConstant(t);
			t.consume(",");
		}
		Parser.parseConstant(t);
	}
	
	@Test
	public void testParsingSingleLineCSV() throws IOException, ParseException {
		String csv = "dog,is_a,animal";
		Tokenizer t = makeTokenizer(csv);
		consumeCommaSeparatedTriple(t);
		assertFalse(t.hasNext());
	}

	@Test
	public void testParsingMultiLineCSV() throws IOException, ParseException {
		String eol = System.lineSeparator();
		String csv = "dog,is_a,animal" + eol + "cat,is_a,animal" + eol + " cow,is_a,animal";
		Tokenizer t = makeTokenizer(csv);
		consumeCommaSeparatedTriple(t);
		consumeCommaSeparatedTriple(t);
		consumeCommaSeparatedTriple(t);
		assertFalse(t.hasNext());
	}
	
//	@Test
//	public void testParsingIdentifiersWithWhitespace() throws IOException, ParseException {
//		String s = " foo  foo\tfoo\nfoo\t\nfoo";
//		Parser p = makeParser(s);
//		for (int i = 0; i < 5; ++i) {
//			p.parseIdentifier();
//		}
//	}

//	@Test
//	public void testConsumingCharactersWithWhitespace() throws IOException, ParseException {
//		String s = " x  x\tx\nx\t\nx";
//		Parser p = new Parser(new StringReader(s));
//		for (int i = 0; i < 5; ++i) {
//			p.consume('x');
//		}
//	}
	
	@Test
	public void testParsingConjunct() throws IOException, ParseException {
		String s = "p(a, b)";
		Tokenizer t = makeTokenizer(s);
		Parser.parseConjunct(t);
	}
	
//	@Test
//	public void testParsingConjunctWithWhitespace() throws IOException, ParseException {
//		String s = " p  (\na,\tb\t\n)";
//		Parser p = makeParser(s);
//		p.parseConjunct();
//	}
//	
	@Test
	public void testParsingEmptyQuery() throws IOException, ParseException {
		String s = ":- .";
		Tokenizer t = makeTokenizer(s);
		Parser.parseQuery(t);
	}
	
	@Test
	public void testParsingQuery() throws IOException, ParseException {
		String s = ":- p(X,Y), q(a,b), r(X,c).";
		Tokenizer t = makeTokenizer(s);
		Parser.parseQuery(t);
	}
	
}
