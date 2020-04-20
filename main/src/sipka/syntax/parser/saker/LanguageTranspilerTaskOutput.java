package sipka.syntax.parser.saker;

import saker.build.file.path.SakerPath;
import saker.nest.scriptinfo.reflection.annot.NestFieldInformation;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;

@NestInformation("Output of the " + LanguageJavaTranspilerTaskFactory.TASK_NAME + "() task.\n"
		+ "Provides access to the base Java source directory that can be used as an input to the "
		+ "Java compilation.")
@NestFieldInformation(value = "SourceDirectory",
		type = @NestTypeUsage(SakerPath.class),
		info = @NestInformation("Path to the source directory where the language definition class were generated."))
public interface LanguageTranspilerTaskOutput {

	public SakerPath getSourceDirectory();

}