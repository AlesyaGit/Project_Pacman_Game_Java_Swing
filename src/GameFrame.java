import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;

public class GameFrame extends JFrame {

    private final TableModel tableModel;


    private Thread gameEngine;

    private Thread threadTable;

    private JTable table;


    private final JLabel scoreLabel = new JLabel("Score: ");

    private final JLabel heartLabel = new JLabel();



    public void start(){
        setThreads();
        setVisible(true);
        tableModel.start();
        threadTable.start();
        gameEngine.start();
    }

    public void stop(){
        setVisible(false);
        tableModel.stop();
    }

    public GameFrame(ScoreStore scoreStore){
        this.tableModel = new TableModel(this, scoreStore);
//создаётся экземпляр объекта TableModel,которому передаются такие значения как
        // текущий объект GameFrame и scoreStore
        table = new JTable(tableModel);
        //затем мы создаём JTable таблицу в которую передаём как рах таки наш новосозданный экземпляр
        //TableModel
        table.setTableHeader(null);
        table.setRowHeight(50);
        List<TableColumn> list = Collections.list(table.getColumnModel().getColumns());//с помщью метода колектион.лист
        //мы преобразовываем наши столбцы в лист столбцов,а потом с помощью метода форИАЧ мы задаём им всем ширину 50 пикселей
        list.forEach(x->x.setPreferredWidth(50));
        table.setCellSelectionEnabled(false);
        table.setDefaultRenderer(Object.class, new CellRenderer(tableModel));
        //Рендерер определяет, как отображать содержимое ячеек таблицы.
        setTitle("Game");
        setPreferredSize(new Dimension(800, 495));
        setLayout(new BorderLayout());//BorderLayout для расположения элементов в окне.
        add(table, BorderLayout.CENTER);//Добавляется таблица в центральную часть окна.
        add(scoreLabel, BorderLayout.NORTH); //Добавляется метка счета в верхнюю часть окна.


        ImageIcon heartIcon = new ImageIcon("resources/heart1.png");
        heartLabel.setIcon(heartIcon);

        JPanel scorePanel = new JPanel();

        scorePanel.setBackground(Color.YELLOW);
        scorePanel.add(scoreLabel);
        scorePanel.add(heartLabel);

        Font labelFont = heartLabel.getFont();

        scoreLabel.setFont(labelFont.deriveFont(labelFont.getSize() + 8f));
        heartLabel.setFont(labelFont.deriveFont(labelFont.getSize() + 8f));

        add(scorePanel, BorderLayout.NORTH);


        setFocusable(true);
        requestFocus();
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                switch (keyCode) {
                    case KeyEvent.VK_UP -> tableModel.setDirection(0, -1);
                    case KeyEvent.VK_DOWN -> tableModel.setDirection(0, 1);
                    case KeyEvent.VK_LEFT -> tableModel.setDirection(-1, 0);
                    case KeyEvent.VK_RIGHT -> tableModel.setDirection(1, 0);
                }
            }
        });
        //включая Java Swing, используется инвертированная система координат, где положительное значение оси Y направлено вниз, а отрицательное значение направлено вверх.
        //
        //Поэтому, в данном случае, чтобы движение объекта вверх соответствовало
        // ориентации системы координат в Java Swing,
        // используется значение -1 для вертикального направления движения вверх.
        KeyStroke keyStroke = KeyStroke.getKeyStroke("control shift Q");

        Action action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stop();
                tableModel.saveScore();

            }
        };

        InputMap inputMap = table.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = table.getActionMap();

        inputMap.put(keyStroke, "stopGame");
        actionMap.put("stopGame", action);



        pack();
        //После этого вызывается pack(), чтобы окно автоматически подстроилось под свое содержимое.
    }
    ///////////////////////////
    public void updateScore(int score) {
        scoreLabel.setText("Score: " + score);
    }
    public void setThreads(){
        this.gameEngine = new Thread(()->{
            while (tableModel.isRunning()){
                try {
                    Thread.sleep(700);
                } catch (Exception e) {
                    //
                }
                tableModel.moveGhosts();
                tableModel.movePacman();
                tableModel.gameWin();
            }
        });
        this.threadTable = new Thread(()->{
            while (tableModel.isRunning()){
                try {
                    Thread.sleep(18);
                } catch (Exception e) {
                    //
                }
                tableModel.fireTableDataChanged();
            }
        });
    }
}

