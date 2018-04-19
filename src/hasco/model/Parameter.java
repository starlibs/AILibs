package hasco.model;

public abstract class Parameter {
	private final String name;
	private final Object defaultValue;

	public Parameter(String name, Object defaultValue) {
		super();
		this.name = name;
		this.defaultValue = defaultValue;
	}

	public String getName() {
		return name;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Parameter other = (Parameter) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
