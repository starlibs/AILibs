package jaicore.logic.fol.theories.set;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jaicore.basic.sets.SetUtil;
import jaicore.logic.fol.structure.Monom;

public abstract class SetTheoryUtil {

	public static List<String> getObjectsInSet(Monom state, String setDescriptor) {
		if (setDescriptor.startsWith("{") && setDescriptor.endsWith("}")) {
			return new ArrayList<>(SetUtil.unserializeSet(setDescriptor));
		}
		return state.stream().filter(l -> l.getPropertyName().equals("in") && l.getParameters().get(1).getName().equals(setDescriptor))
				.map(l -> l.getConstantParams().get(0).getName()).collect(Collectors.toList());
	}
}
