
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;


public class GamePanel extends JPanel {
    private JPanel keywordPanel;
    private JPanel exitPanel;
    private JPanel southPanel;
    private Color currentColor = Color.BLACK;
    private boolean isErasing = false;
    private ClientManager clientManager;
    private static List<DrawingLine> lines = new ArrayList<>();

    private int prevX, prevY;
    private boolean isDrawing = true;
    private MouseAdapter mouseAdapter;
    private MouseMotionAdapter mouseMotionAdapter;

    private static final Color ERASER_COLOR = Color.WHITE;

    @Override
    protected void processMouseEvent(MouseEvent e) {
        if (!isEnabled()) {
            return; // 패널이 비활성화 상태일 경우 이벤트 무시
        }
        super.processMouseEvent(e); // 활성화 상태일 경우 기본 동작 수행
    }

    @Override
    protected void processMouseMotionEvent(MouseEvent e) {
        if (!isEnabled()) {
            return; // 패널이 비활성화 상태일 경우 이벤트 무시
        }
        super.processMouseMotionEvent(e); // 활성화 상태일 경우 기본 동작 수행
    }

    public GamePanel(ClientManager clientManager) {
        this.clientManager = clientManager;
        setPreferredSize(new Dimension(500, 500));
        setupDrawingListeners();
        setBackground(Color.WHITE);
    }

