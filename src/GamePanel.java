
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class GamePanel extends JPanel {
    //public String createCenterPanel;
    private Color currentColor = Color.BLACK;
    private boolean isErasing = false;
    private ClientManager clientManager;
    private static List<DrawingLine> lines = new ArrayList<>();
    private List<DrawingLine> tempLines = new ArrayList<>();
    private ObjectOutputStream out;

    private int prevX, prevY;

    private boolean isDrawing = false;

    public GamePanel(ClientManager clientManager) {
        this.clientManager = clientManager;
        setPreferredSize(new Dimension(500, 500));
        setupDrawingListeners();


    }

    private void setupDrawingListeners() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(500, 500));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                startDrawing(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                stopDrawing();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                continueDrawing(e);
            }
        });
    }

    private void startDrawing(MouseEvent e) {
        prevX = e.getX();
        prevY = e.getY();
        isDrawing = true;
        requestFocusInWindow();
    }

    private void continueDrawing(MouseEvent e) {
        if (!isDrawing) return;

        int currentX = e.getX();
        int currentY = e.getY();
        // Paint 생성
        Paint paintDTO = new Paint(prevX, prevY, currentX, currentY, currentColor, isErasing);

        // 로컬 그리기
//        DrawingLine tempLine = new DrawingLine(prevX, prevY, currentX, currentY, currentColor);
//        synchronized (tempLines) {
//            tempLines.add(tempLine);
//        }

        // 서버로 메시지 전송
        clientManager.sendDrawingData(prevX, prevY, currentX, currentY, currentColor, isErasing);


        // 즉시 화면 갱신
        repaint();

        prevX = currentX;
        prevY = currentY;
    }

    private void stopDrawing() {
        isDrawing = false;

        // 임시 선들을 영구 선 목록에 추가
        synchronized (lines) {
            lines.addAll(tempLines);
            tempLines.clear();
        }

        revalidate();
        repaint();
    }

    // ClientManager에 추가할 메서드 제안
    public void receiveRemoteDrawing(int startX, int startY, int endX, int endY, Color color) {
        DrawingLine remoteLine = new DrawingLine(startX, startY, endX, endY, color);
        synchronized (lines) {
            lines.add(remoteLine);
        }
        repaint(); // UI 갱신
    }



    public void drawLine(int startX, int startY, int endX, int endY) {
        synchronized (lines) {
            // 현재 색상(검정)으로 선 추가
            lines.add(new DrawingLine(startX, startY, endX, endY, currentColor));
        }
        System.out.println("Drawing line: (" + startX + ", " + startY + ") to (" + endX + ", " + endY + ") with color " + currentColor);
        revalidate();
        SwingUtilities.invokeLater(this::repaint);
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
        lines.add(new DrawingLine(startX, startY, endX, endY, currentColor));
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
            this.color = Color.BLACK;
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

    private JPanel createItemPanel(){
        JPanel itemPanel = new JPanel();
        itemPanel.setLayout(new BoxLayout(itemPanel, BoxLayout.Y_AXIS));
        itemPanel.setBackground(Color.LIGHT_GRAY);

        JLabel title = new JLabel("도구 선택");
        itemPanel.add(title);

        JButton colorButton = new JButton("색상 선택");
//        colorButton.addActionListener(e -> {
//            Color selectedColor = JColorChooser.showDialog(null, "색상 선택", Color.BLACK);
//            if (selectedColor != null) {
//                // gamePanel 내부 메서드 호출
//                gamePanel.setCurrentColor(selectedColor);
//            }
//        });
//        itemPanel.add(colorButton);
//
//        JButton eraseButton = new JButton("지우개");
//        eraseButton.addActionListener(e -> {
//            gamePanel.setErasing(true);
//        });
//        itemPanel.add(eraseButton);
//
//        JButton drawButton = new JButton("그리기");
//        drawButton.addActionListener(e -> {
//            gamePanel.setErasing(false);
//        });
//        itemPanel.add(drawButton);

        return itemPanel;
    }

    public JPanel createCenterPanel(){
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JPanel gamepanel = new GamePanel(clientManager);
        JPanel Itempanel = createItemPanel();

        panel.add(gamepanel, BorderLayout.CENTER);
        panel.add(Itempanel, BorderLayout.SOUTH);

        return panel;
    }

}
