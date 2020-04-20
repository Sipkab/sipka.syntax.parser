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

import java.util.Iterator;
import java.util.List;
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
import sipka.syntax.parser.model.rule.RuleVisitor;
import sipka.syntax.parser.model.rule.container.ContainerRule;
import sipka.syntax.parser.model.statement.CollectionStatement;
import sipka.syntax.parser.model.statement.Statement;
import sipka.syntax.parser.model.statement.repair.CollectionParsingInformation;
import sipka.syntax.parser.model.statement.repair.ParsingInformation;
import sipka.syntax.parser.util.Pair;

public class FirstOrderRule extends ContainerRule {
	protected static class FirstOrderParsingInformation extends CollectionParsingInformation {
		private OccurrenceCounter occounter;
		private Pair<Rule, ParseTimeData> parsedRule;

		public FirstOrderParsingInformation(Rule rule, DocumentRegion regionOfInterest,
				List<ParsingInformation> children, Pair<Rule, ParseTimeData> parsedrule, OccurrenceCounter occounter) {
			super(rule, regionOfInterest, children);
			this.parsedRule = parsedrule;
			this.occounter = occounter;
		}

		@Override
		public CollectionParsingInformation clone() {
			FirstOrderParsingInformation result = (FirstOrderParsingInformation) super.clone();
			return result;
		}

		public OccurrenceCounter getOccurrenceCounter() {
			return occounter;
		}

	}

	public FirstOrderRule(String identifierName) {
		super(identifierName);
	}

	@Override
	public void accept(RuleVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	protected ParsingResult parseChildren(ParseHelper helper, DocumentData s, ParseContext context,
			ParseTimeData parsedata) {
		CollectionStatement.Builder result = new CollectionStatement.Builder();

		int startoffset = s.getDocumentOffset();
		DocumentRegion regionofinterest = new DocumentRegion();
		regionofinterest.setOffset(startoffset);

		OccurrenceCounter occounter = new OccurrenceCounter();

		for (final Pair<Rule, ParseTimeData> rule : getChildren()) {
			ParseTimeData parsetimedata = rule.value;
			ParseContext rulecontext = CallingContext.merge(parsetimedata.getDeclaringContext(), context);
			occounter.reset(parsetimedata.getOccurrence(helper, rulecontext));
			result.clear();
			DocumentData buf = new DocumentData(s);

			final int slen = buf.length();
			while (occounter.canOccurOnceMore()) {
				final int buflen = buf.length();
				ParsingResult parsed = rule.key.parseStatement(helper, buf, rulecontext, parsetimedata);

				regionofinterest.expandTo(parsed.getParsingInformation().getRegionOfInterest());

				if (!parsed.isSucceeded()) {
					break;
				}

				if (buflen == buf.length()) {
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
			if (occounter.isValidOccurrenceCount()) {
				s.removeFromStart(slen - buf.length());
				return result.build(s, new DocumentRegion(startoffset, s.getDocumentOffset() - startoffset),
						new FirstOrderParsingInformation(this, regionofinterest, result.getParsingInformations(), rule,
								occounter));
			}
		}
		return result.fail(new ParsingInformation(this, regionofinterest));
	}

	@Override
	protected ParsingResult repairChildren(ParseHelper helper, Statement statement, ParsingInformation parsinginfo,
			DocumentData s, ParseContext context, Predicate<? super Statement> modifiedstatementpredicate,
			ParseTimeData parsedata) {
		CollectionStatement.Builder result = new CollectionStatement.Builder();
		int startoffset = s.getDocumentOffset();
		DocumentRegion regionofinterest = new DocumentRegion();
		regionofinterest.setOffset(startoffset);

		List<Statement> stmchildren = statement.getDirectChildren();

		FirstOrderParsingInformation containerinfo = (FirstOrderParsingInformation) parsinginfo;
		List<ParsingInformation> childreninfos = containerinfo.getChildren();

		Iterator<ParsingInformation> infoit = childreninfos.listIterator();
		Iterator<Statement> childstmit = stmchildren.listIterator();

		//we cant repair the child statement first, as preceding rules could match instead

		OccurrenceCounter occounter = new OccurrenceCounter();

		for (final Pair<Rule, ParseTimeData> rule : getChildren()) {
			ParseTimeData parsetimedata = rule.value;
			ParseContext rulecontext = CallingContext.merge(parsetimedata.getDeclaringContext(), context);
			result.clear();

			DocumentData buf = new DocumentData(s);
			final int slen = buf.length();

			occounter.reset(parsetimedata.getOccurrence(helper, rulecontext));
			if (rule == containerinfo.parsedRule) {
				//try to repair the previous match
				while (childstmit.hasNext()) {
					Statement childstm = childstmit.next();
					ParsingInformation childinfo = infoit.next();

					if (buf.getDocumentOffset() != childstm.getOffset() || modifiedstatementpredicate.test(childstm)) {
						//repair
						final int buflen = buf.length();
						ParsingResult repaired = rule.key.repairStatement(helper, childstm, childinfo, buf, rulecontext,
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
//							occounter.removeOccurrence();
							//TODO remove any further matches to this rule???!!!!!!
						}
						regionofinterest.expandTo(repaired.getParsingInformation().getRegionOfInterest());
					} else {
						//data not changed for statement
						occounter.addOccurrence();
						repairAndAdjustDocument(rule.key, childstm, rulecontext, childinfo, buf);
						result.add(new ParsingResult(childstm, childinfo));
						regionofinterest.expandTo(childinfo.getRegionOfInterest());
					}
				}
			}

			while (occounter.canOccurOnceMore()) {
				final int buflen = buf.length();
				ParsingResult parsed = rule.key.parseStatement(helper, buf, rulecontext, parsetimedata);

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
			if (occounter.isValidOccurrenceCount()) {
				s.removeFromStart(slen - buf.length());
				return result.build(s, new DocumentRegion(startoffset, s.getDocumentOffset() - startoffset),
						new FirstOrderParsingInformation(this, regionofinterest, result.getParsingInformations(), rule,
								occounter));
			}
		}
		return result.fail(new ParsingInformation(this, regionofinterest));
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " [children=" + getChildren().size() + "]";
	}

}
