import javax.sound.sampled.Line;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;


public class GamePanel extends JPanel {
    private int startX, startY, endX, endY;
    private Color currentColor = Color.BLACK;
    private boolean isErasing = false;
    private ClientManager clientManager;
    private List<Line> lines = new ArrayList<>();

    public GamePanel(ClientManager clientManager) {
        this.clientManager = clientManager;
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(550, 490));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                startX = e.getX();
                startY = e.getY();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                endX = e.getX();
                endY = e.getY();

                Color drawColor = isErasing ? Color.WHITE : currentColor;

                // 선 추가 (로컬 및 서버)
                addLine(startX, startY, endX, endY, drawColor);
                clientManager.sendDrawingData(startX, startY, endX, endY, drawColor); // 서버 전송

                // 갱신
                repaint();

                startX = endX;
                startY = endY;
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(3));

        for (Line line : lines) {
            g2d.setColor(line.getColor());
            g2d.drawLine(line.getStartX(), line.getStartY(), line.getEndX(), line.getEndY());
        }
    }

    public void drawLine(int startX, int startY, int endX, int endY, Color color) {
        addLine(startX, startY, endX, endY, color);
        repaint();
    }

    private void addLine(int startX, int startY, int endX, int endY, Color color) {
        lines.add(new Line(startX, startY, endX, endY, color));
    }

    // 현재 색상을 설정하는 메서드
    public void setCurrentColor(Color color) {
        this.currentColor = color;
    }

    // 지우개 상태를 설정하는 메서드
    public void setErasing(boolean erasing) {
        this.isErasing = erasing;
    }

    private static class Line {
        private final int startX, startY, endX, endY;
        private final Color color;

        public Line(int startX, int startY, int endX, int endY, Color color) {
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
            this.color = color;
        }

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

        public Color getColor() {
            return color;
        }
    }
}
