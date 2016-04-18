public class Application {
    final static int boardWidth  = 60;
    final static int boardHeight = 40;

    static void start() {
        Board model = new Board(boardWidth, boardHeight);

        // Random set board
        final int cellsCount = (int) (0.1 * boardWidth * boardHeight);
        final int populationsCount = 2;
        for (int i = 0; i < cellsCount; ++i) {
            model.set(
                    (int) (1000 * Math.random()) % boardWidth,
                    (int) (1000 * Math.random()) % boardHeight,
                    (int) (1000 * Math.random()) % populationsCount
            );
        }
        model.setPopulationsCount(populationsCount);

        BoardView view = new BoardView(model);
        BoardController controller = new BoardController(model, view);
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(Application::start);
    }
}
