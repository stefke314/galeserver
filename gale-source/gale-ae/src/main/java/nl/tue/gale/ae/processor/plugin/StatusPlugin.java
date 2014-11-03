package nl.tue.gale.ae.processor.plugin;

import java.net.MalformedURLException;

import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.ae.ProcessorException;
import nl.tue.gale.ae.Resource;
import nl.tue.gale.common.GaleUtil;
import nl.tue.gale.common.uri.URI;
import nl.tue.gale.common.uri.URIs;
import nl.tue.gale.dm.data.Attribute;
import nl.tue.gale.um.data.EntityValue;

import org.dom4j.Element;

public class StatusPlugin extends AbstractPlugin {
	@Override
	public void doGet(Resource resource) throws ProcessorException {
		GaleContext gale = GaleContext.of(resource);
		Element html = GaleUtil.createHTMLElement("html");
		Element body = html.addElement("body");
		Element ul = body.addElement("ul");
		URI uri = GaleUtil.addUserInfo(gale.conceptUri(), gale.userId());
		for (Attribute attr : gale.concept().getVirtualAttributes()) {
			URI attrURI = URIs.builder().uri(uri).fragment(attr.getName())
					.build();
			EntityValue ev = gale.um().get(attrURI);
			if (ev != null)
				ul.addElement("li").addText(
						"#" + attr.getName() + " -> " + ev.getValueString());
		}
		resource.put("xml", html);
		resource.put("mime", "text/xhtml");
		resource.put("original-url", gale.conceptUri().toString());
		try {
			resource.put("url", gale.conceptUri().toURL());
		} catch (MalformedURLException e) {
		}
		gale.usedStream();
	}
}
