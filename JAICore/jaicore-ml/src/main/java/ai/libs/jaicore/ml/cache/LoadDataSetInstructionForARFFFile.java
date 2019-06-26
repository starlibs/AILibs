package ai.libs.jaicore.ml.cache;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import ai.libs.jaicore.ml.core.dataset.IDataset;
import ai.libs.jaicore.ml.core.dataset.weka.WekaInstances;
import weka.core.Instances;

public class LoadDataSetInstructionForARFFFile extends LoadDataSetInstruction {

	public LoadDataSetInstructionForARFFFile(final File file) {
		super(DataProvider.ARFFFILE, file.getAbsolutePath());
	}

	@Override
	public List<IDataset> getOutputInstances(final List<IDataset> inputs) throws InstructionFailedException, InterruptedException {

		// load openml or local dataset
		try {
			Instances data = new Instances(new FileReader(new File(this.getId())));
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