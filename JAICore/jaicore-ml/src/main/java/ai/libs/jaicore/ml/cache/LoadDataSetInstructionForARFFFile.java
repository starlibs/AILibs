package ai.libs.jaicore.ml.cache;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.api4.java.ai.ml.core.dataset.IDataset;

import ai.libs.jaicore.ml.core.dataset.weka.WekaInstances;
import weka.core.Instances;

public class LoadDataSetInstructionForARFFFile extends LoadDataSetInstruction {

	private static final long serialVersionUID = -9005760846394433953L;
	private final int classIndex;

	public LoadDataSetInstructionForARFFFile(final File file) {
		this(file, -1);
	}

	public LoadDataSetInstructionForARFFFile(final File file, final int classIndex) {
		super(DataProvider.ARFFFILE, file.getAbsolutePath());
		this.classIndex = classIndex;
	}

	@Override
	public List<IDataset> getOutputInstances(final List<IDataset> inputs) throws InstructionFailedException, InterruptedException {

		// load openml or local dataset
		try {
			Instances data = new Instances(new FileReader(new File(this.getId())));
			data.setClassIndex(this.classIndex >= 0 ? this.classIndex : data.numAttributes() - 1);
			return Arrays.asList(new WekaInstances<>(data));
		} catch (NumberFormatException | IOException | ClassNotFoundException e) {
			throw new InstructionFailedException(e);
		}
	}

	@Override
	public Instruction clone() {
		return new LoadDataSetInstructionForARFFFile(new File(this.getId()));
	}
}