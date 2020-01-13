package ai.libs.jaicore.ml.core.dataset.serialization;

import java.io.File;

import org.api4.java.ai.ml.core.dataset.descriptor.IFileDatasetDescriptor;

public class FileDatasetDescriptor implements IFileDatasetDescriptor {

	private final File datasetFile;

	public FileDatasetDescriptor(final File datasetFile) {
		this.datasetFile = datasetFile;
	}

	@Override
	public File getDatasetDescription() {
		return this.datasetFile;
	}

}
