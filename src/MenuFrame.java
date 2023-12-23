import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ButtonInfo {
    private final String title;
    private final ActionListener action;

    public ButtonInfo(String title, ActionListener action) {
        this.title = title;
        this.action = action;
    }

    public String getTitle() {
        return title;
    }

    public ActionListener getAction() {
        return action;
    }
}

public class MenuFrame extends JFrame {
    private GameFrame gameFrame;
    private ScoreStore scoreStore;
    private ScoreFrame scoreFrame;
    public MenuFrame() {
        this.scoreStore = new ScoreStore();
        this.scoreFrame = new ScoreFrame(scoreStore);

        this.gameFrame = new GameFrame(scoreStore);
        setTitle("Pac-Man");
        setPreferredSize(new Dimension(850, 478));
        List<ButtonInfo> buttonInfos = new ArrayList<>(Arrays.asList(
                new ButtonInfo("Play", (e) -> {
                    gameFrame.start();
                }),
                new ButtonInfo("High Scores", (e) -> {
                    scoreFrame.showScores();
                }),
                new ButtonInfo("Exit", (e) -> {
                    System.exit(0);
                })
        ));
        ImageIcon icon = new ImageIcon("resources/background.png");
        Image image = icon.getImage();
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(image, 0, 0, this);
            }
        };

        panel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        for (int i = 0; i < buttonInfos.size(); i++) {

            ButtonInfo btn = buttonInfos.get(i);
            JButton button = new JButton(btn.getTitle());
            button.addActionListener(btn.getAction());

            button.setBackground(Color.YELLOW);

            button.setForeground(Color.BLACK);

            button.setFont(button.getFont().deriveFont(20f).deriveFont(Font.BOLD));


            button.setBorder(new LineBorder(Color.BLACK, 2));

            gbc.gridx = 0;
            gbc.gridy = i;
            panel.add(button, gbc);



        }
    add(panel);
    pack();
    setLocationRelativeTo(null);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
}}



