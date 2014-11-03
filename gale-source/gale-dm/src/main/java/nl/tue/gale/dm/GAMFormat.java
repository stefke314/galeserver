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
 * GAMFormat.java
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
package nl.tue.gale.dm;

import static nl.tue.gale.common.GaleUtil.defaultValue;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import nl.tue.gale.common.cache.Cache;
import nl.tue.gale.common.parser.PCComment;
import nl.tue.gale.common.parser.ParseComponent;
import nl.tue.gale.common.parser.ParseInfo;
import nl.tue.gale.common.parser.ParseList;
import nl.tue.gale.common.parser.ParseNode;
import nl.tue.gale.common.parser.ParseString;
import nl.tue.gale.common.parser.Parser;
import nl.tue.gale.common.parser.ParserException;
import nl.tue.gale.common.parser.Token;
import nl.tue.gale.common.parser.VariableLocator;
import nl.tue.gale.common.uri.URI;
import nl.tue.gale.common.uri.URIs;
import nl.tue.gale.dm.data.Attribute;
import nl.tue.gale.dm.data.Concept;
import nl.tue.gale.dm.data.ConceptRelation;

public class GAMFormat {
	private static final GAMParser parser = new GAMParser();

	public static ParseNode parseGAM(String gamCode) {
		BlocksNode gamBlock = null;
		try {
			gamBlock = (BlocksNode) parser.parse(gamCode);
		} catch (Exception e) {
			throw new IllegalArgumentException("parse error in '" + gamCode
					+ "': " + e.getMessage(), e);
		}
		return gamBlock;
	}

	public static ParseNode parseGAM(String gamCode, URI baseuri) {
		gamCode = filterRelativeCode(gamCode, baseuri);
		return parseGAM(gamCode);
	}

	public static List<Concept> readGAM(String gamCode, URI baseuri,
			Cache<Concept> dm) {
		BlocksNode gamBlock = (BlocksNode) parseGAM(gamCode, baseuri);
		return readGAM(gamBlock, baseuri, dm);
	}

	public static List<Concept> readGAM(BlocksNode gam, URI baseuri,
			Cache<Concept> dm) {
		List<Concept> result = new LinkedList<Concept>();
		Options options = new Options();
		for (BlockNode bn : gam.getChildren()) {
			if (bn.getUri().toString().equals("$options"))
				options.updateOptions(readConcept(bn, null, dm, options));
			else {
				bn.setUri(resolveUri(baseuri, bn.getUri()));
				readConcept(bn, result, dm, options);
			}
		}
		return result;
	}

	private static String filterRelativeCode(String gamCode, URI baseuri) {
		int i = 0;
		int j = 0;
		StringBuilder result = new StringBuilder(gamCode.length() + 128);
		while (i < gamCode.length() && i >= 0) {
			i = gamCode.indexOf("[[=", i);
			if (i >= 0) {
				result.append(gamCode.substring(j, i));
				j = gamCode.indexOf("]]", i);
				if (j < 0)
					j = gamCode.length();
				result.append(baseuri.resolve(gamCode.substring(i + 3, j)));
				j += 2;
				i = j;
			} else
				result.append(gamCode.substring(j));
		}
		return result.toString();
	}

	private static Concept readConcept(BlockNode gam, List<Concept> list,
			Cache<Concept> dm, Options options) {
		Concept concept = new Concept(URIs.of(gam.getUri()));
		concept.addTransientData("node", gam);
		if (list != null)
			list.add(concept);
		Map<String, String> properties = readProperties(gam);
		for (String defaultProp : options.defaultProperties)
			if (!properties.containsKey(defaultProp)) {
				properties.put(defaultProp, "");
				concept.setProperty("~extends." + defaultProp, "+");
			}
		int order = (properties.get("order") == null ? -1 : Integer
				.parseInt(properties.get("order")));
		order = options.getAndUpdateOrder(order);
		if (order >= 0)
			properties.put("order", Integer.toString(order));
		for (Map.Entry<String, String> property : properties.entrySet()) {
			if (property.getKey().equals("event"))
				concept.setEventCode(property.getValue());
			else
				concept.setProperty(property.getKey(), property.getValue());
		}
		for (ParseNode pn : gam.getChildren()) {
			if (pn instanceof VariableNode) {
				VariableNode vn = (VariableNode) pn;
				if (vn.getModifier() == '#') {
					Attribute attr = readAttribute(vn, options);
					attr.setProperty("~extends",
							Character.toString(vn.getOperation()));
					concept.addAttribute(attr);
					if (attr.getName().equals("resource")
							&& attr.getDefaultCode() == null)
						attr.setDefaultCode("\"" + concept.getUriString()
								+ "\"");
				} else {
					concept.setProperty("~extends." + vn.getName(),
							Character.toString(vn.getOperation()));
				}
			} else if (pn instanceof RelationNode) {
				RelationNode rn = (RelationNode) pn;
				readRelation(rn, concept, list, dm, options);
			}
		}
		return concept;
	}

