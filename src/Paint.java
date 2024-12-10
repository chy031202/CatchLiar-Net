import java.awt.*;
import java.io.Serializable;

public class Paint implements Serializable {
    private static final long serialVersionUID = 1L;

    private int startX;
    private int startY;
    private int endX;
    private int endY;
    private Color color;
    private boolean isErasing;

    public Paint(int startX, int startY, int endX, int endY, Color color, boolean isErasing) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.color = color;
        this.isErasing = isErasing;
    }

    // Getter 및 Setter
    // Getters and Setters
    public int getStartX() {
        return startX;
    }

    public void setStartX(int startX) {
        this.startX = startX;
    }

    public int getStartY() {
        return startY;
    }

    public void setStartY(int startY) {
        this.startY = startY;
    }

    public int getEndX() {
        return endX;
    }

    public void setEndX(int endX) {
        this.endX = endX;
    }

    public int getEndY() {
        return endY;
    }

    public void setEndY(int endY) {
        this.endY = endY;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public boolean isErasing() {
        return isErasing;
    }

    public void setErasing(boolean erasing) {
        isErasing = erasing;
    }

    @Override
    public String toString() {
        return "PaintDTO{" +
                "startX=" + startX +
                ", startY=" + startY +
                ", endX=" + endX +
                ", endY=" + endY +
                ", color=" + color +
                '}';
    }
}
