package jaicore.basic.sets;

public class ElementDecorator<E> {
	private final E element;

	public ElementDecorator(final E element) {
		super();
		this.element = element;
	}

	public E getElement() {
		return this.element;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.element == null) ? 0 : this.element.hashCode());
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
		ElementDecorator other = (ElementDecorator) obj;
		if (this.element == null) {
			if (other.element != null) {
				return false;
			}
		} else if (!this.element.equals(other.element)) {
			return false;
		}
		return true;
	}
}
