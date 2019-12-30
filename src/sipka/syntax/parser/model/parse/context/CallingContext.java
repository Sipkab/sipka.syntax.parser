package sipka.syntax.parser.model.parse.context;

public class CallingContext extends ParseContext {
	public static CallingContext merge(ParseContext contexta, ParseContext contextb) {
		CallingContext result = new CallingContext();
		result.localsMap.putAll(contexta.localsMap);
		result.localsMap.putAll(contextb.localsMap);
		return result;
	}

	public CallingContext() {
	}

	public CallingContext(ParseContext context) {
		context.putAllObjectTo(localsMap);
	}

	public void putAllBuiltInFrom(ParseContext context) {
		context.putAllBuiltInTo(localsMap);
	}

	public void putObject(String name, Object item) {
		localsMap.put(name, item);
	}

	public void removeObject(String name) {
		localsMap.remove(name);
	}

	@Override
	public String toString() {
		return "CallingContext [localsMap=" + localsMap + "]";
	}

}
