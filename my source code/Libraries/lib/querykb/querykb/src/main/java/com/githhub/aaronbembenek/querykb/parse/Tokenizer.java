package com.githhub.aaronbembenek.querykb.parse;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;

public class Tokenizer {

	private final StreamTokenizer t;

	public Tokenizer(Reader r) {
		t = new StreamTokenizer(r);
		t.ordinaryChar('.');
		t.ordinaryChar('-');
		t.ordinaryChars('0', '9');
		t.wordChars('0', '9');
		t.wordChars('_', '_');
	}

	public String peek() throws IOException, ParseException {
		String token = next();
		t.pushBack();
		return token;
	}

	public String next() throws IOException, ParseException {
		int token = t.nextToken();
		if (t.ttype == StreamTokenizer.TT_EOL) {
			System.out.println("hello");
		}
		switch (t.ttype) {
		case StreamTokenizer.TT_EOF:
			throw new ParseException("Unexpected EOF.");
		case StreamTokenizer.TT_EOL:
			throw new AssertionError("EOL should not be significant.");
		case StreamTokenizer.TT_NUMBER:
			throw new AssertionError("Nothing should be tokenized as a number.");
		case StreamTokenizer.TT_WORD:
			return t.sval;
		default:
			return Character.toString((char) token);
		}
	}

	public boolean hasNext() throws IOException {
		t.nextToken();
		t.pushBack();
		return t.ttype != StreamTokenizer.TT_EOF;
	}

	public void consume(String s) throws IOException, ParseException {
		Tokenizer t = new Tokenizer(new StringReader(s));
		while (t.hasNext()) {
			String expected = t.next();
			String found = next();
			if (!expected.equals(found)) {
				throw new ParseException("Tried to consume \"" + expected + "\", but found \"" + found + "\".");
			}
		}
//		StringBuilder sb = new StringBuilder();
//		while (sb.length() < s.length()) {
//			sb.append(next());
//		}
//		String consumed = sb.toString();
//		if (!consumed.equals(s)) {
//			throw new ParseException("Tried to consume \"" + s + "\", but found \"" + consumed + "\".");
//		}
	}

}