	private static void readRelation(RelationNode rn, Concept baseConcept,
			List<Concept> list, Cache<Concept> dm, Options options) {
		if (rn.getBlock() == null)
			throw new IllegalArgumentException("relation needs block ('" + rn
					+ "')");
		BlockNode block = rn.getBlock();
		Concept related;
		URI relatedUri;
		boolean proxy = false;
		if (block.getChildren().size() == 0) {
			// proxy concept
			relatedUri = URIs.of(resolveUri(baseConcept.getUri(),
					block.getUri()));
			related = dm.getProxy(Concept.class, relatedUri);
			proxy = true;
		} else {
			// inline concept
			block.setUri(resolveUri(baseConcept.getUri(), block.getUri()));
			relatedUri = URIs.of(block.getUri());
			related = readConcept(block, list, dm, options);
		}
		URI inUri;
		URI outUri;
		Concept inConcept;
		Concept outConcept;
		if (rn.getModifier().equals("->")) {
			inUri = baseConcept.getUri();
			outUri = relatedUri;
			inConcept = baseConcept;
			outConcept = related;
		} else {
			inUri = relatedUri;
			outUri = baseConcept.getUri();
			inConcept = related;
			outConcept = baseConcept;
		}
		for (String relName : rn.getRelations()) {
			ConceptRelation cr = new ConceptRelation(false, relName);
			cr.setEqualsString(relName + ";" + inUri + ";" + outUri);
			if (!proxy) {
				cr.changeInConcept(inConcept);
				cr.setOutConcept(dm.getProxy(Concept.class, outUri));
				cr = new ConceptRelation(false, relName);
				cr.setEqualsString(relName + ";" + inUri + ";" + outUri);
				cr.setInConcept(dm.getProxy(Concept.class, inUri));
				cr.changeOutConcept(outConcept);
			} else {
				if (rn.getModifier().equals("->")) {
					cr.changeInConcept(inConcept);
					cr.setOutConcept(outConcept);
				} else {
					cr.changeOutConcept(outConcept);
					cr.setInConcept(inConcept);
				}
			}
		}
	}

	private static Attribute readAttribute(VariableNode vn, Options options) {
		Attribute result = new Attribute();
		result.addTransientData("node", vn);
		String name = vn.getName();
		String type = "java.lang.String";
		if (name.indexOf(":") >= 0) {
			type = name.substring(name.indexOf(":") + 1);
			name = name.substring(0, name.indexOf(":"));
			if (type.indexOf(".") < 0)
				type = "java.lang." + type;
		}
		boolean persistent = false;
		if (name.startsWith("[")) {
			name = name.substring(1, name.length() - 1);
			persistent = true;
		}
		result.setProperty("persistent", (persistent ? "true" : "false"));
		result.setName(name);
		result.setType(type);
		if (vn.isImmediate() && vn.getValue() != null)
			if ("".equals(vn.getValue())) {
				String content = null;
				try {
					content = defaultValue(Class.forName(type)).toString();
				} catch (Exception e) {
				}
				result.setDefaultCode("new " + type + "("
						+ (content == null ? "" : content) + ")");
			} else
				result.setDefaultCode("new "
						+ type
						+ "(\""
						+ vn.getValue().replace("\"", "\\\"")
								.replace("\r\n", "\\n").replace("\n", "\\n")
								.replace("${", "$\"+\"{")
								.replace("#{", "#\"+\"{") + "\")");
		else
			result.setDefaultCode(vn.getValue());
		if (vn.getBlock() != null)
			for (Map.Entry<String, String> property : readProperties(
					vn.getBlock()).entrySet()) {
				if (property.getKey().equals("event"))
					result.setEventCode(property.getValue());
				else
					result.setProperty(property.getKey(), property.getValue());
			}
		return result;
	}

