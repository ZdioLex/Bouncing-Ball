// Yongcheng Shi -B01956048

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.lang.management.*;

public class BouncingBall extends JPanel implements ActionListener {
    private final ArrayList<Ball> balls = new ArrayList<>();
    private final com.sun.management.OperatingSystemMXBean osBean;
    private final Timer systemUpdateTimer;
    private final Runtime runtime;
    private final int MAX_BALLS = 200;
    private final long PROGRAM_MEMORY_THRESHOLD = 50 * 1024 * 1024; // 50MB threshold
    private long lastProgramMemoryUsage = 0;
    private boolean isInitialized = false;

    public BouncingBall() {
        osBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        runtime = Runtime.getRuntime();

        setBackground(Color.WHITE);

        // Create systemUpdateTimer
        systemUpdateTimer = new Timer(1000, e -> updateSystemMetrics());


        Timer initTimer = new Timer(500, e -> {
            // Initial ball creation and property update before setting isInitialized
            updateBallCount();
            updateBallProperties();
            isInitialized = true;

            // Start the regular timers only after initialization
            Timer animationTimer = new Timer(16, this);
            animationTimer.start();
            systemUpdateTimer.start();
            ((Timer) e.getSource()).stop();  // Stop the init timer
        });
        initTimer.setRepeats(false);
        initTimer.start();

        // Display area
        JFrame frame = new JFrame("Bouncing Balls");
        frame.setLayout(new BorderLayout());
        frame.add(this, BorderLayout.CENTER);

        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
   // main function to update balls
    private void updateSystemMetrics() {
        if (!isInitialized) return;

        long currentProgramMemory = runtime.totalMemory() - runtime.freeMemory();
        if (currentProgramMemory - lastProgramMemoryUsage < PROGRAM_MEMORY_THRESHOLD) {
            updateBallCount();
            updateBallProperties();
        }
        lastProgramMemoryUsage = currentProgramMemory;
    }

    // function to update amount of balls
    private void updateBallCount() {
        // Calculate system memory usage
        long systemTotalMemory = osBean.getTotalPhysicalMemorySize();
        long systemFreeMemory = osBean.getFreePhysicalMemorySize();
        double systemMemoryUsage = (double) (systemTotalMemory - systemFreeMemory) / systemTotalMemory;

        // Calculate desired ball count based on system memory usage
        int desiredBallCount = (int) (MAX_BALLS * systemMemoryUsage);

        // Ensure at least one ball and respect MAX_BALLS limit
        desiredBallCount = Math.max(1, Math.min(desiredBallCount, MAX_BALLS));

        // Add or remove balls to match desired count
        while (balls.size() < desiredBallCount) {
            Ball newBall = new Ball(30, 10);
            balls.add(newBall);
        }
        while (balls.size() > desiredBallCount) {
            balls.remove(balls.size() - 1);
        }
    }

    // function to update ball Properties
    private void updateBallProperties() {
        if (balls.isEmpty()) return;

        // Get system CPU load
        double cpuLoad = osBean.getCpuLoad();

        // Validate cpuLoad value
        if (cpuLoad < 0) {
            cpuLoad = 0;
        }

        // Use system memory usage ratio to determine ball size
        long systemTotalMemory = osBean.getTotalPhysicalMemorySize();
        long systemFreeMemory = osBean.getFreePhysicalMemorySize();
        double systemMemoryUsage = (double) (systemTotalMemory - systemFreeMemory) / systemTotalMemory;

        for (Ball ball : balls) {
            // Speed based on CPU usage (1-30 pixels per frame)
            int speed = (int) (cpuLoad * 29) + 1;
            ball.setSpeed(speed);

            // Size based on memory usage (5-50 pixels)
            int size = (int) (systemMemoryUsage * 45) + 5;
            ball.setSize(size);

            // Color based on CPU usage (Green to Red)
            float color = (float) ((1 - cpuLoad) * (1.0 / 3.0));
            ball.setColor(Color.getHSBColor(color, 1.0f, 1.0f));
        }
    }

    // function to paint whole display area
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (!isInitialized) return;  // Don't draw anything until initialized

        // Get updated CPU load for display
        double cpuLoad = osBean.getCpuLoad();
        if (cpuLoad < 0) cpuLoad = 0;

        // Draw system metrics
        g.setColor(Color.BLACK);
        g.drawString(String.format("CPU Load: %.2f%%", cpuLoad * 100), 10, 20);
        g.drawString(String.format("Free Memory: %.2f GB",
                osBean.getFreePhysicalMemorySize() / (1024.0 * 1024.0 * 1024.0)), 10, 40);
        g.drawString(String.format("System Memory Usage: %.2f%%",
                (1 - (double) osBean.getFreePhysicalMemorySize() / osBean.getTotalPhysicalMemorySize()) * 100), 10, 60);
        g.drawString("Ball Count: " + balls.size(), 10, 80);
        g.drawString(String.format("Program Memory: %.2f MB",
                (runtime.totalMemory() - runtime.freeMemory()) / (1024.0 * 1024.0)), 10, 100);

        // Draw balls
        for (Ball ball : balls) {
            ball.draw(g);
        }
    }

