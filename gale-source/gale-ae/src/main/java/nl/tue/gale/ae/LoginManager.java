package nl.tue.gale.ae;

import java.util.List;
import java.util.Map;

import org.dom4j.Element;

public interface LoginManager {

	public static final String LOGINMETHOD = "login";

	public abstract void setHandlerList(List<LoginHandler> handlerList);

	public abstract List<LoginHandler> getHandlerList();

	public abstract void setCss(String css);

	public abstract String getCss();

	public abstract Element getLoginPage(String method,
			Map<String, String[]> parameters);

	public abstract void doLoginPage(Resource resource);

	public abstract String getLoginName(Resource resource);

	public abstract void logout(Resource resource);

}