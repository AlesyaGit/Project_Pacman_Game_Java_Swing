import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ScoreStore {
    private List<Score> scores;
    public List<Score> getScores(){
        return scores;
    }

    public ScoreStore(){
        scores = new ArrayList<>();
        readScores();
    }

    public void readScores() {
        try
        {
            File scoreFile = new File("scores.dat");
            scoreFile.createNewFile();
            FileInputStream fileInputStream = new FileInputStream(scoreFile);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

            scores = (ArrayList) objectInputStream.readObject();
        }
        catch (Exception ignored)
        {

        }
    }
    public void writeScore(String playerName, int score) {
        try
        {
            FileOutputStream objectInputStream = new FileOutputStream("scores.dat");
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(objectInputStream);
            scores.add(new Score(playerName, score));
            scores.sort((o1, o2) ->  o2.getScore() - o1.getScore());
            objectOutputStream.writeObject(scores);
        }
        catch (Exception ignored)
        {

        }
    }
}
