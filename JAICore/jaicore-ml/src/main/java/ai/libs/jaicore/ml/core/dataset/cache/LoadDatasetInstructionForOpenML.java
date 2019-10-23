package ai.libs.jaicore.ml.core.dataset.cache;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.api4.java.ai.ml.core.dataset.IDataset;
import org.api4.java.ai.ml.core.dataset.IInstance;
import org.api4.java.ai.ml.core.exception.DatasetTraceInstructionFailedException;
import org.openml.apiconnector.io.OpenmlConnector;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import ai.libs.jaicore.ml.core.newdataset.reader.ArffDatasetReader;

@JsonIgnoreProperties({ "provider" })
public class LoadDatasetInstructionForOpenML extends LoadDataSetInstruction {
	private static final long serialVersionUID = 2125875356626308199L;
	private final String apiKey;

	public LoadDatasetInstructionForOpenML(@JsonProperty("apiKey") final String apiKey, @JsonProperty("id") final int id) {
		super(DataProvider.OPENML, String.valueOf(id));
		this.apiKey = apiKey;
	}

	@Override
	public List<IDataset<? extends IInstance>> getOutputDatasets(final List<IDataset<?>> inputs) throws DatasetTraceInstructionFailedException, InterruptedException {
		try {
			OpenmlConnector connector = new OpenmlConnector(this.apiKey);
			File f = connector.datasetGet(connector.dataGet(Integer.parseInt(this.getId())));
			ArffDatasetReader reader = new ArffDatasetReader(f);
			return Arrays.asList(reader.loadDataset());
		} catch (Exception e) {
			throw new DatasetTraceInstructionFailedException(e);
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
