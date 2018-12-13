package jaicore.search.testproblems.nqueens;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class QueenNode implements Serializable {
	int dimension;
	List<Integer> positions;
	

	/**
	 * Creates a QueenNode with a empty board
	 * @param dimension
	 * 		The dimension of the board.
	 */
	public QueenNode(int dimension) {
		this.positions = new ArrayList<>();
		this.dimension = dimension;
	}
	
	/**
	 * Creates a QueenNode with one Queen on it
	 * @param x
	 * 	The row position of the queen.
	 * @param y
	 * 	The column position of the queen.
	 * @param dimension
	 * 	The dimension of the board.
	 */
	public QueenNode(int x, int y, int dimension) {
		positions = new ArrayList<>();
//		positions.add(new Position(x, y));
		positions.add(x,y);
		this.dimension = dimension;
		
	}
	
	/**
	 * Creates a QueenNode with exiting positions of other queens
	 * @param pos
	 * 		The  positions of the other queens.
	 * @param y
	 * 		The column position of the newly placed queen.
	 * @param dimension
	 * 		The dimension of the board.
	 */
	public QueenNode(List<Integer> pos,int y, int dimension) {
		this.positions = new ArrayList<>();
		for(Integer p:pos)
			this.positions.add(new Integer(p));
//		positions.add(new Position(x, y));
		positions.add(y);
		this.dimension = dimension;
	}
	
	/**
	 * Creates a QueenNode with exiting positions of other queens
	 * @param pos
	 * 		The  positions of the other queens.
	 * @param x
	 * 		The row position of the newly placed queen.
	 * @param y
	 * 		The column position of the newly placed queen.
	 * @param dimension
	 * 		The dimension of the board.
	 */
	public QueenNode(List<Integer> pos, int x, int y, int dimension) {
		for(Integer p:pos)
			this.positions.add(new Integer(p));
		this.positions = pos;
//		positions.add(new Position(x, y));
		positions.add(x,y);
		this.dimension = dimension;
	}
	
	/**
	 * Creates a new QueenNode out of another QueenNode to add a new queen.
	 * @param n
	 * 		The old QueenNode.
	 * @param y
	 * 		The column position of the new queen.
	 */
	public QueenNode(QueenNode n, int y) {
		//Cloning the list
		this.positions = new ArrayList<>(n.getPositions().size());
		for(Integer p : n.getPositions())
			this.positions.add(new Integer(p));
		
		positions.add( y);
		this.dimension = n.getDimension();
	}
	
	


	public List<Integer> getPositions(){
		return this.positions;
	}
	
	public int getDimension() {
		return this.dimension;
	}
	
	@Override
	public String toString() {
		return positions.toString();
	}
	
	public String boardVisualizationAsString() {
		String s = "";
		for(int i = 0; i< dimension; i++) 
			s+="----";
		
		s+="\n|";
		
		for(int i = 0; i < dimension; i++) {
			for(int j = 0; j < dimension; j++) {
//				if(positions.contains(new Position(i,j)))
				if(positions.size() > i && positions.get(i) == j)
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
	
	/**
	 * Checks if a cell is attacked by the queens on the board
	 * @param i
	 * 		The row of the cell to be checked.
	 * @param j
	 * 		The collumn of the cell to be checked.
	 * @return
	 * 		<code>true</code> if the cell is attacked, <code>false</code> otherwise.
	 */
	public boolean attack(int i, int j) {
//		for(Position p: positions)
//			if(p.attack(i, j, dimension))
//				return true;
//	
//		return false;
		for(Integer p : positions) {
			if(j == p)
				return true;
			int x = Math.abs(i-this.positions.indexOf(p));
			if(j == p+x || p-x == j)
				return true;
		}
		return false;
	}
	
	
	public String toStringAttack() {
		String s = "";
		for(int i = 0; i< dimension; i++) 
			s+="----";
		
		s+="\n|";
		
		for(int i = 0; i < dimension; i++) {
			for(int j = 0; j < dimension; j++) {
//				if(positions.contains(new Position(i,j)))
				if(positions.get(i)== j)
					s+= " Q |";
				
				else {
					boolean attack = attack(i,j);					
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
	
	public int getNumberOfQueens() {
		return positions.size();
	}
	
	/**
	 * Returns the number of attacked cells of the current boardconfiguration
	 * @return	
	 * 		The number of attacked cells.
	 */
	public int getNumberOfAttackedCells() {
		int attackedCells = positions.size() * dimension;
		for(int i = positions.size(); i < dimension; i++) {
			for(int j  =0 ; j < dimension; j++) {
				if(this.attack(i, j))
					attackedCells ++;
			}
		}
		return attackedCells;
	}
	
	/**
	 * Returns the number of attacked cells in the next free row from top down.
	 * @return
	 * 		The number of attacked cells in the next row.
	 * @throws InterruptedException 
	 */
	public int getNumberOfAttackedCellsInNextRow() throws InterruptedException {
		int attackedCells = 0;
		for(int i = 0; i < dimension; i++) {
			if (Thread.interrupted())
				throw new InterruptedException();
			if(this.attack(dimension-1, i))
				attackedCells ++;
		}
		return attackedCells;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + dimension;
		result = prime * result + ((positions == null) ? 0 : positions.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QueenNode other = (QueenNode) obj;
		if (dimension != other.dimension)
			return false;
		if (positions == null) {
			if (other.positions != null)
				return false;
		} else if (!positions.equals(other.positions))
			return false;
		return true;
	}


	public int getNumberOfNotAttackedCells() {
		return (dimension*dimension)- getNumberOfAttackedCells();
	}
}
