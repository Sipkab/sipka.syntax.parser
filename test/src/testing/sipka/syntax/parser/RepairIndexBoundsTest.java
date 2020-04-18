package testing.sipka.syntax.parser;

import sipka.syntax.parser.model.rule.ParsingResult;
import sipka.syntax.parser.model.statement.repair.ReparationRegion;
import testing.saker.SakerTest;

@SakerTest
public class RepairIndexBoundsTest extends ParserTestCase {

	@Override
	protected void runTestImpl() throws Throwable {
		ParsingResult result = parseData("123");
		assertEquals(result.getStatement().firstValue("v"), "123");

		ReparationRegion rr1 = rr(0, 0, "1");
		result = repair(result, listOf(rr1, rr1, rr1, rr1, rr1, rr1, rr(0, 9, "")));
		assertEquals(result.getStatement().firstValue("v"), "");
	}

}
