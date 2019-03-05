package jaicore.basic.aggregate;

import java.util.List;

public interface IAggregateFunction<DOMAIN> {

	public DOMAIN aggregate(List<DOMAIN> values);

}
