package ai.libs.jaicore.ml.cache;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.api4.java.ai.ml.IDataset;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import ai.libs.jaicore.ml.core.dataset.weka.WekaInstances;
import ai.libs.jaicore.ml.openml.OpenMLHelper;
import weka.core.Instances;

@JsonIgnoreProperties({ "provider" })
public class LoadDatasetInstructionForOpenML extends LoadDataSetInstruction {

	private static final long serialVersionUID = 2125875356626308199L;
	private final String apiKey;

	public LoadDatasetInstructionForOpenML(@JsonProperty("apiKey") final String apiKey, @JsonProperty("id") final int id) {
		super(DataProvider.OPENML, String.valueOf(id));
		this.apiKey = apiKey;
	}

	@Override
	public List<IDataset> getOutputInstances(final List<IDataset> inputs) throws InstructionFailedException, InterruptedException {

		// load openml or local dataset
		OpenMLHelper.setApiKey(this.apiKey);
		try {
			Instances instances = OpenMLHelper.getInstancesById(Integer.valueOf(this.getId()));
			return Arrays.asList(new WekaInstances<>(instances));
		} catch (NumberFormatException | IOException | ClassNotFoundException e) {
			throw new InstructionFailedException(e);
		}
	}

	public String getApiKey() {
		return this.apiKey;
	}

	@Override
	public Instruction clone() {
		return new LoadDatasetInstructionForOpenML(this.apiKey, Integer.valueOf(this.getId()));
	}
}
