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
 * PCCommon.java
 * Last modified: $Date$
 * In revision:   $Revision$
 * Modified by:   $Author: dsmits $
 *
 * Copyright (c) 2008-2011 Eindhoven University of Technology.
 * All Rights Reserved.
 *
 * This software is proprietary information of the Eindhoven University
 * of Technology. It may be used according to the GNU LGPL license.
 */
package nl.tue.gale.conversion.parser;

import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import nl.tue.gale.common.parser.ParseComponent;
import nl.tue.gale.common.parser.ParseInfo;
import nl.tue.gale.common.parser.ParseList;
import nl.tue.gale.common.parser.ParseNode;
import nl.tue.gale.common.parser.ParseString;
import nl.tue.gale.common.parser.ParserException;
import nl.tue.gale.common.parser.Token;
import nl.tue.gale.common.parser.VariableLocator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * This parse component scans for common tokens like constants and symbols.
 * Place this after the identifier parse component to ensure that boolean
 * constants are found.
 */
public class PCCommon implements ParseComponent {
	private List<List<String>> OP = ImmutableList.of(
			(List<String>) ImmutableList.of("=>"), ImmutableList.of("||"),
			ImmutableList.of("&&"),
			ImmutableList.of("<", "<=", ">", ">=", "==", "!="),
			ImmutableList.of("+", "-"), ImmutableList.of("*", "/", "%"),
			ImmutableList.of("-", "!", "~"));

	public String[] getParseMethods() {
		return new String[] { "EXPR", "DENOT" };
	}

	/**
	 * Scans the string to see if the first token in the string is handled by
	 * this component.
	 */
	public Token scan(ParseString ps, Token token) throws ParserException {
		if (token != null) {
			// a token is already found, so handle it here
			Token result = token;
			if (token.getType().equals("id")) {
				// token is an identifier
				String name = (String) token.get("name");
				if ((name.equals("true")) || (name.equals("false"))) {
					// token is a boolean constant
					result = new Token("const");
					result.add("value", new Boolean(name));
				}
				if (name.equals("hash")) {
					// token is a hash operator
					result = opToken("~");
				}
			}
			return result;
		}
		// no token is found yet
		char ch = ps.nextChar();
		Token result = null;
		if (ch == ';') {
			result = new Token("semicol");
		} else if (ch == ',') {
			result = new Token("comma");
		} else if (ch == '(') {
			result = new Token("lbrack");
		} else if (ch == ')') {
			result = new Token("rbrack");
		} else if (ch == '{') {
			result = new Token("lacc");
		} else if (ch == '}') {
			result = new Token("racc");
		} else if (ch == '+') {
			char ach = ps.nextChar();
			if (ach == '=')
				result = new Token("plusassign");
			else {
				ps.returnChar(ach);
				result = opToken("+");
			}
		} else if (ch == '-') {
			char ach = ps.nextChar();
			if (ach == '=')
				result = new Token("minusassign");
			else {
				ps.returnChar(ach);
				result = opToken("-");
			}
		} else if (ch == '*') {
			result = opToken("*");
		} else if (ch == '/') {
			result = opToken("/");
		} else if (ch == '%') {
			result = opToken("%");
		} else if (ch == '~') {
			result = opToken("~");
		} else if (ch == '|') {
			char ach = ps.nextChar();
			if (ach == '|')
				result = opToken("||");
			else
				ps.returnChar(ach);
		} else if (ch == '&') {
			char ach = ps.nextChar();
			if (ach == '&')
				result = opToken("&&");
			else
				ps.returnChar(ach);
		} else if (ch == '=') {
			char ach = ps.nextChar();
			if (ach == '=')
				result = opToken("==");
			else if (ach == '>')
				result = opToken("=>");
			else {
				ps.returnChar(ach);
				result = new Token("assign");
			}
		} else if (ch == '!') {
			char ach = ps.nextChar();
			if (ach == '=')
				result = opToken("!=");
			else {
				ps.returnChar(ach);
				result = opToken("!");
			}
		} else if (ch == '<') {
			char ach = ps.nextChar();
			if (ach == '=')
				result = opToken("<=");
			else {
				ps.returnChar(ach);
				result = opToken("<");
			}
		} else if (ch == '>') {
			char ach = ps.nextChar();
			if (ach == '=')
				result = opToken(">=");
			else {
				ps.returnChar(ach);
				result = opToken(">");
			}
		} else if (ch == '"') {
			StringBuffer aconst = new StringBuffer();
			char ach = ps.nextChar();
			while (ach != '"') {
				aconst.append(ach);
				ach = ps.nextChar();
			}
			result = new Token("const");
			result.add("value", aconst.toString());
		} else if (Character.isDigit(ch)) {
			StringBuffer aconst = new StringBuffer();
			aconst.append(ch);
			char ach = ps.nextChar();
			while ((Character.isDigit(ach)) || (ach == '.')) {
				aconst.append(ach);
				ach = ps.nextChar();
			}
			ps.returnChar(ach);

			Object or;
			if (aconst.indexOf(".") < 0)
				or = new Integer(Integer.parseInt(aconst.toString()));
			else
				or = new Float(Float.parseFloat(aconst.toString()));
			// System.out.println("+++ constant token found: "+or.getClass().getName()+", "+or.toString()+" (org: "+aconst+", "+aconst.indexOf(".")+")");
			result = new Token("const");
			result.add("value", or);
		} else if (ch == '#') {
			StringBuffer adate = new StringBuffer();
			char ach = ps.nextChar();
			while (ach != '#') {
				adate.append(ach);
				ach = ps.nextChar();
			}
			try {
				result = new Token("const");
				result.add("value",
						DateFormat.getDateInstance().parse(adate.toString()));
			} catch (Exception e) {
				throw new ParserException("not a valid date: #"
						+ adate.toString() + "#: " + e.getMessage());
			}
		} else {
			ps.returnChar(ch);
		}
		return result;
	}

