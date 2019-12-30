package sipka.syntax.parser.model.parse.context;

import java.util.Collection;

import sipka.syntax.parser.util.Pair;

public class DeclaringContext extends ParseContext {

	public DeclaringContext() {
	}

	public DeclaringContext(Collection<Pair<String, Object>> locals) {
		for (Pair<String, Object> local : locals) {
			localsMap.put(local.key, local.value);
		}
	}

	@Override
	public String toString() {
		return "DeclaringContext [localsMap=" + localsMap + "]";
	}

}
