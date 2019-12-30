package sipka.syntax.parser.model.rule;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import sipka.syntax.parser.model.ParsingCancelledException;
import sipka.syntax.parser.model.parse.ParseTimeData;
import sipka.syntax.parser.model.parse.context.ParseContext;
import sipka.syntax.parser.model.parse.document.DocumentData;
import sipka.syntax.parser.model.parse.document.DocumentRegion;
import sipka.syntax.parser.model.statement.Statement;
import sipka.syntax.parser.model.statement.repair.ParsingInformation;
import sipka.syntax.parser.util.Pair;

public abstract class Rule {
	public static final String BUILTIN_VAR_PREFIX = "@";
	private static final String REPLACE_ALIAS_VAR_NAME = BUILTIN_VAR_PREFIX + "alias_replace";
	private static final String PARAMETER_VAR_NAME = BUILTIN_VAR_PREFIX + "param";

	private static final AtomicInteger ruleCounter = new AtomicInteger();

	private final String identifierName;
	private List<Pair<String, Class<?>>> params;
	private final int ruleId;
	private DocumentRegion ruleDocumentPosition;

	public Rule(String identifierName) {
		this(identifierName, null);
	}

	public Rule(String identifierName, DocumentRegion ruledocposition) {
		this.identifierName = identifierName;
		this.ruleId = ruleCounter.getAndIncrement();
		this.ruleDocumentPosition = ruledocposition;
	}

	public DocumentRegion getRuleDocumentPosition() {
		return ruleDocumentPosition;
	}

	/* package */ void setRuleDocumentPosition(DocumentRegion ruleDocumentPosition) {
		this.ruleDocumentPosition = ruleDocumentPosition;
	}

	protected abstract ParsingResult parseStatementImpl(ParseHelper helper, DocumentData s, ParseContext context,
			ParseTimeData parsedata);

	protected abstract ParsingResult repairStatementImpl(Statement statement, ParsingInformation parsinginfo,
			DocumentData s, ParseContext context, Predicate<? super Statement> modifiedstatementpredicate,
			ParseTimeData parsedata);

	/**
	 * XXX documented to avoid unused warning. might document method later
	 * 
	 * @param statement
	 * @param context
	 */
	protected void repairStatementSkippedImpl(Statement statement, ParseContext context) {
	}

	public final ParsingResult parseStatement(ParseHelper helper, DocumentData s, ParseContext context,
			ParseTimeData parsedata) {
		if (helper.getProgressMonitor().isCancelled()) {
			throw new ParsingCancelledException();
		}
		helper.pushRule(this);
		try {
			// System.out.println("parse with " + s + " " + getRuleId() + " " + this + " " );
			DocumentData cs = new DocumentData(s);

			final int slen = cs.length();
			// System.out.println("try parse with: " + getRuleId() + " " + this);
			ParsingResult result = parseStatementImpl(helper, cs, context, parsedata);

			if (result.isSucceeded()) {
				int count = slen - cs.length();
				s.removeFromStart(count);
			}
			// System.out.println("parsed with count: " + count + " : " + getRuleId() + " " + this + " to :\n" + s);
			return result;
		} finally {
			helper.popRule();
		}
	}

	public final ParsingResult repairStatement(Statement statement, ParsingInformation parsinginfo, DocumentData s,
			ParseContext context, Predicate<? super Statement> modifiedstatementpredicate, ParseTimeData parsedata) {
//		System.out.println("Rule.repairStatement() repairing " + statement.getPosition() + " - " + statement.getValue());
		ParsingResult result = repairStatementImpl(statement, parsinginfo, s, context, modifiedstatementpredicate,
				parsedata);
		return result;
	}

	public final void repairStatementSkipped(Statement statement, ParseContext context) {
		repairStatementSkippedImpl(statement, context);
	}

	public final String getIdentifierName() {
		return identifierName;
	}

	public final int getParamsCount() {
		return params == null ? 0 : params.size();
	}

	public final boolean hasParams() {
		return params != null && params.size() > 0;
	}

	public final List<Pair<String, Class<?>>> getDeclaredParams() {
		if (params == null) {
			params = new ArrayList<>();
		}
		return params;
	}

	public final int getRuleId() {
		return ruleId;
	}

	// private final static ByteArrayOutputStream COPY_BAOS = new ByteArrayOutputStream();
	//
	// public final Rule copy() {
	// COPY_BAOS.reset();
	// try (ObjectOutputStream oos = new ObjectOutputStream(COPY_BAOS)) {
	// oos.writeObject(this);
	// oos.flush();
	// oos.close();
	// try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(COPY_BAOS.toByteArray()))) {
	// return (Rule) ois.readObject();
	// }
	// } catch (IOException | ClassNotFoundException e) {
	// e.printStackTrace();
	// }
	// return null;
	// }

	public String createParameterName(String name) {
		return PARAMETER_VAR_NAME + "_" + getRuleId() + "_" + name;
	}

	protected static String getRuleAliasVarName(Rule r) {
		return REPLACE_ALIAS_VAR_NAME + r.getRuleId();
	}

}
