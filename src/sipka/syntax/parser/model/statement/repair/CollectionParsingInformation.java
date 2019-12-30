package sipka.syntax.parser.model.statement.repair;

import java.util.ArrayList;
import java.util.List;

import sipka.syntax.parser.model.parse.document.DocumentRegion;
import sipka.syntax.parser.model.rule.Rule;

public class CollectionParsingInformation extends ParsingInformation {
	private List<ParsingInformation> children;

	public CollectionParsingInformation(Rule rule, DocumentRegion regionOfInterest, List<ParsingInformation> children) {
		super(rule, regionOfInterest);
		this.children = children;
	}

	public CollectionParsingInformation(ParsingInformation info, List<ParsingInformation> children) {
		super(info);
		this.children = children;
	}

	@Override
	public CollectionParsingInformation clone() {
		CollectionParsingInformation result = (CollectionParsingInformation) super.clone();
		result.children = new ArrayList<>();
		for (ParsingInformation info : this.children) {
			result.children.add(info.clone());
		}
		return result;
	}

	@Override
	public List<ParsingInformation> getChildren() {
		return children;
	}

	@Override
	public String toString() {
		return "CollectionParsingInformation [" + (children != null ? "children=" + children : "") + "]";
	}

}
