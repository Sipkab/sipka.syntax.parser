package testing.sipka.syntax.parser;

import sipka.syntax.parser.model.rule.ParsingResult;
import sipka.syntax.parser.model.statement.repair.ReparationRegion;
import testing.saker.SakerTest;

@SakerTest
public class ValueFailRecoverTest extends ParserTestCase {

	@Override
	protected void runTestImpl() throws Throwable {
		ParsingResult parseresult = parseData("123");
		assertEquals(parseresult.getStatement().firstValue("v"), "123");

		System.out.println(parseresult.getParsingInformation());
		System.out.println(parseresult.getParsingInformation().getRule());

		parseresult = repair(parseresult, listOf(new ReparationRegion(1, 2, "2x")));
		assertEquals(parseresult.getStatement().firstValue("v"), "12x");

		parseresult = repair(parseresult, listOf(new ReparationRegion(1, 2, "23")));
		assertEquals(parseresult.getStatement().firstValue("v"), "123");
	}

}
