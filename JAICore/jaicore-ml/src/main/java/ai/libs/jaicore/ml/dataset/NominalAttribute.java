package ai.libs.jaicore.ml.dataset;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class NominalAttribute extends AAttribute {

	private List<String> values;

	public NominalAttribute(final String name, final Collection<String> values) {
		super(name);
		this.values = new LinkedList<>(values);
	}

	public List<String> getValues() {
		return new LinkedList<>(this.values);
	}

	public double getValueID(final String value) {
		return this.values.indexOf(value);
	}

}
