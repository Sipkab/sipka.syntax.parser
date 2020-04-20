/*
 * Copyright (C) 2020 Bence Sipka
 *
 * This program is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package sipka.syntax.parser.saker;

import saker.build.file.path.SakerPath;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.utils.SimpleStructuredObjectTaskResult;
import saker.build.task.utils.annot.SakerInput;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.io.FileUtils;
import saker.build.trace.BuildTrace;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestParameterInformation;
import saker.nest.scriptinfo.reflection.annot.NestTaskInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.nest.utils.FrontendTaskFactory;

@NestTaskInformation(returnType = @NestTypeUsage(LanguageTranspilerTaskOutput.class))
@NestInformation("Transpiles a language description file into a Java class.\n"
		+ "The task reads a language definition defined by the sipka.syntax.parser library and outputs "
		+ "the source code for a Java class that is equivalent with the definition.\n"
		+ "The generated class can be compiled by the Java compiler and added to your application. In this "
		+ "case you don't need to include the language definition file in your application bundle.\n"
		+ "You can use the static get<language-name>() methods to retrieve the language objects.")
@NestParameterInformation(value = "Input",
		aliases = { "" },
		required = true,
		type = @NestTypeUsage(SakerPath.class),
		info = @NestInformation("Path to the language definition file."))
@NestParameterInformation(value = "ClassName",
		aliases = { "Class" },
		type = @NestTypeUsage(String.class),
		info = @NestInformation("The fully qualified class name for the generated Java class.\n"
				+ "This option sets the qualified name that the generated Java class should have.\n"
				+ "If not set, the language.<file-name> will be used."))
@NestParameterInformation(value = "Identifier",
		type = @NestTypeUsage(String.class),
		info = @NestInformation("Sets an identifier for the task.\n"
				+ "This identifier will be used to determine the output directory for the transpilation.\n"
				+ "If not set, the name of the language file will be used."))
public class LanguageJavaTranspilerTaskFactory extends FrontendTaskFactory<Object> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "sipka.syntax.parser.transpile";

	@Override
	public ParameterizableTask<? extends Object> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<Object>() {

			@SakerInput(value = { "", "Input" }, required = true)
			public SakerPath inputOption;

			@SakerInput(value = { "Class", "ClassName" })
			public String classNameOption;

			@SakerInput(value = { "Identifier" })
			public String identifierOption;

			@Override
			public Object run(TaskContext taskcontext) throws Exception {
				if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
					BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_FRONTEND);
				}
				SakerPath inpath = taskcontext.getTaskWorkingDirectoryPath().tryResolve(inputOption);
				String classname = classNameOption;
				if (ObjectUtils.isNullOrEmpty(classname)) {
					classname = "languages." + FileUtils.removeExtension(inputOption.getFileName());
				}

				String identifier = identifierOption;
				if (ObjectUtils.isNullOrEmpty(identifier)) {
					identifier = inpath.getFileName();
				}

				LanguageTranspilerWorkerTaskIdentifier workertaskid = new LanguageTranspilerWorkerTaskIdentifier(
						identifier);
				taskcontext.startTask(workertaskid, new LanguageTranspilerWorkerTaskFactory(inpath, classname), null);
				SimpleStructuredObjectTaskResult result = new SimpleStructuredObjectTaskResult(workertaskid);
				taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
				return result;
			}
		};
	}

}
