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
 * MCModule.java
 * Last modified: $Date$
 * In revision:   $Revision$
 * Modified by:   $Author: vramos $
 *
 * Copyright (c) 2008-2011 Eindhoven University of Technology.
 * All Rights Reserved.
 *
 * This software is proprietary information of the Eindhoven University
 * of Technology. It may be used according to the GNU LGPL license.
 */
package nl.tue.gale.ae.processor.xmlmodule;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.ae.ProcessorException;
import nl.tue.gale.ae.Resource;
import nl.tue.gale.common.GaleUtil;
import nl.tue.gale.common.UrlEncodedQueryString;
import nl.tue.gale.common.uri.URI;
import nl.tue.gale.common.uri.URIs;
import nl.tue.gale.dm.data.Concept;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Node;

public class MCModule extends AbstractModule {
	private static final String xhtmlns = "http://www.w3.org/1999/xhtml";
	
	private List<String> mimeToHandle = Arrays
			.asList(new String[] { "text/xhtml" });

	public List<String> getMimeToHandle() {
		return mimeToHandle;
	}

	public void setMimeToHandle(List<String> mimeToHandle) {
		this.mimeToHandle = mimeToHandle;
	}

	private String plugin = "mc";

	public void setPlugin(String plugin) {
		this.plugin = plugin;
	}
	
	public Element traverse(Element element, Resource resource)
			throws ProcessorException {
		try {
			GaleContext gale = GaleContext.of(resource);
			
			Element resultElement = element.element("result");
			if (resultElement != null)
				element.remove(resultElement);
			processor.traverseChildren(element, resource);
			if (resultElement != null)
				element.add(resultElement);

			Test test = readTest(element);

			boolean eval = (Boolean) gale.eval(test.expr);
			if (!eval) {
				GaleUtil.replaceNode(element, DocumentFactory.getInstance()
						.createText((test.alt == null ? "" : test.alt)));
				return null;
			}

			test.create();
			String guid = GaleUtil.newGUID();
			gale.req().getSession().setAttribute(guid, test);
			String url = GaleUtil.getRequestURL(gale.req());
			UrlEncodedQueryString qs = UrlEncodedQueryString
					.parse(URIs.of(url));
			if ("true".equals(qs.get("frame"))) {
				qs.remove("frame");
				qs.append("framewait", "true");
			}
			qs.append("plugin", plugin);
			qs.append("guid", guid);
			url = qs.apply(URIs.of(url)).toString();
			Node n = resource
					.getTyped(Node.class,
							"nl.tue.gale.ae.processor.xmlmodule.AdaptLinkModule.content");
			Element result = test.toXHTML(url, n);

			return (Element) GaleUtil.replaceNode(element, result);
		} catch (Exception e) {
			e.printStackTrace();
			return (Element) GaleUtil.replaceNode(element,
					GaleUtil.createErrorElement("[" + e.getMessage() + "]"));
		}
	}

	@SuppressWarnings("unchecked")
	private Test readTest(Element element) {
		Test result = (new Test()).setTitle(element.attributeValue("title"))
				.setAction(element.attributeValue("action"))
				.setReal_knowledge_expr(element.attributeValue("realKnowledgeExpr"))
				.setExpr(element.attributeValue("expr"))
				.setAsk(new Integer(element.attributeValue("ask")))
				.setResult(element.element("result"))
				.setAlt(element.attributeValue("alt"))
				.setVerbose(new Boolean(element.attributeValue("verbose")));
		for (Element q : (List<Element>) element.elements("question"))
			result.getQuestions().add(readQuestion(q));
		return result;
	}

	@SuppressWarnings("unchecked")
	private Question readQuestion(Element element) {
		Element q = DocumentFactory.getInstance()
				.createElement("span", xhtmlns);
		for (Node n : (List<Node>) element.content()) {
			boolean b = true;
			if (n instanceof Element)
				if (((Element) n).getName().equals("answer") || ((Element) n).getName().equals("concept_relation"))
					b = false;
			if (b)
				q.add((Node) n.clone());
		}
		Question result = (new Question()).setQuestion(q)
				.setCount(new Integer(element.attributeValue("answers")))
				.setRight(new Integer(element.attributeValue("right")));
		//Check if the question has an ID.
		if (element.attributeValue("id") != null && !"".equals(element.attributeValue("id")))
				result.setId(new Integer(element.attributeValue("id")));
		if ("checkbox".equals(element.attributeValue("type")))
			result.setType(1);
		for (Element a : (List<Element>) element.elements("answer"))
			result.getAnswers().add(readAnswer(a));
		
		
		/*
		 * Created by: Vinicius Ramos
		 * DATE: Sept 13th 2012
		 * 
		 * READ the concepts in XHTML
		 */
		int v = 0;
		//Read in xml file all the concepts related to the question
		for (Element c : (List<Element>) element.elements("concept_relation")) {
			result.getConcepts().add(c.getText());
		}
		
		return result;
	}

