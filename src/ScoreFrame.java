import javax.swing.*;
import java.awt.*;

public class ScoreFrame extends JFrame {
    private final ScoreStore scoreStore;
    private final JList<String> scoreList;
    public ScoreFrame(ScoreStore scoreStore){
        this.scoreStore = scoreStore;
        setPreferredSize(new Dimension(300, 400));
        setTitle("High Score");
        String[] scoresArray = scoreStore.getScores().stream().map(Score::toString).toArray(String[]::new);
        this.scoreList = new JList<>(scoresArray);
        JScrollPane scrollPane = new JScrollPane(scoreList);
        add(scrollPane, BorderLayout.CENTER);
        pack();
    }
    public void showScores(){
        setVisible(true);
        scoreList.setListData(scoreStore.getScores().stream().map(Score::toString).toArray(String[]::new));
    }
}
