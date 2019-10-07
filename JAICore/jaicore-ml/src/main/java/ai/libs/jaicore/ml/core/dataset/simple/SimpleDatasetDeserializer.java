package ai.libs.jaicore.ml.core.dataset.simple;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.api4.java.ai.ml.core.dataset.schema.ILabeledInstanceSchema;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;
import org.api4.java.ai.ml.core.dataset.serialization.IDatasetDeserializer;

import ai.libs.jaicore.ml.core.dataset.schema.LabeledInstanceSchema;

public class SimpleDatasetDeserializer implements IDatasetDeserializer<SimpleDataset> {

	private static final String ARFF_RELATION = "@relation";
	private static final String ARFF_ATTRIBUTE = "@attribute";
	private static final String ARFF_DATA = "@data";

	@Override
	public SimpleDataset deserializeDataset(final File datasetFile) throws InterruptedException {
		String relationName;
		List<IAttribute> featureAttributes;
		IAttribute labelAttribute;

		try (BufferedReader br = new BufferedReader(new FileReader(datasetFile))) {
			String line;
			boolean dataMode = false;

			while ((line = br.readLine()) != null) {
				String trimmedLine = line.trim();

				if (!dataMode) {
					if (trimmedLine.startsWith(ARFF_RELATION)) {
						// read relation
					} else if (trimmedLine.startsWith(ARFF_ATTRIBUTE)) {
						// read attribute
					} else if (trimmedLine.startsWith(ARFF_DATA)) {
						dataMode = true;
					}
				} else {
					// read instance
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		ILabeledInstanceSchema schema = new LabeledInstanceSchema(relationName, featureAttributes, labelAttribute);

		SimpleDataset dataset = new SimpleDataset(schema);

		return dataset;
	}

}
