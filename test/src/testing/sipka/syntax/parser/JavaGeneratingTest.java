package testing.sipka.syntax.parser;

import sipka.syntax.parser.util.RuleJavaGenerator;
import testing.saker.SakerTest;

/**
 * The test just tests that the source generation doesn't crash.
 * <p>
 * The tested language was copied from saker.build
 */
@SakerTest
public class JavaGeneratingTest extends ParserTestCase {

	@Override
	protected void runTestImpl() throws Throwable {
		System.out.println(RuleJavaGenerator.generateLanguageJavaClass(getLanguage(), "test.Lang"));
	}

}
