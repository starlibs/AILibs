package jaicore.logic.fol.structure;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TypeModule {

	private final Map<String, Type> typeMap = new HashMap<>();
	
	private final Map<String,ConstantParam> constants = new HashMap<>();

	public TypeModule() {}
	
	public TypeModule(Collection<Type> types) {
		for (Type type : types) {
			this.typeMap.put(type.getName(), type);
		}
	}

	public Type getType(String nameOfType) {
		if (nameOfType.trim().equals("")) {
			throw new IllegalArgumentException("Empty string is no valid name for a datatype.");
		}

		Type resultType = typeMap.get(nameOfType);
		if (resultType == null) {
			resultType = new Type(nameOfType);
			this.typeMap.put(nameOfType, resultType);
		}

		return resultType;
	}

	public int size() {
		return this.typeMap.size();
	}

	public Collection<Type> getAllTypes() {
		return this.typeMap.values();
	}
	
	public List<Type> getListOfAllTypes() {
		List<Type> result = new LinkedList<>();
		result.addAll(this.typeMap.values());
		return result;
	}

	public void merge(TypeModule typeModule) {
		for (Type otherType : typeModule.getAllTypes()) {
			this.getType(otherType.getName());
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for (Type type : this.typeMap.values()) {
			sb.append(type);
			sb.append("\n");
		}

		return sb.toString();
	}

	public void setConstantType(String constant, Type type) {
		constants.put(constant, new ConstantParam(constant, type));
	}
	
	public Type getConstantType(String constant) {
		if (!constants.containsKey(constant))
			throw new IllegalArgumentException("Constant " + constant + " not found!");
		return constants.get(constant).getType();
	}

	public Collection<ConstantParam> getConstants() {
		return constants.values();
	}
}
