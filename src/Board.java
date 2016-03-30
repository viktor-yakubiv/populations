import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

import static java.util.concurrent.Executors.newFixedThreadPool;

public class Board implements Runnable, Iterable<Cell> {
    // Board dimensions
    private int width;
    private int height;

    // Board data - map af alive cells and it's states
    private HashMap<Point, Integer> cells = new HashMap<>();

    public Board(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Integer get(int x, int y) {
        return cells.get(new Point(x, y));
    }

    public void set(int x, int y, Integer populationIndex) {
        updateCell(new Cell(this, new Point(x, y), populationIndex));
    }

    /**
     * Returns list of alive neighbours for specified cell.
     * @param cell  cell to look-up neighbours
     * @return list of alive neighbours
     */
    public List<Cell> getNeighbours(Cell cell) {
        List<Cell> neighbours = new ArrayList<>(8);

        int cellX = cell.getPoint().x;
        int cellY = cell.getPoint().y;

        for (int y = cellY - 1; y <= cellY + 1; ++y) {
            for (int x = cellX - 1; x <= cellX + 1; ++x) {
                if ((x < 0) || (x >= getWidth())) continue;
                if ((y < 0) || (y >= getHeight())) continue;
                if ((x == cellX) && (y == cellY)) continue;

                Point neighbourKey = new Point(x, y);
                Integer neighbourValue = cells.get(neighbourKey);
                if (neighbourValue != null) {
                    neighbours.add(new Cell(this, neighbourKey, neighbourValue));
                }
            }
        }

        return neighbours;
    }

    List<Cell> getSpaceNearCell(Cell cell) {
        List<Cell> freeCells = new ArrayList<>(8);

        int cellX = cell.getPoint().x;
        int cellY = cell.getPoint().y;

        for (int y = cellY - 1; y <= cellY + 1; ++y) {
            for (int x = cellX - 1; x < cellX + 1; ++x) {
                if ((x < 0) || (x >= getWidth())) continue;
                if ((y < 0) || (y >= getHeight())) continue;
                if ((x == cellX) && (y == cellY)) continue;

                Point neighbourKey = new Point(x, y);
                Integer neighbourValue = cells.get(neighbourKey);
                if (neighbourValue == null) {
                    freeCells.add(new Cell(this, neighbourKey, null));
                }
            }
        }

        return freeCells;
    }

    public void increaseGeneration() {
        // Concurrent
        ExecutorService executor = newFixedThreadPool(4);

        // Return if nothing to process
        if (cells.size() == 0) return;

        // Generate cells for processing
        Set<Cell> cellsToProcess = new HashSet<>(9 * cells.size());
        for (Cell cell : this) {
            cellsToProcess.add(cell);
            cellsToProcess.addAll(getSpaceNearCell(cell));
        }

        // Init next cell states
        Queue<Future<Cell>> cellsToUpdate = new LinkedBlockingQueue<>(9 * cells.size());
        for (Cell cell : cellsToProcess) {
            cellsToUpdate.add(executor.submit(() -> {
                cell.nextState();
                return cell;
            }));
        }

        // Flush cells state
        Queue<Future<Cell>> cellsDone = new LinkedBlockingQueue<>(9 * cells.size());
        for (Future<Cell> cellFuture : cellsToUpdate) {
            try {
                Cell cell = cellFuture.get();
                cellsDone.add(executor.submit(() -> {
                    cell.flushState();
                    return cell;
                }));
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        // Wait all cells update
        while (!cellsDone.isEmpty()) {
            try {
                cellsDone.poll().get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            executor.shutdownNow();
        }
    }

    void updateCell(Cell cell) {
        if (cell.getState() == null) {
            cells.remove(cell.getPoint());
        } else {
            cells.put(cell.getPoint(), cell.getState());
        }
    }

    @Override
    public Iterator<Cell> iterator() {
        return new BoardIterator(this, this.cells);
    }

    @Override
    public void run() {
        increaseGeneration();
    }

    @Override
    public String toString() {
        String s = "";
        for (int y = 0; y < getHeight(); ++y) {
            for (int x = 0; x < getWidth(); ++x) {
                Integer population = get(x, y);
                s += ((population == null) ? " " : population);
            }
            s += "\n";
        }
        return s;
    }


    public static void main(String[] args) {
        final int boardWidth = 50;
        final int boardHeight = 10;
        final int cellsCount = (int) (0.1 * boardWidth * boardHeight);
        final int populationsCount = 1;

        Board board = new Board(boardWidth, boardHeight);

        // Random set board
        for (int i = 0; i < cellsCount; ++i) {
            board.set(
                    (int) (1000 * Math.random()) % boardWidth,
                    (int) (1000 * Math.random()) % boardHeight,
                    (int) (1000 * Math.random()) % populationsCount
            );
        }

        // Add alive area
        board.set(boardWidth / 4, boardHeight / 2, 0);
        board.set(boardWidth / 4 + 1, boardHeight / 2, 0);
        board.set(boardWidth / 4, boardHeight / 2 + 1, 0);
        board.set(boardWidth / 4 + 1, boardHeight / 2 + 1, 0);

        // Create boards delimiter
        String delimiter = "\n";
        for (int i = 0; i < boardWidth; ++i) delimiter += '-';
        delimiter += "\n";

        // Output 10 generations
        System.out.println(board + delimiter);
        for (int i = 0; i < 9; ++i) {
            board.increaseGeneration();
            System.out.println(board + delimiter);
        }
    }
}


class BoardIterator implements Iterator<Cell> {
    final Board parent;
    final Iterator<Map.Entry<Point, Integer>> parentIterator;

    BoardIterator(Board parent, Map<Point, Integer> cells) {
        this.parent = parent;
        this.parentIterator = cells.entrySet().iterator();
    }


    @Override
    public boolean hasNext() {
        return parentIterator.hasNext();
    }

    @Override
    public Cell next() {
        Map.Entry<Point, Integer> entry = parentIterator.next();
        return new Cell(parent, entry.getKey(), entry.getValue());
    }

    @Override
    public void remove() {
        parentIterator.remove();
    }
}

class Cell {
    final Board parent;
    final Point point;
    Integer state;
    Integer nextState = null;

    Cell(Board parent, Point point, Integer state) {
        this.parent = parent;
        this.point = point;
        this.state = state;
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

        if (isAlive() && ((neighbours.size() == 2) || neighbours.size() == 3)) {
            nextState = state;
        } else if (neighbours.size() == 3) {
            nextState = neighbours.get(0).getState(); // FIXME: check
        } else {
            nextState = null;
        }

        return nextState;
    }

    public void flushState() {
        state = nextState;
        parent.updateCell(this);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Cell cell = (Cell) o;

        return parent.equals(cell.parent) && point.equals(cell.point);

    }

    @Override
    public int hashCode() {
        int result = parent.hashCode();
        result = 31 * result + point.hashCode();
        return result;
    }


    @Override
    public String toString() {
        return "Cell[" + point.x + "," + point.y + "] " +
                ((state == null) ? "empty" : "= " + state);
    }
}