	@SuppressWarnings("unchecked")
	private Answer readAnswer(Element element) {
		Element a = DocumentFactory.getInstance()
				.createElement("span", xhtmlns);
		for (Node n : (List<Node>) element.content()) {
			boolean b = true;
			if (n instanceof Element)
				if (((Element) n).getName().equals("explain"))
					b = false;
			if (b)
				a.add((Node) n.clone());
		}
		Answer result = (new Answer()).setAnswer(a)
				.setExplain(element.element("explain"))
				.setCorrect(new Boolean(element.attributeValue("correct")));
		//Check if the answer has an ID.
		if (element.attributeValue("id") != null && !"".equals(element.attributeValue("id")))
			result.setId(new Integer(element.attributeValue("id")));

		return result;
	}
	
	public static class Test {
		private List<Question> questions = new LinkedList<Question>();
		private String title = null;
		private String action = null;
		private String real_knowledge_expr = null;
		private String expr = null;
		private String alt = null;
		private Element result = null;
		private int ask = 1;
		private boolean verbose = true;
		
		/*
		 * ADD a new element Concepts.
		 */

		public List<Question> getQuestions() {
			return questions;
		}

		public Test setQuestions(List<Question> questions) {
			this.questions = questions;
			return this;
		}

		public String getTitle() {
			return title;
		}

		public Test setTitle(String title) {
			this.title = title;
			return this;
		}

		public String getAction() {
			return action;
		}

		public Test setAction(String action) {
			this.action = action;
			return this;
		}

		public String getReal_knowledge_expr() {
			return real_knowledge_expr;
		}

		public Test setReal_knowledge_expr(String real_knowledge_expr) {
			this.real_knowledge_expr = real_knowledge_expr;
			return this;
		}

		public String getExpr() {
			return expr;
		}

		public Test setExpr(String expr) {
			this.expr = expr;
			return this;
		}

		public String getAlt() {
			return alt;
		}

		public Test setAlt(String alt) {
			this.alt = alt;
			return this;
		}

		public Element getResult() {
			return result;
		}

		public Test setResult(Element result) {
			this.result = result;
			return this;
		}

		public int getAsk() {
			return ask;
		}

		public Test setAsk(int ask) {
			this.ask = ask;
			return this;
		}

		public boolean isVerbose() {
			return verbose;
		}

		public Test setVerbose(boolean verbose) {
			this.verbose = verbose;
			return this;
		}

		public void create() {
			List<Question> result = new LinkedList<Question>();
			while (result.size() < ask)
				result.add(questions.remove((int) Math.floor(Math.random()
						* questions.size())));
			for (Question q : result)
				q.create();
			questions = result;
		}

		// ${->(parent)[0]#knowledge?persistent}
		public Element toXHTML(String action, Node n) {
			DocumentFactory df = DocumentFactory.getInstance();
			Element result = df.createElement("span", xhtmlns);

			Element h1 = df.createElement("h1", xhtmlns);
			h1.addText(title);
			result.add(h1);

			Element form = df.createElement("form", xhtmlns)
					.addAttribute("method", "POST")
					.addAttribute("action", action);
			result.add(form);
			// possibly add content to the form, based on previous processing
			if (n != null) {
				n = (Node) n.clone();
				form.add(n);
			}

			Element ol = df.createElement("ol", xhtmlns);
			for (Question q : questions) {
				Element li = df.createElement("li", xhtmlns);
				li.add(q.toXHTML(questions.indexOf(q)));
				ol.add(li);
			}
			form.add(ol);

			Element hr = df.createElement("hr", xhtmlns);
			form.add(hr);
			Element input = df.createElement("input", xhtmlns)
					.addAttribute("type", "SUBMIT")
					.addAttribute("value", "Evaluate this test");
			form.add(input);
			input = df.createElement("input", xhtmlns)
					.addAttribute("type", "RESET")
					.addAttribute("value", "Wipe answers to try again");
			form.add(input);
			return result;
		}
	}

