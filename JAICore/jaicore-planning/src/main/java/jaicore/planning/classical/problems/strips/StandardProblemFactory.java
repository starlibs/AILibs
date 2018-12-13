package jaicore.planning.classical.problems.strips;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.structure.VariableParam;

public class StandardProblemFactory {

	public static StripsPlanningProblem getBlocksWorldProblem() {

		/* create planning domain */
		List<StripsOperation> operations = new ArrayList<>();
		operations.add(new StripsOperation("pick-up", Arrays.asList(new VariableParam[] { new VariableParam("x") }), new Monom("clear(x) & ontable(x) & handempty()"),
				new Monom("holding(x)"), new Monom("clear(x) & ontable(x) & handempty()")));
		operations.add(new StripsOperation("put-down", Arrays.asList(new VariableParam[] { new VariableParam("x") }), new Monom("holding(x)"),
				new Monom("clear(x) & handempty() & ontable(x)"), new Monom("holding(x)")));
		operations.add(new StripsOperation("stack", Arrays.asList(new VariableParam[] { new VariableParam("x"), new VariableParam("y") }), new Monom("holding(x) & clear(y)"),
				new Monom("clear(x) & handempty() & on(x,y)"), new Monom("holding(x) & clear(y)")));
		operations.add(new StripsOperation("unstack", Arrays.asList(new VariableParam[] { new VariableParam("x"), new VariableParam("y") }),
				new Monom("handempty() & clear(x) & on(x,y)"), new Monom("clear(y) & holding(x)"), new Monom("handempty() & on(x,y) & clear(x)")));
		StripsPlanningDomain domain = new StripsPlanningDomain(operations);

		/* create planning problem */
		Monom init = new Monom("clear('c') & clear('a') & clear('b') & clear('d') & ontable('c') & ontable('a') & ontable('b') & ontable('d') & handempty()");
		Monom goal = new Monom("on('d','c') & on('c','b') & on('b','a')");
		StripsPlanningProblem problem = new StripsPlanningProblem(domain, init, goal);

		return problem;
	}

	public static StripsPlanningProblem getDockworkerProblem() {

		/* create planning domain */
		List<StripsOperation> operations = new ArrayList<>();
		operations.add(new StripsOperation("move", Arrays.asList(new VariableParam[] { new VariableParam("l"), new VariableParam("m"), new VariableParam("r") }),
				new Monom("adjacent(l,m) & at(r,l) & !occupied(m)"), new Monom("at(r,m) & occupied(m)"), new Monom("at(r,l) & occupied(l)")));
		operations.add(
				new StripsOperation("load", Arrays.asList(new VariableParam[] { new VariableParam("l"), new VariableParam("k"), new VariableParam("r"), new VariableParam("c") }),
						new Monom("belong(k,l) & holding(k,c) & at(r,l) & unloaded(r)"), new Monom("empty(k) & loaded(r,c)"), new Monom("holding(k,c) & unloaded(r)")));
		operations.add(
				new StripsOperation("unload", Arrays.asList(new VariableParam[] { new VariableParam("l"), new VariableParam("k"), new VariableParam("r"), new VariableParam("c") }),
						new Monom("belong(k,l) & empty(k) & at(r,l) & loaded(r,c)"), new Monom("holding(k,c) & unloaded(r)"), new Monom("empty(k) & loaded(r,c)")));
		operations.add(new StripsOperation("put",
				Arrays.asList(new VariableParam[] { new VariableParam("k"), new VariableParam("l"), new VariableParam("c"), new VariableParam("d"), new VariableParam("p") }),
				new Monom("belong(k,l) & attached(p,l) & holding(k,c) & top(d,p)"), new Monom("empty(k) & in(c,p) & top(c,p) & on(c,d)"), new Monom("holding(k,c) & top(d,p)")));
		operations.add(new StripsOperation("take",
				Arrays.asList(new VariableParam[] { new VariableParam("k"), new VariableParam("l"), new VariableParam("c"), new VariableParam("d"), new VariableParam("p") }),
				new Monom("belong(k,l) & attached(p,l) & empty(k) & on(c,d) & top(c,p)"), new Monom("holding(k,c) & top(d,p)"),
				new Monom("empty(k) & in(c,p) & top(c,p) & on(c,d)")));
		StripsPlanningDomain domain = new StripsPlanningDomain(operations);

		/* create planning problem */
		Monom init = new Monom(
				"attached('p1','l1') & attached('p2','l2') & in('c1','p1') & in('c3','p1') & top('c3','p1') & on('c3','c1') & on('c1','pallet') & in('c2','p2') & top('c2','p2') & on('c2','pallet') & belong('crane1','l1') & empty('crane1') & adjacent('l1','l2') & adjacent('l2','l1') & at('r1','l2') & occupied('l2') & unloaded('r1')");
		Monom goal = new Monom("loaded('r1','c3') & at('r1','l1')");
		StripsPlanningProblem problem = new StripsPlanningProblem(domain, init, goal);
		return problem;
	}
}