	private static Map<String, String> readProperties(BlockNode block) {
		Map<String, String> result = new HashMap<String, String>();
		for (ParseNode pn : block.getChildren())
			if (pn instanceof VariableNode) {
				VariableNode vn = (VariableNode) pn;
				if (vn.getModifier() == '?') {
					String current = result.get(vn.getName());
					if (current == null)
						current = "";
					if (vn.getOperation() == '+')
						current += vn.getValue();
					else
						current = vn.getValue();
					result.put(vn.getName(), current);
					if (!vn.isImmediate())
						throw new IllegalArgumentException(
								"properties are always immediate ('" + block
										+ "')");
					if (vn.getBlock() != null)
						throw new IllegalArgumentException(
								"properties are not allowed to have inner blocks ('"
										+ block + "')");
				}
			}
		return result;
	}

	private static String resolveUri(URI base, String uri) {
		if (uri == null || "".equals(uri.trim()))
			return base.toString();
		return base.resolve(uri).toString();
	}

	private static class GAMParser {
		private Parser parser = null;

		public GAMParser() {
			parser = new Parser();
			parser.registerParseComponent(new PCComment());
			parser.registerParseComponent(new PCGAM());
		}

		public synchronized ParseNode parse(String expr) throws ParserException {
			return parser.parse("BLOCKS", expr);
		}

		private static class PCGAM implements ParseComponent {
			public Object evaluate(ParseNode node, ParseInfo info,
					VariableLocator vl) throws ParserException {
				return null;
			}

			public String[] getParseMethods() {
				return new String[] { "BLOCKS", "BLOCK", "STAT" };
			}

			public ParseNode parse(String method, ParseList pl, ParseInfo info)
					throws ParserException {
				if ("BLOCKS".equals(method))
					return BLOCKS(pl, info);
				if ("BLOCK".equals(method))
					return BLOCK(pl, info);
				if ("STAT".equals(method))
					return STAT(pl, info);
				throw new ParserException("method '" + method
						+ "' is not handled by PCCommon");
			}

			private BlocksNode BLOCKS(ParseList pl, ParseInfo info)
					throws ParserException {
				BlocksNode result = new BlocksNode(null);
				boolean hasBlock = true;
				do {
					Token token = pl.current();
					hasBlock = (token.getType().equals("id") || token.getType()
							.equals("lacc"));
					if (hasBlock)
						result.getChildren().add(BLOCK(pl, info));
				} while (hasBlock);
				return result;
			}

			private BlockNode BLOCK(ParseList pl, ParseInfo info)
					throws ParserException {
				BlockNode result = new BlockNode(null);
				Token token = pl.current();
				if (token.getType().equals("id")) {
					result.setUri((String) token.get("name"));
					pl.moveNext();
					token = pl.current();
				}
				if (!token.getType().equals("lacc"))
					return result;
				pl.moveNext();
				do {
					token = pl.current();
					if (!token.getType().equals("racc")) {
						ParseNode child = STAT(pl, info);
						child.setParent(result);
						result.getChildren().add(child);
					}
				} while (!token.getType().equals("racc"));
				pl.moveNext();
				return result;
			}

			private ParseNode STAT(ParseList pl, ParseInfo info)
					throws ParserException {
				Token token = pl.current();
				if (token.getType().equals("lbrack")
						|| token.getType().equals("->")
						|| token.getType().equals("<-"))
					return REL(pl, info);
				else
					return VAR(pl, info);
			}

			private RelationNode REL(ParseList pl, ParseInfo info)
					throws ParserException {
				RelationNode rn = new RelationNode(null);
				Token token = pl.current();
				if (token.getType().equals("->")
						|| token.getType().equals("<-")) {
					rn.setModifier(token.getType());
					pl.moveNext();
					token = pl.current();
				}
				token = SYM("lbrack", "'(' expected", pl);
				do {
					if (!token.getType().equals("rbrack")) {
						if (!token.getType().equals("id"))
							throw new ParserException("identifier expected",
									token);
						rn.getRelations().add((String) token.get("name"));
						pl.moveNext();
						token = pl.current();
						if (!token.getType().equals("rbrack"))
							token = SYM("comma", "',' expected", pl);
					}
				} while (!token.getType().equals("rbrack"));
				pl.moveNext();
				token = pl.current();
				if (token.getType().equals("id")
						|| token.getType().equals("lacc"))
					rn.setBlock(BLOCK(pl, info));
				return rn;
			}

			private Token SYM(String type, String message, ParseList pl)
					throws ParserException {
				if (!pl.current().getType().equals(type))
					throw new ParserException(message, pl.current());
				pl.moveNext();
				return pl.current();
			}

