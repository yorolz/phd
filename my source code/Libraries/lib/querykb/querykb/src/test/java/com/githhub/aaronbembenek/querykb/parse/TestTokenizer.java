package com.githhub.aaronbembenek.querykb.parse;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;

public class TestTokenizer {

	private Tokenizer makeTokenizer(String s) {
		return new Tokenizer(new StringReader(s));
	}

	@Test
	public void testHasNextTrue() throws IOException {
		assertTrue(makeTokenizer("foo").hasNext());
	}

	@Test
	public void testHasNextFalse1() throws IOException {
		assertFalse(makeTokenizer("").hasNext());
	}

	@Test
	public void testHasNextFalse2() throws IOException {
		assertFalse(makeTokenizer("\n \t").hasNext());
	}

	@Test
	public void testConsumeWhitespace() throws IOException, ParseException {
		String s = "hello";
		Tokenizer t = makeTokenizer(s);
		t.consume(" ");
		t.consume("\n");
		t.consume("\t");
		assertTrue(t.peek().equals("hello"));
	}
	
	@Test
	public void testConsumeSingleToken() throws IOException, ParseException {
		String s = "a A 0 _ ; , - . a42 42a a_42 _42a";
		Tokenizer t = makeTokenizer(s);
		for (String ss : s.split(" ")) {
			t.consume(ss);
		}
		assertFalse(t.hasNext());
	}

	@Test
	public void testConsumeMultiToken1() throws IOException, ParseException {
		String s = ":- ab;ba (.)";
		Tokenizer t = makeTokenizer(s);
		t.consume(":-ab");
		t.consume(";ba(");
		t.consume(".)");
		assertFalse(t.hasNext());
	}
	
	@Test
	public void testConsumeMultiToken2() throws IOException, ParseException {
		String s = ":- ab;ba (.)";
		Tokenizer t = makeTokenizer(s);
		t.consume(":- ab");
		t.consume(";   ba\t(");
		t.consume(".\n)");
		assertFalse(t.hasNext());
	}

	@Test
	public void testNext() throws IOException, ParseException {
		String s = "a A 0 _ ; , - . a42 42a a_42 _42a";
		Tokenizer t = makeTokenizer(s);
		for (String ss : s.split(" ")) {
			assertTrue(ss.equals(t.next()));
		}
		assertFalse(t.hasNext());
	}
	
	@Test
	public void testPeek() throws IOException, ParseException {
		String s = "a b c";
		Tokenizer t = makeTokenizer(s);
		assertTrue(t.peek().equals("a"));
		assertTrue(t.peek().equals("a"));
		t.consume("a");
		assertTrue(t.peek().equals("b"));
		assertTrue(t.peek().equals("b"));
		t.next();
		assertTrue(t.peek().equals("c"));
		assertTrue(t.peek().equals("c"));
	}

}
