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
		Language lang = Language.fromFile(getFilePath("test.lang").toFile()).get("test");
		assertNonNull(lang, "language");
		return lang;
	}

	public Statement parseStatement(String data) throws ParseFailedException, IOException {
		Statement stm = parseData(data).getStatement();
		stm.prettyprint(System.out);
		return stm;
	}

	public ParsingResult parseData(String data) throws ParseFailedException, IOException {
		return getLanguage().parseData(data);
	}

	public static ParsingResult repair(ParsingResult parseresult, List<ReparationRegion> modifications)
			throws ParseFailedException {
		ParsingResult repairresult = parseresult.getStatement().repair(parseresult.getParsingInformation(),
				modifications);
		return repairresult;
	}
}
