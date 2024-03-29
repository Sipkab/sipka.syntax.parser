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
package sipka.syntax.parser.model.statement;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import sipka.syntax.parser.model.ParseFailedException;
import sipka.syntax.parser.model.parse.context.ParseContext;
import sipka.syntax.parser.model.parse.document.DocumentData;
import sipka.syntax.parser.model.parse.document.DocumentRegion;
import sipka.syntax.parser.model.rule.Language;
import sipka.syntax.parser.model.rule.ParseHelper;
import sipka.syntax.parser.model.rule.ParsingResult;
import sipka.syntax.parser.model.statement.repair.ParsingInformation;
import sipka.syntax.parser.model.statement.repair.ReparationRegion;
import sipka.syntax.parser.util.ArrayRangeCharSequence;
import sipka.syntax.parser.util.Pair;

public abstract class Statement implements Serializable, Cloneable {
	private static final long serialVersionUID = 4581488159262562627L;

	protected DocumentRegion position;

	public Statement(DocumentRegion position) {
		this.position = position;
	}

	public abstract List<Statement> getDirectChildren();

	public abstract Map<String, List<Statement>> getPossibleScopes();

	public abstract List<Pair<String, Statement>> getScopes();

	protected abstract ArrayRangeCharSequence toValueSequence();

	protected abstract ArrayRangeCharSequence toRawSequence();

	public abstract List<Statement> scopeTo(String scoper);

	public List<String> scopeValues(String scoper) {
		List<String> result = new ArrayList<>();
		for (Statement stm : scopeTo(scoper)) {
			result.add(stm.getValue());
		}
		return result;
	}

	public boolean isScopesEmpty() {
		return getScopes().isEmpty();
	}

	public boolean isScopesEmpty(String scoper) {
		return firstScope(scoper) == null;
	}

	public Statement firstScope(String scoper) {
		List<Statement> scopes = scopeTo(scoper);
		if (!scopes.isEmpty())
			return scopes.get(0);
		return null;
	}

	public String firstValue(String scoper) {
		Statement resscope = firstScope(scoper);
		return resscope == null ? null : resscope.getValue();
	}

	public void collectValues(String statementname, Collection<? super String> result) {
		if (statementname.equals(getName())) {
			result.add(getValue());
		}
		for (Pair<String, Statement> s : getScopes()) {
			s.value.collectValues(statementname, result);
		}
	}

	public void collectValues(Map<String, ? extends Collection<String>> statementnameresults) {
		Collection<String> coll = statementnameresults.get(getName());
		if (coll != null) {
			coll.add(getValue());
		}
		for (Pair<String, Statement> s : getScopes()) {
			s.value.collectValues(statementnameresults);
		}
	}

	public void collectStatements(String statementname, Collection<? super Statement> result) {
		if (statementname.equals(getName())) {
			result.add(this);
		}
		for (Pair<String, Statement> s : getScopes()) {
			s.value.collectStatements(statementname, result);
		}
	}

	public void collectStatements(Map<String, ? extends Collection<Statement>> statementnameresults) {
		Collection<Statement> coll = statementnameresults.get(getName());
		if (coll != null) {
			coll.add(this);
		}
		for (Pair<String, Statement> s : getScopes()) {
			s.value.collectStatements(statementnameresults);
		}
	}

	public void collectStatements(Consumer<? super Statement> consumer) {
		consumer.accept(this);
		for (Pair<String, Statement> s : getScopes()) {
			s.value.collectStatements(consumer);
		}
	}

	public Pair<String, Statement> getScopeAtOffset(int offset) {
		for (Pair<String, Statement> s : getScopes()) {
			if (s.value.getPosition().isInside(offset)) {
				return s;
			}
		}
		return null;
	}

	public String getName() {
		return "";
	}

	public final String getValue() {
		return toValueSequence().toString();
	}

	public final String getRawValue() {
		return toRawSequence().toString();
	}

	public final CharSequence getValueSequence() {
		return toValueSequence();
	}

	public final CharSequence getRawValueSequence() {
		return toRawSequence();
	}

	@Override
	public Statement clone() {
		try {
			Statement result = (Statement) super.clone();
			result.position = this.position.clone();
			return result;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("Failed to clone " + this.getClass());
		}
	}

	@Override
	public String toString() {
		return getName() + ": " + getValue();
	}

	public final String toDocumentPositionString() {
		return position.getOffset() + " - " + (position.getOffset() + position.getLength());
	}

	public final int getLength() {
		return position.getLength();
	}

	public final int getOffset() {
		return position.getOffset();
	}

	public final int getEndOffset() {
		return position.getEndOffset();
	}

	public final DocumentRegion getPosition() {
		return position;
	}