class TableModel extends AbstractTableModel {
    private final ScoreStore scoreStore;
    private int foodCount = 0;
    private int directionX = 1;

    public int getDirectionX() {
        return directionX;
    }

    public int getDirectionY() {
        return directionY;
    }

    private int directionY = 0;
    private int[][] ghosts;
    private boolean isRunning = false;
    private final GameFrame frame;

    private final String[][] initBoard = {
            { "#", "#", "#", "#", "#", "#", "#", "#", "#", "#", "#", "#", "#", "#", "#", "#"},
            { "#", ".", ".", ".", ".", ".", ".", ".", ".", ".", ".", ".", ".", ".", ".", "#"},
            { "#", ".", "#", "#", "#", ".", "#", "#", ".", "#", "#", "#", "#", ".", ".", "#"},
            { "#", ".", ".", ".", ".", ".", ".", ".", ".", ".", ".", ".", ".", ".", ".", "#"},
            { "#", ".", "#", "#", "#", ".", "#", "#", ".", "#", "#", "#", "#", ".", ".", "#"},
            { "#", ".", ".", ".", ".", ".", ".", ".", ".", ".", ".", ".", ".", ".", ".", "#"},
            { "#", " ", "#", "#", "#", ".", "#", "#", ".", "#", "#", "#", "#", ".", ".", "#"},
            { "#", " ", ".", ".", ".", ".", ".", ".", ".", ".", ".", ".", ".", ".", ".", "#"},
            { "#", "#", "#", "#", "#", "#", "#", "#", "#", "#", "#", "#", "#", "#", "#", "#"},
    };
    private String[][] board = copy(initBoard);

    public TableModel(GameFrame frame, ScoreStore scoreStore){
        this.frame = frame;
        this.scoreStore = scoreStore;
    }
    private int pacmanRow = 3;
    private int pacmanCol = 5;
    public int getColumnCount() {
        return board[0].length;
    }

    public int getRowCount() {
        return board.length;
    }

    public Object getValueAt(int row, int col) {
        return board[row][col];
    }
    public void moveGhosts() {
        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
        for (int[] ghost : ghosts) {
            ArrayList<int[]> validDirections = new ArrayList<>();

            for (int[] direction : directions) {
                int dx = direction[0];
                int dy = direction[1];

                if (dx == -ghost[2] && dy == -ghost[3]) continue;

                int newGhostRow = ghost[0] + dx;
                int newGhostCol = ghost[1] + dy;
                if (isFreeCell(newGhostRow, newGhostCol)) {
                    validDirections.add(direction);
                }
            }

            if (validDirections.size() > 0) {
                int randomIndex = new Random().nextInt(validDirections.size());
                ghost[2] = validDirections.get(randomIndex)[0];
                ghost[3] = validDirections.get(randomIndex)[1];


                board[ghost[0]][ghost[1]] = board[ghost[0]][ghost[1]].replace("G1", "").replace("G0", "");
                ghost[0] += ghost[2];
                ghost[1] += ghost[3];


                board[ghost[0]][ghost[1]] += "G" + ghost[4];
            }
            if (ghost[0] == pacmanRow && ghost[1] == pacmanCol) {
                saveScore();
                frame.stop();
            }
        }
        fireTableDataChanged();
    }
    public void saveScore(){
        String playerName = JOptionPane.showInputDialog("Enter your name:");
        if (playerName != null) {
            scoreStore.writeScore(playerName, foodCount);
        }
    }
    public void gameWin(){
        for (String[] strings : board) {
            for (String string : strings) {
                if (string.contains(".")) {
                    return;
                }
            }
        }
        JOptionPane.showMessageDialog(null, "Win");
        saveScore();
        frame.stop();
    }
    private boolean isFreeCell(int row, int col) {
        return row >= 0 && row < getRowCount() && col >= 0 && col < getColumnCount() && !board[row][col].equals("#");
    }

