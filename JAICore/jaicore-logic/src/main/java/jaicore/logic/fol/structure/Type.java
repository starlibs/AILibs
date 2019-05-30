package jaicore.logic.fol.structure;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@SuppressWarnings("serial")
public class Type implements Serializable {

	private final String name;
	private final List<Type> superTypeList;
	private final List<Type> subTypeList;

	private final List<Type> subTypeOfBuffer;
	private final List<Type> supTypeOfBuffer;

	Type(final String name, final Collection<Type> parentTypeList) {
		// call plain constructor
		this(name);

		if (parentTypeList != null) {
			this.superTypeList.addAll(parentTypeList);
			for (Type parentType : parentTypeList) {
				parentType.subTypeList.add(this);
			}
		}
	}

	Type(final String name, final Type parentType) {
		// call plain constructor
		this(name);

		if (parentType != null) {
			this.superTypeList.add(parentType);
			parentType.subTypeList.add(this);
		}
	}

	public Type(final String name) {
		// no legal type definition
		if (name.trim().isEmpty()) {
			throw new IllegalArgumentException();
		}

		// assign type definition
		this.name = name;

		// initialize lists
		this.subTypeList = new LinkedList<>();
		this.superTypeList = new LinkedList<>();

		this.supTypeOfBuffer = new LinkedList<>();
		this.subTypeOfBuffer = new LinkedList<>();
	}

	public String getName() {
		return this.name;
	}

	public void addSubType(final Type newSubType) {
		if (this.isSubTypeOf(newSubType)) {
			throw new IllegalArgumentException("Cannot add " + newSubType + " as a sub-type of " + this + ", because the relation already exists the other way around.");
		}
		newSubType.superTypeList.add(this);
		this.subTypeList.add(newSubType);
	}

	public void removeSubType(final Type removeSubType) {
		removeSubType.superTypeList.remove(this);
		this.subTypeList.remove(removeSubType);
	}

	public List<Type> getDirectSubTypes() {
		return this.subTypeList;
	}

	public List<Type> getAllSubTypes() {
		List<Type> allSubTypeList = new LinkedList<>(this.getDirectSubTypes());

		for (Type subType : this.getDirectSubTypes()) {
			allSubTypeList.addAll(subType.getAllSubTypes());
		}

		return allSubTypeList;
	}

	public List<Type> getAllSubTypesIncl() {
		List<Type> allSubTypeInclList = this.getAllSubTypes();
		allSubTypeInclList.add(this);
		return allSubTypeInclList;
	}

	public void addSuperType(final Type newSuperType) {
		if (this.isSuperTypeOf(newSuperType)) {
			throw new IllegalArgumentException("Cannot add " + newSuperType + " as a super-type of " + this + ", because the relation already exists the other way around.");
		}
		newSuperType.subTypeList.add(this);
		this.superTypeList.add(newSuperType);
	}

	public void removeSuperType(final Type removeSuperType) {
		removeSuperType.subTypeList.remove(this);
		this.superTypeList.remove(removeSuperType);
	}

	public List<Type> getDirectSuperTypes() {
		return this.superTypeList;
	}

	public boolean isRootType() {
		return this.superTypeList.isEmpty();
	}

	/**
	 * Given the parameter typeToCheck, this method checks whether typeToCheck is actually a sub type of the current object. Thus, it checks whether this is a super type of typeToCheck.
	 *
	 * @param typeToCheck
	 *            A DataType to check whether it is a sub-type of this DataType.
	 *
	 * @return It returns true iff the given DataType typeToCheck is a sub-type of this DataType.
	 */
	public boolean isSuperTypeOf(final Type typeToCheck) {
		// robustness check
		if (typeToCheck == null) {
			throw new IllegalArgumentException("Null is not a feasible type for this function");
		}
		if (typeToCheck == this || this.subTypeOfBuffer.indexOf(typeToCheck) >= 0) {
			return true;
		}
		assert !this.subTypeList.contains(this) : ("Type " + this.getName() + " contains itself as a sub-type!");
		for (Type subType : this.subTypeList) {
			if (subType.isSuperTypeOf(typeToCheck)) {
				this.subTypeOfBuffer.add(typeToCheck);
				return true;
			}
		}
		return false;
	}

	/**
	 * Given the parameter typeToCheck, this method checks whether typeToCheck is actually a super type of the current object. Thus, it checks whether this is a sub type of typeToCheck.
	 *
	 * @param typeToCheck
	 *            A DataType to check whether it is a super type of this DataType
	 *
	 * @return It returns true iff the given DataType typeToCheck is a super type of this DataType.
	 */
	public boolean isSubTypeOf(final Type typeToCheck) {
		if (typeToCheck == this || this.supTypeOfBuffer.indexOf(typeToCheck) >= 0) {
			return true;
		}
		if (!this.superTypeList.isEmpty() && typeToCheck.isSuperTypeOf(this)) {
			this.supTypeOfBuffer.add(typeToCheck);
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.name + ";");
		for (Type superType : this.superTypeList) {
			sb.append(superType.name);
			if (this.superTypeList.indexOf(superType) < this.superTypeList.size() - 1) {
				sb.append('&');
			}
		}
		return sb.toString();
	}

	public void addSuperType(final Set<Type> parentTypeCollection) {
		for (Type superType : parentTypeCollection) {
			this.addSuperType(superType);
		}
	}

	/**
	 * Searches in data type DAG for a greatest sub type of the given two types.
	 *
	 * @param type
	 *            Type to check for sub type relation.
	 * @param type2
	 *            Type to check for sub type relation.
	 * @return Returns the concreter type if the two types are related to each other. Otherwise, it returns null.
	 */
	public static Type getGreatestSubType(final Type type, final Type type2) {
		if (type.isSubTypeOf(type2)) {
			return type;
		} else {
			if (type2.isSubTypeOf(type)) {
				return type2;
			} else {
				return null;
			}
		}
	}

	public List<Type> getInheritanceHierarchyIncludingType() {
		List<Type> inheritanceList = new LinkedList<>();
		inheritanceList.add(this);
		for (Type superType : this.superTypeList) {
			inheritanceList.addAll(superType.getInheritanceHierarchyIncludingType());
		}
		return inheritanceList;
	}

	public List<Type> getConcretesHierarchyIncludingType() {
		List<Type> concretesList = new LinkedList<>();
		concretesList.add(this);
		for (Type subType : this.subTypeList) {
			concretesList.addAll(subType.getConcretesHierarchyIncludingType());
		}
		return concretesList;
	}

	@Override
	public int hashCode() {
		return this.name.hashCode();
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof Type)) {
			return false;
		}
		Type other = (Type) o;
		return this.name.equals(other.name);
	}

	public List<Type> getAllSuperTypes() {
		List<Type> superTypes = new LinkedList<>(this.getDirectSuperTypes());

		for(Type superType : this.getDirectSuperTypes()) {
			superTypes.addAll(superType.getAllSuperTypesIncl());
		}

		return superTypes;
	}

	public List<Type> getAllSuperTypesIncl() {
		List<Type> superTypes = new LinkedList<>(this.getAllSuperTypes());
		superTypes.add(this);
		return superTypes;
	}

	public String serialize() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getName());
		sb.append(";");
		if (!this.isRootType()) {
			for (Type superType : this.getDirectSuperTypes()) {
				sb.append(superType.getName());
				if (this.getDirectSuperTypes().indexOf(superType) < this.getDirectSuperTypes().size() - 1) {
					sb.append("&");
				}
			}
		}
		return sb.toString();
	}

}