	private Token opToken(String s) {
		Token result = new Token("op");
		result.add("operation", s);
		return result;
	}

	/**
	 * Uses the specified method to parse the next tokens in the list.
	 */
	public ParseNode parse(String method, ParseList pl, ParseInfo info)
			throws ParserException {
		if ("EXPR".equals(method))
			return EXPR(pl, info);
		if ("DENOT".equals(method))
			return DENOT(pl, info);
		throw new ParserException("method '" + method
				+ "' is not handled by PCCommon");
	}

	public static class ConstNode extends ParseNode implements Cloneable {
		public Object value = null;

		public ConstNode(ParseNode parent) {
			super(parent);
		}

		public List<ParseNode> getChildList() {
			return new LinkedList<ParseNode>();
		}

		public Object get(String key) {
			if (key.equals("value"))
				return value;
			return null;
		}

		public String toString(Map<String, String> options) {
			if (value instanceof String)
				return "\"" + value + "\"";
			else if (value instanceof Date)
				return "#"
						+ DateFormat.getDateInstance(DateFormat.SHORT).format(
								(Date) value) + "#";
			else
				return value.toString();
		}

		public String getType() {
			return "const";
		}

		public Object clone() {
			ConstNode result = new ConstNode(parent);
			result.value = value;
			return result;
		}

		public int hashCode() {
			return (value == null ? 0 : value.hashCode());
		}

		public boolean equals(Object object) {
			if (object == null)
				return false;
			if (!(object instanceof ConstNode))
				return false;
			ConstNode node = (ConstNode) object;
			return (value == null ? node.value == null : value
					.equals(node.value));
		}
	}

	public static class ExprNode extends ParseNode implements Cloneable {
		public ParseNode first = null;
		public String operator = null;
		public ParseNode second = null;

		public ExprNode(ParseNode parent) {
			super(parent);
		}

