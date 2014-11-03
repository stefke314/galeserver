package nl.tue.gale.ae.processor.xmlmodule;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.ae.ProcessorException;
import nl.tue.gale.ae.Resource;
import nl.tue.gale.ae.processor.xmlmodule.MCModule.Answer;
import nl.tue.gale.ae.processor.xmlmodule.MCModule.Question;
import nl.tue.gale.ae.processor.xmlmodule.MCModule.Test;
import nl.tue.gale.common.GaleUtil;
import nl.tue.gale.common.UrlEncodedQueryString;
import nl.tue.gale.common.uri.URIs;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Node;

public class QuizModule extends AbstractModule {
	
	private static final String xhtmlns = "http://www.w3.org/1999/xhtml";

	private List<String> mimeToHandle = Arrays
			.asList(new String[] { "text/xhtml", "text/xtml" });

	public List<String> getMimeToHandle() {
		return mimeToHandle;
	}

	public void setMimeToHandle(List<String> mimeToHandle) {
		this.mimeToHandle = mimeToHandle;
	}

	private String plugin = "quiz";

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
			
			Quiz quiz = readQuiz(element);

			boolean eval = (Boolean) gale.eval(quiz.expr);
			if (!eval) {
				GaleUtil.replaceNode(element, DocumentFactory.getInstance()
						.createText((quiz.alt == null ? "" : quiz.alt)));
				return null;
			}
			
			String guid = GaleUtil.newGUID();
			gale.req().getSession().setAttribute(guid, quiz);
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
			
			Element result = quiz.toXHTML(url, n);

			return (Element) GaleUtil.replaceNode(element, result);
			
		} catch (Exception e) {
			e.printStackTrace();
			return (Element) GaleUtil.replaceNode(element,
					GaleUtil.createErrorElement("[" + e.getMessage() + "]"));
		}
	}
	
	private Quiz readQuiz(Element element) {
		Quiz result = (new Quiz()).setTitle(element.attributeValue("title"))
				.setAction(element.attributeValue("action"))
				.setExpr(element.attributeValue("expr"))
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
				if (((Element) n).getName().equals("answer"))
					b = false;
			if (b)
				q.add((Node) n.clone());
		}
		Question result = (new Question()).setQuestion(q);
		//Check if the question has an ID.
		if (element.attributeValue("id") != null && !"".equals(element.attributeValue("id")))
			result.setId(new Integer(element.attributeValue("id")));
		if ("checkbox".equals(element.attributeValue("type")))
			result.setType(1);
		for (Element a : (List<Element>) element.elements("answer"))
			result.getAnswers().add(readAnswer(a));
		
		return result;
	}
	
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
		Answer result = (new Answer()).setAnswer(a);
		//Check if the answer has an ID.
		if (element.attributeValue("id") != null && !"".equals(element.attributeValue("id")))
			result.setId(new Integer(element.attributeValue("id")));
		if (element.attributeValue("other") != null && (new Boolean(element.attributeValue("other"))))
			result.setOther(true);
		//Check if the answer has an Explanations.
		if (element.attributeValue("explain") != null && !"".equals(element.attributeValue("explain")))
			result.setId(new Integer(element.attributeValue("explain")));
		return result;
	}
	
	
	public static class Quiz {
		private List<Question> questions = new LinkedList<Question>();
		private String title = null;
		private String action = null;
		private String expr = null;
		private String alt = null;
		private Element result = null;
		private boolean verbose = true;
		
		/*
		 * ADD a new element Concepts.
		 */

		public List<Question> getQuestions() {
			return questions;
		}

		public Quiz setQuestions(List<Question> questions) {
			this.questions = questions;
			return this;
		}

		public String getTitle() {
			return title;
		}

		public Quiz setTitle(String title) {
			this.title = title;
			return this;
		}

		public String getAction() {
			return action;
		}

		public Quiz setAction(String action) {
			this.action = action;
			return this;
		}

		public String getExpr() {
			return expr;
		}

		public Quiz setExpr(String expr) {
			this.expr = expr;
			return this;
		}

		public String getAlt() {
			return alt;
		}

		public Quiz setAlt(String alt) {
			this.alt = alt;
			return this;
		}

		public Element getResult() {
			return result;
		}

		public Quiz setResult(Element result) {
			this.result = result;
			return this;
		}

		public boolean isVerbose() {
			return verbose;
		}

		public Quiz setVerbose(boolean verbose) {
			this.verbose = verbose;
			return this;
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

			Element input = df.createElement("input", xhtmlns)
					.addAttribute("type", "SUBMIT")
					.addAttribute("value", "Save");
			form.add(input);
			input = df.createElement("input", xhtmlns)
					.addAttribute("type", "RESET")
					.addAttribute("value", "Reset quiz");
			form.add(input);
			Element hr = df.createElement("hr", xhtmlns);
			form.add(hr);
			return result;
		}
	}
	
	
	public static class Question {
		private List<Answer> answers = new LinkedList<Answer>();
		private Element question = null;
		private int type = 0;
		private int id = -1;

		public List<Answer> getAnswers() {
			return answers;
		}

		public Question setAnswers(List<Answer> answers) {
			this.answers = answers;
			return this;
		}

		public Element getQuestion() {
			return question;
		}

		public Question setQuestion(Element question) {
			this.question = question;
			return this;
		}
		
		public boolean getRadio() {
			return (type == 0 ? true : false);
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
			//br = df.createElement("br", xhtmlns);
			//result.add(br);
			return result;
		}
	}
	
	
	public static class Answer {
		private Element answer = null;
		private Element explain = null;
		private int id = -1;
		//The other field is an attribute of the answer in the XML file.
		private boolean other = false;

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

		public boolean isOther() {
			return other;
		}

		public Answer setOther(boolean other) {
			this.other = other;
			return this;
		}
		public Element getExplain() {
			return explain;
		}

		public Answer setExplain(Element explain) {
			this.explain = explain;
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
			if (this.isOther()) result.add(otherToHTML(qindex, aindex, radio));
			return result;
		}
		
		private Element otherToHTML(int qindex, int aindex, boolean radio) {
			DocumentFactory df = DocumentFactory.getInstance();
			Element result = df.createElement("input", xhtmlns)
			.addAttribute("type", "text")
			.addAttribute("name",
					"qother_" + qindex + (radio ? "" : "_" + aindex));
			return result;
		}
		
	}

}
