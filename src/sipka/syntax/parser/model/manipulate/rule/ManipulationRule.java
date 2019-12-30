package sipka.syntax.parser.model.manipulate.rule;

import sipka.syntax.parser.model.manipulate.ModifiableStatement;

public interface ManipulationRule {
	public boolean applyTo(ModifiableStatement stm);
}