		public List<ParseNode> getChildList() {
			List<ParseNode> result = new LinkedList<ParseNode>();
			result.add(first);
			if (second != null)
				result.add(second);
			return result;
		}

		public Object get(String key) {
			if (key.equals("first"))
				return first;
			if (key.equals("operator"))
				return operator;
			if (key.equals("second"))
				return second;
			return null;
		}

		public String toString(Map<String, String> options) {
			if ((options != null)
					&& ("true".equals(options.get("string-comp")))) {
				if ("==".equals(operator)) {
					int use = 0;
					if (first.getType().equals("const"))
						if ((first.get("value") instanceof Boolean)
								|| (first.get("value") instanceof Number))
							use++;
					if ((use == 1) && (second.getType().equals("const")))
						if ((second.get("value") instanceof Boolean)
								|| (second.get("value") instanceof Number))
							use++;
					if (use == 0)
						return "(" + first.toString(options) + ".equals("
								+ second.toString(options) + "))";
					if (use == 1)
						return "(" + second.toString(options) + ".equals("
								+ first.toString(options) + "))";
				}
			}
			if (second != null) {
				return "(" + first.toString(options) + operator
						+ second.toString(options) + ")";
			} else {
				return "(" + operator + first.toString(options) + ")";
			}
		}

		public String getType() {
			return "expr";
		}

		public Object clone() {
			ExprNode result = new ExprNode(parent);
			if (first != null)
				result.first = (ParseNode) first.clone();
			if (second != null)
				result.second = (ParseNode) second.clone();
			result.operator = operator;
			return result;
		}

		public int hashCode() {
			int result = 0;
			if (first != null)
				result += first.hashCode();
			if (operator != null)
				result += operator.hashCode();
			if (second != null)
				result += second.hashCode();
			return result;
		}

		public boolean equals(Object object) {
			if (object == null)
				return false;
			if (!(object instanceof ExprNode))
				return false;
			ExprNode node = (ExprNode) object;
			boolean result = true;
			result = result
					&& (first == null ? node.first == null : first
							.equals(node.first));
			result = result
					&& (operator == null ? node.operator == null : operator
							.equals(node.operator));
			result = result
					&& (second == null ? node.second == null : second
							.equals(node.second));
			return result;
		}
	}

	private ParseNode EXPR(ParseList pl, ParseInfo info) throws ParserException {
		return EX(0, pl, info);
	}

	private ParseNode EX(int i, ParseList pl, ParseInfo info)
			throws ParserException {
		ParseNode result = null;
		Token token = pl.current();
		if (i < 6) {
			result = EX(i + 1, pl, info);
			token = pl.current();
			while ((token.getType().equals("op"))
					&& (OP.get(i).contains(token.get("operation")))) {
				String op = (String) token.get("operation");
				pl.moveNext();
				ParseNode second = EX(i + 1, pl, info);
				token = pl.current();
				ParseNode first = result;
				result = new ExprNode(null);
				((ExprNode) result).first = first;
				first.setParent(result);
				((ExprNode) result).second = second;
				second.setParent(result);
				((ExprNode) result).operator = op;
				// if (result.equals("(UM.GM.Concept == \"access\")")) result =
				// "(specialLAGcurrentconceptname.access == true)";
			}
		} else if (i == 6) {
			if ((token.getType().equals("op"))
					&& (OP.get(6).contains(token.get("operation")))) {
				String op = (String) token.get("operation");
				pl.moveNext();
				ParseNode first = EX(6, pl, info);
				result = new ExprNode(null);
				((ExprNode) result).first = first;
				first.setParent(result);
				((ExprNode) result).operator = op;
			} else {
				result = EX(7, pl, info);
			}
		} else {
			if (token.getType().equals("const")) {
				result = info.parse("DENOT", pl);
			} else if (token.getType().equals("id")) {
				result = info.parse("VARP", pl);
				token = pl.current();
				String rname = (String) result.get("name");
				if ((token.getType().equals("lbrack"))
						&& (info.existsMethod(rname.toLowerCase()))
						&& (rname.equals(rname.toLowerCase()))) {
					// assume function call
					pl.movePrevious();
					result = info.parse(rname, pl);
				}
			} else if (token.getType().equals("lbrack")) {
				pl.moveNext();
				result = EX(0, pl, info);
				token = pl.current();
				if (!token.getType().equals("rbrack"))
					throw new ParserException("')' expected", token);
				pl.moveNext();
			} else
				throw new ParserException("syntax error in expression");
		}
		return result;
	}

