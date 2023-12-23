import javax.swing.*;
import java.awt.*;

class RotatedIcon extends JLabel {
    private ImageIcon icon;
    private double angle;

    public RotatedIcon(ImageIcon icon) {
        this.icon = icon;
    }

    public void setAngle(double angle) {
        this.angle = angle;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();


        g2d.rotate(Math.toRadians(angle), getWidth() / 2, getHeight() / 2);


        icon.paintIcon(this, g2d, 0, 0);

        g2d.dispose();
    }
}
