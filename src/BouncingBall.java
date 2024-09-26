// Yongcheng Shi -B01956048

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class BouncingBall extends JPanel implements ActionListener {
    // array for store balls
    private final ArrayList<Ball> balls = new ArrayList<>();

    public BouncingBall() {
        // timer for ball action
        Timer timer = new Timer(16, this);
        timer.start();

        // buttons
        JButton addButton = new JButton("Add Ball");
        addButton.addActionListener(e -> addBall());

        JButton removeButton = new JButton("Remove Ball");
        removeButton.addActionListener(e -> removeBall());

        // display area
        JFrame frame = new JFrame("Bouncing Balls");
        frame.setLayout(new BorderLayout());
        frame.add(this, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        frame.setSize(500, 400); // set window size
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    // method for add new ball
    public void addBall() {
        if (balls.size() < 10) { // maximum 10 balls
            Ball newBall = new Ball(30, 10); // ball size 30 px and speed is 10
            balls.add(newBall);
        } else {
            JOptionPane.showMessageDialog(this, "You can't add more than 10 balls!");
        }
    }

    // method for remove  ball
    public void removeBall() {
        if (balls.size() > 1) { // minimum is 1
            balls.remove(balls.size() - 1);
        } else {
            JOptionPane.showMessageDialog(this, "You must have at least 1 ball!");
        }
    }

    // draw all balls
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (Ball ball : balls) {
            ball.draw(g);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // update the position of new ball
        for (Ball ball : balls) {
            ball.move(getWidth(), getHeight());
        }
        checkCollisions();
        repaint();
    }

    //  class of ball
    class Ball {
        private int x, y;
        private int size;
        private int xSpeed, ySpeed;

        public Ball(int size, int speed) {
            this.size = size;
            this.xSpeed = speed;
            this.ySpeed = speed;
            // random initial position of a
            this.x = (int)(Math.random() * (getWidth() - size));
            this.y = (int)(Math.random() * (getHeight() - size));
        }

        public void move(int panelWidth, int panelHeight) {
            x += xSpeed;
            y += ySpeed;

            // Prevent infinite loop
            if (x <= 0) {
                xSpeed = -xSpeed;
                x = 0;
            } else if (x >= panelWidth - size) {
                xSpeed = -xSpeed;
                x = panelWidth - size;
            }

            if (y <= 0) {
                ySpeed = -ySpeed;
                y = 0;
            } else if (y >= panelHeight - size) {
                ySpeed = -ySpeed;
                y = panelHeight - size;
            }
        }
        // set ball
        public void draw(Graphics g) {
            g.setColor(Color.BLUE);
            g.fillOval(x, y, size, size);
        }

        // return center
        public int getCenterX() {
            return x + size / 2;
        }

        public int getCenterY() {
            return y + size / 2;
        }

        public int getRadius() {
            return size / 2;
        }
    }

    // Detect whether the ball hits the boundary.
    // If yes, switch the speed
    public void checkCollisions() {
        for (int i = 0; i < balls.size(); i++) {
            Ball ball1 = balls.get(i);
            for (int j = i + 1; j < balls.size(); j++) {
                Ball ball2 = balls.get(j);

                // Prevent infinite loop
                int dx = ball1.getCenterX() - ball2.getCenterX();
                int dy = ball1.getCenterY() - ball2.getCenterY();
                int distance = (int) Math.sqrt(dx * dx + dy * dy);
                int radiusSum = ball1.getRadius() + ball2.getRadius();

                if (distance < radiusSum) {
                    int overlap = radiusSum - distance;
                    int moveX = (int)(dx * (overlap / (double) distance));
                    int moveY = (int)(dy * (overlap / (double) distance));

                    ball1.x += moveX / 2;
                    ball1.y += moveY / 2;
                    ball2.x -= moveX / 2;
                    ball2.y -= moveY / 2;

                    // switch speed
                    int tempXSpeed = ball1.xSpeed;
                    int tempYSpeed = ball1.ySpeed;
                    ball1.xSpeed = ball2.xSpeed;
                    ball1.ySpeed = ball2.ySpeed;
                    ball2.xSpeed = tempXSpeed;
                    ball2.ySpeed = tempYSpeed;
                }
            }
        }
    }

    public static void main(String[] args) {
        new BouncingBall(); // start game
    }
}