	public static class Question {
		private List<Answer> answers = new LinkedList<Answer>();
		private List<String> concepts = new LinkedList<String>();
		private Element question = null;
		private int count = 1;
		private int right = 1;
		private int type = -1;
		private int id = -1;

		public List<Answer> getAnswers() {
			return answers;
		}

		public Question setAnswers(List<Answer> answers) {
			this.answers = answers;
			return this;
		}

		public List<String> getConcepts() {
			return concepts;
		}

		public Question setConcepts(List<String> concepts) {
			this.concepts = concepts;
			return this;
		}

		public Element getQuestion() {
			return question;
		}

		public Question setQuestion(Element question) {
			this.question = question;
			return this;
		}

		public int getCount() {
			return count;
		}

		public Question setCount(int count) {
			this.count = count;
			return this;
		}

		public int getRight() {
			return right;
		}

		public Question setRight(int right) {
			this.right = right;
			return this;
		}

		public boolean getRadio() {
			return (type == -1 ? right == 1 : type == 0);
		}

		public int getType() {
			return type;
		}

		public Question setType(int type) {
			this.type = type;
			return this;
		}

		public int getId() {
			return id;
		}

		public Question setId(int id) {
			this.id = id;
			return this;
		}

		public void create() {
			int rcount = 0;
			int rlcount = 0;
			for (Answer a : answers)
				if (a.correct)
					rlcount++;
			List<Answer> result = new LinkedList<Answer>();
			while (result.size() < count) {
				boolean takeright = (Math.random() < (right - rcount)
						/ (((int) Math.floor(Math.random()
								* (count - result.size()))) + 1));
				if (takeright) {
					int chosen = (int) Math.floor(Math.random() * rlcount);
					Answer found = null;
					int i = 0;
					for (Answer a : answers)
						if (a.correct) {
							if (i == chosen)
								found = a;
							i++;
						}
					result.add(found);
					answers.remove(found);
					rcount++;
					rlcount--;
				} else {
					int chosen = (int) Math.floor(Math.random()
							* (answers.size() - rlcount));
					Answer found = null;
					int i = 0;
					for (Answer a : answers)
						if (!a.correct) {
							if (i == chosen)
								found = a;
							i++;
						}
					result.add(found);
					answers.remove(found);
				}
			}
			answers = result;
		}

		public Element toXHTML(int index) {
			DocumentFactory df = DocumentFactory.getInstance();
			Element result = df.createElement("span", xhtmlns);
			question.detach();
			result.add(question);

			Element br = df.createElement("br", xhtmlns);
			result.add(br);
			for (Answer a : answers) {
				result.add(a.toXHTML(index, answers.indexOf(a), getRadio()));
				br = df.createElement("br", xhtmlns);
				result.add(br);
			}
			br = df.createElement("br", xhtmlns);
			result.add(br);
			return result;
		}
	}

	public static class Answer {
		private Element answer = null;
		private Element explain = null;
		private boolean correct = true;
		private int id = -1;

		public int getId() {
			return id;
		}

		public Answer setId(int id) {
			this.id = id;
			return this;
		}

		public Element getAnswer() {
			return answer;
		}

		public Answer setAnswer(Element answer) {
			this.answer = answer;
			return this;
		}

		public Element getExplain() {
			return explain;
		}

		public Answer setExplain(Element explain) {
			this.explain = explain;
			return this;
		}

		public boolean isCorrect() {
			return correct;
		}

		public Answer setCorrect(boolean correct) {
			this.correct = correct;
			return this;
		}

		public Element toXHTML(int qindex, int aindex, boolean radio) {
			DocumentFactory df = DocumentFactory.getInstance();
			Element result = df
					.createElement("input", xhtmlns)
					.addAttribute("type", (radio ? "RADIO" : "CHECKBOX"))
					.addAttribute("name",
							"q_" + qindex + (radio ? "" : "_" + aindex))
					.addAttribute("value", (radio ? "a_" + aindex : "true"));
			answer.detach();
			result.add(answer);
			return result;
		}
	}
	
	/*
	 * Create a new class Concept to print the suggestions. Maybe I have to implement something in MCPlugin.
	 */
	
}