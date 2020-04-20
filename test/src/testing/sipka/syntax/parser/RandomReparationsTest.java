package testing.sipka.syntax.parser;

import java.io.IOException;
import java.util.Random;

import sipka.syntax.parser.model.ParseFailedException;
import sipka.syntax.parser.model.rule.ParsingResult;
import sipka.syntax.parser.model.statement.repair.ReparationRegion;
import testing.saker.SakerTest;

@SakerTest
public class RandomReparationsTest extends ParserTestCase {

	@Override
	protected void runTestImpl() throws Throwable {
		//this caused the input to become empty, at index 8691
		testWithSeed(1587319149180L, 10000);
		testWithSeed(System.currentTimeMillis());
	}

	private void testWithSeed(long seed) throws ParseFailedException, IOException, Exception {
		testWithSeed(seed, 50000);
	}

	private void testWithSeed(long seed, int iterationcount) throws ParseFailedException, IOException, Exception {
		System.out.println("Seed: " + seed);
		Random r = new Random(seed);

		int len = r.nextInt(32 + r.nextInt(32));
		StringBuilder sb = new StringBuilder(generateRandomNumber(r, len));
		System.out.println(sb);

		ParsingResult result = parseData(sb.toString());

		for (int i = 0; i < iterationcount; i++) {
			int editoffset = sb.length() == 0 ? 0 : r.nextInt(sb.length());
			int editlen = r.nextInt(10);
			if (editoffset + editlen > sb.length()) {
				editlen = sb.length() - editoffset;
			}
			String text = generateRandomNumber(r, r.nextInt(10));
			ReparationRegion rr = rr(editoffset, editlen, text);
			rr.apply(sb);
			try {
				ParsingResult nresult = result.getStatement().repair(result.getParsingInformation(), listOf(rr));
				if (!nresult.getStatement().getRawValue().equals(sb.toString())) {
					System.err.println("index: " + i);
					System.err.println(rr);
					System.err.println("Old:");
					System.err.println(result.getStatement().getRawValue());
					System.err.println("New:");
					System.err.println(nresult.getStatement().getRawValue());
					System.err.println("Expected:");
					System.err.println(sb);
					fail("Content mismatch.");
				}
				result = nresult;
			} catch (Exception e) {
				System.err.println("index: " + i);
				System.err.println(rr);
				System.err.println(sb);
				throw e;
			}
		}
	}

	private static String generateRandomNumber(Random r, int len) {
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			sb.append((char) ('0' + r.nextInt(10)));
		}
		return sb.toString();
	}
}
