package ai.libs.jaicore.math.probability.pl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;

public class PLInferenceProblemEncoder {
	private Map<Object, Short> ext2int;

	public PLInferenceProblem encode(final Collection<? extends List<?>> rankings) {
		if (this.ext2int != null) {
			throw new IllegalStateException();
		}
		this.ext2int = new HashMap<>();
		List<ShortList> encodedRankings = new ArrayList<>(rankings.size());
		for (List<?> ranking : rankings) {
			ShortList encodedRanking = new ShortArrayList();
			for (Object o : ranking) {
				short index = this.ext2int.computeIfAbsent(o, obj -> (short)this.ext2int.size());
				encodedRanking.add(index);
			}
			encodedRankings.add(encodedRanking);
		}
		return new PLInferenceProblem(encodedRankings);
	}

	public short getIndexOfObject(final Object o) {
		return this.ext2int.get(o);
	}
}
