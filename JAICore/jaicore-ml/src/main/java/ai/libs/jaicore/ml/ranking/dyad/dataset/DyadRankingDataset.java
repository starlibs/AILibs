package ai.libs.jaicore.ml.ranking.dyad.dataset;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.api4.java.ai.ml.core.dataset.IDataset;
import org.api4.java.ai.ml.core.dataset.schema.ILabeledInstanceSchema;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyad;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyadRankingDataset;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyadRankingInstance;
import org.api4.java.common.math.IVector;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.math.linearalgebra.DenseDoubleVector;
import ai.libs.jaicore.ml.core.dataset.Dataset;
import ai.libs.jaicore.ml.core.dataset.schema.LabeledInstanceSchema;
import ai.libs.jaicore.ml.core.dataset.schema.attribute.DyadRankingAttribute;
import ai.libs.jaicore.ml.core.dataset.schema.attribute.SetOfObjectsAttribute;
import ai.libs.jaicore.ml.ranking.dyad.learner.Dyad;

/**
 * A dataset representation for dyad ranking. Contains
 * {@link IDyadRankingInstance}s. In particular, this dataset is just an
 * extension to the {@link ArrayList} implementation with typecasts to
 * {@link IDyadRankingInstance}.
 *
 * @author Helena Graf, Mirko Jürgens, Michael Braun, Jonas Hanselle
 *
 */
public class DyadRankingDataset extends AGeneralDatasetBackedDataset<IDyadRankingInstance> implements IDyadRankingDataset {

	private static final String MSG_REMOVAL_FORBIDDEN = "Cannot remove a column for dyad DyadRankingDataset.";
	private Logger logger = LoggerFactory.getLogger(DyadRankingDataset.class);

	private LabeledInstanceSchema labeledInstanceSchema;

	public DyadRankingDataset() {
		this("");
	}

	public DyadRankingDataset(final String relationName) {
		this.createInstanceSchema(relationName);
		this.setInternalDataset(new Dataset(this.labeledInstanceSchema));
	}

	public DyadRankingDataset(final LabeledInstanceSchema labeledInstanceSchema) {
		this.labeledInstanceSchema = labeledInstanceSchema.getCopy();
		this.setInternalDataset(new Dataset(this.labeledInstanceSchema));
	}

	public DyadRankingDataset(final String relationName, final Collection<IDyadRankingInstance> c) {
		this(relationName);
		this.addAll(c);
	}

	public DyadRankingDataset(final Collection<IDyadRankingInstance> c) {
		this("", c);
	}

	private void createInstanceSchema(final String relationName) {
		IAttribute dyadSetAttribute = new SetOfObjectsAttribute<>("dyads", IDyad.class);
		IAttribute dyadRankingAttribute = new DyadRankingAttribute("ranking");
		this.labeledInstanceSchema = new LabeledInstanceSchema(relationName, Arrays.asList(dyadSetAttribute), dyadRankingAttribute);
	}

	public void serialize(final OutputStream out) {
		// currently, this always creates a dense dyad representation of the dyad
		// ranking dataset
		try {
			for (IDyadRankingInstance instance : this) {
				for (IDyad dyad : instance) {
					out.write(dyad.getContext().toString().getBytes());
					out.write(";".getBytes());
					out.write(dyad.getAlternative().toString().getBytes());
					out.write("|".getBytes());
				}
				out.write("\n".getBytes());
			}
		} catch (IOException e) {
			this.logger.warn(e.getMessage());
		}
	}