    public void movePacman(int dx, int dy) {
        int newPacmanRow = pacmanRow + dy;
        int newPacmanCol = pacmanCol + dx;
        if (board[newPacmanRow][newPacmanCol].equals(" ")) {
            setValueAt(" ",pacmanRow, pacmanCol);
            setPacmanPos(newPacmanRow, newPacmanCol);
        }
        if (board[newPacmanRow][newPacmanCol].equals(".")) {
            setValueAt(" ",pacmanRow, pacmanCol);
            setPacmanPos(newPacmanRow, newPacmanCol);
            this.foodCount+=1;
            frame.updateScore(foodCount);
        }
    }
    private void setPacmanPos(int row, int column){
        setValueAt(" ",row, column);
        setValueAt("P",row, column);
        pacmanRow = row;
        pacmanCol = column;
    }
    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        board[rowIndex][columnIndex] = (String) value;
    }
    public void setDirection(int dx, int dy) {
        directionX = dx;
        directionY = dy;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void stop() {
        isRunning = false;
    }

    public void movePacman() {
        movePacman(directionX, directionY);
    }

    public void start() {
        board = copy(initBoard);
        this.isRunning = true;

        this.ghosts = new int[][] {{1 , 1 , 0 , 1 , 0, }, {1, 13, 0, 1, 1}};
        this.pacmanRow = 3;
        this.pacmanCol = 5;
        this.foodCount = 0;
    }

    private static String[][] copy(String[][] src) {
        if (src == null) {
            return null;
        }

        String[][] copy = new String[src.length][];

        for (int i = 0; i < src.length; i++) {
            copy[i] = new String[src[i].length];
            System.arraycopy(src[i], 0, copy[i], 0, src[i].length);
        }

        return copy;
    }
}
class CellRenderer extends DefaultTableCellRenderer {
    public TableModel tableModel;

    public CellRenderer(TableModel tableModel) {
        this.tableModel = tableModel;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        setFont(new Font("Monospaced", Font.BOLD, 24));
        setHorizontalAlignment(JLabel.CENTER);
        setText("");
        String x = (String) value;
        setIcon(null);
        setForeground(null);
        setBackground(null);
        if (Objects.equals(x, "#")) {
            setBackground(Color.BLACK);
            setForeground(Color.WHITE);
        } else if (Objects.equals(x, " ")) {
            setBackground(Color.WHITE);
            setForeground(Color.WHITE);
        } else if (Objects.equals(x, "P")) {
            setForeground(Color.BLACK);
            renderPacman();
        } else if (Objects.equals(x, ".")) {
            setBackground(Color.WHITE);
            setForeground(Color.RED);
            setText((String) value);
        } else if ((x).contains("G")) {
            renderGhost(x);
        }
        return this;
    }

    private void renderPacman() {
        if (tableModel.getDirectionX() == 1 && tableModel.getDirectionY() == 0) {
            if (isMouthOpen()) {
                setIcon(new ImageIcon("resources/pacman-right.png"));
            } else {
                setIcon(new ImageIcon("resources/pacman-closed.png"));
            }
        } else if (tableModel.getDirectionX() == -1 && tableModel.getDirectionY() == 0) {
            if (isMouthOpen()) {
                setIcon(new ImageIcon("resources/pacman-left.png"));
            } else {
                setIcon(new ImageIcon("resources/pacman-closed.png"));
            }
        } else if (tableModel.getDirectionX() == 0 && tableModel.getDirectionY() == 1) {
            if (isMouthOpen()) {
                setIcon(new ImageIcon("resources/pacman-bottom.png"));
            } else {
                setIcon(new ImageIcon("resources/pacman-closed.png"));
            }
        } else {
            if (isMouthOpen()) {
                setIcon(new ImageIcon("resources/pacman-up.png"));
            } else {
                setIcon(new ImageIcon("resources/pacman-closed.png"));
            }
        }
    }

    private boolean isMouthOpen() {

        return System.currentTimeMillis() % 1000 < 600;
    }

    private void renderGhost(String ghost) {

        if (ghost.contains("G0")) {
            setIcon((new ImageIcon("resources/red.png")));
        } else {
            setIcon((new ImageIcon("resources/yellow.png")));
        }
    }
}
