package jaicore.search.algorithms.standard.rstar;

import scala.Int;

public class GridWorld  {

    static int[][] myGrid = {
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 1},
            {1, 1, 1, 1, 7, 7, 7, 7, 7, 7, 7, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 7, 7, 7, 8, 7, 7, 7, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 7, 7, 8, 8, 8, 7, 7, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 7, 8, 8, 9, 8, 8, 7, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 7, 7, 8, 8, 8, 7, 7, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 7, 7, 7, 8, 7, 7, 7, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 7, 7, 7, 7, 7, 7, 7, 4, 4, 4, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 4, 4, 5, 4, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 4, 5, 6, 5, 4, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 4, 4, 5, 4, 4, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 4, 4, 4, 4, 4, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
    };

    private int posx, posy;

    public GridWorld(int x, int y) {
        assert x >= 0 && x < 16 : "x has to be greater equals zero and less 16.";
        assert y <= 0 && x < 16 : "y has to be greater equals zero and less 16.";

        posx = x;
        posy = y;
    }

    public int getX() {
        return posx;
    }


    public int getY() {
        return posy;
    }

    @Override
    public int hashCode() {
        // posx = 03, posy = 13 => 0313
        // posx = 10, posy = 1  => 1001
        return posx*100+posy;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GridWorld) {
            return ((posx == ((GridWorld) obj).getX()) && (posy == ((GridWorld) obj).getY()));
        } else {
            return false;
        }
    }
}
