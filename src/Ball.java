// Yongcheng Shi -B01956048

import java.awt.*;

public class Ball {
    private int x, y;
    private int size;
    private int xSpeed, ySpeed;
    private Color color;
    private String cityName;
    private String cityAbbreviation;
    private double temperature;
    private double humidity;
    private boolean isSelected = false;
    private final int MAX_SPEED = 3;

    public Ball(String cityName, double temperature, double humidity) {
        this.cityName = cityName;
        this.temperature = temperature;
        this.humidity = humidity;
        this.cityAbbreviation = getAbbreviation(cityName);
        this.color = temperatureToColor(temperature);
        this.size = humidityToSize(humidity);

        // random start position
        x = (int) (Math.random() * (800 - size));
        y = (int) (Math.random() * (600 - size));

        // random start speed
        xSpeed = (int) (Math.random() * (2 * MAX_SPEED + 1)) - MAX_SPEED;
        ySpeed = (int) (Math.random() * (2 * MAX_SPEED + 1)) - MAX_SPEED;
        if (xSpeed == 0) xSpeed = 1;
        if (ySpeed == 0) ySpeed = 1;
    }

    // Generate city abbreviation
    private String getAbbreviation(String cityName) {
        if (cityName.contains(" ")) {
            String[] words = cityName.split(" ");
            StringBuilder abbreviation = new StringBuilder();
            for (String word : words) {
                if (!word.isEmpty()) {
                    abbreviation.append(word.charAt(0));
                }
            }
            return abbreviation.toString().toUpperCase();
        } else {
            return cityName.substring(0, Math.min(3, cityName.length())).toUpperCase();
        }
    }

    // Background color contrast
    private boolean isColorDark(Color color) {
        double brightness = (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255;
        return brightness < 0.5;
    }

    // Map temperature to color
    private Color temperatureToColor(double temp) {
        if (temp < 0) {
            return new Color(0, 0, 255);
        } else if (temp < 10) {
            return new Color(0, 255, 255);
        } else if (temp < 20) {
            return new Color(0, 255, 0);
        } else if (temp < 25) {
            return new Color(255, 255, 0);
        } else if (temp < 30) {
            return new Color(255, 165, 0);
        } else if (temp < 35) {
            return new Color(255, 80, 0);
        } else if (temp < 40) {
            return new Color(255, 0, 0);
        } else {
            return new Color(128, 0, 0);
        }
    }

    // Map humidity to size
    private int humidityToSize(double humidity) {
        return (int) (30 + (humidity / 100) * 30);
    }

    public void updateAppearance() {
        this.color = temperatureToColor(temperature);
        this.size = humidityToSize(humidity);
    }

    public void move(int panelWidth, int panelHeight) {
        x += xSpeed;
        y += ySpeed;

        // Collision detection
        if (x < 0) {
            x = 0;
            xSpeed = -xSpeed;
        } else if (x > panelWidth - size) {
            x = panelWidth - size;
            xSpeed = -xSpeed;
        }

        if (y < 0) {
            y = 0;
            ySpeed = -ySpeed;
        } else if (y > panelHeight - size) {
            y = panelHeight - size;
            ySpeed = -ySpeed;
        }
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // draw balls
        g2d.setColor(color);
        g2d.fillOval(x, y, size, size);

        // Highlight the selected ball
        if (isSelected) {
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(3));
            g2d.drawOval(x, y, size, size);
            g2d.setStroke(new BasicStroke(1));
        }

        // draw city abbreviation
        Color textColor = isColorDark(color) ? Color.WHITE : Color.BLACK;
        g2d.setColor(textColor);

        int fontSize = Math.max(size / 5, 10);
        g2d.setFont(new Font("SansSerif", Font.BOLD, fontSize));

        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(cityAbbreviation);
        int textHeight = fm.getAscent();

        int textX = x + (size - textWidth) / 2;
        int textY = y + (size + textHeight) / 2 - fm.getDescent();

        g2d.drawString(cityAbbreviation, textX, textY);
    }

    // Getters and setters
    public String getCityName() { return cityName; }
    public double getTemperature() { return temperature; }
    public double getHumidity() { return humidity; }
    public int getSize() { return size; }
    public Color getColor() { return color; }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getXSpeed() { return xSpeed; }
    public int getYSpeed() { return ySpeed; }

    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setXSpeed(int xSpeed) { this.xSpeed = xSpeed; }
    public void setYSpeed(int ySpeed) { this.ySpeed = ySpeed; }

    public int getCenterX() {
        return x + size / 2;
    }

    public int getCenterY() {
        return y + size / 2;
    }

    public int getRadius() {
        return size / 2;
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
    }

    public boolean isSelected() {
        return isSelected;
    }
}
