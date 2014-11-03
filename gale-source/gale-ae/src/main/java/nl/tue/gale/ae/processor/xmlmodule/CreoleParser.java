/*

	This file is part of GALE (Generic Adaptation Language and Engine).

    GALE is free software: you can redistribute it and/or modify it under the 
    terms of the GNU Lesser General Public License as published by the Free 
    Software Foundation, either version 3 of the License, or (at your option) 
    any later version.

    GALE is distributed in the hope that it will be useful, but WITHOUT ANY 
    WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
    FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for 
    more details.

    You should have received a copy of the GNU Lesser General Public License
    along with GALE. If not, see <http://www.gnu.org/licenses/>.
    
 */
/**
 * CreoleParser.java
 * Last modified: $Date$
 * In revision:   $Revision$
 * Modified by:   $Author$
 *
 * Copyright (c) 2008-2011 Eindhoven University of Technology.
 * All Rights Reserved.
 *
 * This software is proprietary information of the Eindhoven University
 * of Technology. It may be used according to the GNU LGPL license.
 */
package nl.tue.gale.ae.processor.xmlmodule;

import static com.google.common.base.Preconditions.checkNotNull;
import static nl.tue.gale.common.GaleUtil.adaptns;
import static nl.tue.gale.common.GaleUtil.createHTMLElement;
import static nl.tue.gale.common.GaleUtil.createNSElement;
import static nl.tue.gale.common.GaleUtil.xhtmlns;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import nl.tue.gale.common.GaleUtil;
import nl.tue.gale.common.parser.ParseString;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Node;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.MapMaker;

public final class CreoleParser {
	private CreoleParser() {
	}

	private static final CreoleParser parser = new CreoleParser();

	public static CreoleParser instance() {
		return parser;
	}

	private final Map<String, Element> cache = new MapMaker().maximumSize(250)
			.makeComputingMap(new Function<String, Element>() {
				@Override
				public Element apply(String input) {
					return internalParse(input);
				}
			});

	public Element parse(String text) {
		if (text == null)
			return null;
		return cache.get(text).createCopy();
	}

	private Element internalParse(String text) {
		try {
			Element span = createHTMLElement("span");
			for (Node node : Parser.CREOLE(new Tokens(new Scanner(text))))
				span.add(node);
			return span;
		} catch (Exception e) {
			throw new IllegalArgumentException("unable to parse creole '"
					+ text + "'", e);
		}
	}

	private static final class Tokens {
		private static final int maxTokens = 5;

		private final Scanner scanner;
		private final ArrayList<Token> tokenList = new ArrayList<Token>(
				maxTokens + 2);
		private int pos = 0;

		public Tokens(Scanner scanner) {
			this.scanner = scanner;
		}

		public Token current() {
			if (pos >= tokenList.size())
				addTokens();
			return tokenList.get(pos);
		}

		public void moveNext() {
			pos++;
			if (pos >= maxTokens)
				addTokens();
		}

		public void movePrevious() {
			pos--;
			if (pos < 0)
				throw new IllegalStateException("token list too small");
		}

		public boolean hasPrevious() {
			return pos > 0;
		}

		private void addTokens() {
			tokenList.addAll(scanner.scan());
			while (tokenList.size() > maxTokens) {
				pos--;
				tokenList.remove(0);
			}
		}
	}

	private static final class Token {
		private final String type;
		private final String text;
		private final boolean useText;

		private volatile int hashCode = 0;

		private Token(String type, String text) {
			this.type = type;
			this.text = text;
			useText = ("symbol".equals(type) || "text".equals(type));
		}

		public static Token of(String type) {
			checkNotNull(type);
			return new Token(type, null);
		}

		public static Token of(String type, String text) {
			checkNotNull(type);
			Token result = new Token(type, text);
			return result;
		}

		public String getType() {
			return type;
		}

		public String getText() {
			return text;
		}

		@Override
		public String toString() {
			if (useText)
				return text;
			else
				return type;
		}

		public String toString(boolean includeWhitespace) {
			if (!includeWhitespace)
				return toString();
			if (type.contains(" ") || type.contains("\n"))
				return text;
			else
				return toString();
		}