    private void setupDrawingListeners() {
        //mouseAdapter = new MouseAdapter() {
        addMouseListener(new MouseAdapter() {
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
        });
        //};

        //mouseMotionAdapter = new MouseMotionAdapter() {
            addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                continueDrawing(e);
            }
            });
        //};


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
        //isDrawing = false;
        //다음 그리기 준비
        prevX = -1;
        prevY = -1; // 이전 좌표 초기화
        revalidate();
        repaint();
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

        revalidate();
        SwingUtilities.invokeLater(this::repaint);
    }


    public void enableDrawing() {
        if (!isDrawing) {
            isDrawing = true;
            addMouseListener(mouseAdapter);
            addMouseMotionListener(mouseMotionAdapter);
            System.out.println("Drawing enabled.");
        }

        revalidate();
        SwingUtilities.invokeLater(this::repaint);
    }

    public void disableDrawing() {
        if (!isDrawing) {
            isDrawing = false;
            removeMouseListener(mouseAdapter);
            removeMouseMotionListener(mouseMotionAdapter);
            System.out.println("Drawing enabled.");
        }

        revalidate();
        SwingUtilities.invokeLater(this::repaint);
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
        itemPanel.setPreferredSize(new Dimension(0, 120));
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
        } else {
            keywordPanel.add(exitPanel, BorderLayout.CENTER);
        }

        return keywordPanel;
    }

    private JPanel createExitPanel() {
//        System.out.println("나가기패널");
        exitPanel = new JPanel();
        exitPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 25)); // 가로 간격 10px, 세로 간격 0px 설정
        exitPanel.setBackground(new Color(64,48,47));

        String[] imagePaths = {
                "/images/home.png",
                "/images/retry.png",
                "/images/exit.png"
        };

        JButton home = createImageButton(imagePaths[0]);
        home.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clientManager.sendLogout(clientManager.getUser());
            }
        });

        JButton retry = createImageButton(imagePaths[1]);
        retry.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//                clientManager.sendLogout(clientManager.getUser());
            }
        });

        JButton exit = createImageButton(imagePaths[2]);
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clientManager.sendRoomExit(clientManager.getUser());
            }
        });

        exitPanel.add(home);
        exitPanel.add(retry);
        exitPanel.add(exit);

        return exitPanel;
    }

    private JButton createImageButton(String imagePath) {
        // 이미지 아이콘 로드
        ImageIcon icon = null;
        try {
            // 클래스패스를 통해 리소스를 로드
            icon = new ImageIcon(getClass().getResource(imagePath));
        } catch (Exception e) {
            System.out.println("Error loading image: " + imagePath);
            e.printStackTrace();
            return new JButton("Missing Image");
        }

        // 이미지 크기 조정
        Image image = icon.getImage().getScaledInstance(35, 30, Image.SCALE_SMOOTH);
        icon = new ImageIcon(image);

        // 버튼 생성
        JButton button = new JButton(icon);
        button.setPreferredSize(new Dimension(30, 30));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);

        return button;
    }


    private JPanel createSouthPanel() {
        southPanel = new JPanel(new GridLayout(1,2));
        southPanel.add(createItemPanel());
        southPanel.add(createExitPanel());


        return southPanel;
    }

    public void addKeyword(String word) {
        southPanel.remove(exitPanel);
        keywordPanel = createKeywordPanel(word);
        southPanel.add(keywordPanel);
    }

    public void changeGameResult(JLabel backgroundLabel){
        // 기존 선 정보 초기화
        clearLines(); // 기존 그림 데이터 초기화

        // 캔버스 크기 지정
        Dimension canvasSize = new Dimension(500, 500); // 캔버스 크기 (현재 GamePanel 크기에 맞게 설정)
        backgroundLabel.setPreferredSize(canvasSize);
        backgroundLabel.setHorizontalAlignment(JLabel.CENTER);
        backgroundLabel.setVerticalAlignment(JLabel.CENTER);
        // 기존의 모든 컴포넌트를 제거하고 새로 구성
        removeAll();
        setLayout(new BorderLayout());

        // 새로운 배경 이미지를 추가
        add(backgroundLabel, BorderLayout.CENTER);
        // 기존 SouthPanel 유지
        //add(createSouthPanel(), BorderLayout.SOUTH);

        revalidate();
        repaint();
    }

    public void changeGameResultWithOverlay(String imagePath, String resultMessage) {
        // 기존 선 정보 초기화
        clearLines(); // 기존 그림 데이터 초기화

        // 캔버스 크기 지정
        Dimension canvasSize = new Dimension(300, 300);

        // 배경 이미지 설정
        ImageIcon resizedIcon = resizeImageIcon(imagePath, canvasSize);
        JLabel backgroundLabel = new JLabel(resizedIcon);
        backgroundLabel.setBounds(0, 0, canvasSize.width, canvasSize.height);

        // 결과 메시지 설정
        JLabel messageLabel = new JLabel(resultMessage, SwingConstants.CENTER);
        messageLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 30));
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setHorizontalAlignment(JLabel.CENTER);

        // 메시지 위치: 배경 이미지 중앙보다 약간 아래
        int centerY = canvasSize.height / 2; // 중앙 Y 좌표
        int offsetY = 50; // 아래로 내릴 거리 (픽셀)
        messageLabel.setBounds(0, centerY + offsetY, canvasSize.width, 40);

        // JLayeredPane 설정
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(canvasSize);
        layeredPane.setLayout(null);

        // 배경과 텍스트를 서로 다른 레이어에 추가
        layeredPane.add(backgroundLabel, Integer.valueOf(0)); // 배경 이미지
        layeredPane.add(messageLabel, Integer.valueOf(1)); // 메시지 텍스트

        // 기존 컴포넌트를 모두 제거하고 JLayeredPane 추가
        removeAll();
        setLayout(new BorderLayout());
        add(layeredPane, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private ImageIcon resizeImageIcon(String imagePath, Dimension targetSize) {
        try {
            // 이미지를 로드
            ImageIcon originalIcon = new ImageIcon(getClass().getResource(imagePath));
            Image originalImage = originalIcon.getImage();

            // 크기를 조정
            Image resizedImage = originalImage.getScaledInstance(
                    targetSize.width,
                    targetSize.height,
                    Image.SCALE_SMOOTH // 부드럽게 조정
            );

            return new ImageIcon(resizedImage);
        } catch (Exception e) {
            System.err.println("이미지를 로드하거나 크기를 조정할 수 없습니다: " + imagePath);
            return null;
        }
    }


    public JPanel createCenterPanel(){
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // 기존 GamePanel 인스턴스 재사용
        panel.add(this, BorderLayout.CENTER); // 현재 GamePanel 객체를 추가
        panel.add(createSouthPanel(), BorderLayout.SOUTH);

        return panel;
    }

}
