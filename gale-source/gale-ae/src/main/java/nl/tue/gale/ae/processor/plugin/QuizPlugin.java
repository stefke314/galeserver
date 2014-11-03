package nl.tue.gale.ae.processor.plugin;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.RequestDispatcher;

import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.ae.ProcessorException;
import nl.tue.gale.ae.Resource;
import nl.tue.gale.ae.processor.xmlmodule.QuizModule;
import nl.tue.gale.common.GaleUtil;
import nl.tue.gale.common.cache.CacheSession;
import nl.tue.gale.common.code.Argument;
import nl.tue.gale.um.data.EntityValue;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;

public class QuizPlugin extends AbstractPlugin {
	private static final String xhtmlns = "http://www.w3.org/1999/xhtml";
	private boolean log = false;

	public boolean isLog() {
		return log;
	}

	public void setLog(boolean log) {
		this.log = log;
	}

	public void doGet(Resource resource) throws ProcessorException {
		doPost(resource);
	}

	public void doPost(Resource resource) throws ProcessorException {
		GaleContext gale = GaleContext.of(resource);
		if ("true".equals(gale.req().getParameter("framewait")))
			return;
		try {
			DocumentFactory df = DocumentFactory.getInstance();
			Element html = df.createElement("html", xhtmlns);
			
			QuizModule.Quiz quiz = null;
			try {
				quiz = (QuizModule.Quiz) gale.req().getSession()
						.getAttribute(gale.req().getParameter("guid"));
			} catch (Exception e) {
			}
			if (quiz == null)
				throw new ProcessorException(
						"unable to find quiz-data in session");
			Element h1 = df.createElement("h1", xhtmlns).addText(
					quiz.getTitle());
			html.add(h1);
			Element ol = df.createElement("ol", xhtmlns);
			html.add(ol);
			
			int qnr = 0, cnr = 0;
			
			String logQandA = "";
			int questions = 0;
			int answers = 0;
			for (QuizModule.Question q : quiz.getQuestions()) {
				Element li = df.createElement("li", xhtmlns);
				li.add((Element) q.getQuestion().clone());
				ol.add(li);
				Element br = df.createElement("br", xhtmlns);
				li.add(br);
				//Log the questions and answers
				if(questions > 0) {
					//Separate questions by "-"
					logQandA += "-";
				}
				questions++;
				logQandA += q.getId()+":";
				
				answers=0;
				for (QuizModule.Answer a : q.getAnswers()) {
					int anr = q.getAnswers().indexOf(a);
					//Check if the answer is marked
					boolean checked = checkAnswer(qnr, anr, q.getRadio(), gale);
					//Insert the element in the HTML (Checkbox or radiobuttom)
					Element ea = a.toXHTML(qnr, anr, q.getRadio());
					ea.addAttribute("disabled", "true");
					if (checked)
						ea.addAttribute("checked", "true");
					li.add(ea);
					br = df.createElement("br", xhtmlns);
					li.add(br);
					if (checked && a.getExplain() != null
							&& !"".equals(a.getExplain())) {
						Element i = df.createElement("i", xhtmlns);
						li.add(i);
						i.add((Element) a.getExplain().clone());
						br = df.createElement("br", xhtmlns);
						li.add(br);
					}
					if (checked) {
						logQandA += a.getId();
						//Logging the Other:____ answer 
						if (a.isOther()) logQandA += "|" + getOtherAnswer(qnr, anr, q.getRadio(), gale);
						logQandA +=",";
						answers++;
					}
				}
				//Remove "," from answers from the last question
				if (answers > 0) logQandA = logQandA.substring(0, logQandA.length()-1);
				br = df.createElement("br", xhtmlns);
				li.add(br);
				qnr++;
			}
			
			CacheSession<EntityValue> session = gale.openUmSession();
			
			gale.cm().execute(
					gale.cr(),
					quiz.getAction(),
					Argument.of("gale", "nl.tue.gale.ae.GaleContext", gale,
							"session", "nl.tue.gale.common.cache.CacheSession",
							session));
			
			session.commit();
			
			if (isLog()) {
				// log the result
				StringBuffer sb = new StringBuffer();
				sb.append("\"");
				sb.append(gale.userId());
				sb.append("\";\"");
				sb.append(new Date());
				sb.append("\";\"");
				sb.append(gale.conceptUri());
				sb.append("\";\"");
				// Log Questions and Answers FORMAT:  "Q1:a1,a2,...,aN[|other_an]-Q2:a1,a2,...,aN-...-QN:a1,a2,...,aN"
				sb.append(logQandA);
				sb.append("\n");
				gale.log().log("quiz", sb.toString());
			}
			
			if (quiz.isVerbose()) {
				if (quiz.getResult() != null)
					html.add((Element) quiz.getResult().clone());
				resource.put("xml", html);
				resource.put("mime", "text/xhtml");
				resource.put("original-url", "gale:/empty.xhtml");
				gale.usedStream();
			} else {
				try {
					Map<String, String[]> params = new HashMap<String, String[]>();
					params.putAll(gale.req().getParameterMap());
					params.remove("plugin");
					params.remove("guid");
					Map.Entry<String, String[]> entry;
					for (Iterator<Map.Entry<String, String[]>> iterator = params
							.entrySet().iterator(); iterator.hasNext();) {
						entry = iterator.next();
						if (entry.getKey().startsWith("q_") || entry.getKey().startsWith("qother_"))
							iterator.remove();
					}
					gale.resp().sendRedirect(
							GaleUtil.getRequestURL(gale.req(), params));
				} catch (Exception e) {
					throw new ProcessorException("unable to redirect", e);
				}
				gale.usedResponse();
			}
			
		} catch (Exception e) {
			try {
				RequestDispatcher rd = gale.sc().getRequestDispatcher(
						"/ErrorServlet");
				gale.req()
						.getSession()
						.setAttribute(
								"exception",
								new ProcessorException(
										"unable to log the quiz", e));
				rd.forward(gale.req(), gale.resp());
				gale.usedResponse();
			} catch (Exception ee) {
				throw new ProcessorException(
						"unexpected error while trying to display the errorpage",
						ee);
			}
		}
    }
	
	private boolean checkAnswer(int qnr, int anr, boolean radio,
			GaleContext gale) {
		if (radio)
			return ("a_" + anr).equals(gale.req().getParameter("q_" + qnr));
		else
			return ("true").equals(gale.req().getParameter(
					"q_" + qnr + "_" + anr));
	}
	
	private String getOtherAnswer(int qnr, int anr, boolean radio,
			GaleContext gale) {
		if (radio)
			return gale.req().getParameter("qother_" + qnr);
		else
			return gale.req().getParameter("qother_" + qnr + "_" + anr);
	}
}
