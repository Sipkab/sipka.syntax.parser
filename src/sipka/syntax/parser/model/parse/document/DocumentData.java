package sipka.syntax.parser.model.parse.document;

import sipka.syntax.parser.util.OffsetCharSequence;

public class DocumentData extends OffsetCharSequence {
	public DocumentData(CharSequence data) {
		super(data, 0, data.length());
	}

	public DocumentData(DocumentData other) {
		super(other.subject, other.index, other.length);
	}

	public void removeFromStart(int count) {
		if (count > this.length) {
			throw new IllegalArgumentException(
					"Trying to remove more than length characters: " + count + " from: " + this.length);
		}
		this.index += count;
		this.length -= count;
	}

	public CharSequence subDocumentSequence(DocumentRegion region) {
		return new OffsetCharSequence(subject, region.getOffset(), region.getLength());
	}

	public final DocumentPosition getDocumentPosition() {
		return new DocumentPosition(this.index, 0, 0);
	}

	public final int getDocumentOffset() {
		return this.index;
	}

	public final int getDocumentLength() {
		return this.length;
	}

}
