package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan;

import java.util.ArrayList;
import java.util.List;

public class DisabledOptionPredicate extends OptionsPredicate  {

	@Override
	protected List<? extends Object> getValidValues() {
		return new ArrayList<>();
	}
}