    // function to control the ball movement
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isInitialized) return;

        for (Ball ball : balls) {
            ball.move(getWidth(), getHeight());
        }
        checkCollisions();
        repaint();
    }

    // Ball class
    class Ball {
        private int x, y;
        private int size;
        private int xSpeed, ySpeed;
        private Color color = Color.BLUE;

        public Ball(int size, int speed) {
            this.size = size;
            this.xSpeed = speed;
            this.ySpeed = speed;
            // Ensure initial position is within panel bounds
            x = (int) (Math.random() * (Math.max(1, getWidth() - size)));
            y = (int) (Math.random() * (Math.max(1, getHeight() - size)));
        }

        public void setSpeed(int speed) {
            this.xSpeed = (xSpeed < 0) ? -speed : speed;
            this.ySpeed = (ySpeed < 0) ? -speed : speed;
        }

        public void setSize(int size) {
            // Ensure size is at least 5 pixels
            this.size = Math.max(5, size);
        }

        public void setColor(Color color) {
            this.color = color;
        }

        public void move(int panelWidth, int panelHeight) {
            x += xSpeed;
            y += ySpeed;

            if (x <= 0) {
                xSpeed = Math.abs(xSpeed);
                x = 0;
            } else if (x >= panelWidth - size) {
                xSpeed = -Math.abs(xSpeed);
                x = panelWidth - size;
            }

            if (y <= 0) {
                ySpeed = Math.abs(ySpeed);
                y = 0;
            } else if (y >= panelHeight - size) {
                ySpeed = -Math.abs(ySpeed);
                y = panelHeight - size;
            }
        }

        public void draw(Graphics g) {
            g.setColor(color);
            g.fillOval(x, y, size, size);
        }

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

    public void checkCollisions() {
        for (int i = 0; i < balls.size(); i++) {
            Ball ball1 = balls.get(i);
            for (int j = i + 1; j < balls.size(); j++) {
                Ball ball2 = balls.get(j);

                int dx = ball1.getCenterX() - ball2.getCenterX();
                int dy = ball1.getCenterY() - ball2.getCenterY();
                int distance = (int) Math.sqrt(dx * dx + dy * dy);
                int radiusSum = ball1.getRadius() + ball2.getRadius();

                if (distance < radiusSum) {
                    // Collision response
                    double overlap = radiusSum - distance;
                    double angle = Math.atan2(dy, dx);
                    double moveX = overlap * Math.cos(angle);
                    double moveY = overlap * Math.sin(angle);

                    // Separate balls
                    ball1.x += moveX / 2;
                    ball1.y += moveY / 2;
                    ball2.x -= moveX / 2;
                    ball2.y -= moveY / 2;

                    // Exchange velocities
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
        SwingUtilities.invokeLater(BouncingBall::new);
    }
}
