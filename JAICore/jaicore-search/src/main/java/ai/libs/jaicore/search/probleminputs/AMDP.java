package ai.libs.jaicore.search.probleminputs;

import java.util.Random;

public abstract class AMDP<N, A, V extends Comparable<V>> implements IMDP<N, A, V> {

	private final N initState;
	private final Random rand;

	public AMDP(final N initState) {
		this(initState, new Random());
	}

	public AMDP(final N initState, final Random rand) {
		super();
		this.initState = initState;
		this.rand = rand;
	}

	@Override
	public N getInitState() {
		return this.initState;
	}

	@Override
	public double getProb(final N state, final A action, final N successor) {
		return this.getProb(state, action).get(successor);
	}

	public N drawSuccessorState(final N state, final A action) {
		return MDPUtils.drawSuccessorState(this, state, action, this.rand);
	}
}
