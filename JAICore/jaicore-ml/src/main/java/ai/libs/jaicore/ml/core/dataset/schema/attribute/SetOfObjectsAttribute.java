package ai.libs.jaicore.ml.core.dataset.schema.attribute;

import java.util.Collection;
import java.util.Set;
import java.util.StringJoiner;

import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttributeValue;
import org.api4.java.ai.ml.core.dataset.schema.attribute.ISetOfObjectsAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.ISetOfObjectsAttributeValue;

public class SetOfObjectsAttribute<O> extends ACollectionOfObjectsAttribute<O> implements ISetOfObjectsAttribute<O> {

	private static final long serialVersionUID = 4372755490714119056L;

	private Class<O> classOfObject;
	private Set<O> domain;

	public SetOfObjectsAttribute(final String name, final Class<O> classOfObject, final Set<O> domain) {
		super(name);
		this.domain = domain;
		this.classOfObject = classOfObject;
	}

	public SetOfObjectsAttribute(final String name, final Class<O> classOfObject) {
		this(name, classOfObject, null);
	}

	@Override
	public boolean isValidValue(final Object value) {
		if (value instanceof Set) {
			Set<?> set = (Set<?>) value;
			if (!set.isEmpty()) {
				Object anyElement = set.stream().findAny().get();
				if (classOfObject.isInstance(anyElement)) {
					if (domain != null) {
						return set.stream().allMatch(o -> domain.contains(o));
					}
				}
			}
		}
		if (value instanceof SetOfObjectsAttributeValue) {
			Set<?> setOfObjects = ((SetOfObjectsAttributeValue<?>) value).getValue();
			return isValidValue(setOfObjects);
		}
		return false;
	}

	@Override
	public String getStringDescriptionOfDomain() {
		String description = "[Set]";
		if (domain != null) {
			StringJoiner stringJoiner = new StringJoiner(",");
			for (O object : domain) {
				stringJoiner.add(object.toString());
			}
			description += stringJoiner.toString();
		}
		return description + getName();
	}

	@SuppressWarnings("unchecked")
	@Override
	public IAttributeValue getAsAttributeValue(final Object object) {
		if (this.isValidValue(object)) {
			if (object instanceof SetOfObjectsAttributeValue<?>) {
				return new SetOfObjectsAttributeValue<>(((SetOfObjectsAttributeValue<O>) object).getValue(), this);
			}
			return new SetOfObjectsAttributeValue<>((Set<O>) object, this);
		}
		throw new IllegalArgumentException("No valid value of this attribute");
	}

	@Override
	public double toDouble(final Object object) {
		throw new UnsupportedOperationException("Not supported");
	}

	@Override
	public String serializeAttributeValue(final Object value) {
		throw new UnsupportedOperationException("Not yet implemented!");
	}

	@Override
	public Object deserializeAttributeValue(final String string) {
		throw new UnsupportedOperationException("Not yet implemented!");
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Collection<O> getValueAsTypeInstance(final Object object) {
		if (this.isValidValue(object)) {
			if (object instanceof ISetOfObjectsAttributeValue) {
				Set<?> set = ((ISetOfObjectsAttributeValue<?>) object).getValue();
				if (!set.isEmpty()) {
					Object elementFromSet = set.stream().findAny().get();
					if (classOfObject.isInstance(elementFromSet)) {
						return ((ISetOfObjectsAttributeValue<O>) object).getValue();
					}
				}
			}

		}
		throw new IllegalArgumentException("No valid value for the type");
	}

}
