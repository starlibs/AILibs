package jaicore.search.algorithms.standard.npuzzle;

import java.util.ArrayList;
import java.util.List;

public class NPuzzleNode {
	//within the board, 0 is the empty space
	int [][] board;
	int emptyX;
	int emptyY;
	
	public NPuzzleNode(int n) {
		board = new int[n][n];
		List<Integer> numbers = new ArrayList<>(n*n);
		
		for(int i = 0; i < n*n; i++) 
			numbers.add(i);
		
		for(int i = 0; i <n ; i++) {
			for(int j = 0; j < n; j++) {
				board[i][j] = numbers.get((int) (Math.random()*numbers.size()));
			}
		}
	}
	
	
}
