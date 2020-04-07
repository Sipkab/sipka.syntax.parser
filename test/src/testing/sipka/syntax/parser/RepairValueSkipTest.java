package testing.sipka.syntax.parser;

import sipka.syntax.parser.model.rule.ParsingResult;
import testing.saker.SakerTest;

@SakerTest
public class RepairValueSkipTest extends ParserTestCase {

	@Override
	protected void runTestImpl() throws Throwable {
		ParsingResult result = parseData("123abc");
		assertEquals(result.getStatement().firstValue("v"), "123abc");

		result = repair(result, listOf(rr(0, 1, "0")));
		assertEquals(result.getStatement().firstValue("v"), "023abc");
	}

}
