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
package sipka.syntax.parser.model.rule.consume;

import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sipka.syntax.parser.model.parse.ParseTimeData;
import sipka.syntax.parser.model.parse.context.ParseContext;
import sipka.syntax.parser.model.parse.document.DocumentData;
import sipka.syntax.parser.model.parse.document.DocumentRegion;
import sipka.syntax.parser.model.parse.params.InvokeParam;
import sipka.syntax.parser.model.rule.ParseHelper;
import sipka.syntax.parser.model.rule.ParsingResult;
import sipka.syntax.parser.model.rule.Rule;
import sipka.syntax.parser.model.statement.ConsumedStatement;
import sipka.syntax.parser.model.statement.Statement;
import sipka.syntax.parser.model.statement.repair.ParsingInformation;
import sipka.syntax.parser.util.ArrayRangeCharSequence;

public abstract class ConsumeRule extends Rule {
	private final InvokeParam<Pattern> param;

	public ConsumeRule(String identifierName, InvokeParam<Pattern> param) {
		super(identifierName);
		this.param = param;
	}

	public InvokeParam<Pattern> getParam() {
		return param;
	}

	/**
	 * Notifies the implementation about the successfull match of characters.
	 * <p>
	 * Subclasses can implement their own actions to handle the parsing of characters from the input.
	 * 
	 * @param matcher
	 *            The matcher that was used to parse the characters.
	 * @param parsed
	 *            The parsed characters.
	 * @param context
	 *            The context of parsing.
	 */
	protected void charactersConsumed(Matcher matcher, ArrayRangeCharSequence parsed, ParseContext context) {
	}

	private ParsingResult createStatement(ArrayRangeCharSequence parsed, DocumentRegion position,
			DocumentRegion regionofinterest) {
		return new ParsingResult(new ConsumedStatement(parsed, position),
				new ParsingInformation(this, regionofinterest));
	}

	private ArrayRangeCharSequence tryParse(ParseHelper helper, DocumentData s, Pattern pattern, ParseContext context,
			DocumentRegion outregionofinterest) {
		AccessTrackingCharSequence trackingcs = new AccessTrackingCharSequence(s);
		Matcher matcher = helper.getMatcher(pattern, trackingcs);

		final int start;
		final int end;

		final boolean found = matcher.find();
		//if the pattern requires a minimum length, then the tracking charsequence will not work
		outregionofinterest.setOffset(s.getDocumentOffset());
		if (matcher.hitEnd()) {
			//if the matching hits the end of the input, use the length as end of region of interest
			//add one to get modified when a character is added to the string
			outregionofinterest.setLength(s.length() + 1);
		} else {
			//set the end of the region of interest to the last accessed character
			outregionofinterest.setLength(trackingcs.getMaxAccessIndex() + 1);
		}
		if (!found || (start = matcher.start()) != 0
//				the following is commented out, because we allow consume rules to match empty.
//				|| (end = matcher.end()) == 0
		) {
			return null;
		}
		end = matcher.end();
		if (outregionofinterest.getLength() < end - start) {
			//if by any chance the region is smaller than the matched length, set it
			//this shouldnt normally occurr
			outregionofinterest.setLength(end - start);
			//XXX throw an exception for debugging
			throw new AssertionError();
		}
		ArrayRangeCharSequence parsed = s.subSequence(start, end);
		charactersConsumed(matcher, parsed, context);
		return parsed;
	}

	@Override
	protected final ParsingResult parseStatementImpl(ParseHelper helper, DocumentData s, ParseContext context,
			ParseTimeData parsedata) {
		int startoffset = s.getDocumentOffset();

		Pattern pattern = param.getValue(helper, context);
		DocumentRegion regionofinterest = new DocumentRegion();
		ArrayRangeCharSequence parsed = tryParse(helper, s, pattern, context, regionofinterest);
		if (parsed == null) {
			return new ParsingResult(null, new ParsingInformation(this, regionofinterest));
		}
		DocumentRegion position = new DocumentRegion(startoffset, parsed.length());
		s.removeFromStart(parsed.length());

		return createStatement(parsed, position, regionofinterest);
	}

	@Override
	protected ParsingResult repairStatementImpl(ParseHelper helper, Statement statement, ParsingInformation parsinginfo,
			DocumentData s, ParseContext context, Predicate<? super Statement> modifiedstatementpredicate,
			ParseTimeData parsedata) {
		return parseStatementImpl(helper, s, context, parsedata);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " [" + (param != null ? "param=" + param + ", " : "")
				+ (getIdentifierName() != null ? "getIdentifierName()=" + getIdentifierName() : "") + "]";
	}

}
