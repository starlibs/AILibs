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
import org.api4.java.ai.ml.core.dataset.IInstance;
import org.api4.java.ai.ml.core.dataset.schema.ILabeledInstanceSchema;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyad;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyadRankingDataset;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyadRankingInstance;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.math.linearalgebra.DenseDoubleVector;
import ai.libs.jaicore.math.linearalgebra.IVector;
import ai.libs.jaicore.ml.core.dataset.attribute.DyadRankingAttribute;
import ai.libs.jaicore.ml.core.dataset.schema.LabeledInstanceSchema;
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
public class DyadRankingDataset extends ArrayList<IDyadRankingInstance> implements IDyadRankingDataset {

	private transient Logger logger = LoggerFactory.getLogger(DyadRankingDataset.class);

	private static final long serialVersionUID = -1102494546233523992L;

	private List<IAttribute> featureTypes;
	private IAttribute labelTypes;

	/**
	 * Creates an empty dyad ranking dataset.
	 */
	public DyadRankingDataset() {
		super();
	}

	/**
	 * Creates a dyad ranking dataset containing all elements in the given
	 * {@link Collection} in the order specified by the collections iterator.
	 *
	 * @param c {@link Collection} containing {@link IInstance} objects
	 */
	public DyadRankingDataset(final Collection<IDyadRankingInstance> c) {
		super(c);
	}

	/**
	 * Creates an empty dyad ranking dataset with the given initial capacity.
	 *
	 * @param initialCapacity initial capacity of the dyad ranking dataset
	 */
	public DyadRankingDataset(final int initialCapacity) {
		super(initialCapacity);
	}

	public DyadRankingDataset(final List<IDyadRankingInstance> dyadRankingInstances) {
		super(dyadRankingInstances);
	}

	public void serialize(final OutputStream out) {
		// currently, this always creates a dense dyad representation of the dyad
		// ranking dataset
		try {
			for (IDyadRankingInstance instance : this) {
				for (Dyad dyad : instance) {
					out.write(dyad.getInstance().toString().getBytes());
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
				this.add(new DyadRankingInstance(new LabeledInstanceSchema(new LinkedList<>(), new DyadRankingAttribute("label")), dyads));
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
	private INDArray dyadToVector(final Dyad dyad) {
		INDArray instanceOfDyad = Nd4j.create(dyad.getInstance().asArray());
		INDArray alternativeOfDyad = Nd4j.create(dyad.getAlternative().asArray());
		return Nd4j.hstack(instanceOfDyad, alternativeOfDyad);
	}

	public static DyadRankingDataset fromOrderedDyadList(final List<Dyad> orderedDyad) {
		List<IDyadRankingInstance> dyadRankingInstance = Arrays.asList(new DyadRankingInstance(orderedDyad));
		return new DyadRankingDataset(dyadRankingInstance);
	}

	/**
	 * Converts a dyad ranking to a {@link INDArray} matrix where each row
	 * corresponds to a dyad.
	 *
	 * @param drInstance The dyad ranking to convert to a matrix.
	 * @return The dyad ranking in {@link INDArray} matrix form.
	 */
	private INDArray dyadRankingToMatrix(final IDyadRankingInstance drInstance) {
		List<INDArray> dyadList = new ArrayList<>(drInstance.getNumAttributes());
		for (IDyad dyad : drInstance) {
			INDArray dyadVector = this.dyadToVector(dyad);
			dyadList.add(dyadVector);
		}
		INDArray dyadMatrix;
		dyadMatrix = Nd4j.vstack(dyadList);
		return dyadMatrix;
	}

	@Override
	public IAttribute getLabelAttribute() {
		return this.labelTypes;
	}

	@Override
	public int getNumLabels() {
		return this.labelTypes.size();
	}

	@Override
	public DyadRankingDataset createEmptyCopy() throws DatasetCreationException, InterruptedException {
		return new DyadRankingDataset();
	}

	@Override
	public List<IAttribute> getFeatureTypes() {
		return this.featureTypes;
	}

	@Override
	public int getNumFeatures() {
		return this.featureTypes.size();
	}

	@Override
	public ILabeledInstanceSchema getInstanceSchema() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] getLabelVector() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[][] getFeatureMatrix() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean add(final org.api4.java.ai.ml.ranking.dyad.dataset.IDyadRankingInstance arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void add(final int arg0, final org.api4.java.ai.ml.ranking.dyad.dataset.IDyadRankingInstance arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean addAll(final Collection<? extends org.api4.java.ai.ml.ranking.dyad.dataset.IDyadRankingInstance> arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addAll(final int arg0, final Collection<? extends org.api4.java.ai.ml.ranking.dyad.dataset.IDyadRankingInstance> arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public org.api4.java.ai.ml.ranking.dyad.dataset.IDyadRankingInstance set(final int arg0, final org.api4.java.ai.ml.ranking.dyad.dataset.IDyadRankingInstance arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDyadRankingInstance get(final int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDyadRankingInstance remove(final int arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}
