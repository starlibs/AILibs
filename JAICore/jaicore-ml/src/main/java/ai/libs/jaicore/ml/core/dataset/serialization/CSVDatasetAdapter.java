package ai.libs.jaicore.ml.core.dataset.serialization;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.core.dataset.descriptor.IDatasetDescriptor;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.ICategoricalAttribute;
import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.serialization.IDatasetDeserializer;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;

public class CSVDatasetAdapter implements IDatasetDeserializer<ILabeledDataset<ILabeledInstance>> {

	public CSVDatasetAdapter() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public ILabeledDataset<ILabeledInstance> deserializeDataset(final IDatasetDescriptor datasetDescription) throws DatasetDeserializationFailedException, InterruptedException {
		throw new UnsupportedOperationException("Not yet supported!");
	}

	public static ILabeledDataset<ILabeledInstance> readDataset(final File csvInputFile) {
		throw new UnsupportedOperationException("Not yet supported!");
	}

	public static void writeDataset(final File arffOutputFile, final ILabeledDataset<? extends ILabeledInstance> data) throws IOException {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(arffOutputFile))) {
			// write header line for csv
			bw.write(data.getInstanceSchema().getAttributeList().stream().map(x -> "\"" + x.getName() + "\"").collect(Collectors.joining(",")));
			bw.write(",");
			bw.write("\"" + data.getLabelAttribute().getName() + "\"");
			bw.write("\n");

			for (ILabeledInstance instance : data) {
				bw.write(IntStream.range(0, instance.getNumAttributes()).mapToObj(x -> serializeAttributeValue(data.getAttribute(x), instance.getAttributeValue(x))).collect(Collectors.joining(",")));
				bw.write(",");
				bw.write(serializeAttributeValue(data.getInstanceSchema().getLabelAttribute(), instance.getLabel()));
				bw.write("\n");
			}
		}
	}

	private static String serializeAttributeValue(final IAttribute att, final Object attValue) {
		if (attValue == null) {
			return "";
		}

		String value = att.serializeAttributeValue(attValue);
		if (att instanceof ICategoricalAttribute && !(value.startsWith("'") && value.endsWith("'"))) {
			value = "'" + value + "'";
		}
		return value;
	}

}