	public void deserialize(final InputStream in) {
		// currently, this always creates a dense dyad ranking dataset
		this.clear();
		try {
			LineIterator input = IOUtils.lineIterator(in, StandardCharsets.UTF_8);
			while (input.hasNext()) {
				String row = input.next();
				if (row.isEmpty()) {
					break;
				}
				List<IDyad> dyads = new LinkedList<>();
				String[] dyadTokens = row.split("\\|");
				for (String dyadString : dyadTokens) {
					String[] values = dyadString.split(";");
					if (values[0].length() > 1 && values[1].length() > 1) {
						String[] instanceValues = values[0].substring(1, values[0].length() - 1).split(",");
						String[] alternativeValues = values[1].substring(1, values[1].length() - 1).split(",");
						IVector instance = new DenseDoubleVector(instanceValues.length);
						for (int i = 0; i < instanceValues.length; i++) {
							instance.setValue(i, Double.parseDouble(instanceValues[i]));
						}

						IVector alternative = new DenseDoubleVector(alternativeValues.length);
						for (int i = 0; i < alternativeValues.length; i++) {
							alternative.setValue(i, Double.parseDouble(alternativeValues[i]));
						}
						IDyad dyad = new Dyad(instance, alternative);
						dyads.add(dyad);
					}
				}
				this.add(new DenseDyadRankingInstance(dyads));
			}
		} catch (IOException e) {
			this.logger.warn(e.getMessage());
		}
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof DyadRankingDataset)) {
			return false;
		}
		DyadRankingDataset dataset = (DyadRankingDataset) o;

		if (dataset.size() != this.size()) {
			return false;
		}

		for (int i = 0; i < dataset.size(); i++) {
			IDyadRankingInstance i1 = this.get(i);
			IDyadRankingInstance i2 = dataset.get(i);
			if (!i1.equals(i2)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		int result = 17;

		for (IDyadRankingInstance instance : this) {
			result = result * 31 + instance.hashCode();
		}

		return result;
	}

	/**
	 * Converts this data set to a list of ND4j {@link INDArray}s.
	 * Each dyad ranking is represented by a 2D-matrix where a row is a dyad.
	 *
	 * @return
	 */
	public List<INDArray> toND4j() {
		List<INDArray> ndList = new ArrayList<>();
		for (IDyadRankingInstance instance : this) {
			IDyadRankingInstance drInstance = instance;
			ndList.add(this.dyadRankingToMatrix(drInstance));
		}
		return ndList;
	}

	/**
	 * Converts a dyad to a {@link INDArray} row vector consisting of a
	 * concatenation of the instance and alternative features.
	 *
	 * @param dyad The dyad to convert.
	 * @return The dyad in {@link INDArray} row vector form.
	 */
	private INDArray dyadToVector(final IDyad dyad) {
		INDArray instanceOfDyad = Nd4j.create(dyad.getContext().asArray());
		INDArray alternativeOfDyad = Nd4j.create(dyad.getAlternative().asArray());
		return Nd4j.hstack(instanceOfDyad, alternativeOfDyad);
	}

	/**
	 * Converts a dyad ranking to a {@link INDArray} matrix where each row
	 * corresponds to a dyad.
	 *
	 * @param drInstance The dyad ranking to convert to a matrix.
	 * @return The dyad ranking in {@link INDArray} matrix form.
	 */
	private INDArray dyadRankingToMatrix(final IDyadRankingInstance drInstance) {
		List<INDArray> dyadList = new ArrayList<>(drInstance.getNumberOfRankedElements());
		for (IDyad dyad : drInstance) {
			INDArray dyadVector = this.dyadToVector(dyad);
			dyadList.add(dyadVector);
		}
		INDArray dyadMatrix;
		dyadMatrix = Nd4j.vstack(dyadList);
		return dyadMatrix;
	}

	@Override
	public ILabeledInstanceSchema getInstanceSchema() {
		return this.labeledInstanceSchema;
	}

	@Override
	public Object[] getLabelVector() {
		return this.getInternalDataset().getLabelVector();
	}

	@Override
	public DyadRankingDataset createEmptyCopy() {
		return new DyadRankingDataset(this.labeledInstanceSchema);
	}

	@Override
	public Object[][] getFeatureMatrix() {
		return this.getInternalDataset().getFeatureMatrix();
	}

	@Override
	public void removeColumn(final int columnPos) {
		throw new UnsupportedOperationException(MSG_REMOVAL_FORBIDDEN);
	}

	@Override
	public void removeColumn(final String columnName) {
		throw new UnsupportedOperationException(MSG_REMOVAL_FORBIDDEN);
	}

	@Override
	public void removeColumn(final IAttribute attribute) {
		throw new UnsupportedOperationException(MSG_REMOVAL_FORBIDDEN);
	}

	@Override
	public IDataset<IDyadRankingInstance> createCopy() throws DatasetCreationException, InterruptedException {
		DyadRankingDataset copy = this.createEmptyCopy();
		for (IDyadRankingInstance i : this) {
			copy.add(i);
		}
		return copy;
	}

}