	private static void collectAllInformationImpl(Map<Statement, ParsingInformation> result, Statement stm,
			ParsingInformation info) {
		result.put(stm, info);
		Iterator<ParsingInformation> infoit = info.getChildren().iterator();
		Iterator<Statement> stmit = stm.getDirectChildren().iterator();
		while (infoit.hasNext()) {
			if (!stmit.hasNext()) {
				throw new IllegalStateException();
			}
			Statement stmnext = stmit.next();
			ParsingInformation infonext = infoit.next();
			collectAllInformationImpl(result, stmnext, infonext);
		}
		if (stmit.hasNext()) {
			throw new IllegalStateException();
		}
	}

	private static Map<Statement, ParsingInformation> collectAllInformation(Statement statement,
			ParsingInformation parsingInformation) {
		//create identity hashmap as the keys hash will be modified
		Map<Statement, ParsingInformation> result = new IdentityHashMap<>();
		collectAllInformationImpl(result, statement, parsingInformation);
		return result;
	}

	private static void applyRegionDelete(DocumentRegion region, int deleteoffset, int deletelength) {
		int overlap = region.getOverlappingLength(deleteoffset, deletelength);
		if (overlap > 0) {
			if (deleteoffset < region.getOffset()) {
				//deleted data before the statement
				region.set(deleteoffset, region.getLength() - overlap);
			} else {
				//deleted data inside statement
				//not moving offset
				region.setLength(region.getLength() - overlap);
			}
		} else {
			//no overlapping data with deletion
			//or the region is empty
			if (deleteoffset < region.getOffset()) {
				int diff = region.getOffset() - deleteoffset;
				//data deleted before this statement
				region.setOffset(region.getOffset() - Math.min(diff, deletelength));
			}
		}
	}

	private static void applyRegionInsert(DocumentRegion region, int insertoffset, int insertlength) {
		if (insertoffset <= region.getOffset()) {
			//inserted before this statement
			region.setOffset(region.getOffset() + insertlength);
		} else {
			int overlap = region.getOverlappingLength(insertoffset, insertlength);
			if (overlap > 0) {
				region.setLength(region.getLength() + overlap);
			}
		}
	}

	private static int getFinalLength(int original, Iterable<ReparationRegion> reparations, int[] outdiffsum) {
		int diffsum = 0;
		for (ReparationRegion rr : reparations) {
			CharSequence text = rr.getText();
			int tlen = text.length();
			int diff = tlen - rr.getLength();
			original = original + diff;
			if (diff > 0) {
				diffsum += diff;
			}
		}
		outdiffsum[0] = diffsum;
		return original;
	}

	private ParsingResult repairImpl(ParsingInformation parsinginfo, List<ReparationRegion> reparations,
			ParseContext context) throws ParseFailedException {
		Map<Statement, ParsingInformation> allinformation = collectAllInformation(this, parsinginfo);
		Collection<Statement> contentmodifiedstatements = Collections
				.newSetFromMap(new IdentityHashMap<>(allinformation.size()));

		ArrayRangeCharSequence originalrawseq = toRawSequence();

		int[] diffsum = { 0 };
		int originallength = originalrawseq.length();
		int newlen = getFinalLength(originallength, reparations, diffsum);
		char[] nrawarray = Arrays.copyOfRange(originalrawseq.array(), originalrawseq.index(),
				Math.max(originallength + diffsum[0], newlen));
		int workinglen = originallength;

		for (ReparationRegion r : reparations) {
			int regionoffset = r.getOffset();
			int deletelength = r.getLength();
			CharSequence rtext = r.getText();
			int insertlength = rtext == null ? 0 : rtext.length();
			r.apply(nrawarray, workinglen);
			workinglen += insertlength - deletelength;
			if (deletelength > 0) {
				for (Entry<Statement, ParsingInformation> entry : allinformation.entrySet()) {
					Statement s = entry.getKey();
					DocumentRegion spos = s.getPosition();
					DocumentRegion regionofinterest = entry.getValue().getRegionOfInterest();
					int interestoverlap = regionofinterest.getOverlappingLength(regionoffset, deletelength);
					if (interestoverlap > 0) {
						contentmodifiedstatements.add(entry.getKey());
					}
					applyRegionDelete(spos, regionoffset, deletelength);
					applyRegionDelete(regionofinterest, regionoffset, deletelength);

					if (!regionofinterest.isInside(spos)) {
						throw new IllegalStateException("Statement position is not fully inside its interest region. "
								+ spos + " - " + regionofinterest);
					}
				}
			}
			if (insertlength > 0) {
				for (Entry<Statement, ParsingInformation> entry : allinformation.entrySet()) {
					Statement s = entry.getKey();
					DocumentRegion spos = s.getPosition();
					DocumentRegion regionofinterest = entry.getValue().getRegionOfInterest();
					int interestoverlap = regionofinterest.getOverlappingLength(regionoffset, insertlength);
					if (interestoverlap > 0) {
						contentmodifiedstatements.add(entry.getKey());
					}
					applyRegionInsert(spos, regionoffset, insertlength);
					applyRegionInsert(regionofinterest, regionoffset, insertlength);

					if (!regionofinterest.isInside(spos)) {
						throw new IllegalStateException("Statement position is not fully inside its interest region. "
								+ spos + " - " + regionofinterest);
					}
				}
			}
		}

		if (contentmodifiedstatements.contains(this)) {
			DocumentData ndocdata = new DocumentData(nrawarray, 0, newlen);
			ParsingResult repaired = parsinginfo.getRule().repairStatement(new ParseHelper(), this, parsinginfo,
					ndocdata, context, contentmodifiedstatements::contains, Language.PARSE_ONCE());
			if (repaired == null || repaired.getStatement() == null
					|| repaired.getStatement().getEndOffset() != newlen) {
				//TODO diagnostics
				throw new ParseFailedException("Failed to parse whole input.");
			}
			return repaired;
		}
		return new ParsingResult(this, parsinginfo);
	}

