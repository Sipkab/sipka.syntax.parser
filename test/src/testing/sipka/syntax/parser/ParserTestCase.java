package testing.sipka.syntax.parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import sipka.syntax.parser.model.ParseFailedException;
import sipka.syntax.parser.model.rule.Language;
import sipka.syntax.parser.model.rule.ParsingResult;
import sipka.syntax.parser.model.statement.Statement;
import sipka.syntax.parser.model.statement.repair.ReparationRegion;
import testing.saker.SakerTestCase;

public abstract class ParserTestCase extends SakerTestCase {

	protected Map<String, String> parameters;

	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		this.parameters = parameters;
		runTestImpl();
	}

	protected abstract void runTestImpl() throws Throwable;

	public byte[] getFileBytes(String filename) throws IOException {
		return Files.readAllBytes(getFilePath(filename));
	}

	protected final Path getFilePath(String filename) {
		return Paths.get(getClass().getName().replace('.', '/') + '/' + filename);
	}

	public Language getLanguage() throws IOException, ParseFailedException {
		try {
			return getTestLanguageFromPath(getFilePath("test.lang"));
		} catch (IOException e) {
			return getTestLanguageFromPath(Paths.get(getClass().getName().replace('.', '/') + ".lang"));
		}
	}

	private static Language getTestLanguageFromPath(Path path)
			throws IOException, ParseFailedException, AssertionError {
		Language lang = Language.fromFile(path.toFile()).get("test");
		assertNonNull(lang, "language");
		return lang;
	}

	public Statement parseStatement(String data) throws ParseFailedException, IOException {
		Statement stm = parseData(data).getStatement();
		stm.prettyprint(System.out);
		return stm;
	}

	public ParsingResult parseData(String data) throws ParseFailedException, IOException {
		ParsingResult result = getLanguage().parseData(data);
		validateResultConsistency(result);
		return result;
	}

	private static void validateResultConsistency(ParsingResult result) {
		if (result == null) {
			return;
		}
		Statement stm = result.getStatement();
		validateStatementConsistency(0, stm);
	}

	private static void validateStatementConsistency(int startoffset, Statement stm) {
		for (Statement childstm : stm.getDirectChildren()) {
			if (startoffset != childstm.getOffset()) {
				throw new AssertionError("Starting offset mismatch: " + startoffset + " - " + childstm.getOffset());
			}
			validateStatementConsistency(startoffset, childstm);
			startoffset = childstm.getEndOffset();
		}
	}

	public static ParsingResult repair(ParsingResult parseresult, List<ReparationRegion> modifications)
			throws ParseFailedException {
		ParsingResult result = parseresult.getStatement().repair(parseresult.getParsingInformation(), modifications);
		validateResultConsistency(result);
		return result;
	}

	public static ReparationRegion rr(int offset, int length, CharSequence text) {
		return new ReparationRegion(offset, length, text);
	}
}
