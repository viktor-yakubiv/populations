import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BoardController extends JFrame {
    private static final Dimension DEFAULT_WINDOW_SIZE = new Dimension(800, 600);
    private static final Dimension MINIMUM_WINDOW_SIZE = new Dimension(600, 400);

    final Board model;
    final BoardView view;

    JButton startButton;
    JSpinner populationChooser;

    Thread boardIterator;

    public BoardController(Board model, BoardView view) {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(DEFAULT_WINDOW_SIZE);
        setMinimumSize(MINIMUM_WINDOW_SIZE);

        // Model and view
        this.model = model;
        this.view = view;

        // Controls
        initControls();
        view.addMouseMotionListener(new BoardActionsController(this));

        // View
        getContentPane().add(view);

        // Show window
        setVisible(true);
    }

    void initControls() {
        // Controls panel
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        getContentPane().add(panel, BorderLayout.LINE_END);

        // Population chooser.
        JLabel label = new JLabel("Population id:");
        panel.add(label);
        populationChooser = new JSpinner(new SpinnerNumberModel(1, 1, model.getPopulationsCount(), 1));
        panel.add(populationChooser);

        // Emulation controller.
        startButton = new JButton("Start");
        startButton.addActionListener(this::controlStartPause);
        startButton.setActionCommand("start");
        panel.add(startButton);
    }

    void controlStartPause(ActionEvent e) {
        JButton source = (JButton) e.getSource();
        if (e.getActionCommand().equals("start")) {
            boardIterator = new Thread(new BoardIterationsRunner(model, view));
            boardIterator.start();
            source.setText("Pause");
            source.setActionCommand("stop");
        } else {
            boardIterator.interrupt();
            source.setText("Start");
            source.setActionCommand("start");
        }
    }

    void actionSelect(Point p) {
        int width = view.getWidth();
        int height = view.getHeight();

        int cellSize = Math.min(width / model.getWidth(), height / model.getHeight());

        int xOffset = (width - (model.getWidth() * cellSize)) / 2;
        int yOffset = (height - (model.getHeight() * cellSize)) / 2;


        model.set(
                (p.x - xOffset) / cellSize,
                (p.y - yOffset) / cellSize,
                (int) (populationChooser.getValue()) - 1
        );

        view.repaint();
    }

    void actionHighlight(Point p) {
        //
    }
}


class BoardActionsController extends MouseAdapter {
    final BoardController parent;

    public BoardActionsController(BoardController parent) {
        this.parent = parent;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        parent.actionSelect(e.getPoint());
    }

    @Override
    public void mousePressed(MouseEvent e) {
        parent.actionSelect(e.getPoint());
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        parent.actionHighlight(e.getPoint());
    }
}


class BoardIterationsRunner implements Runnable {
    final Board model;
    final BoardView view;

    public BoardIterationsRunner(Board model, BoardView view) {
        this.model = model;
        this.view = view;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            model.increaseGeneration();
            view.repaint();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return;
            }
        }
    }
}
