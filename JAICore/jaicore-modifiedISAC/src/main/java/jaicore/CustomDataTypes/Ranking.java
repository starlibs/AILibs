package jaicore.CustomDataTypes;

import java.util.ArrayList;

public class Ranking<S> {
	ArrayList<Solution<S>> ranking;
	Ranking(){
		this.ranking = new ArrayList<Solution<S>>();
	}
	Ranking(ArrayList<Solution<S>> rank){
		this.ranking = rank;
	}
	
	void addSolutionToRanking(Solution<S> newSolution){
		this.ranking.add(newSolution);
	}
	
	public ArrayList<Solution<S>> getRanking(){
		return this.ranking;
	}
}
