package ai.libs.jaicore.ml.dataset;

import java.util.LinkedList;
import java.util.List;

public class OrdinalAttribute extends AAttribute {

	private List<String> ordinals;

	public OrdinalAttribute(final String name, final List<String> ordinalScala) {
		super(name);
		this.ordinals = new LinkedList<>(ordinalScala);
	}

	public List<String> getOrdinals() {
		return this.ordinals;
	}

}
