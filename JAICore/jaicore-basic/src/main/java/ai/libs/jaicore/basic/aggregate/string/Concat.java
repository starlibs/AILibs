package ai.libs.jaicore.basic.aggregate.string;

import java.util.List;

import ai.libs.jaicore.basic.sets.SetUtil;

/**
 * Concat is an aggregation function for Strings simply concatenating all the String values provided.
 *
 * @author mwever
 */
public class Concat implements IStringAggregateFunction {

	@Override
	public String aggregate(final List<String> values) {
		return SetUtil.implode(values, "");
	}

}
