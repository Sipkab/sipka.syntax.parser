package testing.sipka.syntax.parser;

import sipka.syntax.parser.model.rule.ParsingResult;
import testing.saker.SakerTest;

@SakerTest
public class ValueFailRecoverTest extends ParserTestCase {

	@Override
	protected void runTestImpl() throws Throwable {
		ParsingResult result = parseData("123");
		assertEquals(result.getStatement().firstValue("v"), "123");

		result = repair(result, listOf(rr(1, 2, "2x")));
		assertEquals(result.getStatement().firstValue("v"), "12x");

		result = repair(result, listOf(rr(1, 2, "23")));
		assertEquals(result.getStatement().firstValue("v"), "123");
	}

}
