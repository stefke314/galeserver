package nl.tue.gale.ae.processor.view;

import java.util.List;

import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.ae.Resource;
import nl.tue.gale.common.GaleUtil;
import nl.tue.gale.common.uri.URIs;

import org.dom4j.Element;

import com.google.common.collect.ImmutableList;

public class MultiListView implements LayoutView {
	@Override
	public Element getXml(Resource resource, Object... params) {
		GaleContext gale = GaleContext.of(resource);
		TreeNodes nodes = TreeNodes.of(gale);
		TreeNode currentNode = TreeNode.of(gale);

		Element result = GaleUtil.createHTMLElement("span");
		String[] parents = currentNode.getParents();
		List<String> siblings = ImmutableList
				.copyOf((parents.length > 0 ? nodes.get(parents[0])
						.getChildren() : new String[] {}));
		String[] children = currentNode.getChildren();
		Element ul = result.addElement("ul").addAttribute("style",
				"float:left;display:block");
		ul.add(createLinkElement(gale, currentNode.getConcept()));
		ul = ul.addElement("ul");
		for (String child : children)
			ul.add(createLinkElement(gale, child));
		for (String parent : parents)
			result.add(createLinkElement(gale, parent,
					"[up] " + gale.dm().get(URIs.of(parent)).getTitle())
					.addAttribute("style", "float:left"));
		if (siblings.indexOf(currentNode.getConcept()) > 0) {
			String previous = siblings.get(siblings.indexOf(currentNode
					.getConcept()) - 1);
			result.add(createLinkElement(gale, previous,
					"[previous] " + gale.dm().get(URIs.of(previous)).getTitle())
					.addAttribute("style", "float:left"));
		}
		if (siblings.indexOf(currentNode.getConcept()) < siblings.size() - 1) {
			String next = siblings.get(siblings.indexOf(currentNode
					.getConcept()) + 1);
			result.add(createLinkElement(gale, next,
					"[next] " + gale.dm().get(URIs.of(next)).getTitle())
					.addAttribute("style", "float:left"));
		}
		return result;
	}

	private static Element createLinkElement(GaleContext gale, String concept) {
		return createLinkElement(gale, concept, gale.dm().get(URIs.of(concept))
				.getTitle());
	}

	private static Element createLinkElement(GaleContext gale, String concept,
			String text) {
		Element result = GaleUtil.createNSElement("a", GaleUtil.adaptns)
				.addAttribute("href", concept).addText(text);
		if (text.endsWith("\u232A"))
			result.addAttribute("style", "text-align:right;");
		return result;
	}
}
