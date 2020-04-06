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

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
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

public class InOrderRule extends ContainerRule {
	protected static class InOrderParsingInformation extends CollectionParsingInformation {
		private List<OccurrenceCounter> occurrences;

		public InOrderParsingInformation(Rule rule, DocumentRegion regionofinterest, List<ParsingInformation> children,
				List<OccurrenceCounter> occurrences) {
			super(rule, regionofinterest, children);
			this.occurrences = occurrences;
		}

		@Override
		public CollectionParsingInformation clone() {
			InOrderParsingInformation result = (InOrderParsingInformation) super.clone();
			return result;
		}

		public List<OccurrenceCounter> getOccurrences() {
			return occurrences;
		}

	}

	public InOrderRule(String identifierName) {
		super(identifierName);
	}

	public InOrderRule() {
		super();
	}

	@Override
	protected ParsingResult parseChildren(ParseHelper helper, DocumentData s, ParseContext context,
			ParseTimeData parsedata) {
		CollectionStatement.Builder result = new CollectionStatement.Builder();

		int startoffset = s.getDocumentOffset();
		DocumentRegion regionofinterest = new DocumentRegion();
		regionofinterest.setOffset(startoffset);

		List<OccurrenceCounter> occurrences = new ArrayList<>();

		for (final Pair<Rule, ParseTimeData> rule : getChildren()) {
			ParseTimeData parsetimedata = rule.value;

			CallingContext rulecontext = CallingContext.merge(parsetimedata.getDeclaringContext(), context);

			OccurrenceCounter occounter = new OccurrenceCounter(parsetimedata.getOccurrence(rulecontext));

			occurrences.add(occounter);
			while (occounter.canOccurOnceMore()) {
				final int slen = s.length();
				ParsingResult parsed = rule.key.parseStatement(helper, s, rulecontext, parsetimedata);

				regionofinterest.expandTo(parsed.getParsingInformation().getRegionOfInterest());

				if (!parsed.isSucceeded()) {
					break;
				}

				if (slen == s.length()) {
					if (!occounter.isValidOccurrenceCount()) {
						//not valid occurrence count, add as many times as required
						int added = occounter.addAnyMoreRequired();
						while (added-- > 0) {
							result.add(parsed);
						}
					}
					break;
				}
				occounter.addOccurrence();
				result.add(parsed);
			}
			if (!occounter.isValidOccurrenceCount()) {
				return result.fail(new ParsingInformation(this, regionofinterest));
			}
		}
		return result.build(s, new DocumentRegion(startoffset, s.getDocumentOffset() - startoffset),
				new InOrderParsingInformation(this, regionofinterest, result.getParsingInformations(), occurrences));
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

		InOrderParsingInformation containerinfo = (InOrderParsingInformation) parsinginfo;
		List<ParsingInformation> childreninfos = containerinfo.getChildren();
		List<OccurrenceCounter> childrenoccurrences = containerinfo.getOccurrences();
		List<OccurrenceCounter> occurrences = new ArrayList<>();

		ListIterator<ParsingInformation> infoit = childreninfos.listIterator();
		ListIterator<OccurrenceCounter> occurit = childrenoccurrences.listIterator();
		ListIterator<Statement> childstmit = stmchildren.listIterator();

		Statement childstm = null;
		ParsingInformation childinfo = null;
		Rule childrule = null;
		if (childstmit.hasNext()) {
			childstm = childstmit.next();
			childinfo = infoit.next();
			childrule = childinfo.getRule();
		}
		for (final Pair<Rule, ParseTimeData> rulepair : getChildren()) {
			ParseTimeData parsetimedata = rulepair.value;
			CallingContext rulecontext = CallingContext.merge(parsetimedata.getDeclaringContext(), context);

			OccurrenceCounter occounter = new OccurrenceCounter(parsetimedata.getOccurrence(rulecontext));
//			occurrences.add(occounter);

			while (childrule == rulepair.key) {
				//we got already parsed statements for this rule
				if (buf.getDocumentOffset() != childstm.getOffset() || modifiedstatementpredicate.test(childstm)) {
					//repair
					final int buflen = buf.length();
					ParsingResult repaired = childrule.repairStatement(helper, childstm, childinfo, buf, rulecontext,
							modifiedstatementpredicate, parsedata);
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
//						occounter.removeOccurrence();
						//TODO remove any further matches to this rule???!!!!!!
					}
					regionofinterest.expandTo(repaired.getParsingInformation().getRegionOfInterest());
				} else {
					//no need to repair
					occounter.addOccurrence();
					childrule.repairStatementSkipped(childstm, rulecontext);
					buf.removeFromStart(childstm.getLength());
					result.add(new ParsingResult(childstm, childinfo));
					regionofinterest.expandTo(childinfo.getRegionOfInterest());
				}
				if (childstmit.hasNext()) {
					childstm = childstmit.next();
					childinfo = infoit.next();
					childrule = childinfo.getRule();
				} else {
					childstm = null;
					childinfo = null;
					childrule = null;
				}
			}
			//no more children with this rule
			//check if we can match this rule any more times
			while (occounter.canOccurOnceMore()) {
				final int buflen = buf.length();
				//XXX parsehelper is shit
				ParsingResult parsed = rulepair.key.parseStatement(helper, buf, rulecontext, parsetimedata);

				regionofinterest.expandTo(parsed.getParsingInformation().getRegionOfInterest());

				if (!parsed.isSucceeded()) {
					break;
				}

				if (buflen == buf.length()) {
					if (!occounter.isValidOccurrenceCount()) {
						//not valid occurrence count, add as many times as required
						for (int added = occounter.addAnyMoreRequired(); added-- > 0;) {
							result.add(parsed);
						}
					}
					break;
				}
				occounter.addOccurrence();
				result.add(parsed);
			}
			if (!occounter.isValidOccurrenceCount()) {
				return result.fail(new ParsingInformation(this, regionofinterest));
			}
		}

		s.removeFromStart(startslen - buf.length());
		return result.build(s, new DocumentRegion(startoffset, s.getDocumentOffset() - startoffset),
				new InOrderParsingInformation(this, regionofinterest, result.getParsingInformations(), occurrences));
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " [children=" + getChildren().size() + "]";
	}
}