	private ParseNode DENOT(ParseList pl, ParseInfo info)
			throws ParserException {
		Token token = pl.current();
		if (!token.getType().equals("const"))
			throw new ParserException("constant expected", token);
		ConstNode result = new ConstNode(null);
		result.value = token.get("value");
		pl.moveNext();
		return result;
	}

	/**
	 * Evaluates the specified ParseNode handled by this component.
	 */
	public Object evaluate(ParseNode node, ParseInfo info, VariableLocator vl)
			throws ParserException {
		if (node instanceof ConstNode)
			return node.get("value");
		if (node instanceof ExprNode)
			return doOperation(
					(String) node.get("operator"),
					info.evaluate((ParseNode) node.get("first"), vl),
					(node.get("second") == null ? null : info.evaluate(
							(ParseNode) node.get("second"), vl)));
		throw new ParserException("'" + node + "' not handled by PCCommon");
	}

	private Object doOperation(String op, Object first, Object second)
			throws ParserException {
		@SuppressWarnings("unchecked")
		OperationMethod<Object, Object> om = (OperationMethod<Object, Object>) operations
				.get(new OperationKey(op, first.getClass(),
						(second == null ? null : second.getClass())));
		if (om == null)
			throw new ParserException("invalid operation '" + op + "' on '"
					+ first + "'" + (second == null ? "" : " and " + second));
		// boolean makeint = ((second != null) && (first instanceof Integer) &&
		// (second instanceof
		// Integer));
		// first = makeFloat(first); second = makeFloat(second);
		try {
			Object result = om.doOperation(first, second);
			// if (makeint) result = Math.round((Float)result);
			return result;
		} catch (Exception e) {
			throw new ParserException("unable to perform operation '" + op
					+ "' on '" + first + "'"
					+ (second == null ? "" : " and " + second) + ": "
					+ e.getMessage(), e);
		}
	}

	private static final Map<OperationKey, OperationMethod<? extends Object, ? extends Object>> operations;