	public final ParsingResult repair(ParsingInformation parsinginfo, List<ReparationRegion> reparations,
			ParseContext context) throws ParseFailedException {
		if (reparations.isEmpty()) {
			return new ParsingResult(this, parsinginfo);
		}

		Statement stm = this.clone();
		ParsingInformation infoclone = parsinginfo.clone();
		return stm.repairImpl(infoclone, reparations, context);
	}

	public final ParsingResult repair(ParsingInformation parsinginfo, List<ReparationRegion> reparations)
			throws ParseFailedException {
		return repair(parsinginfo, reparations, ParseContext.EMPTY);
	}

	private static void prettyprint(PrintStream out, Statement stm, int tabcount) {
		for (Pair<String, Statement> scope : stm.getScopes()) {
			for (int i = 0; i < tabcount; i++) {
				out.print("|\t");
			}
			out.println(scope.key + ": \"" + scope.value.toValueSequence() + "\" - " + scope.value.getOffset() + " - "
					+ scope.value.getEndOffset());
			prettyprint(out, scope.value, tabcount + 1);
		}
	}

	public void prettyprint(PrintStream out) {
		if ("".equals(this.getName())) {
			prettyprint(out, this, 0);
		} else {
			out.println(this.getName() + ": \"" + this.getValue() + "\"");
			prettyprint(out, this, 1);
		}
	}

	private static void prettyprintAll(PrintStream out, Statement stm, int tabcount) {
		for (Statement child : stm.getDirectChildren()) {
			for (int i = 0; i < tabcount; i++) {
				out.print("|\t");
			}
			out.println(child.getName() + ": \"" + child.toValueSequence() + "\" - " + child.getOffset() + " - "
					+ child.getEndOffset() + " - " + child.getClass().getSimpleName());
			prettyprintAll(out, child, tabcount + 1);
		}
	}

	public void prettyprintAll(PrintStream out) {
		if ("".equals(this.getName())) {
			prettyprintAll(out, this, 0);
		} else {
			out.println(this.getName() + ": \"" + this.getValue() + "\"");
			prettyprintAll(out, this, 1);
		}
	}

	private void collectTokensImpl(List<Pair<String, Statement>> result) {
		List<Pair<String, Statement>> scopes = getScopes();
		for (Pair<String, Statement> s : scopes) {
			result.add(s);
			s.value.collectTokensImpl(result);
		}
	}

	private List<Pair<String, Statement>> collectTokens() {
		List<Pair<String, Statement>> result = new ArrayList<>();
		if (!this.getName().isEmpty()) {
			result.add(new Pair<>(this.getName(), this));
		}
		collectTokensImpl(result);
		return result;
	}

	public Iterator<Pair<String, Statement>> tokenIterator() {
		return collectTokens().iterator();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((position == null) ? 0 : position.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Statement other = (Statement) obj;
		if (position == null) {
			if (other.position != null)
				return false;
		} else if (!position.equals(other.position))
			return false;
		return true;
	}

	public final boolean contentsEquals(Statement other) {
		if (other == null) {
			return false;
		}
		if (!this.getName().equals(other.getName())) {
			return false;
		}
		if (!toValueSequence().equals(other.toValueSequence())) {
			return false;
		}
		List<Pair<String, Statement>> oscopes = other.getScopes();
		List<Pair<String, Statement>> scopes = getScopes();
		if (oscopes.size() != scopes.size()) {
			return false;
		}
		for (int i = 0; i < scopes.size(); i++) {
			Pair<String, Statement> pair = scopes.get(i);
			Pair<String, Statement> opair = oscopes.get(i);
			if (!pair.value.contentsEquals(opair.value)) {
				return false;
			}
		}
		return true;
	}

	public static boolean contentsEquals(Statement left, Statement right) {
		return (left == right) || (left != null && left.contentsEquals(right));
	}
}
