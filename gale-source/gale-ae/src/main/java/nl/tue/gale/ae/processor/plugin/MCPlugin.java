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
 * MCPlugin.java
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
package nl.tue.gale.ae.processor.plugin;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.RequestDispatcher;

import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.ae.ProcessorException;
import nl.tue.gale.ae.Resource;
import nl.tue.gale.ae.processor.xmlmodule.MCModule;
import nl.tue.gale.common.GaleUtil;
import nl.tue.gale.common.cache.CacheSession;
import nl.tue.gale.common.code.Argument;
import nl.tue.gale.common.uri.URI;
import nl.tue.gale.common.uri.URIs;
import nl.tue.gale.dm.data.Concept;
import nl.tue.gale.um.data.EntityValue;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;

public class MCPlugin extends AbstractPlugin {
	private static final String xhtmlns = "http://www.w3.org/1999/xhtml";
	private static final String adaptns = "http://gale.tue.nl/adaptation";
	
	//private static final Logger logging = Logger.getLogger(AdaptLinkModule.class);

	private boolean log = false;

	public boolean isLog() {
		return log;
	}

	public void setLog(boolean log) {
		this.log = log;
	}

	@Override
	public void doGet(Resource resource) throws ProcessorException {
		doPost(resource);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void doPost(Resource resource) throws ProcessorException {
		GaleContext gale = GaleContext.of(resource);
		if ("true".equals(gale.req().getParameter("framewait")))
			return;
		try {
			DocumentFactory df = DocumentFactory.getInstance();
			Element html = df.createElement("html", xhtmlns);

			MCModule.Test test = null;
			try {
				test = (MCModule.Test) gale.req().getSession()
						.getAttribute(gale.req().getParameter("guid"));
			} catch (Exception e) {
			}
			if (test == null)
				throw new ProcessorException(
						"unable to find test-data in session");

			Element h1 = df.createElement("h1", xhtmlns).addText(
					test.getTitle());
			html.add(h1);
			Element ol = df.createElement("ol", xhtmlns);
			html.add(ol);

			int qnr = 0, cnr = 0;
			
			// Added by Vinicius Ramos - Date: Sept 13th 2012
			Set<String> concepts_error = new TreeSet<String>();
			boolean fail = false;
			//Log the questions and answers
			String logQandA = "";
			int questions = 0;
			int answers = 0;
			for (MCModule.Question q : test.getQuestions()) {
				boolean correct = true;
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
				for (MCModule.Answer a : q.getAnswers()) {
					int anr = q.getAnswers().indexOf(a);
					boolean checked = checkAnswer(qnr, anr, q.getRight() == 1,
							gale);
					correct = correct && (checked == a.isCorrect());
					Element ea = a.toXHTML(qnr, anr, q.getRight() == 1);
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
						logQandA += a.getId()+",";
						answers++;
					}
				}
				//Remove "," from answers from the last question
				if (answers > 0) logQandA = logQandA.substring(0, logQandA.length()-1);
				br = df.createElement("br", xhtmlns);
				li.add(br);
				qnr++;
				cnr += (correct ? 1 : 0);
				
				/*
				 * Implemented by Vinicius Ramos 
				 * DATE: Sept 13th 2012
				 * 
				 * Get concepts related to the failed questions by the user
				 */
				if (!correct) {
					//Store the failed question
					List<String> concepts = q.getConcepts();
					for (String concept : concepts) {
						concepts_error.add(concept);
						fail=true;
					}
				}
			}
			double result = (cnr * 1.0) / qnr * 100;
			
			CacheSession<EntityValue> session = gale.openUmSession();

			/*
			 * Implemented by Vinicius Ramos 
			 * DATE: Sept 13th 2012
			 * 
			 * Add links to the concepts related to the wrong answers
			 */
			if (fail) {
				session = gale.openUmSession();
				/*Element p = df.createElement("p", xhtmlns);
				p.addText("We recommend you to read the following concepts: ");
				html.add(p);
				Element ul = df.createElement("ul", xhtmlns);
				html.add(ul);*/
				//String failedQuestions = failedQuestions.substring(0, failedQuestions.length()-1);
				//List<String> failedConcepts = new ArrayList<String>();
				for (String concept : concepts_error) {
					//List of failed concepts
					//failedConcepts.add(concept);
					/*Element li = df.createElement("li", xhtmlns);
					ul.add(li);
					Element a = df.createElement("a", adaptns);
					a.addAttribute("href", concept);
					a.addText(getConceptTitleByName(concept, gale));
					li.add(a);*/
					String execCode = test.getReal_knowledge_expr().replaceAll("%concept", concept);
					gale.cm().execute(
							gale.cr(),
							execCode,
							Argument.of("gale", "nl.tue.gale.ae.GaleContext", gale,
									"session", "nl.tue.gale.common.cache.CacheSession",
									session));

				}
				//Store the failed concepts in UM
				if (concepts_error.size() > 0) {
					String failedConcepts = new String();
					for(String c : concepts_error) {
						failedConcepts += c+",";
					}
					failedConcepts = failedConcepts.substring(0, failedConcepts.length()-1);
					String execCode = "${#failed-concepts} = \""+failedConcepts+"\";";
					gale.cm().execute(
							gale.cr(),
							execCode,
							Argument.of("gale", "nl.tue.gale.ae.GaleContext", gale,
									"session", "nl.tue.gale.common.cache.CacheSession",
									session));
					execCode = "#{#failed, true};";
					gale.cm().execute(
							gale.cr(),
							execCode,
							Argument.of("gale", "nl.tue.gale.ae.GaleContext", gale,
									"session", "nl.tue.gale.common.cache.CacheSession",
									session));
				}
			}

			gale.cm().execute(
					gale.cr(),
					test.getAction(),
					Argument.of("gale", "nl.tue.gale.ae.GaleContext", gale,
							"session", "nl.tue.gale.common.cache.CacheSession",
							session, "value", "java.lang.Double", new Double(
									result)));
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
				// Log Questions and Answers FORMAT:  "Q1:a1,a2,...,aN-Q2:a1,a2,...,aN-...-QN:a1,a2,...,aN"  
				sb.append(logQandA);
				sb.append("\";");
				sb.append(result);
				gale.log().log("mctest", sb.toString());
			}

			if (test.isVerbose()) {
				if (test.getResult() != null)
					html.add((Element) test.getResult().clone());
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
						if (entry.getKey().startsWith("q_"))
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
										"unable to evaluate test", e));
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
	
	private String getConceptTitleByName(String sConcept, GaleContext gale) {
		URI uri_a = gale.conceptUri();
		String suri_a = uri_a.toString();

		URI uri = URIs.of(suri_a.substring(0, suri_a.lastIndexOf("/")+1).concat(sConcept));
		Concept concept = gale.dm().get(uri);
		return concept.getTitle();
	}
}