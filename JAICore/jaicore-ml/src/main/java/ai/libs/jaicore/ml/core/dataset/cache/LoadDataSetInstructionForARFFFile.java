package ai.libs.jaicore.ml.core.dataset.cache;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.api4.java.ai.ml.core.dataset.IDataset;
import org.api4.java.ai.ml.core.dataset.IInstance;
import org.api4.java.ai.ml.core.exception.DatasetTraceInstructionFailedException;

import ai.libs.jaicore.ml.core.dataset.serialization.ArffDatasetAdapter;
import ai.libs.jaicore.ml.core.dataset.serialization.FileDatasetDescriptor;

public class LoadDataSetInstructionForARFFFile extends LoadDataSetInstruction {

	private static final long serialVersionUID = -9005760846394433953L;
	private final ArffDatasetAdapter reader;

	public LoadDataSetInstructionForARFFFile(final File file) {
		super(DataProvider.ARFFFILE, file.getAbsolutePath());
		this.reader = new ArffDatasetAdapter(false, new FileDatasetDescriptor(file));
	}

	@Override
	public List<IDataset<? extends IInstance>> getOutputDatasets(final List<IDataset<? extends IInstance>> input) throws DatasetTraceInstructionFailedException, InterruptedException {
		// load openml or local dataset
		try {
			return Arrays.asList(this.reader.deserializeDataset());
		} catch (Exception e) {
			throw new DatasetTraceInstructionFailedException(e);
		}
	}

	@Override
	public Instruction clone() {
		return new LoadDataSetInstructionForARFFFile(new File(this.getId()));
	}

}