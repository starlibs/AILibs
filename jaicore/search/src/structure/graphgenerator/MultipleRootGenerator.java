package jaicore.search.structure.graphgenerator;

import java.util.Collection;

public interface MultipleRootGenerator<T> extends RootGenerator<T> {
	public Collection<T> getRoots();
}