			private VariableNode VAR(ParseList pl, ParseInfo info)
					throws ParserException {
				VariableNode vn = new VariableNode(null);
				Token token = pl.current();
				if (token.getType().equals("prop")
						|| token.getType().equals("fragment")) {
					if (token.getType().equals("fragment"))
						vn.setModifier('#');
					pl.moveNext();
					token = pl.current();
				}
				if (!token.getType().equals("id"))
					throw new ParserException("identifier expected", token);
				vn.setName((String) token.get("name"));
				pl.moveNext();
				token = pl.current();
				if (token.getType().equals("add")
						|| token.getType().equals("and")
						|| token.getType().equals("or")) {
					if (token.getType().equals("add"))
						vn.setOperation('+');
					else if (token.getType().equals("and"))
						vn.setOperation('&');
					else if (token.getType().equals("or"))
						vn.setOperation('|');
					pl.moveNext();
					token = pl.current();
				}
				if (token.getType().equals("mark")) {
					vn.setImmediate(false);
					pl.moveNext();
					token = pl.current();
				}
				if (token.getType().equals("const"))
					vn.setValue((String) token.get("value"));
				if (token.getType().equals("empty"))
					vn.setValue("");
				if (!token.getType().equals("const")
						&& !token.getType().equals("empty")
						&& !token.getType().equals("default")) {
					vn.setValue("");
				} else {
					pl.moveNext();
					token = pl.current();
				}
				boolean block = false;
				if (token.getType().equals("id")) {
					pl.moveNext();
					block = pl.current().getType().equals("lacc");
					pl.movePrevious();
				} else {
					block = pl.current().getType().equals("lacc");
				}
				if (block)
					vn.setBlock(BLOCK(pl, info));
				return vn;
			}

			public Token scan(ParseString ps, Token token)
					throws ParserException {
				if (token != null)
					return token; // if a token is already found, do nothing
				// with it
				Token result = null;
				char ch = ps.nextChar();
				if (ch == '!' || ch == '=') {
					result = new Token("mark");
				} else if (ch == '#') {
					result = new Token("fragment");
				} else if (ch == '&') {
					result = new Token("and");
				} else if (ch == '?') {
					result = new Token("prop");
				} else if (ch == '|') {
					result = new Token("or");
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
					result = new Token("add");
				} else if (ch == '-') {
					char ach = ps.nextChar();
					if (ach == '>')
						result = new Token("->");
					else
						ps.returnChar(ach);
				} else if (ch == '<') {
					char ach = ps.nextChar();
					if (ach == '-')
						result = new Token("<-");
					else
						ps.returnChar(ach);
				} else if (ch == '`' || ch == '\'' || ch == '"') {
					StringBuffer aconst = new StringBuffer();
					char ach = ps.nextChar();
					while (ach != ch) {
						aconst.append(ach);
						ach = ps.nextChar();
					}
					result = new Token("const");
					result.add("value", aconst.toString());
				} else {
					result = new Token("id");
					StringBuffer idname = new StringBuffer();
					while (!Character.isWhitespace(ch) && ch != '\f'
							&& ch != '|' && ch != '`' && ch != '{' && ch != '}'
							&& ch != '(' && ch != ')' && ch != ',') {
						idname.append(ch);
						ch = ps.nextChar();
					}
					ps.returnChar(ch);
					String name = idname.toString();
					result.add("name", name);
					if ("empty".equals(name))
						result = new Token("empty");
					if ("default".equals(name))
						result = new Token("default");
					return result;
				}
				return result;
			}
		}
	}

	public static class BlocksNode extends ParseNode implements Cloneable {
		private List<BlockNode> children = new LinkedList<BlockNode>();

		public BlocksNode(ParseNode parent) {
			super(parent);
		}

		public Object clone() {
			BlocksNode result = new BlocksNode(parent);
			for (BlockNode child : children)
				result.children.add((BlockNode) child.clone());
			return result;
		}

		public boolean equals(Object object) {
			if (object == null)
				return false;
			if (!(object instanceof BlocksNode))
				return false;
			BlocksNode bn = (BlocksNode) object;
			return children.equals(bn.children);
		}

		public Object get(String key) {
			if ("children".equals(key))
				return children;
			return null;
		}

		public List<BlockNode> getChildren() {
			return children;
		}

		public void setChildren(List<BlockNode> children) {
			this.children = children;
		}

