/*
 * Copyright (C) 2020 Bence Sipka
 *
 * This program is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package sipka.syntax.parser.model.rule.container.order;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import sipka.syntax.parser.model.parse.OccurrenceCounter;
import sipka.syntax.parser.model.parse.ParseTimeData;
import sipka.syntax.parser.model.parse.context.CallingContext;
import sipka.syntax.parser.model.parse.context.ParseContext;
import sipka.syntax.parser.model.parse.document.DocumentData;
import sipka.syntax.parser.model.parse.document.DocumentRegion;
import sipka.syntax.parser.model.rule.ParseHelper;
import sipka.syntax.parser.model.rule.ParsingResult;
import sipka.syntax.parser.model.rule.Rule;
import sipka.syntax.parser.model.rule.container.ContainerRule;
import sipka.syntax.parser.model.statement.CollectionStatement;
import sipka.syntax.parser.model.statement.Statement;
import sipka.syntax.parser.model.statement.repair.CollectionParsingInformation;
import sipka.syntax.parser.model.statement.repair.ParsingInformation;
import sipka.syntax.parser.util.Pair;

public class AnyOrderRule extends ContainerRule {
	protected static class AnyOrderParsingInformation extends CollectionParsingInformation {
		private Map<Rule, OccurrenceCounter> occurrences;

		public AnyOrderParsingInformation(Rule rule, DocumentRegion regionOfInterest, List<ParsingInformation> children,
				Map<Rule, OccurrenceCounter> occurrences) {
			super(rule, regionOfInterest, children);
			this.occurrences = occurrences;
		}

		@Override
		public CollectionParsingInformation clone() {
			AnyOrderParsingInformation result = (AnyOrderParsingInformation) super.clone();
			result.occurrences = new HashMap<>(this.occurrences);
			for (Entry<Rule, OccurrenceCounter> entry : result.occurrences.entrySet()) {
				entry.setValue(new OccurrenceCounter(entry.getValue()));
			}
			return result;
		}

		public Map<Rule, OccurrenceCounter> getOccurrences() {
			return occurrences;
		}
	}

	public AnyOrderRule(String identifierName) {
		super(identifierName);
	}

	private Map<Rule, ParseContext> getRuleContextMap(ParseContext context) {
		Map<Rule, ParseContext> result = new HashMap<>();
		for (Pair<Rule, ParseTimeData> rule : getChildren()) {
			ParseContext rulecontext = CallingContext.merge(rule.value.getDeclaringContext(), context);
			result.put(rule.key, rulecontext);
		}
		return result;
	}

	private Map<Rule, OccurrenceCounter> createOccurrencesMap(ParseHelper helper, Map<Rule, ParseContext> contextmap) {
		HashMap<Rule, OccurrenceCounter> result = new HashMap<>();
		for (Pair<Rule, ParseTimeData> rule : getChildren()) {
			result.put(rule.key, new OccurrenceCounter(rule.value.getOccurrence(helper, contextmap.get(rule.key))));
		}
		return result;
	}

	@Override
	protected ParsingResult parseChildren(ParseHelper helper, DocumentData s, ParseContext context,
			ParseTimeData parsedata) {
		CollectionStatement.Builder result = new CollectionStatement.Builder();

		int startoffset = s.getDocumentOffset();
		DocumentRegion regionofinterest = new DocumentRegion();
		regionofinterest.setOffset(startoffset);

		Map<Rule, ParseContext> contextmap = getRuleContextMap(context);
		Map<Rule, OccurrenceCounter> occurrences = createOccurrencesMap(helper, contextmap);

		executeParsing(helper, s, result, occurrences, contextmap, regionofinterest);

		boolean failed = false;
		for (Entry<Rule, OccurrenceCounter> entry : occurrences.entrySet()) {
			if (!entry.getValue().isValidOccurrenceCount()) {
				failed = true;
				break;
			}
		}
		if (failed) {
			return result.fail(new ParsingInformation(this, regionofinterest));
		}
		return result.build(s, new DocumentRegion(startoffset, s.getDocumentOffset() - startoffset),
				new AnyOrderParsingInformation(this, regionofinterest, result.getParsingInformations(), occurrences));
	}

	private void executeParsing(ParseHelper helper, DocumentData s, CollectionStatement.Builder result,
			Map<Rule, OccurrenceCounter> occurrences, Map<Rule, ParseContext> contextmap,
			DocumentRegion regionofinterest) {
		Collection<Pair<Rule, ParseTimeData>> children = getChildren();
		for (Iterator<Pair<Rule, ParseTimeData>> it = children.iterator(); it.hasNext();) {
			Pair<Rule, ParseTimeData> rule = it.next();
			OccurrenceCounter occounter = occurrences.get(rule.key);
			if (!occounter.canOccurOnceMore())
				continue;
			ParseContext rulecontext = contextmap.get(rule.key);
			final int slen = s.length();
			ParsingResult parsed = rule.key.parseStatement(helper, s, rulecontext, rule.value);

			regionofinterest.expandTo(parsed.getParsingInformation().getRegionOfInterest());

			if (!parsed.isSucceeded()) {
				continue;
			}

			if (slen == s.length()) {
				if (!occounter.isValidOccurrenceCount()) {
					//not valid occurrence count, add as many times as required
					for (int added = occounter.addAnyMoreRequired(); added-- > 0;) {
						result.add(parsed);
					}
				}
				//continue with the next rule
				continue;
			}

			occounter.addOccurrence();
			result.add(parsed);
			it = children.iterator();
		}
	}

	@Override
	protected ParsingResult repairChildren(ParseHelper helper, Statement statement, ParsingInformation parsinginfo,
			DocumentData s, ParseContext context, Predicate<? super Statement> modifiedstatementpredicate,
			ParseTimeData parsedata) {
		DocumentData buf = new DocumentData(s);
		final int startslen = buf.length();

		CollectionStatement.Builder result = new CollectionStatement.Builder();
		int startoffset = buf.getDocumentOffset();
		DocumentRegion regionofinterest = new DocumentRegion();
		regionofinterest.setOffset(startoffset);

		List<Statement> stmchildren = statement.getDirectChildren();

		AnyOrderParsingInformation containerinfo = (AnyOrderParsingInformation) parsinginfo;
		List<ParsingInformation> childreninfos = containerinfo.getChildren();
		Map<Rule, OccurrenceCounter> prevoccurrences = containerinfo.getOccurrences();

		Iterator<ParsingInformation> infoit = childreninfos.listIterator();
		Iterator<Statement> childstmit = stmchildren.listIterator();

		Map<Rule, ParseContext> contextmap = getRuleContextMap(context);
		Map<Rule, OccurrenceCounter> occurrences = createOccurrencesMap(helper, contextmap);

		while (childstmit.hasNext()) {
			Statement childstm = childstmit.next();
			ParsingInformation childinfo = infoit.next();

			Rule childrule = childinfo.getRule();
			OccurrenceCounter occounter = occurrences.get(childrule);
			if (buf.getDocumentOffset() != childstm.getOffset() || modifiedstatementpredicate.test(childstm)) {
				//repair
				final int buflen = buf.length();
				ParsingResult repaired = childrule.repairStatement(helper, childstm, childinfo, buf,
						contextmap.get(childrule), modifiedstatementpredicate, parsedata);
				if (repaired.isSucceeded()) {
					if (buflen == buf.length()) {
						if (!occounter.isValidOccurrenceCount()) {
							//not valid occurrence count, add as many times as required
							for (int added = occounter.addAnyMoreRequired(); added-- > 0;) {
								result.add(repaired);
							}
						}
					} else {
						result.add(repaired);
						occounter.addOccurrence();
					}
				} else {
					//failed to repair the statement, remove it, and remove an occurrence
//					occounter.removeOccurrence();
				}
				regionofinterest.expandTo(repaired.getParsingInformation().getRegionOfInterest());
			} else {
				//data not changed for statement
				occounter.addOccurrence();
				repairAndAdjustDocument(childrule, childstm, contextmap.get(childrule), childinfo, buf);
				result.add(new ParsingResult(childstm, childinfo));
				regionofinterest.expandTo(childinfo.getRegionOfInterest());
			}
		}
		executeParsing(helper, buf, result, occurrences, contextmap, regionofinterest);

		boolean failed = false;
		for (Entry<Rule, OccurrenceCounter> entry : occurrences.entrySet()) {
			if (!entry.getValue().isValidOccurrenceCount()) {
				failed = true;
				break;
			}
		}
		if (failed) {
			return result.fail(new ParsingInformation(this, regionofinterest));
		}

		s.removeFromStart(startslen - buf.length());
		return result.build(s, new DocumentRegion(startoffset, s.getDocumentOffset() - startoffset),
				new AnyOrderParsingInformation(this, regionofinterest, result.getParsingInformations(), occurrences));
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " [children=" + getChildren().size() + "]";
	}
}
