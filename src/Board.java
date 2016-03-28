import java.awt.*;
import java.util.List;

public class Board {
    // Board dimensions
    private int width;
    private int height;

    public Board(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Returns list of alive neighbours for specified cell.
     * @param cell  cell to look-up neighbours
     * @return list of alive neighbours
     */
    List<Cell> getNeighbours(Cell cell) {
        return null;
    }

    boolean updateCell(Cell cell) {
        return false;
    }
}

class Cell {
    final Board parent;
    final Point point;
    Integer state;
    Integer nextState = null;

    Cell(Board parent, Point point) {
        this.parent = parent;
        this.point = point;
    }

    public Point getPoint() {
        return point;
    }

    public boolean isAlive() {
        return !(state == null);
    }

    public Integer getState() {
        return state;
    }

    public Integer nextState() {
        List<Cell> neighbours = parent.getNeighbours(this);

        nextState = null;
        return nextState;
    }

    public void flushState() {
        parent.updateCell(this);
    }
}