		public List<ParseNode> getChildList() {
			List<ParseNode> result = new LinkedList<ParseNode>();
			result.addAll(children);
			return result;
		}

		public String getType() {
			return "blocks";
		}

		public int hashCode() {
			return children.hashCode();
		}

		public String toString(Map<String, String> options) {
			String indent = "";
			if (options != null && options.containsKey("indent"))
				indent = options.get("indent");
			StringBuffer result = new StringBuffer();
			result.append(indent);
			result.append("{\n");
			Map<String, String> coptions = new HashMap<String, String>();
			if (options != null)
				coptions.putAll(options);
			coptions.put("indent", "  " + indent);
			for (ParseNode child : children)
				result.append(child.toString(coptions));
			result.append(indent);
			result.append("}\n");
			return result.toString();
		}
	}

	public static class BlockNode extends ParseNode implements Cloneable {
		private String uri = null;
		private List<ParseNode> children = new LinkedList<ParseNode>();

		public BlockNode(ParseNode parent) {
			super(parent);
		}

		public Object clone() {
			BlockNode result = new BlockNode(parent);
			result.uri = uri;
			for (ParseNode child : children)
				result.children.add((ParseNode) child.clone());
			return result;
		}

		public boolean equals(Object object) {
			if (object == null)
				return false;
			if (!(object instanceof BlockNode))
				return false;
			BlockNode bn = (BlockNode) object;
			if (uri == null && bn.uri != null)
				return false;
			if (uri != null && bn.uri == null)
				return false;
			if (uri != null && !uri.equals(bn.uri))
				return false;
			return children.equals(bn.children);
		}

		public Object get(String key) {
			if ("uri".equals(key))
				return uri;
			if ("children".equals(key))
				return children;
			return null;
		}

		public String getUri() {
			return uri;
		}

		public void setUri(String uri) {
			this.uri = uri;
		}

		public List<ParseNode> getChildren() {
			return children;
		}

		public void setChildren(List<ParseNode> children) {
			this.children = children;
		}

		public List<ParseNode> getChildList() {
			List<ParseNode> result = new LinkedList<ParseNode>();
			for (ParseNode child : children)
				result.add(child);
			return result;
		}

		public String getType() {
			return "block";
		}

		public int hashCode() {
			return (uri != null ? uri.hashCode() : 0) + children.hashCode();
		}

		public String toString(Map<String, String> options) {
			String indent = "";
			if (options != null && options.containsKey("indent"))
				indent = options.get("indent");
			StringBuffer result = new StringBuffer();
			result.append(indent);
			if (uri != null) {
				result.append(uri);
				result.append(" ");
			}
			result.append("{\n");
			Map<String, String> coptions = new HashMap<String, String>();
			if (options != null)
				coptions.putAll(options);
			coptions.put("indent", "  " + indent);
			for (ParseNode child : children)
				result.append(child.toString(coptions));
			result.append(indent);
			result.append("}\n");
			return result.toString();
		}
	}

	public static class VariableNode extends ParseNode implements Cloneable {
		private char modifier = '?';
		private String name = null;
		private String value = null;
		private BlockNode block = null;
		private boolean immediate = true;
		private char operation = '=';

		public char getOperation() {
			return operation;
		}

		public void setOperation(char operation) {
			this.operation = operation;
		}

		public boolean isImmediate() {
			return immediate;
		}

		public void setImmediate(boolean immediate) {
			this.immediate = immediate;
		}

		public char getModifier() {
			return modifier;
		}

