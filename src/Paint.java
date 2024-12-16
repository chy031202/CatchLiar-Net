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

    public int getStartX() { return startX; }
    public int getStartY() { return startY; }

    public int getEndX() { return endX; }
    public int getEndY() { return endY; }

    public Color getColor() { return color; }

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
