package jaicore.ml.ranking.clusterbased.customdatatypes;

import java.util.List;

/**
 * Group.java - Stores a group with it center as ID and the associated instances
 *
 * @author Helen Bierling
 *
 * @param <C>
 *            The identifier of the group
 * @param <I>
 *            The instances in the group
 */
public class Group<C, I> {
	private List<ProblemInstance<I>> problemInstances;
	private GroupIdentifier<C> groupIdentifier;

	public Group(final List<ProblemInstance<I>> instanlist, final GroupIdentifier<C> id) {
		this.problemInstances = instanlist;
		this.groupIdentifier = id;
	}

	public List<ProblemInstance<I>> getInstances() {
		return this.problemInstances;
	}

	public void setInstances(final List<ProblemInstance<I>> newInstances) {
		this.problemInstances = newInstances;
	}

	public void setGroupIdentifier(final GroupIdentifier<C> newIdentifer) {
		this.groupIdentifier = newIdentifer;
	}

	public void addInstance(final ProblemInstance<I> newInstance) {
		this.problemInstances.add(newInstance);
	}

	public GroupIdentifier<C> getId() {
		return this.groupIdentifier;
	}

}
