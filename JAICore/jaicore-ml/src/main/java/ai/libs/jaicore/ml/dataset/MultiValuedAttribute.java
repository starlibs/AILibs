package ai.libs.jaicore.ml.dataset;

import java.util.Collection;

public class MultiValuedAttribute extends NominalAttribute {

	public MultiValuedAttribute(final String name, final Collection<String> values) {
		super(name, values);
	}

}
