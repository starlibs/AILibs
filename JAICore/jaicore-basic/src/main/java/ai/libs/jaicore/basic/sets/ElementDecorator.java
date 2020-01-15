package ai.libs.jaicore.basic.sets;

import org.api4.java.common.attributedobjects.IElementDecorator;

public class ElementDecorator<E> implements IElementDecorator<E> {
	private final E element;

	public ElementDecorator(final E element) {
		super();
		this.element = element;
	}

	@Override
	public E getElement() {
		return this.element;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.getElement() == null) ? 0 : this.getElement().hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		IElementDecorator<?> other = (IElementDecorator<?>) obj;
		if (this.element == null) {
			if (other.getElement() != null) {
				return false;
			}
		} else if (!this.element.equals(other.getElement())) {
			return false;
		}
		return true;
	}
}