		public void setModifier(char modifier) {
			this.modifier = modifier;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public BlockNode getBlock() {
			return block;
		}

		public void setBlock(BlockNode block) {
			this.block = block;
		}

		public VariableNode(ParseNode parent) {
			super(parent);
		}

		public Object clone() {
			VariableNode result = new VariableNode(parent);
			result.name = name;
			result.value = value;
			result.modifier = modifier;
			if (block != null)
				result.block = (BlockNode) block.clone();
			result.immediate = immediate;
			result.operation = operation;
			return result;
		}

		public boolean equals(Object object) {
			if (object == null)
				return false;
			if (!(object instanceof VariableNode))
				return false;
			VariableNode vn = (VariableNode) object;
			boolean result = true;
			result &= (name == null ? vn.name == null : name.equals(vn.name));
			result &= (value == null ? vn.value == null : value
					.equals(vn.value));
			result &= (block == null ? vn.block == null : block
					.equals(vn.block));
			result &= modifier == vn.modifier;
			result &= immediate == vn.immediate;
			result &= operation == vn.operation;
			return result;
		}

		public Object get(String key) {
			if ("name".equals(key))
				return name;
			if ("value".equals(key))
				return value;
			if ("modifier".equals(key))
				return modifier;
			if ("block".equals(key))
				return block;
			if ("immediate".equals(key))
				return immediate;
			if ("operation".equals(key))
				return operation;
			return null;
		}

		public List<ParseNode> getChildList() {
			List<ParseNode> result = new LinkedList<ParseNode>();
			if (block == null)
				return result;
			result.add(block);
			return result;
		}

		public String getType() {
			return "variable";
		}

		public int hashCode() {
			int result = (new Character(modifier)).hashCode();
			result += (name != null ? name.hashCode() : 0);
			result += (value != null ? value.hashCode() : 0);
			result += (block != null ? block.hashCode() : 0);
			return result;
		}

		public String toString(Map<String, String> options) {
			String indent = "";
			if (options != null && options.containsKey("indent"))
				indent = options.get("indent");
			StringBuffer result = new StringBuffer();
			result.append(indent);
			result.append(modifier);
			result.append(name);
			result.append(" ");
			if (operation != '=') {
				result.append(operation);
				result.append(" ");
			}
			if (!immediate)
				result.append("!");
			result.append("`");
			result.append(value);
			result.append("`\n");
			if (block != null)
				result.append(block.toString(options));
			return result.toString();
		}
	}

	public static class RelationNode extends ParseNode implements Cloneable {
		private List<String> relations = new LinkedList<String>();
		private String modifier = "->";
		private BlockNode block = null;

		public List<String> getRelations() {
			return relations;
		}

		public void setRelations(List<String> relations) {
			this.relations = relations;
		}

		public String getModifier() {
			return modifier;
		}

		public void setModifier(String modifier) {
			this.modifier = modifier;
		}

		public BlockNode getBlock() {
			return block;
		}

		public void setBlock(BlockNode block) {
			this.block = block;
		}

		public RelationNode(ParseNode parent) {
			super(parent);
		}

		public Object clone() {
			RelationNode result = new RelationNode(parent);
			result.relations.addAll(relations);
			result.modifier = modifier;
			if (block != null)
				result.block = (BlockNode) block.clone();
			return result;
		}

		public boolean equals(Object object) {
			if (object == null)
				return false;
			if (!(object instanceof RelationNode))
				return false;
			RelationNode rn = (RelationNode) object;
			boolean result = true;
			result &= (modifier != null ? modifier.equals(rn.modifier)
					: rn.modifier == null);
			result &= (relations != null ? relations.equals(rn.relations)
					: rn.relations == null);
			result &= (block != null ? block.equals(rn.block)
					: rn.block == null);
			return result;
		}

		public Object get(String key) {
			if ("block".equals(key))
				return block;
			if ("relations".equals(key))
				return relations;
			if ("modifier".equals(key))
				return modifier;
			return null;
		}

		public List<ParseNode> getChildList() {
			List<ParseNode> result = new LinkedList<ParseNode>();
			if (block != null)
				result.add(block);
			return result;
		}

		public String getType() {
			return "relation";
		}

		public int hashCode() {
			int result = (modifier != null ? modifier.hashCode() : 0);
			if (relations != null)
				result += relations.hashCode();
			if (block != null)
				result += block.hashCode();
			return result;
		}

		public String toString(Map<String, String> options) {
			String indent = "";
			if (options != null && options.containsKey("indent"))
				indent = options.get("indent");
			StringBuffer result = new StringBuffer();
			result.append(indent);
			result.append(modifier);
			result.append(" (");
			boolean first = true;
			for (String rel : relations) {
				if (!first)
					result.append(",");
				result.append(rel);
				first = false;
			}
			result.append(")\n");
			if (block != null)
				result.append(block.toString(options));
			return result.toString();
		}
	}

	private static final class Options {
		public String[] defaultProperties = new String[] {};
		public int order = -1;

		public void updateOptions(Concept options) {
			String property;
			property = options.getProperty("default.properties");
			if (property != null)
				defaultProperties = property.split(";");
			property = options.getProperty("default.order");
			if ("auto".equals(property) && order < 0)
				order = 0;
		}

		public int getAndUpdateOrder(int order) {
			if (this.order < 0)
				return order;
			if (order < this.order)
				return this.order++;
			this.order = order + 1;
			return order;
		}
	}
}
