package jaicore.search.algorithms.standard.nqueens;

import java.util.ArrayList;
import java.util.List;

public class QueenNode {
	/*
	 * Helperclass to store the positions of the queen.
	 */
	public class Position {
		int x;
		int y;
		
		public Position(int x, int y) {
			this.x= x;
			this.y = y;
		}
		
		public Position(Position pos) {
			this.x = pos.getX();
			this.y = pos.getY();
		}

		public int getX() {
			return x;
		}


		public int getY() {
			return y;
		}

		public boolean equals(Object obj) {
			Position p =(Position) obj;
			if(p.getX() == x && p.getY() == y)
				return true;
			else
				return false;
		}
		
		@Override
		public String toString() {
			return "("+x+"," + y+ ")";
		}

		public boolean attack(int i, int j, int dimension ) {
			if(i == x || j == y || isOnDiag(i,j, dimension))
				return true;
			return false;
		}

		private boolean isOnDiag(int i, int j, int dimension) {
			int ex = x;
			int ey = y;
			//left up
			while(ex >= 0 && ey >= 0) {
				ex --;
				ey --;
				if(ex == i && ey == j) 
					return true;				
			}
			//right up
			ex = x;
			ey = y;
			while(ex >= 0 && ey < dimension) {
				ex --;
				ey ++;
				if(ex == i && ey == j) 
					return true;				
			}
			//right down
			ex = x;
			ey = y;
			while(ex <dimension  && ey < dimension) {
				ex ++;
				ey ++;
				if(ex == i && ey == j) 
					return true;
			}
			//left down
			ex = x;
			ey = y;
			while(ex < dimension && ey >= 0) {
				ex ++;
				ey --;
				if(ex == i && ey == j) 
					return true;				
			}
			return false;
		}
		
		
	}

	List<Position> positions;
	
	int dimension;
	
	public QueenNode(int x, int y, int dimension) {
		positions = new ArrayList<>();
		positions.add(new Position(x, y));
		this.dimension = dimension;
		
	}
	
	public QueenNode(List<Position> pos, int x, int y, int dimension) {
		for(Position p:pos)
			this.positions.add(new Position(p));
		this.positions = pos;
		positions.add(new Position(x, y));
		this.dimension = dimension;
	}
	
	public QueenNode(QueenNode n, int x, int y) {
		//Cloning the list
		this.positions = new ArrayList<>(n.getPositions().size());
		for(Position p : n.getPositions())
			this.positions.add(new Position(p));
		
		positions.add(new Position(x, y));
		this.dimension = n.getDimension();
	}
	
	
	public List<Position> getPositions(){
		return this.positions;
	}
	
	public int getDimension() {
		return this.dimension;
	}
	
	@Override
	public String toString() {
		String s = "";
		for(int i = 0; i< dimension; i++) 
			s+="----";
		
		s+="\n|";
		
		for(int i = 0; i < dimension; i++) {
			for(int j = 0; j < dimension; j++) {
				if(positions.contains(new Position(i,j)))
					s+= " Q |";
				else
					s+= "   |";
			}
			s+= "\n";
			for(int j = 0; j<dimension; j++) {
				s+= "----";
			}
			if(i < dimension-1)
				s+= "\n|";
		}
		
		return s;
	}
	
	public boolean attack(int i, int j) {
		for(Position p: positions)
			if(p.attack(i, j, dimension))
				return true;
	
		return false;
	}
	public String toStringAttack() {
		String s = "";
		for(int i = 0; i< dimension; i++) 
			s+="----";
		
		s+="\n|";
		
		for(int i = 0; i < dimension; i++) {
			for(int j = 0; j < dimension; j++) {
				if(positions.contains(new Position(i,j)))
					s+= " Q |";
				
				else {
					boolean attack = false;
					for(Position p : positions) {
						attack = attack(i,j);
						if(attack)
							break;
					}
					if(attack)
						s+= " O |";
					else
						s+= "   |";
				}
			}
			s+= "\n";
			for(int j = 0; j<dimension; j++) {
				s+= "----";
			}
			if(i < dimension-1)
				s+= "\n|";
		}
		
		return s;
	}
}
