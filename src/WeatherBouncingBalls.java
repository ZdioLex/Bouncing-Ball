import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;

public class WeatherBouncingBalls extends JPanel implements ActionListener, MouseListener {
    private final List<Ball> balls = new ArrayList<>();
    private final Timer animationTimer;
    private final Timer dataUpdateTimer;
    private final int PANEL_WIDTH = 1000;
    private final int PANEL_HEIGHT = 1000;
    private boolean loading = true;

    public WeatherBouncingBalls() {
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setBackground(Color.WHITE);
        addMouseListener(this);

        // Initialize balls with weather data
        initializeBalls();

        // Set up the frame
        JFrame frame = new JFrame("Weather Bouncing Balls");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(this);
        frame.pack();
        frame.setVisible(true);

        // Animation timer (60 FPS)
        animationTimer = new Timer(16, this);

        // Data update timer (every 60 seconds)
        dataUpdateTimer = new Timer(60000, e -> updateWeatherData());
    }

    // Initialize balls method
    private void initializeBalls() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                Map<String, String> cityMap = getCityIdMap();
                List<String> cityIDs = new ArrayList<>(cityMap.values());

                int batchSize = 20;
                List<JSONObject> allCityWeatherData = new ArrayList<>();

                for (int i = 0; i < cityIDs.size(); i += batchSize) {
                    int end = Math.min(i + batchSize, cityIDs.size());
                    List<String> batchIDs = cityIDs.subList(i, end);

                    try {
                        JSONArray weatherArray = WeatherFetcher.getWeatherDataByCityIDs(batchIDs);

                        for (int j = 0; j < weatherArray.length(); j++) {
                            JSONObject cityData = weatherArray.getJSONObject(j);
                            allCityWeatherData.add(cityData);
                        }

                        // Sleep to respect API rate limits (60 calls per minute)
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        System.err.println("Error fetching data for batch: " + e.getMessage());
                    }
                }

                // Create balls after all data has been fetched
                for (JSONObject cityData : allCityWeatherData) {
                    String cityName = cityData.getString("name");
                    double temperature = cityData.getJSONObject("main").getDouble("temp");
                    double humidity = cityData.getJSONObject("main").getDouble("humidity");

                    Ball ball = new Ball(cityName, temperature, humidity);
                    balls.add(ball);
                }
                return null;
            }

            @Override
            protected void done() {
                loading = false;
                animationTimer.start();
                dataUpdateTimer.start();
                repaint();
            }
        };
        worker.execute();
    }

    // Generate city map
    private Map<String, String> getCityIdMap() {
        Map<String, String> cityMap = new LinkedHashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader("resources/cities.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.trim().split(",");
                if (parts.length == 2) {
                    String cityName = parts[0].trim();
                    String cityID = parts[1].trim();
                    cityMap.put(cityName, cityID);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cityMap;
    }

    private void updateWeatherData() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                Map<String, String> cityMap = getCityIdMap();
                List<String> cityIDs = new ArrayList<>(cityMap.values());

                int batchSize = 20;
                Map<String, JSONObject> cityWeatherDataMap = new HashMap<>();

                for (int i = 0; i < cityIDs.size(); i += batchSize) {
                    int end = Math.min(i + batchSize, cityIDs.size());
                    List<String> batchIDs = cityIDs.subList(i, end);

                    try {
                        JSONArray weatherArray = WeatherFetcher.getWeatherDataByCityIDs(batchIDs);

                        for (int j = 0; j < weatherArray.length(); j++) {
                            JSONObject cityData = weatherArray.getJSONObject(j);
                            String cityName = cityData.getString("name");
                            cityWeatherDataMap.put(cityName, cityData);
                        }
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        System.err.println("Error updating data for batch: " + e.getMessage());
                    }
                }

                // Update balls with new data
                for (Ball ball : balls) {
                    JSONObject cityData = cityWeatherDataMap.get(ball.getCityName());
                    if (cityData != null) {
                        double temperature = cityData.getJSONObject("main").getDouble("temp");
                        double humidity = cityData.getJSONObject("main").getDouble("humidity");

                        ball.setTemperature(temperature);
                        ball.setHumidity(humidity);
                        ball.updateAppearance();
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                repaint();
            }
        };
        worker.execute();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for (Ball ball : balls) {
            ball.move(getWidth(), getHeight());
        }
        checkCollisions();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (loading) {
            // Show loading message
            g.setColor(Color.BLACK);
            g.setFont(new Font("SansSerif", Font.BOLD, 24));
            FontMetrics fm = g.getFontMetrics();
            String loadingText = "Loading...";
            int textWidth = fm.stringWidth(loadingText);
            int textX = (PANEL_WIDTH - textWidth) / 2;
            int textY = PANEL_HEIGHT / 2;
            g.drawString(loadingText, textX, textY);
            return;
        }

        // Display current time
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        g.setColor(Color.BLACK);
        g.drawString("Current Time: " + currentTime, 10, 20);

        // Draw balls
        for (Ball ball : balls) {
            ball.draw(g);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point clickPoint = e.getPoint();
        boolean ballFound = false;

        for (Ball ball : balls) {
            Ellipse2D.Double circle = new Ellipse2D.Double(ball.getX(), ball.getY(), ball.getSize(), ball.getSize());
            if (circle.contains(clickPoint)) {
                for (Ball b : balls) {
                    b.setSelected(false);
                }
                ball.setSelected(true);
                repaint();
                showCityInfo(ball);
                ballFound = true;
                break;
            }
        }
        if (!ballFound) {
            for (Ball b : balls) {
                b.setSelected(false);
            }
            repaint();
        }
    }

    private void showCityInfo(Ball ball) {
        JOptionPane.showMessageDialog(this,
                "City: " + ball.getCityName() +
                        "\nTemperature: " + ball.getTemperature() + "°C" +
                        "\nHumidity: " + ball.getHumidity() + "%",
                "City Weather Info",
                JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void mouseClicked(MouseEvent e) { }
    @Override
    public void mouseReleased(MouseEvent e) { }
    @Override
    public void mouseEntered(MouseEvent e) { }
    @Override
    public void mouseExited(MouseEvent e) { }

    // check collisions of two balls
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
                    double overlap = radiusSum - distance;
                    double angle = Math.atan2(dy, dx);
                    double moveX = overlap * Math.cos(angle);
                    double moveY = overlap * Math.sin(angle);

                    ball1.setX(ball1.getX() + (int)(moveX / 2));
                    ball1.setY(ball1.getY() + (int)(moveY / 2));
                    ball2.setX(ball2.getX() - (int)(moveX / 2));
                    ball2.setY(ball2.getY() - (int)(moveY / 2));

                    int tempXSpeed = ball1.getXSpeed();
                    int tempYSpeed = ball1.getYSpeed();
                    ball1.setXSpeed(ball2.getXSpeed());
                    ball1.setYSpeed(ball2.getYSpeed());
                    ball2.setXSpeed(tempXSpeed);
                    ball2.setYSpeed(tempYSpeed);
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(WeatherBouncingBalls::new);
    }
}
