import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;


public class GameRoomPanel extends JPanel {
    //private JPanel gamePanel;



    public GameRoomPanel(JFrame parentFrame,  String roomName) {
        setLayout(new BorderLayout());

        JLabel title = new JLabel(roomName + "방", SwingConstants.CENTER);
        add(title, BorderLayout.NORTH);

        add(UserSidePanel(), BorderLayout.WEST);

        add(CenterPanel(), BorderLayout.CENTER);
        // 오른쪽 채팅 및 이모티콘 패널
        add(RightPanel(), BorderLayout.EAST);

        //MessageDialog.showRandomMessage(parentFrame);


    }
    private JPanel CenterPanel(){
        JPanel cneterpanel = new JPanel();
        cneterpanel.setLayout(new BorderLayout());

        JPanel gamepanel = GamePanel();
        JPanel Itempanel = ItemPanel();

        cneterpanel.add(gamepanel, BorderLayout.CENTER);
        cneterpanel.add(Itempanel, BorderLayout.SOUTH);

        return cneterpanel;
    }

    private JPanel RightPanel() {
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout()); // 위아래로 나눔

        // 위쪽 채팅 패널
        JPanel chatPanel = ChatPanel();
        rightPanel.add(chatPanel, BorderLayout.NORTH);

        // 아래쪽 이모티콘 패널
        JPanel imgPanel = ImgPanel();
        rightPanel.add(imgPanel, BorderLayout.SOUTH);

        return rightPanel;
    }

    private JPanel UserSidePanel(){
        JPanel userSidePanel = new JPanel();
        userSidePanel.setLayout(new BoxLayout(userSidePanel, BoxLayout.Y_AXIS));
        userSidePanel.setBackground(Color.CYAN);

        JLabel user = new JLabel("유저 패널");

        userSidePanel.add(user);
        return userSidePanel;
    }

    private JPanel ChatPanel(){
        JPanel chatPanel = new JPanel();
        JLabel user = new JLabel("채팅 패널");
        chatPanel.setBackground(Color.ORANGE);

        chatPanel.add(user);
        return chatPanel;
    }

    private JPanel ImgPanel(){
        JPanel imgPanel = new JPanel();
        JLabel user = new JLabel("이모티콘 패널");
        imgPanel.setBackground(Color.pink);

        imgPanel.add(user);
        return imgPanel;
    }

    //캔버스
    class GamePanel extends JPanel{
        private int startX, startY, endX, endY;
        private Color currentColor = Color.BLACK;
        private boolean isErasing = false;



        public GamePanel() {
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

                    Graphics g = getGraphics();
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setStroke(new BasicStroke(3));

                    if (isErasing) {
                        g2d.setColor(Color.WHITE);
                        g2d.setStroke(new BasicStroke(10)); // 지우개 크기
                    } else {
                        g2d.setColor(currentColor);
                    }

                    g2d.drawLine(startX, startY, endX, endY);

                    startX = endX;
                    startY = endY;
                }
            });
        }

        // 현재 색상을 설정하는 메서드
        public void setCurrentColor(Color color) {
            this.currentColor = color;
        }

        // 지우개 상태를 설정하는 메서드
        public void setErasing(boolean erasing) {
            this.isErasing = erasing;
        }
    }
    private GamePanel gamePanel;

    private JPanel GamePanel() {
        gamePanel = new GamePanel(); // GamePanel 클래스의 인스턴스 생성
        return gamePanel; // JPanel로 반환 가능
    }

    //색깔 같은거 있는 패널
    private JPanel ItemPanel() {
        JPanel itemPanel = new JPanel();
        itemPanel.setLayout(new BoxLayout(itemPanel, BoxLayout.Y_AXIS));
        itemPanel.setBackground(Color.LIGHT_GRAY);

        JLabel title = new JLabel("도구 선택");
        itemPanel.add(title);

        JButton colorButton = new JButton("색상 선택");
        colorButton.addActionListener(e -> {
            Color selectedColor = JColorChooser.showDialog(null, "색상 선택", Color.BLACK);
            if (selectedColor != null) {
                // gamePanel 내부 메서드 호출
                ((GamePanel) gamePanel).setCurrentColor(selectedColor);
            }
        });
        itemPanel.add(colorButton);

        JButton eraseButton = new JButton("지우개");
        eraseButton.addActionListener(e -> {
            ((GamePanel) gamePanel).setErasing(true);
        });
        itemPanel.add(eraseButton);

        JButton drawButton = new JButton("그리기");
        drawButton.addActionListener(e -> {
            ((GamePanel) gamePanel).setErasing(false);
        });
        itemPanel.add(drawButton);

        return itemPanel;
    }

    // 유저 패널을 UserSidePanel에 추가하는 메서드
    public void addUserToPanel(String username) {
        System.out.println("addUserToPanel " + username);

        // UserSidePanel이 제대로 가져와지는지 확인하는 로그 추가
        JPanel userSidePanel = (JPanel) getComponent(1);  // UserSidePanel 위치는 BorderLayout.WEST
        if (userSidePanel == null) {
            System.out.println("userSidePanel is null");
        } else {
            System.out.println("userSidePanel found: " + userSidePanel);
        }

        JPanel userPanel = createUserPanel(username);
        userSidePanel.add(userPanel);  // 새 유저 패널을 UserSidePanel에 추가

        // UI 업데이트를 스윙 이벤트 큐에 추가하여 강제로 리프레시
        SwingUtilities.invokeLater(() -> {
            userSidePanel.revalidate();    // 레이아웃 다시 계산
            userSidePanel.repaint();       // 다시 그리기
        });
    }

    // 유저 정보를 담은 패널을 생성하는 메서드
    public JPanel createUserPanel(String username) {
        System.out.println("createUserPanel " + username);
        JPanel userPanel = new JPanel();
        userPanel.setLayout(new BorderLayout());
        userPanel.setBackground(Color.PINK); // 원하는 색상 설정

        JLabel userLabel = new JLabel(username);
        userPanel.add(userLabel, BorderLayout.CENTER);

        return userPanel;
    }

}
