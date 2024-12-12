
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;


public class GamePanel extends JPanel {
    private JPanel keywordPanel;
    private JPanel southPanel;
    private Color currentColor = Color.BLACK;
    private boolean isErasing = false;
    private ClientManager clientManager;
    private static List<DrawingLine> lines = new ArrayList<>();

    private int prevX, prevY;
    private boolean isDrawing = false;
    private MouseAdapter mouseAdapter;
    private MouseMotionAdapter mouseMotionAdapter;

    private static final Color ERASER_COLOR = Color.WHITE;

    public GamePanel(ClientManager clientManager) {
        this.clientManager = clientManager;
        setPreferredSize(new Dimension(500, 500));
        setupDrawingListeners();
        setBackground(Color.WHITE);
    }

    private void setupDrawingListeners() {
        mouseAdapter = new MouseAdapter() {
        //addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                //startDrawing(e.getX(), e.getY(), isErasing ? ERASER_COLOR : currentColor, isErasing);
                if (isDrawing) {
                    startDrawing(e.getX(), e.getY(), isErasing ? ERASER_COLOR : currentColor, isErasing);
                }

            }
            @Override
            public void mouseReleased(MouseEvent e) {
                stopDrawing();
            }
        //});
        };

        mouseMotionAdapter = new MouseMotionAdapter() {
            //addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                continueDrawing(e);
            }
            //});
        };


    }

    private void startDrawing(int x, int y, Color color, boolean erasing) {
        prevX = x;
        prevY = y;
        this.currentColor = color;  // 현재 그리기 색상 업데이트
        this.isErasing = erasing;  // 지우개 상태 업데이트
        isDrawing = true;
        requestFocusInWindow();
        System.out.println(String.format(
                "그리기 시작: (%d, %d), 색상: %s, 지우개 모드: %b",
                x, y, color.toString(), erasing
        ));
    }

    private void continueDrawing(MouseEvent e) {
        if (!isDrawing) return;

        int currentX = e.getX();
        int currentY = e.getY();

        // 지우개 모드일 경우 하얀색으로, 아니면 현재 선택된 색상으로
        Color drawColor = isErasing ?ERASER_COLOR: currentColor;
        //Color drawColor = currentColor;


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
        //다음 그리기 준비
        prevX = -1;
        prevY = -1; // 이전 좌표 초기화
//        revalidate();
//        repaint();
    }

    // ClientManager에 추가할 메서드 제안
    public void receiveRemoteDrawing(int startX, int startY, int endX, int endY, Color color) {
        //System.out.println("Drawing received: (" + startX + ", " + startY + ") -> (" + endX + ", " + endY + "), Color: " + color);
        synchronized (lines) {
            lines.add(new DrawingLine(startX, startY, endX, endY, color));
        }
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
//                g2d.drawLine(line.getStartX(), line.getStartY(), line.getEndX(), line.getEndY());
                if (line.getColor().equals(ERASER_COLOR)) {
                    g2d.setStroke(new BasicStroke(6)); // 지우개 크기 적용
                } else {
                    g2d.setStroke(new BasicStroke(3)); // 기본 크기
                }
                g2d.drawLine(line.getStartX(), line.getStartY(), line.getEndX(), line.getEndY());
            }
        }
        revalidate();
        SwingUtilities.invokeLater(this::repaint);
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
        System.out.println("색상 변경: " + color);
    }

    // 지우개 상태를 설정하는 메서드
    public void toggleEraser() {
        this.isErasing = !this.isErasing;
        System.out.println("지우개 모드 전환: " + (isErasing ? "활성화" : "비활성화"));

        // 지우개 모드 활성화 시 색상은 변경하지 않고 동작만 설정
        if (isErasing) {
            System.out.println("지우개 사용 - 색상: 흰색");
        } else {
            System.out.println("그리기 모드 사용 - 현재 색상: " + currentColor.toString());
        }
    }

    public void setDrawingEnabled(boolean enabled) {
        this.isDrawing = enabled;
        if (enabled) {
            enableDrawing(); // 리스너 등록
        } else {
            disableDrawing(); // 리스너 해제
        }
    }


    public void enableDrawing() {
        if (!isDrawing) {
            isDrawing = true;
            addMouseListener(mouseAdapter);
            addMouseMotionListener(mouseMotionAdapter);
            System.out.println("Drawing enabled.");
        }
    }

    public void disableDrawing() {
        if (!isDrawing) {
            isDrawing = false;
            removeMouseListener(mouseAdapter);
            removeMouseMotionListener(mouseMotionAdapter);
            System.out.println("Drawing enabled.");
        }
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


    private JPanel createItemPanel(){
        JPanel itemPanel = new JPanel();
        itemPanel.setPreferredSize(new Dimension(0, 105));
        itemPanel.setLayout(new GridLayout(2,3,10,10));
        itemPanel.setBorder(new EmptyBorder(15, 5, 15, 15));
//        itemPanel.setBackground(Color.LIGHT_GRAY);
        itemPanel.setBackground(new Color(64,48,47));

//        JLabel title = new JLabel("도구 선택");
//        itemPanel.add(title);

        // 색상 선택 버튼들
        Color[] colors = {Color.BLACK, Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW};
        for (Color color : colors) {
            JButton colorButton = new JButton();
            colorButton.setBackground(color);
            colorButton.setPreferredSize(new Dimension(20, 20));
            colorButton.addActionListener(e -> {
                setCurrentColor(color);
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
//        itemPanel.add(customColorButton);

        // 지우개 버튼
        JToggleButton eraserButton = new JToggleButton("지우개");
        eraserButton.addActionListener(e -> {
            toggleEraser();
            eraserButton.setSelected(isErasing);
        });
        itemPanel.add(eraserButton);

        revalidate();
        SwingUtilities.invokeLater(this::repaint);

        return itemPanel;
    }

    private JPanel createKeywordPanel(String word) {
        keywordPanel = new JPanel(new BorderLayout());
        keywordPanel.setBackground(new Color(64,48,47));
        keywordPanel.setBorder(new EmptyBorder(20, 15, 20, 10));


        if(word != null) {
            JPanel keyword = new JPanel(new BorderLayout());
            keyword.setBackground(new Color(201,208,191));

            JLabel label = new JLabel(word, SwingConstants.CENTER); // 텍스트 가운데 정렬
            label.setForeground(Color.WHITE);
            label.setFont(new Font("Malgun Gothic", Font.BOLD, 30));
            keyword.add(label, BorderLayout.CENTER);

            keywordPanel.add(keyword, BorderLayout.CENTER);
        }

        return keywordPanel;
    }

    private JPanel createSouthPanel() {
        southPanel = new JPanel(new GridLayout(1,2));
        southPanel.add(createItemPanel());
        southPanel.add(createKeywordPanel(null));


        return southPanel;
    }

    public void addKeyword(String word) {
        southPanel.remove(keywordPanel);
        keywordPanel = createKeywordPanel(word);
        southPanel.add(keywordPanel);
    }

    public JPanel createCenterPanel(){
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

//        JPanel gamepanel = new GamePanel(clientManager);
//        JPanel Itempanel = createItemPanel();

        // 기존 GamePanel 인스턴스 재사용
        panel.add(this, BorderLayout.CENTER); // 현재 GamePanel 객체를 추가
        panel.add(createSouthPanel(), BorderLayout.SOUTH);


        return panel;
    }

}
