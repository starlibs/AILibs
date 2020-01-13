package ai.libs.jaicore.ml.core.dataset;

import java.io.File;

import org.api4.java.ai.ml.core.dataset.descriptor.IFileDatasetDescriptor;

public class FileDatasetDescriptor implements IFileDatasetDescriptor {

	private final File file;

	public FileDatasetDescriptor(final File file) {
		super();
		this.file = file;
	}

	@Override
	public File getDatasetDescription() {
		return this.file;
	}

}
