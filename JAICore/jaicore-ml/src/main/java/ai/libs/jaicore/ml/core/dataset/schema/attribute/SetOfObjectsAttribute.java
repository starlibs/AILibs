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
	private transient Set<O> domain;

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
				Object anyElement = set.iterator().next();
				if (this.classOfObject.isInstance(anyElement) && this.domain != null) {
					return set.stream().allMatch(o -> this.domain.contains(o));
				}
			}
		}
		if (value instanceof SetOfObjectsAttributeValue) {
			Set<?> setOfObjects = ((SetOfObjectsAttributeValue<?>) value).getValue();
			return this.isValidValue(setOfObjects);
		}
		return false;
	}

	@Override
	public String getStringDescriptionOfDomain() {
		String description = "[Set]";
		if (this.domain != null) {
			StringJoiner stringJoiner = new StringJoiner(",");
			for (O object : this.domain) {
				stringJoiner.add(object.toString());
			}
			description += stringJoiner.toString();
		}
		return description + this.getName();
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
		if (this.isValidValue(object) && object instanceof ISetOfObjectsAttributeValue) {
			Set<?> set = ((ISetOfObjectsAttributeValue<?>) object).getValue();
			if (!set.isEmpty()) {
				Object elementFromSet = set.iterator().next();
				if (this.classOfObject.isInstance(elementFromSet)) {
					return ((ISetOfObjectsAttributeValue<O>) object).getValue();
				}
			}
		}

		throw new IllegalArgumentException("No valid value for the type");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((this.classOfObject == null) ? 0 : this.classOfObject.hashCode());
		result = prime * result + ((this.domain == null) ? 0 : this.domain.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		SetOfObjectsAttribute other = (SetOfObjectsAttribute) obj;
		if (this.classOfObject == null) {
			if (other.classOfObject != null) {
				return false;
			}
		} else if (!this.classOfObject.equals(other.classOfObject)) {
			return false;
		}
		if (this.domain == null) {
			if (other.domain != null) {
				return false;
			}
		} else if (!this.domain.equals(other.domain)) {
			return false;
		}
		return true;
	}

}
