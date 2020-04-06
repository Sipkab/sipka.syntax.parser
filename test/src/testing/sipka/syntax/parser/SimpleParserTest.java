package testing.sipka.syntax.parser;

import sipka.syntax.parser.model.ParseFailedException;
import sipka.syntax.parser.model.statement.Statement;
import testing.saker.SakerTest;

@SakerTest
public class SimpleParserTest extends ParserTestCase {

	@Override
	protected void runTestImpl() throws Throwable {
		Statement stm = parseStatement("12");
		assertEquals(stm.getRawValue(), "12");

		assertException(ParseFailedException.class, () -> parseStatement("123"));
		assertException(ParseFailedException.class, () -> parseStatement("1"));
	}

}
