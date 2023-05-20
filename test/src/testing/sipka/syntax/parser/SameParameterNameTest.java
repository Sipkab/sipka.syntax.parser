package testing.sipka.syntax.parser;

import sipka.syntax.parser.model.rule.ParsingResult;
import testing.saker.SakerTest;

@SakerTest
public class SameParameterNameTest extends ParserTestCase {

	@Override
	protected void runTestImpl() throws Throwable {
		ParsingResult result = parseData("123");
		assertEquals(result.getStatement().firstValue("numbers"), "123");
	}

}
