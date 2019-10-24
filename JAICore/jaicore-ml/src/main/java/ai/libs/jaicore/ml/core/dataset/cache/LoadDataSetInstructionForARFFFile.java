package ai.libs.jaicore.ml.core.dataset.cache;

import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;

import org.api4.java.ai.ml.core.dataset.IDataset;
import org.api4.java.ai.ml.core.dataset.IInstance;
import org.api4.java.ai.ml.core.exception.DatasetTraceInstructionFailedException;

import ai.libs.jaicore.ml.core.dataset.reader.ArffDatasetAdapter;
import weka.core.Instances;

public class LoadDataSetInstructionForARFFFile extends LoadDataSetInstruction {

	private static final long serialVersionUID = -9005760846394433953L;
	private final int classIndex;
	private final ArffDatasetAdapter reader;

	public LoadDataSetInstructionForARFFFile(final File file, final int classIndex) {
		super(DataProvider.ARFFFILE, file.getAbsolutePath());
		this.classIndex = classIndex;
		this.reader = new ArffDatasetAdapter(false, file);
	}

	public LoadDataSetInstructionForARFFFile(final File file) {
		this(file, -1);
	}

	@Override
	public List<IDataset<? extends IInstance>> getOutputDatasets(final List<IDataset<? extends IInstance>> input) throws DatasetTraceInstructionFailedException, InterruptedException {
		// load openml or local dataset
		try {
			Instances data = new Instances(new FileReader(new File(this.getId())));
			data.setClassIndex(this.classIndex >= 0 ? this.classIndex : data.numAttributes() - 1);
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