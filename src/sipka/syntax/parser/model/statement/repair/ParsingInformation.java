package sipka.syntax.parser.model.statement.repair;

import java.util.Collections;
import java.util.List;

import sipka.syntax.parser.model.parse.document.DocumentRegion;
import sipka.syntax.parser.model.rule.Rule;

public class ParsingInformation implements Cloneable {
	private Rule rule;
	private DocumentRegion regionOfInterest;

	public ParsingInformation(Rule rule, DocumentRegion regionOfInterest) {
		this.rule = rule;
		this.regionOfInterest = regionOfInterest;
	}

	public ParsingInformation(ParsingInformation info) {
		this.rule = info.rule;
		this.regionOfInterest = new DocumentRegion(info.regionOfInterest);
	}

	@Override
	public ParsingInformation clone() {
		try {
			ParsingInformation result = (ParsingInformation) super.clone();
			result.regionOfInterest = result.regionOfInterest.clone();
			return result;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	public Rule getRule() {
		return rule;
	}

	public DocumentRegion getRegionOfInterest() {
		return regionOfInterest;
	}

	public List<ParsingInformation> getChildren() {
		return Collections.emptyList();
	}

	@Override
	public String toString() {
		return "ParsingInformation [" + (rule != null ? "rule=" + rule + ", " : "")
				+ (regionOfInterest != null ? "regionOfInterest=" + regionOfInterest : "") + "]";
	}

}
