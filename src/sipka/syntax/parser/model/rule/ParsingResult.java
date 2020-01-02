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
package sipka.syntax.parser.model.rule;

import java.io.PrintStream;
import java.util.Iterator;

import sipka.syntax.parser.model.parse.document.DocumentRegion;
import sipka.syntax.parser.model.statement.Statement;
import sipka.syntax.parser.model.statement.repair.ParsingInformation;

public class ParsingResult {
	private Statement statement;
	private ParsingInformation parsingInformation;

	public ParsingResult(Statement statement, ParsingInformation parsingInformation) {
		this.statement = statement;
		this.parsingInformation = parsingInformation;

		if (statement != null) {
			DocumentRegion interest = parsingInformation.getRegionOfInterest();
			DocumentRegion pos = statement.getPosition();
			if (!interest.isInside(pos)) {
				throw new IllegalStateException(
						"Statement position is not fully inside its interest region. " + pos + " - " + interest);
			}
		}
	}

	public boolean isSucceeded() {
		return statement != null;
	}

	public Statement getStatement() {
		return statement;
	}

	public ParsingInformation getParsingInformation() {
		return parsingInformation;
	}

	@Override
	public String toString() {
		return "ParsingResult [" + (statement != null ? "statement=" + statement + ", " : "")
				+ (parsingInformation != null ? "parsingInformation=" + parsingInformation : "") + "]";
	}

	private void printInformationStructure(PrintStream out, Statement stm, ParsingInformation info) {
		Iterator<Statement> stmit = stm.getDirectChildren().iterator();
		Iterator<ParsingInformation> infoit = info.getChildren().iterator();
		while (stmit.hasNext()) {
			Statement cstm = stmit.next();
			ParsingInformation cinfo = infoit.next();
			DocumentRegion interest = cinfo.getRegionOfInterest();
			DocumentRegion pos = cstm.getPosition();
			out.println(cstm.getClass().getSimpleName() + ":\t" + cstm.getName() + ":\t\"" + cstm.getValue() + "\"\t["
					+ pos.getOffset() + ", " + pos.getEndOffset() + ")\tinterest:\t[" + interest.getOffset() + ", "
					+ interest.getEndOffset() + ")");
			if (!interest.isInside(pos)) {
				throw new IllegalStateException("Statement position is not fully inside its interest region.");
			}
			printInformationStructure(out, cstm, cinfo);
		}
	}

	public void printInformationStructure(PrintStream out) {
		printInformationStructure(out, statement, parsingInformation);
	}
}
