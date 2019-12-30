package sipka.syntax.parser.model.manipulate;

import java.util.Collection;
import java.util.LinkedList;

import sipka.syntax.parser.model.manipulate.rule.ManipulationRule;

public class Manipulator {
	@SuppressWarnings("unused")
	private static class CalcManipulator implements ManipulationRule {
		@Override
		public boolean applyTo(ModifiableStatement stm) {
			int count = stm.getChildCount();
			if (count >= 3) {
				ModifiableStatement n1 = stm.getChildAt(0);
				ModifiableStatement op = stm.getChildAt(1);
				ModifiableStatement n2 = stm.getChildAt(2);
				if (n1.getName().equals("number") && n2.getName().equals("number") && op.getName().equals("operator")) {
					int a = Integer.parseInt(n1.getValue());
					int b = Integer.parseInt(n2.getValue());
					final ModifiableStatement res;
					switch (op.getValue()) {
						case "-": {
							res = new ModifiableStatement("number", a - b + "", stm);
							break;
						}
						case "+": {
							res = new ModifiableStatement("number", a + b + "", stm);
							break;
						}
						case "*": {
							res = new ModifiableStatement("number", a * b + "", stm);
							break;
						}
						case "/": {
							res = new ModifiableStatement("number", a / b + "", stm);
							break;
						}
						default: {
							throw new RuntimeException();
						}
					}
					stm.removeChildAt(0);
					stm.removeChildAt(0);
					stm.replaceChildAt(0, res);
					return true;
				}
			} else if (count == 1 && stm.getChildAt(0).getName().equals("number")
					&& stm.getName().equals("expression")) {
				stm.getParent().replaceChild(stm, stm.getChildAt(0));
				return true;
			}
			return false;
		}
	}

	private final String name;
	private final Collection<ManipulationRule> rules = new LinkedList<>();

	public Manipulator(String name) {
		this.name = name;
		//rules.add(new CalcManipulator());
	}

	public void manipulate(ModifiableStatement root) {
		ModifiableStatementTreeWalker walker = new ModifiableStatementTreeWalker(root);

		while (walker.hasNext()) {
			ModifiableStatement stm = walker.next();
			for (ManipulationRule rule : rules) {
				if (rule.applyTo(stm)) {
					walker.reset();
					break;
				}
			}
		}
	}

	public final String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "Manipulator [" + (name != null ? "name=" + name : "") + "]";
	}

}
