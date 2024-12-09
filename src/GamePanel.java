
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
    private static List<DrawingLine> lines = new ArrayList<>();
    private List<DrawingLine> tempLines = new ArrayList<>();

    private int prevX, prevY;
    private boolean isDrawing = false;

    public GamePanel(ClientManager clientManager) {
        this.clientManager = clientManager;
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(550, 490));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                prevX = e.getX();
                prevY = e.getY();
                isDrawing = true;
                requestFocusInWindow();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isDrawing = false;
                // 마우스 릴리즈 시 tempLines를 영구 lines에 추가
                synchronized (lines) {
                    lines.addAll(tempLines);
                    tempLines.clear();
                }
                revalidate();
                repaint();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!isDrawing) return;

                int currentX = e.getX();
                int currentY = e.getY();

                Color drawColor = currentColor;

                // 임시 선 추가
                DrawingLine tempLine = new DrawingLine(prevX, prevY, currentX, currentY, currentColor);
                tempLines.add(tempLine);

                // 실시간으로 선 그리기와 데이터 전송
                SwingUtilities.invokeLater(() -> {
                    drawLine(prevX, prevY, currentX, currentY, currentColor);
                    clientManager.sendDrawingData(prevX, prevY, currentX, currentY, currentColor);
                });
//                // 선 추가 (로컬 및 서버)
//                addLine(startX, startY, endX, endY, drawColor);
//                clientManager.sendDrawingData(startX, startY, endX, endY, drawColor); // 서버 전송

                // 갱신
                revalidate();
                repaint();

//                startX = endX;
//                startY = endY;
                prevX = currentX;
                prevY = currentY;
            }
        });
    }

    public void drawLine(int startX, int startY, int endX, int endY, Color color) {
//        addLine(startX, startY, endX, endY, color);
//        repaint();
        // 모든 클라이언트에서 동일한 리스트에 선 추가
        synchronized (lines) {
            lines.add(new DrawingLine(startX, startY, endX, endY, color));
        }
        System.out.println("Drawing line: (" + startX + ", " + startY + ") to (" + endX + ", " + endY + ") with color " + color);
        // UI 스레드에서 repaint 호출
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(3));

        // 영구 선들 그리기
        synchronized (lines) {
            for (DrawingLine line : lines) {
                g2d.setColor(line.getColor());
                g2d.drawLine(line.getStartX(), line.getStartY(), line.getEndX(), line.getEndY());
            }
        }

        // 임시 선들 그리기
        for (DrawingLine line : tempLines) {
            g2d.setColor(line.getColor());
            g2d.drawLine(line.getStartX(), line.getStartY(), line.getEndX(), line.getEndY());
        }
    }

    private void addLine(int startX, int startY, int endX, int endY, Color color) {
        lines.add(new DrawingLine(startX, startY, endX, endY, color));
    }

    // 방 입장 시 기존 선들 초기화하는 메서드 추가
    public void clearLines() {
        synchronized (lines) {
            lines.clear();
        }
        repaint();
    }
    // 현재 색상을 설정하는 메서드
    public void setCurrentColor(Color color) {
        this.currentColor = color;
    }

    // 지우개 상태를 설정하는 메서드
    public void setErasing(boolean erasing) {
        this.isErasing = erasing;
    }

    private static class DrawingLine {
        private final int startX, startY, endX, endY;
        private final Color color;

        public DrawingLine(int startX, int startY, int endX, int endY, Color color) {
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
