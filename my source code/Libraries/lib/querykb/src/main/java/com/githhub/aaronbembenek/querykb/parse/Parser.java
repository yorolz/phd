package com.githhub.aaronbembenek.querykb.parse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.githhub.aaronbembenek.querykb.Conjunct;
import com.githhub.aaronbembenek.querykb.Query;

public final class Parser {

	private Parser() {
		throw new AssertionError();
	}

	public static String parseConstant(Tokenizer t) throws IOException, ParseException {
		String s = parseIdentifier(t);
		if (!isConstantFirstCharacter(s.charAt(0))) {
			throw new ParseException("Tried to parse a constant, but found \"" + s + "\".");
		}
		return s;
	}

	public static String parseVariable(Tokenizer t) throws IOException, ParseException {
		String s = parseIdentifier(t);
		if (!isVariableFirstCharacter(s.charAt(0))) {
			throw new ParseException("Tried to parse a variable, but found \"" + s + "\".");
		}
		return s;
	}

	public static String parseIdentifier(Tokenizer t) throws IOException, ParseException {
		String token = t.next();
		if (!isIdentifier(token)) {
			throw new ParseException("Tried to parse an identifier, but found \"" + token + "\".");
		}
		return token;
	}

	public static Conjunct parseConjunct(Tokenizer t) throws IOException, ParseException {
		String predicate = parseConstant(t);
		t.consume("(");
		String subject = parseIdentifier(t);
		t.consume(",");
		String object = parseIdentifier(t);
		t.consume(")");
		return Conjunct.make(predicate, subject, object);
	}

	public static Query parseQuery(Tokenizer t) throws IOException, ParseException {
		t.consume(":-");
		List<Conjunct> conjuncts = new ArrayList<>();
		String next = t.peek();
		while (!next.equals(".")) {
			conjuncts.add(parseConjunct(t));
			next = t.peek();
			if (!next.equals(".")) {
				t.consume(",");
			}
		}
		t.consume(".");
		return Query.make(conjuncts);
	}

	public static boolean isIdentifier(String s) {
		if (s.length() < 1) {
			return false;
		}
		for (int i = 0; i < s.length(); i++) {
			if (!isIdentifierCharacter(s.charAt(0))) {
				return false;
			}
		}
		return true;
	}

	public static boolean isConstant(String s) {
		return isIdentifier(s) && isConstantFirstCharacter(s.charAt(0));
	}

	public static boolean isVariable(String s) {
		return isIdentifier(s) && isVariableFirstCharacter(s.charAt(0));
	}

	private static boolean isIdentifierCharacter(char ch) {
		return Character.isLetterOrDigit(ch) || ch == '_';
	}

	private static boolean isConstantFirstCharacter(char ch) {
		return Character.isLowerCase(ch) || Character.isDigit(ch);
	}

	private static boolean isVariableFirstCharacter(char ch) {
		return Character.isUpperCase(ch) || ch == '_';
	}

}
