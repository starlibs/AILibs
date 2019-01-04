package jaicore.search.algorithms.standard.rstar;


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
        assert x >= 0 && x < 16 : "x has to be greater equals zero and less 16. Given x = " + x;
        assert y >= 0 && x < 16 : "y has to be greater equals zero and less 16. Given y = " + y;

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

    public GridWorld onAction(String a) {

        int corr_a = Integer.parseInt(a);
        return onAction(corr_a);
    }

    /**
     * Calculate resulting GridWorld when taking action a.
     * @param a
     * @return null, if we would land outside the grid. A new Gridworld otherwise.
     */
    public GridWorld onAction(int a) {
        // x direction movement
        int dx = 1;
        if (a==2 || a==7)
            dx = 0;
        if (a==1 || a==4 || a==6)
            dx = -1;

        // y direction movement
        int dy = 1;
        if (a==4 || a==5)
            dy = 0;
        if (a==1 || a==2 || a==3)
            dy = -1;

        if (posx + dx < 0 || posy + dy < 0 || posx + dx > 15 || posy + dy > 15) {
            return null;
        } else {
            return new GridWorld(posx+dx, posy+dy);
        }
    }

    @Override
    public String toString() {
        return String.format("(%d, %d)", posx, posy);
    }
}
