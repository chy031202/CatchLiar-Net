import java.io.Serializable;

public class DrawingData implements Serializable {
    private int startX, startY, endX, endY, color;
    private boolean isErasing;

    public DrawingData(int startX, int startY, int endX, int endY, int color, boolean isErasing) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.color = color;
        this.isErasing = isErasing;
    }

    // Getter와 Setter
    public int getStartX() {
        return startX;
    }

    public int getStartY() {
        return startY;
    }

    public int getEndX() {
        return endX;
    }

    public int getEndY() {
        return endY;
    }

    public int getColor() {
        return color;
    }

    public boolean isErasing() {
        return isErasing;
    }
}