	static {
		ImmutableMap.Builder<OperationKey, OperationMethod<? extends Object, ? extends Object>> operationsBuilder = ImmutableMap
				.builder();
		operationsBuilder.put(new OperationKey("-", Float.class, Float.class),
				new OperationMethod<Float, Float>() {
					public Object doOperation(Float first, Float second) {
						return first - second;
					}
				});
		operationsBuilder.put(new OperationKey("+", Float.class, Float.class),
				new OperationMethod<Float, Float>() {
					public Object doOperation(Float first, Float second) {
						return first + second;
					}
				});
		operationsBuilder.put(
				new OperationKey("+", String.class, String.class),
				new OperationMethod<String, String>() {
					public Object doOperation(String first, String second) {
						return first + second;
					}
				});
		operationsBuilder.put(new OperationKey("*", Float.class, Float.class),
				new OperationMethod<Float, Float>() {
					public Object doOperation(Float first, Float second) {
						return first * second;
					}
				});
		operationsBuilder.put(new OperationKey("/", Float.class, Float.class),
				new OperationMethod<Float, Float>() {
					public Object doOperation(Float first, Float second) {
						return first / second;
					}
				});
		operationsBuilder.put(new OperationKey("%", Float.class, Float.class),
				new OperationMethod<Float, Float>() {
					public Object doOperation(Float first, Float second) {
						return abs(first) % abs(second);
					}
				});
		operationsBuilder.put(new OperationKey("<", Float.class, Float.class),
				new OperationMethod<Float, Float>() {
					public Object doOperation(Float first, Float second) {
						return first < second;
					}
				});
		operationsBuilder.put(new OperationKey("<=", Float.class, Float.class),
				new OperationMethod<Float, Float>() {
					public Object doOperation(Float first, Float second) {
						return first <= second;
					}
				});
		operationsBuilder.put(new OperationKey(">", Float.class, Float.class),
				new OperationMethod<Float, Float>() {
					public Object doOperation(Float first, Float second) {
						return first > second;
					}
				});
		operationsBuilder.put(new OperationKey(">=", Float.class, Float.class),
				new OperationMethod<Float, Float>() {
					public Object doOperation(Float first, Float second) {
						return first >= second;
					}
				});
		operationsBuilder.put(new OperationKey("==", Float.class, Float.class),
				new OperationMethod<Float, Float>() {
					public Object doOperation(Float first, Float second) {
						return first.equals(second);
					}
				});
		operationsBuilder.put(new OperationKey("==", Boolean.class,
				Boolean.class), new OperationMethod<Boolean, Boolean>() {
			public Object doOperation(Boolean first, Boolean second) {
				return first.equals(second);
			}
		});
		operationsBuilder.put(
				new OperationKey("==", String.class, String.class),
				new OperationMethod<String, String>() {
					public Object doOperation(String first, String second) {
						return first.equals(second);
					}
				});
		operationsBuilder.put(new OperationKey("!=", Float.class, Float.class),
				new OperationMethod<Float, Float>() {
					public Object doOperation(Float first, Float second) {
						return !first.equals(second);
					}
				});
		operationsBuilder.put(new OperationKey("!=", Boolean.class,
				Boolean.class), new OperationMethod<Boolean, Boolean>() {
			public Object doOperation(Boolean first, Boolean second) {
				return !first.equals(second);
			}
		});
		operationsBuilder.put(
				new OperationKey("!=", String.class, String.class),
				new OperationMethod<String, String>() {
					public Object doOperation(String first, String second) {
						return !first.equals(second);
					}
				});
		operationsBuilder.put(new OperationKey("=>", Boolean.class,
				Boolean.class), new OperationMethod<Boolean, Boolean>() {
			public Object doOperation(Boolean first, Boolean second) {
				if (!first)
					return true;
				if ((first) && (second))
					return false;
				return false;
			}
		});
		operationsBuilder.put(new OperationKey("||", Boolean.class,
				Boolean.class), new OperationMethod<Boolean, Boolean>() {
			public Object doOperation(Boolean first, Boolean second) {
				return first || second;
			}
		});
		operationsBuilder.put(new OperationKey("&&", Boolean.class,
				Boolean.class), new OperationMethod<Boolean, Boolean>() {
			public Object doOperation(Boolean first, Boolean second) {
				return first && second;
			}
		});
		operationsBuilder.put(new OperationKey("-", Float.class, null),
				new OperationMethod<Float, Float>() {
					public Object doOperation(Float first, Float second) {
						return -first;
					}
				});
		operationsBuilder.put(new OperationKey("!", Boolean.class, null),
				new OperationMethod<Boolean, Boolean>() {
					public Object doOperation(Boolean first, Boolean second) {
						return !first;
					}
				});
		operationsBuilder.put(new OperationKey("~", Float.class, null),
				new OperationMethod<Float, Float>() {
					public Object doOperation(Float first, Float second) {
						return first.hashCode();
					}
				});
		operationsBuilder.put(new OperationKey("~", Boolean.class, null),
				new OperationMethod<Boolean, Boolean>() {
					public Object doOperation(Boolean first, Boolean second) {
						return first.hashCode();
					}
				});
		operationsBuilder.put(new OperationKey("~", String.class, null),
				new OperationMethod<String, String>() {
					public Object doOperation(String first, String second) {
						return first.hashCode();
					}
				});

		operationsBuilder.put(new OperationKey("-", Integer.class,
				Integer.class), new OperationMethod<Integer, Integer>() {
			public Object doOperation(Integer first, Integer second) {
				return first - second;
			}
		});
		operationsBuilder.put(new OperationKey("+", Integer.class,
				Integer.class), new OperationMethod<Integer, Integer>() {
			public Object doOperation(Integer first, Integer second) {
				return first + second;
			}
		});
		operationsBuilder.put(new OperationKey("*", Integer.class,
				Integer.class), new OperationMethod<Integer, Integer>() {
			public Object doOperation(Integer first, Integer second) {
				return first * second;
			}
		});
		operationsBuilder.put(new OperationKey("/", Integer.class,
				Integer.class), new OperationMethod<Integer, Integer>() {
			public Object doOperation(Integer first, Integer second) {
				return first / second;
			}
		});
		operationsBuilder.put(new OperationKey("%", Integer.class,
				Integer.class), new OperationMethod<Integer, Integer>() {
			public Object doOperation(Integer first, Integer second) {
				return abs(first) % abs(second);
			}
		});
		operationsBuilder.put(new OperationKey("<", Integer.class,
				Integer.class), new OperationMethod<Integer, Integer>() {
			public Object doOperation(Integer first, Integer second) {
				return first < second;
			}
		});
		operationsBuilder.put(new OperationKey("<=", Integer.class,
				Integer.class), new OperationMethod<Integer, Integer>() {
			public Object doOperation(Integer first, Integer second) {
				return first <= second;
			}
		});
		operationsBuilder.put(new OperationKey(">", Integer.class,
				Integer.class), new OperationMethod<Integer, Integer>() {
			public Object doOperation(Integer first, Integer second) {
				return first > second;
			}
		});
		operationsBuilder.put(new OperationKey(">=", Integer.class,
				Integer.class), new OperationMethod<Integer, Integer>() {
			public Object doOperation(Integer first, Integer second) {
				return first >= second;
			}
		});
		operationsBuilder.put(new OperationKey("==", Integer.class,
				Integer.class), new OperationMethod<Integer, Integer>() {
			public Object doOperation(Integer first, Integer second) {
				return first.equals(second);
			}
		});
		operationsBuilder.put(new OperationKey("!=", Integer.class,
				Integer.class), new OperationMethod<Integer, Integer>() {
			public Object doOperation(Integer first, Integer second) {
				return !first.equals(second);
			}
		});
		operationsBuilder.put(new OperationKey("-", Integer.class, null),
				new OperationMethod<Integer, Integer>() {
					public Object doOperation(Integer first, Integer second) {
						return -first;
					}
				});
		operationsBuilder.put(new OperationKey("~", Integer.class, null),
				new OperationMethod<Integer, Integer>() {
					public Object doOperation(Integer first, Integer second) {
						return first.hashCode();
					}
				});
		operations = operationsBuilder.build();
	}

	private static class OperationKey {
		public String op;
		public Class<?> first;
		public Class<?> second;

		public OperationKey(String op, Class<?> first, Class<?> second) {
			this.op = op;
			this.first = first;
			this.second = second;
		}

		public int hashCode() {
			int result = op.hashCode() + first.hashCode();
			if (second != null)
				result += second.hashCode();
			return result;
		}

		public boolean equals(Object obj) {
			if (!(obj instanceof OperationKey))
				return false;
			OperationKey ok = (OperationKey) obj;
			boolean result = true;
			result = result && op.equals(ok.op);
			result = result && first.equals(ok.first);
			if (second == null)
				result = result && (ok.second == null);
			else
				result = result && second.equals(ok.second);
			return result;
		}
	}

	private interface OperationMethod<T, U> {
		public Object doOperation(T first, U second);
	}

	private static float abs(float f) {
		return (f < 0 ? -f : f);
	}
}