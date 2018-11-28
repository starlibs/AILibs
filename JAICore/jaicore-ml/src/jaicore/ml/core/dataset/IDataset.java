package jaicore.ml.core.dataset;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import jaicore.ml.core.dataset.attribute.IAttributeType;

public interface IDataset {

	public IAttributeType getTargetType();

	public List<IAttributeType> getAttributeTypes();

	public List<IInstance> getInstances();

	public IInstance get(int index);

	public void serialize(OutputStream out);

	public void deserialize(InputStream in);

}