		@Override
		public int hashCode() {
			if (hashCode == 0) {
				int result = 5394;
				result = 31 * result + type.hashCode();
				if (useText)
					result = 31 * result
							+ (text == null ? 3958 : text.hashCode());
				hashCode = (result == 0 ? 1 : result);
			}
			return hashCode;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this)
				return true;
			if (obj.hashCode() != hashCode())
				return false;
			if (!(obj instanceof Token))
				return false;
			Token other = (Token) obj;
			if (!type.equals(other.type))
				return false;
			if (useText) {
				if (text == null)
					return other.text == null;
				if (!text.equals(other.text))
					return false;
			}
			return true;
		}
	}

	private static final class Scanner {
		private final ParseString ps;

		public Scanner(String parseString) {
			this.ps = new ParseString(parseString);
		}

		public List<Token> scan() {
			StringBuilder sb = new StringBuilder();
			do {
				scanInternal();
				if (tokenString == null && lastChar != '\f') {
					if (Character.isLetterOrDigit(lastChar))
						sb.append(lastChar);
				}
			} while (tokenString == null && lastChar != '\f'
					&& Character.isLetterOrDigit(lastChar));
			ImmutableList.Builder<Token> builder = ImmutableList.builder();
			if (sb.length() > 0)
				builder.add(Token.of("text", sb.toString()));
			if (tokenString != null)
				builder.add(Token.of(tokenString, tokenText));
			else if (lastChar == '\f')
				builder.add(Token.of("end"));
			else
				builder.add(Token.of("symbol", "" + lastChar));
			return builder.build();
		}

		private String tokenString;
		private String tokenText;
		private char lastChar;

		private void scanInternal() {
			tokenString = null;
			tokenText = null;
			char ch = ps.nextChar();
			if (ch == '~') {
				ch = ps.nextChar();
			} else if (ch == '/' || ch == '[' || ch == ']' || ch == '\\') {
				char ach = ps.nextChar();
				if (ach == ch) {
					if (ch == '[') {
						ach = ps.nextChar();
						if (ach == '=')
							tokenString = "" + ch + ch + ach;
						else {
							tokenString = "" + ch + ch;
							ps.returnChar(ach);
						}
					} else
						tokenString = "" + ch + ch;
				} else {
					ps.returnChar(ach);
					if (ch == '\\')
						tokenString = "\\";
				}
			} else if (ch == '*' || ch == '-' || ch == '=' || ch == '#') {
				char ach;
				StringBuilder sb = new StringBuilder();
				sb.append(ch);
				while ((ach = ps.nextChar()) == ch)
					sb.append(ch);
				ps.returnChar(ach);
				if (ch == '-' && sb.length() < 4)
					for (int i = 0; i < sb.length() - 1; i++)
						ps.returnChar(ch);
				else
					tokenString = sb.toString();
			} else if (ch == '|') {
				char ach = ps.nextChar();
				if (ach == '=')
					tokenString = "|=";
				else {
					ps.returnChar(ach);
					tokenString = "|";
				}
			} else if (ch == '(') {
				char ach = ps.nextChar();
				if (ach == '%') {
					tokenString = "(%";
					StringBuilder text = new StringBuilder();
					ach = ps.nextChar();
					char bch;
					boolean done = false;
					while (ach != '\f' && !done) {
						text.append(ach);
						bch = ps.nextChar();
						done = (ach == '%' && bch == ')');
						ach = bch;
					}
					text.delete(text.length() - 1, text.length());
					tokenText = text.toString();
				} else
					ps.returnChar(ach);
			} else if (ch == '%') {
				char ach = ps.nextChar();
				if (ach == ')')
					tokenString = "%)";
				else
					ps.returnChar(ach);
			} else if (ch == '{' || ch == '}') {
				char ach = ps.nextChar();
				if (ach == ch) {
					char bch = ps.nextChar();
					if (bch == ch)
						tokenString = "" + ch + ch + ch;
					else {
						ps.returnChar(bch);
						tokenString = "" + ch + ch;
					}
				} else
					ps.returnChar(ach);
			} else if (Character.isWhitespace(ch) && !('\f' == ch)) {
				char ach = ch;
				char last;
				int lineCount = 0;
				StringBuilder sb = new StringBuilder();
				do {
					if (Character.isWhitespace(ach))
						sb.append(ach);
					last = ach;
					if (ach == '\n')
						lineCount++;
					ach = ps.nextChar();
				} while (Character.isWhitespace(ach) && !('\f' == ach));
				tokenText = sb.toString();
				ps.returnChar(ach);
				ch = ' ';
				if (lineCount >= 1) {
					tokenString = ((lineCount == 1) ? "\n" : "\n\n")
							+ (last == '\n' ? "" : " ");
				} else
					tokenString = " ";
			}
			lastChar = ch;
		}
	}

	private static final class Parser {
		private static final Token END_TOKEN = Token.of("end");

		public static List<Node> CREOLE(Tokens tokens) {
			List<Node> result = new LinkedList<Node>();
			while (!END_TOKEN.equals(tokens.current())) {
				while (tokens.current().getType().startsWith("\n"))
					tokens.moveNext();
				result.add(PARA(tokens));
			}
			return ImmutableList.copyOf(result);
		}

		public static Element PARA(Tokens tokens) {
			Element result = createHTMLElement("p");
			@SuppressWarnings("unchecked")
			List<Node> content = result.content();
			content.addAll(TEXT(tokens));
			return result;
		}

		public static List<Node> TEXT(Tokens tokens) {
			return TEXT(tokens, (String) null);
		}

		public static List<Node> TEXT(Tokens tokens, String mark) {
			if (mark == null)
				return TEXT(tokens, ImmutableList.<Token> of());
			else
				return TEXT(tokens, ImmutableList.of(Token.of(mark)));
		}

		public static List<Node> TEXT(Tokens tokens, List<Token> mark) {
			List<Node> result = new LinkedList<Node>();
			while (!END_TOKEN.equals(tokens.current())
					&& !tokens.current().getType().startsWith("\n\n")
					&& (mark == null || !mark.contains(tokens.current()))) {
				if (tokens.current().getType().equals("//")) {
					result.add(EM(tokens));
				} else if (tokens.current().getType().equals("**")) {
					result.add(STRONG(tokens));
				} else if (tokens.current().getType().equals("(%")) {
					result.add(INLINE(tokens));
				} else if (tokens.current().getType().startsWith("[[")) {
					result.add(LINK(tokens));
				} else if (tokens.current().getType().equals("{{")) {
					result.add(IMAGE(tokens));
				} else if (tokens.current().getType().equals("{{{")) {
					result.add(NOWIKI(tokens));
				} else if (tokens.current().getType().startsWith("----")) {
					result.add(HR(tokens));
				} else if ((tokens.current().getType().equals("*") || tokens
						.current().getType().equals("#"))
						&& isListStart(tokens)) {
					result.add(LIST(tokens, 1));
				} else if (tokens.current().getType().startsWith("=")
						&& isListStart(tokens)) {
					result.add(HEADING(tokens));
				} else if ((tokens.current().getType().equals("|=") || tokens
						.current().getType().equals("|"))
						&& isListStart(tokens)) {
					result.add(TABLE(tokens));
				} else if (tokens.current().getType().equals("\\\\")) {
					result.add(BR(tokens));
				} else if (tokens.current().getType().equals("\\")) {
					result.add(VARIABLE(tokens));
				} else {
					String text = tokens.current().toString();
					if (text.equals("\n"))
						text = " ";
					result.add(DocumentFactory.getInstance().createText(text));
					tokens.moveNext();
				}
			}
			if (mark != null && mark.contains(tokens.current()))
				tokens.moveNext();
			return ImmutableList.copyOf(result);
		}

		public static Element EM(Tokens tokens) {
			tokens.moveNext();
			Element result = createHTMLElement("em");
			@SuppressWarnings("unchecked")
			List<Node> content = result.content();
			content.addAll(TEXT(tokens, "//"));
			return result;
		}

		public static Element STRONG(Tokens tokens) {
			tokens.moveNext();
			Element result = createHTMLElement("strong");
			@SuppressWarnings("unchecked")
			List<Node> content = result.content();
			content.addAll(TEXT(tokens, "**"));
			return result;
		}

		public static Element LINK(Tokens tokens) {
			boolean external = tokens.current().getType().endsWith("=");
			tokens.moveNext();
			Element result = createNSElement("a", adaptns);
			boolean inLabel = false;
			StringBuilder link = new StringBuilder();
			List<Node> label = new LinkedList<Node>();
			while (!END_TOKEN.equals(tokens.current())
					&& !tokens.current().getType().startsWith("\n\n")
					&& !tokens.current().getType().equals("]]")) {
				if (tokens.current().getType().equals("|")) {
					inLabel = true;
				} else {
					if (inLabel) {
						if (tokens.current().getType().equals("\\")) {
							label.add(VARIABLE(tokens));
							tokens.movePrevious();
						} else
							label.add(DocumentFactory.getInstance().createText(
									tokens.current().toString()));
					} else
						link.append(tokens.current().toString());
				}
				tokens.moveNext();
			}
			if (!END_TOKEN.equals(tokens.current()))
				tokens.moveNext();
			if (label.size() == 0) {
				if (!inLabel || external)
					label.add(DocumentFactory.getInstance().createText(
							link.toString()));
				else
					label.add(createNSElement("variable", adaptns)
							.addAttribute("expr",
									"${" + link.toString() + "}.getTitle()"));
			}
			if (tokens.current().getType().equals("text")) {
				label.add(DocumentFactory.getInstance().createText(
						tokens.current().toString()));
				tokens.moveNext();
			}
			if (external) {
				result.addAttribute("href", "?external=" + link.toString());
				result.addAttribute("adapt", "false");
			} else
				result.addAttribute("href", link.toString());
			for (Node node : label)
				result.add(node);
			return result;
		}

		private static final String[] ulStart = { "*", "**", "***", "****",
				"*****" };
		private static final String[] olStart = { "#", "##", "###", "####",
				"#####" };

		public static Element LIST(Tokens tokens, int level) {
			String type = (tokens.current().getType().startsWith("*") ? "ul"
					: "ol");
			Element result = createHTMLElement(type);
			do {
				tokens.moveNext();
				Element li = createHTMLElement("li");
				@SuppressWarnings("unchecked")
				List<Node> content = li.content();
				content.addAll(TEXT(tokens, ImmutableList.of(Token.of("\n"))));
				result.add(li);
				if (!isListPart(tokens))
					break;
				if (type.equals("ul") ? ulStart[level].equals(tokens.current()
						.getType()) : olStart[level].equals(tokens.current()
						.getType())) {
					result.add(LIST(tokens, level + 1));
					if (!isListPart(tokens))
						break;
				}
			} while (type.equals("ul") ? ulStart[level - 1].equals(tokens
					.current().getType()) : olStart[level - 1].equals(tokens
					.current().getType()));
			return result;
		}

		private static boolean isListStart(Tokens tokens) {
			boolean isStart = !tokens.hasPrevious();
			if (!isStart) {
				tokens.movePrevious();
				isStart = tokens.current().getType().endsWith("\n");
				tokens.moveNext();
			}
			return isStart;
		}

		private static boolean isListPart(Tokens tokens) {
			tokens.movePrevious();
			boolean isPart = tokens.current().getType().equals("\n");
			tokens.moveNext();
			return isPart;
		}

		public static Element HR(Tokens tokens) {
			tokens.moveNext();
			return createHTMLElement("hr");
		}

		private static final List<Token> headingList = ImmutableList.of(
				Token.of("="), Token.of("=="), Token.of("==="),
				Token.of("===="), Token.of("====="), Token.of("======"),
				Token.of("\n"), Token.of("\n "));

		public static Element HEADING(Tokens tokens) {
			Element result = createHTMLElement("h"
					+ (headingList.indexOf(tokens.current()) + 1));
			tokens.moveNext();
			@SuppressWarnings("unchecked")
			List<Node> content = result.content();
			content.addAll(TEXT(tokens, headingList));
			return result;
		}

		public static Element BR(Tokens tokens) {
			tokens.moveNext();
			return createHTMLElement("br");
		}

		public static Element IMAGE(Tokens tokens) {
			tokens.moveNext();
			Element result = createNSElement("img", xhtmlns);
			boolean inLabel = false;
			StringBuilder link = new StringBuilder();
			StringBuilder label = new StringBuilder();
			while (!END_TOKEN.equals(tokens.current())
					&& !tokens.current().getType().startsWith("\n\n")
					&& !tokens.current().getType().equals("}}")) {
				if (tokens.current().getType().equals("|")) {
					inLabel = true;
				} else {
					if (inLabel)
						label.append(tokens.current().toString());
					else
						link.append(tokens.current().toString());
				}
				tokens.moveNext();
			}
			if (!END_TOKEN.equals(tokens.current()))
				tokens.moveNext();
			if (label.length() == 0)
				label.append(link);
			result.addAttribute("src", link.toString())
					.addAttribute("alt", label.toString())
					.addAttribute("title", label.toString());
			return result;
		}

		public static Element NOWIKI(Tokens tokens) {
			boolean pre = isListStart(tokens);
			tokens.moveNext();
			pre &= tokens.current().getType().contains("\n");
			Element result = createHTMLElement((pre ? "pre" : "tt"));
			StringBuilder content = new StringBuilder();
			while (((pre && !(isListStart(tokens) && tokens.current().equals(
					Token.of("}}}")))) || (!pre && !tokens.current().equals(
					Token.of("}}}"))))
					&& (!tokens.current().equals(END_TOKEN))) {
				content.append(tokens.current().toString(true));
				tokens.moveNext();
			}
			if (!tokens.current().equals(END_TOKEN))
				tokens.moveNext();
			result.addText(content.toString());
			return result;
		}

		public static Element VARIABLE(Tokens tokens) {
			tokens.moveNext();
			String variable = tokens.current().toString();
			tokens.moveNext();
			Element result = createNSElement("object", adaptns);
			result.addAttribute("name", "_variable/" + variable);
			return result;
		}

		public static Element INLINE(Tokens tokens) {
			String xmlString = tokens.current().getText().trim();
			tokens.moveNext();
			if (xmlString.startsWith("<")) {
				Element result = GaleUtil.parseXML(new StringReader(xmlString))
						.getRootElement();
				result.detach();
				return result;
			}
			throw new IllegalArgumentException("error in inline code '"
					+ xmlString + "'");
		}

		public static Element TABLE(Tokens tokens) {
			Element result = createHTMLElement("table");
			do {
				Element tr = result.addElement("tr");
				boolean eol = false;
				do {
					boolean header = ("|=".equals(tokens.current().getType()));
					tokens.moveNext();
					eol = tokens.current().getType().contains("\n")
							|| tokens.current().equals(END_TOKEN);
					if (!eol) {
						Element td = (header ? tr.addElement("th") : tr
								.addElement("td"));
						while (!eol && !"|".equals(tokens.current().getType())
								&& !"|=".equals(tokens.current().getType())) {
							if (tokens.current().getType().equals("//")) {
								td.add(EM(tokens));
							} else if (tokens.current().getType().equals("**")) {
								td.add(STRONG(tokens));
							} else if (tokens.current().getType()
									.startsWith("[[")) {
								td.add(LINK(tokens));
							} else {
								td.addText(tokens.current().toString());
								tokens.moveNext();
							}
							eol = tokens.current().getType().contains("\n")
									|| tokens.current().equals(END_TOKEN);
						}
					}
					if (tokens.current().getType().contains("\n"))
						tokens.moveNext();
				} while (!eol);
			} while (isListPart(tokens) && !tokens.current().equals(END_TOKEN));
			tokens.movePrevious();
			if (!tokens.current().getType().contains("\n\n"))
				tokens.moveNext();
			return result;
		}
	}
}
