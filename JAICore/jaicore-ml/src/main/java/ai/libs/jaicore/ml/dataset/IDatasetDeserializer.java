package ai.libs.jaicore.ml.dataset;

import java.io.File;

/**
 * A dataset deserializer reads in the contents of a file to return it as a dataset object.
 *
 * @author mwever
 *
 * @param <I>
 * @param <X>
 */
public interface IDatasetDeserializer<I, D> {

	public D deserializeDataset(final File datasetFile) throws InterruptedException;

}
