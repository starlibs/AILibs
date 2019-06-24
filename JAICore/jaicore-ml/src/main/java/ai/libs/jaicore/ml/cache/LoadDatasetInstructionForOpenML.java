package ai.libs.jaicore.ml.cache;

import java.io.IOException;
import java.util.List;

import ai.libs.jaicore.ml.core.dataset.weka.WekaInstance;
import ai.libs.jaicore.ml.core.dataset.weka.WekaInstances;
import ai.libs.jaicore.ml.openml.OpenMLHelper;
import weka.core.Instances;

public class LoadDatasetInstructionForOpenML<L> extends LoadDataSetInstruction<WekaInstance<L>, WekaInstances<L>> {

	public LoadDatasetInstructionForOpenML(final String apiKey, final String id) {
		super(DataProvider.OPENML, id);
		this.parameters.put("apikey", apiKey);
	}

	@Override
	public WekaInstances<L> getOutputInstances(final List<WekaInstances<L>> inputs) throws InstructionFailedException, InterruptedException {

		// load openml or local dataset
		OpenMLHelper.setApiKey((String)this.parameters.get("apiKey"));
		try {
			Instances instances = OpenMLHelper.getInstancesById(Integer.parseInt((String)this.parameters.get("id")));
			return new WekaInstances<>(instances);
		} catch (NumberFormatException | IOException | ClassNotFoundException e) {
			throw new InstructionFailedException(e);
		}
	}
}
