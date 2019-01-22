package jaicore.ml.dyadranking.dataset;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.util.Strings;

import de.upb.isys.linearalgebra.DenseDoubleVector;
import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.dataset.attribute.IAttributeType;
import jaicore.ml.dyadranking.Dyad;

/**
 * A dataset representation for dyad ranking. Contains
 * {@link IDyadRankingInstance}s. In particular, this dataset is just an
 * extension to the {@link ArrayList} implementation with typecasts to
 * {@link IDyadRankingInstance}.
 * 
 * @author Helena Graf, Mirko Jürgens
 *
 */
public class DyadRankingDataset extends ArrayList<IInstance> implements IDataset {

	private static final long serialVersionUID = -1102494546233523992L;

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
	public DyadRankingDataset(Collection<IInstance> c) {
		super(c);
	}
	
	/**
	 * Creates an empty dyad ranking dataset with the given initial capacity.
	 * 
	 * @param initialCapacity initial capacity of the dyad ranking dataset
	 */
	public DyadRankingDataset(int initialCapacity) {
		super(initialCapacity);
	}

	public DyadRankingDataset(List<IDyadRankingInstance> sparseDyadRankingInstances) {
		super(sparseDyadRankingInstances);
	}

	@Override
	public <T> IAttributeType<T> getTargetType(Class<? extends T> clazz) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IAttributeType<?>> getAttributeTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNumberOfAttributes() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void serialize(OutputStream out) {
		try {
			for (IInstance instance : this) {
				IDyadRankingInstance drInstance = (IDyadRankingInstance) instance;
				for (Dyad dyad : drInstance) {
					out.write(dyad.getInstance().toString().getBytes());
					out.write(";".getBytes());
					out.write(dyad.getAlternative().toString().getBytes());
					out.write("|".getBytes());
				}
				out.write("\n".getBytes());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void deserialize(InputStream in) {
		this.clear();
		try {
			String input = IOUtils.toString(in, StandardCharsets.UTF_8);
			String[] rows = Strings.split(input, '\n');
			for (String row : rows) {
				if(row.isEmpty())
					break;
				List<Dyad> dyads = new LinkedList<Dyad>();
				String[] dyadTokens = Strings.split(row, '|');
				for (String dyadString : dyadTokens) {
					String[] values = Strings.split(dyadString, ';');
					if (values[0].length() > 1 && values[1].length() > 1) {
						String[] instanceValues = Strings.split(values[0].substring(1, values[0].length() - 1), ',');
						String[] alternativeValues = Strings.split(values[1].substring(1, values[1].length() - 1), ',');
						Vector instance = new DenseDoubleVector(instanceValues.length);
						for (int i = 0; i < instanceValues.length; i++) {
							instance.setValue(i, Double.parseDouble(instanceValues[i]));
						}

						Vector alternative = new DenseDoubleVector(alternativeValues.length);
						for (int i = 0; i < alternativeValues.length; i++) {
							alternative.setValue(i, Double.parseDouble(alternativeValues[i]));
						}
						Dyad dyad = new Dyad(instance, alternative);
						dyads.add(dyad);
					}
				}
				this.add(new DyadRankingInstance(dyads));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof DyadRankingDataset)) {
			return false;
		}
		DyadRankingDataset dataset = (DyadRankingDataset) o;
		
		if(dataset.size() != this.size()) {
			return false;
		}
			
		for(int i = 0; i < dataset.size(); i++) {
			IDyadRankingInstance i1 = this.get(i);
			IDyadRankingInstance i2 = dataset.get(i);
			if(! i1.equals(i2)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public IDyadRankingInstance get(int index) {
		return (IDyadRankingInstance) super.get(index);
	}

}
