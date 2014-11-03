package nl.tue.gale.ae.processor.view;

import java.util.List;

import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.ae.Resource;
import nl.tue.gale.common.GaleUtil;
import nl.tue.gale.common.uri.URIs;

import org.dom4j.Element;

import com.google.common.collect.ImmutableList;

public class SingleListView implements LayoutView {
	@Override
	public Element getXml(Resource resource, Object... params) {
		GaleContext gale = GaleContext.of(resource);
		TreeNodes nodes = TreeNodes.of(gale);
		TreeNode currentNode = TreeNode.of(gale);

		Element result = GaleUtil.createHTMLElement("ul");
		result.add(createLinkElement(gale, currentNode.getConcept()));
		Element ul = result.addElement("ul");
		String[] parents = currentNode.getParents();
		List<String> siblings = ImmutableList
				.copyOf((parents.length > 0 ? nodes.get(parents[0])
						.getChildren() : new String[] {}));
		String[] children = currentNode.getChildren();
		if (parents.length > 0 || children.length > 0) {
			ul.add(createSeparator());
			for (String parent : parents)
				ul.add(createLinkElement(gale, parent, "\u2329\u2329 "
						+ gale.dm().get(URIs.of(parent)).getTitle()));
			if (siblings.size() > 1) {
				ul.add(createSeparator());
				if (siblings.indexOf(currentNode.getConcept()) > 0) {
					String previous = siblings.get(siblings.indexOf(currentNode
							.getConcept()) - 1);
					ul.add(createLinkElement(gale, previous, "\u2329 "
							+ gale.dm().get(URIs.of(previous)).getTitle()));
				}
				if (siblings.indexOf(currentNode.getConcept()) < siblings
						.size() - 1) {
					String next = siblings.get(siblings.indexOf(currentNode
							.getConcept()) + 1);
					ul.add(createLinkElement(gale, next,
							gale.dm().get(URIs.of(next)).getTitle() + " \u232A"));
				}
			}
			if (parents.length > 0 && children.length > 0)
				ul.add(createSeparator());
			for (String child : children)
				ul.add(createLinkElement(gale, child));
		}
		return result;
	}

	private Element createSeparator() {
		return GaleUtil.createHTMLElement("span").addAttribute("class",
				"separator");
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
