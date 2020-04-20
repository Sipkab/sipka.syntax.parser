package sipka.syntax.parser.saker;

import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

import saker.build.file.ByteArraySakerFile;
import saker.build.file.SakerDirectory;
import saker.build.file.SakerFile;
import saker.build.file.path.SakerPath;
import saker.build.file.provider.SakerPathFiles;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.CommonTaskContentDescriptors;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskFactory;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.build.trace.BuildTrace;
import sipka.syntax.parser.model.rule.Language;
import sipka.syntax.parser.util.RuleJavaGenerator;

public class LanguageTranspilerWorkerTaskFactory implements TaskFactory<LanguageTranspilerTaskOutputImpl>,
		Task<LanguageTranspilerTaskOutputImpl>, Externalizable {
	private static final long serialVersionUID = 1L;

	private SakerPath inputPath;
	private String className;

	/**
	 * For {@link Externalizable}.
	 */
	public LanguageTranspilerWorkerTaskFactory() {
	}

	public LanguageTranspilerWorkerTaskFactory(SakerPath inputPath, String className) {
		SakerPathFiles.requireAbsolutePath(inputPath);
		Objects.requireNonNull(className, "class name");
		this.inputPath = inputPath;
		this.className = className;
	}

	@Override
	public Task<? extends LanguageTranspilerTaskOutputImpl> createTask(ExecutionContext arg0) {
		return this;
	}

	@Override
	public int getRequestedComputationTokenCount() {
		return 1;
	}

	@Override
	public LanguageTranspilerTaskOutputImpl run(TaskContext taskcontext) throws Exception {
		if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
			BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_WORKER);
			BuildTrace.setDisplayInformation("syntax.transpile",
					LanguageJavaTranspilerTaskFactory.TASK_NAME + ":" + inputPath.getFileName());
		}
		SakerFile f = taskcontext.getTaskUtilities().resolveFileAtPath(inputPath);
		if (f == null) {
			taskcontext.abortExecution(new NoSuchFieldError(Objects.toString(inputPath, null)));
			taskcontext.reportInputFileDependency(null, inputPath, CommonTaskContentDescriptors.NOT_PRESENT);
			return null;
		}
		LanguageTranspilerWorkerTaskIdentifier taskid = (LanguageTranspilerWorkerTaskIdentifier) taskcontext
				.getTaskId();
		taskcontext.getTaskUtilities().reportInputFileDependency(null, f);
		Map<String, Language> langs;
		try (InputStream is = f.openInputStream()) {
			langs = Language.fromInputStream(is);
		}
		SakerDirectory outputdir = SakerPathFiles.requireBuildDirectory(taskcontext)
				.getDirectoryCreate(LanguageJavaTranspilerTaskFactory.TASK_NAME)
				.getDirectoryCreate(taskid.getIdentifier());

		String genclassstr = RuleJavaGenerator.generateLanguageJavaClass(className, langs);
		SakerPath outfilepath = SakerPath.valueOf(className.replace('.', '/') + ".java");

		//clear any previous file state
		outputdir.clear();

		SakerDirectory srcoutdir;
		if (!SakerPath.EMPTY.equals(outfilepath.getParent())) {
			srcoutdir = taskcontext.getTaskUtilities().resolveDirectoryAtRelativePathCreate(outputdir,
					outfilepath.getParent());
		} else {
			srcoutdir = outputdir;
		}
		ByteArraySakerFile outfile = new ByteArraySakerFile(outfilepath.getFileName(),
				genclassstr.getBytes(StandardCharsets.UTF_8));
		srcoutdir.add(outfile);
		outputdir.synchronize();

		taskcontext.getTaskUtilities().reportOutputFileDependency(null, outfile);

		return new LanguageTranspilerTaskOutputImpl(outputdir.getSakerPath());
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(inputPath);
		out.writeObject(className);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		inputPath = SerialUtils.readExternalObject(in);
		className = SerialUtils.readExternalObject(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((className == null) ? 0 : className.hashCode());
		result = prime * result + ((inputPath == null) ? 0 : inputPath.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LanguageTranspilerWorkerTaskFactory other = (LanguageTranspilerWorkerTaskFactory) obj;
		if (className == null) {
			if (other.className != null)
				return false;
		} else if (!className.equals(other.className))
			return false;
		if (inputPath == null) {
			if (other.inputPath != null)
				return false;
		} else if (!inputPath.equals(other.inputPath))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "LanguageTranspilerWorkerTaskFactory[inputPath=" + inputPath + ", className=" + className + "]";
	}

}
