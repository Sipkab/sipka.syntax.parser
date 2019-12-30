package sipka.syntax.parser.model.manipulate;

import java.util.Iterator;
import java.util.Stack;

public class ModifiableStatementTreeWalker implements Iterator<ModifiableStatement> {
	private final ModifiableStatement root;

	private ModifiableStatement next;
	private Stack<Iterator<ModifiableStatement>> childrenStack = new Stack<>();

	public ModifiableStatementTreeWalker(ModifiableStatement root) {
		this.root = root;
		reset();
	}

	@Override
	public boolean hasNext() {
		return next != null;
	}

	@Override
	public ModifiableStatement next() {
		final ModifiableStatement result = next;
		// get next

		while (!childrenStack.empty() && !childrenStack.peek().hasNext()) {
			childrenStack.pop();
		}
		if (childrenStack.empty()) {
			next = null;
		} else {
			next = childrenStack.peek().next();
			childrenStack.push(next.iterator());
		}

		return result;
	}

	public void reset() {
		next = root;
		childrenStack.clear();
		childrenStack.push(next.iterator());
	}

}
