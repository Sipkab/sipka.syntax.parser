package sipka.syntax.parser.model.parse.context;

import java.util.Map;
import java.util.Map.Entry;

import sipka.syntax.parser.model.rule.Rule;

import java.util.TreeMap;

public abstract class ParseContext {
	protected final Map<String, Object> localsMap = new TreeMap<>();

	public ParseContext() {
	}

	public Object getObjectForName(String name) {
		return getObjectForName(name, null);
	}

	public Object getObjectForName(String name, Object defaultValue) {
		Object result = localsMap.get(name);
		if (result == null)
			return defaultValue;
		return result;
	}

	public void putAllObjectTo(Map<String, Object> target) {
		target.putAll(localsMap);
	}

	public void putAllBuiltInTo(Map<String, Object> target) {
		for (Entry<String, Object> entry : localsMap.entrySet()) {
			if (entry.getKey().startsWith(Rule.BUILTIN_VAR_PREFIX)) {
				target.put(entry.getKey(), entry.getValue());
			}
		}
	}

	@Override
	public String toString() {
		return "ParseContext [localsMap=" + localsMap + "]";
	}
}
