package nl.tue.gale.common.code;

import java.util.List;

public interface GELResolver {
	public void resolveStateChange(List<Argument> oldValues,
			List<Argument> newValues);

	public Argument resolveGaleVariable(String name, List<Argument> params);
}
