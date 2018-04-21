package jaicore.planning.algorithms;

import java.util.List;

import jaicore.planning.model.core.Action;

/**
 * Planning algorithms return objects of a class that implements this interface.
 * On one hand, this makes sure that the client can retrieve the desired plan.
 * On the other hand, using such an interface instead of only returning List<Action>
 * allows the planner to use more complex solution objects that can be used to interact with
 * the client to later retrieve desired pieces of information.
 * 
 * @author fmohr
 *
 */
public interface IPlanningSolution {
	public List<Action> getPlan();
}
