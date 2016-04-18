import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

class BoardView extends JPanel {
    // Population colors
    // Google Material Palette
    private final static int[] colors = {
            0xf44336, // Red
            0x2196f3, // Blue
            0xffeb3b, // Yellow
            0x4caf50, // Green
            0xffc107, // Amber
            0x8bc34a, // Light Green
            0x9c27b0, // Purple
            0xff9800, // Orange
            0x3f51b5, // Indigo
            0xcddc39, // Lime
            0x673ab7, // Deep Purple
            0xff5722, // Deep Orange
            0x03a9f4, // Light Blue
            0x00bcd4, // Cyan
            0x009688, // Teal
            0xe91e63  // Pink
    };

    // Model
    final Board model;

    // Board cells
    ArrayList<Rectangle> cells;
    Point selectedCell;


    public BoardView(Board model) {
        this.model = model;
        this.cells = new ArrayList<>(this.model.getWidth() * this.model.getHeight());
    }


    @Override
    public Dimension getPreferredSize() {
        return new Dimension(8 * model.getWidth(), 8 * model.getHeight());
    }

    @Override
    public void invalidate() {
        cells.clear();
        selectedCell = null;
        super.invalidate();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();

        int colCount = model.getWidth();
        int rowCount = model.getHeight();

        int width = getWidth();
        int height = getHeight();

        int cellSize = Math.min(width / colCount, height / rowCount);

        int xOffset = (width - (colCount * cellSize)) / 2;
        int yOffset = (height - (rowCount * cellSize)) / 2;

        if (cells.size() != rowCount * colCount) cells.clear();
        if (cells.isEmpty()) {
            for (int row = 0; row < rowCount; row++) {
                for (int col = 0; col < colCount; col++) {
                    Rectangle cellRect = new Rectangle(
                            xOffset + (col * cellSize),
                            yOffset + (row * cellSize),
                            cellSize,
                            cellSize);
                    cells.add(cellRect);
                }
            }
        }

        // Show alive cells
        int populationsCount = model.getPopulationsCount();
        for (Cell cell : model) {
            Rectangle cellRect = cells.get(cell.getPoint().y * colCount + cell.getPoint().x);
            g2d.setColor(getColor(cell.getState(), populationsCount));
            g2d.fill(cellRect);
        }

        // Show preview cell
        if (selectedCell != null) {
            int index = selectedCell.x + (selectedCell.y * colCount);
            Rectangle cell = cells.get(index);
            g2d.setColor(Color.BLUE);
            g2d.fill(cell);
        }

        // Show all cells
        g2d.setColor(new Color(0x616161));
        cells.forEach(g2d::draw);

//        for (int row = 0; row < rowCount; row++) {
//            for (int col = 0; col < colCount; col++) {
//                Rectangle cell = cells.get(row * colCount + col);
//                int colorIndex = model.get(row, col);
//                if (colorIndex != -1) {
//                    g2d.setColor(colors[colorIndex]);
//                    g2d.fill(cell);
//                }
//                g2d.setColor(Color.GRAY);
//                g2d.draw(cell);
//            }
//        }

        g2d.dispose();
    }

    Color getColor(int colorIndex, int colorsCount) {
        return new Color(colors[colorIndex % colors.length]);
    }
}
