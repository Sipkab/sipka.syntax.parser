package sipka.syntax.parser.saker;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.io.SerialUtils;

public class LanguageTranspilerTaskOutputImpl implements Externalizable, LanguageTranspilerTaskOutput {
	private static final long serialVersionUID = 1L;

	private SakerPath sourceDirectory;

	/**
	 * For {@link Externalizable}.
	 */
	public LanguageTranspilerTaskOutputImpl() {
	}

	public LanguageTranspilerTaskOutputImpl(SakerPath sourceDirectory) {
		this.sourceDirectory = sourceDirectory;
	}

	@Override
	public SakerPath getSourceDirectory() {
		return sourceDirectory;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(sourceDirectory);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		sourceDirectory = SerialUtils.readExternalObject(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sourceDirectory == null) ? 0 : sourceDirectory.hashCode());
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
		LanguageTranspilerTaskOutputImpl other = (LanguageTranspilerTaskOutputImpl) obj;
		if (sourceDirectory == null) {
			if (other.sourceDirectory != null)
				return false;
		} else if (!sourceDirectory.equals(other.sourceDirectory))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "LanguageTranspilerTaskOutput[sourceDirectory=" + sourceDirectory + "]";
	}

}
