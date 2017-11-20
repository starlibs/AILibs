package jaicore.search.algorithms.standard.npuzzle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jaicore.search.structure.core.NodeExpansionDescription;
import jaicore.search.structure.core.NodeType;

public class NPuzzleNode {
	//within the board, 0 is the empty space
	private int [][] board;
	private int emptyX;
	private int emptyY;
	
	public NPuzzleNode(int n) {
		board = new int[n][n];
		List<Integer> numbers = new ArrayList<>(n*n);
		
		for(int i = 0; i < n*n; i++) 
			numbers.add(i);
		//creating of a random starting configuration for the NPuzzleProblem
		for(int i = 0; i <n ; i++) {
			for(int j = 0; j < n; j++) {
				int index = (int) (Math.random()* numbers.size());
				int number = numbers.remove(index);
				board[i][j] = number;
				if(number == 0) {
					emptyX = j;
					emptyY = i;
				}
			}
		}
	}
	//TODO Shuffle the board does not work right now
//	public NPuzzleNode(int n, int perm) {
//		board = new int[n][n];
//		int x = 1;
//		for(int i = 0; i < n; i++) {
//			for(int j = 0; j < n; j++) {
//				board[i][j] = x;
//				x++;
//			}
//		}
//		emptyX = n-1;
//		emptyY = n-1;
//		/
//		for(int i = 0; i < perm; i++) {
//			String s = "";
//			if(emptyX> 0)//move left
//				s+= "l";
//			if(emptyX< n -1)//move right
//				s+= "r";
//			
//			if(emptyY>0)//move down
//				s+="u";
//			
//			if(emptyY< n -1)//move up
//				s+= "d";
//			
//			move(s.charAt((int) (Math.random()*s.length())));
//		}
//		System.out.println(this.toString());
//	}
	
	public NPuzzleNode(int [][] board, int emptyX, int emptyY) {
		this.board = board;
		this.emptyX = emptyX;
		this.emptyY = emptyY;
	}

	
	public int[][] getBoard() {
		return board;
	}

	public int getEmptyX() {
		return emptyX;
	}

	public int getEmptyY() {
		return emptyY;
	}
	
	
	/**
	 * Returns a graphical version of the board configuration. 
	 * Works best if there is no number with two or more digits.
	 */
	@Override
	public String toString() {
//		return "NPuzzleNode [board=" + Arrays.toString(board) + ", emptyX=" + emptyX + ", emptyY=" + emptyY + "]";
		String s = "";
		
		for(int j = 0; j < board.length; j++) {
			s+= "----";
		}
		s+= "\n";
		
		for(int i = 0; i< board.length; i++) {
			s += "| ";
			for(int j = 0; j < board.length; j++) {
				s += board[i][j] + " | ";
			}
			s += "\n";
			for(int j = 0; j < board.length; j++) {
				s+= "----";
			}
			s += "\n";
		}
		return s;
	}
	
	
	private void move(char m) {
		switch (m) {
			case 'l': 
				move(0,-1);
				break;
			case 'r': 
				move(0,1);
				break;
			case 'u': 
				move(-1,0);
			case 'd': 
				move(1,0);
				break;
		}
		
	}
	
	private void move(int y, int x) {
				
		int eX = getEmptyX();
		int eY = getEmptyY();
		board[eY][eX] = board[eY +y][eX+x];
		board[eY+y][eX+x] = 0;
		emptyY += y;
		emptyX += x;
	}
	
	
	
}
