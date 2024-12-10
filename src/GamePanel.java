
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;


public class GamePanel extends JPanel {
    private Color currentColor = Color.BLACK;
    private boolean isErasing = false;
    private ClientManager clientManager;
    private static List<DrawingLine> lines = new ArrayList<>();
    private List<DrawingLine> tempLines = new ArrayList<>();
    private ObjectOutputStream out;
    private static final Color ERASER_COLOR = Color.WHITE;

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

        // 지우개 모드일 경우 하얀색으로, 아니면 현재 선택된 색상으로
        //Color drawColor = isErasing ? ERASER_COLOR : currentColor;
        Color drawColor = isErasing ? Color.WHITE : currentColor;


        // 서버로 메시지 전송 (지우개 모드 정보 포함)
        clientManager.sendDrawingData(prevX, prevY, currentX, currentY, drawColor, isErasing);

        synchronized (lines) {
            lines.add(new DrawingLine(prevX, prevY, currentX, currentY, drawColor));
        }

        // 즉시 화면 갱신
        repaint();

        prevX = currentX;
        prevY = currentY;
    }

    private void stopDrawing() {
        isDrawing = false;

        revalidate();
        repaint();
    }

    // ClientManager에 추가할 메서드 제안
    public void receiveRemoteDrawing(int startX, int startY, int endX, int endY, Color color) {
        System.out.println("Drawing received: (" + startX + ", " + startY + ") -> (" + endX + ", " + endY + "), Color: " + color);
        synchronized (lines) {
            lines.add(new DrawingLine(startX, startY, endX, endY, color));
        }
        SwingUtilities.invokeLater(() -> {
            repaint();
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(3));

        // 영구 선들 그리기
        synchronized (lines) {
            for (DrawingLine line : lines) {
                g2d.setColor(line.color());
                g2d.drawLine(line.startX(), line.startY(), line.endX(), line.endY());
            }
        }

        revalidate();
        SwingUtilities.invokeLater(this::repaint);
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
        this.isErasing = false;
    }

    // 지우개 상태를 설정하는 메서드
    public void toggleEraser() {
        this.isErasing = !this.isErasing;
    }

    private record DrawingLine(int startX, int startY, int endX, int endY, Color color) {
            private DrawingLine(int startX, int startY, int endX, int endY, Color color) {
                this.startX = startX;
                this.startY = startY;
                this.endX = endX;
                this.endY = endY;
                this.color = color;
            }
        }

    private JPanel createItemPanel(){
        JPanel itemPanel = new JPanel();
        itemPanel.setLayout(new BoxLayout(itemPanel, BoxLayout.Y_AXIS));
        itemPanel.setBackground(Color.LIGHT_GRAY);

        JLabel title = new JLabel("도구 선택");
        itemPanel.add(title);

        // 색상 선택 버튼들
        Color[] colors = {Color.BLACK, Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW};
        for (Color color : colors) {
            JButton colorButton = new JButton();
            colorButton.setBackground(color);
            colorButton.setPreferredSize(new Dimension(50, 30));
            colorButton.addActionListener(e -> {
                setCurrentColor(color);
                // 색상 선택 시 지우개 모드 해제
                isErasing = false;
            });
            itemPanel.add(colorButton);
        }

        // 색상 팔레트 버튼
        JButton customColorButton = new JButton("색상 선택");
        customColorButton.addActionListener(e -> {
            Color selectedColor = JColorChooser.showDialog(this, "색상 선택", currentColor);
            if (selectedColor != null) {
                setCurrentColor(selectedColor);
            }
        });
        itemPanel.add(customColorButton);

        // 지우개 버튼
        JToggleButton eraserButton = new JToggleButton("지우개");
        eraserButton.addActionListener(e -> {
            toggleEraser();
            eraserButton.setSelected(isErasing);
        });
        itemPanel.add(eraserButton);

        JButton drawButton = new JButton("그리기");
//        drawButton.addActionListener(e -> {
//            setErasing(false);
//        });
//        itemPanel.add(drawButton);

        repaint();

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
