package sipka.syntax.parser.model.parse.params;

import sipka.syntax.parser.model.parse.context.ParseContext;

public class VarReferenceParam<T> implements InvokeParam<T> {
	private final String variableName;

	public VarReferenceParam(String varname) {
		this.variableName = varname;
	}

	public final String getVarname() {
		return variableName;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getValue(ParseContext context) {
		try {
			Object result = context.getObjectForName(variableName);
			while (result instanceof InvokeParam<?>) {
				result = ((InvokeParam<?>) result).getValue(context);
			}
			return (T) result;
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("Failed to cast variable reference parameter ", e);
		}
	}

	@Override
	public String toString() {
		return "VarReferenceParam [varname=" + variableName + "]";
	}

}
