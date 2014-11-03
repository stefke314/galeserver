package nl.tue.gale.ae.processor;

import nl.tue.gale.ae.AbstractResourceProcessor;
import nl.tue.gale.ae.GaleContext;
import nl.tue.gale.ae.ProcessorException;
import nl.tue.gale.ae.Resource;
import nl.tue.gale.common.cache.CacheSession;
import nl.tue.gale.um.data.EntityValue;

public class LinkClassLogProcessor extends AbstractResourceProcessor {
	public void processResource(Resource resource) throws ProcessorException {
		GaleContext gale = GaleContext.of(resource);
		if (gale.isObject() || gale.concept() == null)
			return;
		//System.out.println("concept: "+gale.conceptUri()+", "+gale.req().getRequestURL());
		CacheSession<EntityValue> session = gale.openUmSession();
		
		EntityValue ev = session.get(session.resolve("#link.class"));
		String cssclass = (ev != null ? ev.getValue().toString() : null);
		if (cssclass == null) {
			String classexpr = (String) gale.cfgm().getObject(
					"gale://gale.tue.nl/config/link#classexpr",
					resource);
			try {
				cssclass = gale.eval(session, classexpr).toString();
			} catch (Exception e) {
				cssclass = "unknown";
			}
		}
		gale.req().setAttribute("linkClass", cssclass);
		
		//Exec GALE CODE
		//String code = "${_concept-&gt;(personal)#bad-link-follow};";
		//gale.exec(code);
		
	}

}
