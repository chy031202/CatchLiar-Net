import javax.sound.sampled.Line;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;

public class GameRoomPanel extends JPanel {
    private ClientManager clientManager;
    private GameMsg gameMsg;
    private Vector<User> userNames = new Vector<>();  // 방에 들어온 유저 저장
    private Vector<User> readyUsers = new Vector<>(); // 준비 완료한 유저 저장

    private JTextPane chat_display;
    private DefaultStyledDocument document;

    private JPanel userSidePanel; // 전역 변수로 저장
    private HashMap<String, JPanel> userLeftBottomPanels = new HashMap<>();
    private HashMap<String, JPanel> userRightPanels = new HashMap<>();
    private JPanel rightPannel;
    private JPanel readyPanel;
    private JPanel alarmPanel;
    private GamePanel gamePanel;
    private JPanel centerPanel;
    private String currentTurnUserName; // 현재 턴 사용자 이름

    public boolean ready = false;
    private boolean start = false;

    //시계 관련
    int count= 10;
    Timer timer;
    TimerTask timerTask;
    private JLabel alarmLabel;

    //펜 아이콘
    private ImageIcon penIcon;

    public GameRoomPanel(ClientManager clientManager, GameMsg gameMsg) {
        this.clientManager = clientManager;
        this.gameMsg = gameMsg;
//        this.readyUsers = readyUsers;

        gamePanel = new GamePanel(clientManager);
        centerPanel = gamePanel.createCenterPanel();

        userSidePanel = createUserSidePanel();
        readyPanel = createReadyPanel();
        rightPannel = createRightPanel();
        alarmPanel = createAlarmPanel();
        buildGUI();
    }

    public void changeGameMsg(GameMsg gameMsg) {
        this.gameMsg = gameMsg;
    }

    // 턴 사용자 업데이트 및 그림 그리기 활성화/비활성화 제어
    public void updateTurnUser(String userName) {
        currentTurnUserName = userName;

//        if (userName.equals(clientManager.getUser().getName())) {
//            gamePanel.enableDrawing(); // 자신의 턴일 때 그림 그리기 활성화
//        } else {
//            gamePanel.disableDrawing(); // 자신의 턴이 아닐 때 그림 그리기 비활성화
//        }
        
        System.out.println("턴 변경::현재 턴: " + userName);
        nowDrawingUser(userName);

        // UI에 턴 사용자 표시
//        JLabel turnLabel = new JLabel("현재 턴: " + userName);
//        turnLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
//        add(turnLabel, BorderLayout.NORTH);

        revalidate();
        repaint();
    }

    public void updateUser(Vector<User> userNames) {
        System.out.println("들어온 유저 목록: " + userNames);

        this.userNames = userNames;
        System.out.println("현재 userNames : " + userNames);

        refreshUserSidePanel();  // 유저 목록 UI 갱신
    }

    // 유저 새로 들어오면 UserSidePanel 갱신
    private void refreshUserSidePanel() {
        System.out.println("refreshUserSidePanel");

        remove(userSidePanel); // 기존 userSidePanel 제거
        // 새로운 userSidePanel 붙이기
        JPanel newUserSidePanel = createUserSidePanel();
        userSidePanel = newUserSidePanel; // 새로운 참조 유지
        add(userSidePanel, BorderLayout.WEST);

        revalidate();  // 레이아웃 갱신
        repaint();  // 화면 갱신
    }

    public void settingReady() {
        ready = true;
//        readyPanel.removeAll();
        rightPannel.remove(readyPanel);
        JPanel newReadyPanel = createReadyPanel();
        readyPanel = newReadyPanel;
        rightPannel.add(readyPanel, BorderLayout.NORTH);
        //updateTurnUser();

        revalidate();
        repaint();
    }

    public void updateReadyUser(Vector<User> readyUsers, User user) {
        this.readyUsers = readyUsers;
        System.out.println("updateReadyUser : " + readyUsers);
        refreshLeftBottomPanel(user);
    }

    private ImageIcon getPenIcon() {
        //getClass().getResource()?
        if (penIcon == null) {
            URL iconURL = getClass().getResource("/images/drawingpen.png");
            if (iconURL != null) {
                ImageIcon originalIcon = new ImageIcon(iconURL);
                Image scaledImage = originalIcon.getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH);
                penIcon = new ImageIcon(scaledImage);
            } else {
                System.out.println("이미지 파일을 찾을 수 없습니다.");
                penIcon = null; // 로드 실패 시 null 처리
            }
        }
        return penIcon;
    }

    // 현재 그리고 있는 클라이언트 표시
    private void nowDrawingUser(String currentDrawingUserName){
        for (Map.Entry<String, JPanel> entry : userLeftBottomPanels.entrySet()) {
            JPanel leftBottomPanel = entry.getValue();
            leftBottomPanel.removeAll(); // 기존 내용을 제거

            if (entry.getKey().equals(currentDrawingUserName)) {
                // 현재 그림을 그리는 사용자 강조
                ImageIcon penIcon = getPenIcon();
                if (penIcon != null) {
                    JLabel drawingLabel = new JLabel(penIcon, JLabel.CENTER);
                    leftBottomPanel.add(drawingLabel);
                }
                //leftBottomPanel.add(drawingLabel);
                leftBottomPanel.setBackground(new Color(255, 230, 204)); // 강조 배경색
            } else {
                // 기본 상태 유지
                leftBottomPanel.add(new JLabel("대기 중"));
                leftBottomPanel.setBackground(new Color(242, 242, 242)); // 기본 배경색
            }

            leftBottomPanel.revalidate();
            leftBottomPanel.repaint();
        }
    }

    // 유저왼쪽하단 준비 완료 화면 갱신
    private void refreshLeftBottomPanel(User user) {
        System.out.println("refreshLeftBottomPanel");
        if(user != null) { // 준비해제
            JPanel leftBottomPanel = userLeftBottomPanels.get(user.getName());
            if(leftBottomPanel != null) {
                leftBottomPanel.removeAll();
                leftBottomPanel.revalidate();
                leftBottomPanel.repaint();
            }
        } else {
            if(readyUsers != null) {
                for (User readyUser : readyUsers) {
                    String readyUserName = readyUser.getName();
                    JPanel leftBottomPanel = userLeftBottomPanels.get(readyUserName); // 해당 유저의 패널 찾기

                    if (leftBottomPanel != null) {
                        leftBottomPanel.removeAll();
                        leftBottomPanel.add(new JLabel("준비 완료"));
                        leftBottomPanel.revalidate();
                        leftBottomPanel.repaint();
                    }
                }
            }
        }
    }

    public void clearAllLeftBottomPanels() {
        if (userLeftBottomPanels != null) {
            for (JPanel leftBottomPanel : userLeftBottomPanels.values()) {
                if (leftBottomPanel != null) {
                    leftBottomPanel.removeAll();
                    leftBottomPanel.revalidate();
                    leftBottomPanel.repaint();
                }
            }
        }
    }

    // 유저왼쪽하단 준비 완료 화면 갱신
    public void refreshUserRightPanel(User user, String emoticonName) {
        System.out.println("refreshUserRightPanel");
        if(user != null) { // 준비해제
            JPanel userRightPanel = userRightPanels.get(user.getName());
            if(userRightPanel != null) {
                userRightPanel.removeAll();
                String resourcePath = getEmoticonPath(emoticonName);
                if (resourcePath != null) {
                    ImageIcon emoticonIcon = new ImageIcon(getClass().getResource(resourcePath));
                    JLabel emoticonLabel = new JLabel(emoticonIcon);
                    userRightPanel.add(emoticonLabel); // 이모티콘 추가

                    // 6초 후에 이모티콘 제거
                    Timer timer = new Timer(6000, e -> {
                        userRightPanel.remove(emoticonLabel);
                        userRightPanel.revalidate();
                        userRightPanel.repaint();
                    });
                    timer.setRepeats(false); // 반복하지 않도록 설정
                    timer.start(); // 타이머 시작

                } else {
                    userRightPanel.add(new JLabel("Emoticon")); // 경로가 없을 때 대체 텍스트
                }
                userRightPanel.revalidate();
                userRightPanel.repaint();
            }
        }
    }

    private String getEmoticonPath(String emoticonName) {
        switch (emoticonName) {
            case "like":
                return "/images/like-70.gif";
            case "smile":
                return "/images/smile-70.gif";
            case "sleepy":
                return "/images/sleepy-70.gif";
            case "doubt":
                return "/images/doupt-70.gif";
            case "frustrated":
                return "/images/frustrated-70.gif";
            case "angry":
                return "/images/angry-70.gif";
            default:
                return null; // 알 수 없는 이모티콘
        }
    }


    // 게임 시작하면 캔버스 초기화 & readyPanel 없애고 alarmPanel로 갱신 & 키워드 패널 띄움
    public void refreshStartGame() {
        System.out.println("refreshStartGame");
        start = true;
        ready = false;

        // 유저 하단 패널 초기화
        clearAllLeftBottomPanels();

        gamePanel.clearLines(); // 캔버스 초기화
        // 라이어 빼고 화면에 키워드 추가
        if(!gameMsg.user.isLiar) {
            gamePanel.addKeyword(gameMsg.user.currentRoom.getKeyword());
        }
        rightPannel.remove(readyPanel);
        rightPannel.add(alarmPanel, BorderLayout.NORTH);

        revalidate();
        repaint();
    }


    private void buildGUI() {
        setBounds(50, 200, 800, 600);
        setLayout(new BorderLayout());

        add(createTopPanel(), BorderLayout.NORTH);
        add(userSidePanel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        add(rightPannel, BorderLayout.EAST);
        //add(updateTurnUser());

//        refreshLeftBottomPanel();
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(0, 35));
        panel.setBackground(new Color(64,48,47));
        JLabel title = new JLabel(gameMsg.user.name + " 님의 " + gameMsg.message + " 방", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        panel.add(title, BorderLayout.NORTH);
        return panel;
    }

    private JPanel createUserSidePanel(){
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // 세로 방향 정렬
        panel.setPreferredSize(new Dimension(170, 0));
        panel.setBackground(new Color(64,48,47));

        for (User userName : userNames) {
            JPanel userPanel = createIndividualUserPanel(userName.getName());
            userPanel.setMaximumSize(new Dimension(150, 90)); // 크기 고정

            panel.add(userPanel);
            panel.add(Box.createRigidArea(new Dimension(0, 17))); // 간격 추가
//            panel.add(createIndividualUserPanel(userName));
        }

        return panel;
    }


    private JPanel createIndividualUserPanel(String userName) {
        JPanel panel = new JPanel(new GridLayout(1, 2));

        JPanel leftPanel = new JPanel(new GridLayout(2, 1));

        JPanel leftTopPanel = new JPanel();
        leftTopPanel.setBackground(new Color(242,242,242));
        leftTopPanel.add(new JLabel(userName + " 님"));
        leftTopPanel.setBorder(BorderFactory.createLineBorder(new Color(64,48,47), 2)); // 테두리
        leftPanel.add(leftTopPanel);

        JPanel leftBottomPanel;
        if (userLeftBottomPanels.containsKey(userName)) {
            // 기존 패널 가져오기
            System.out.println("기존 leftBottomPanel 패널 가져옴");
            leftBottomPanel = userLeftBottomPanels.get(userName);
        } else {
            // 새 패널 생성
            leftBottomPanel = new JPanel();
            leftBottomPanel.setBackground(new Color(242, 242, 242));
            leftBottomPanel.setBorder(BorderFactory.createLineBorder(new Color(64, 48, 47), 2)); // 테두리

            userLeftBottomPanels.put(userName, leftBottomPanel); // userLeftBottomPanels에 저장해서 관리
        }
        leftPanel.add(leftBottomPanel);

//        JPanel rightPanel = new JPanel();
//        rightPanel.setBackground(new Color(242,242,242));
//        rightPanel.setBorder(BorderFactory.createLineBorder(new Color(64,48,47), 2)); // 테두리

        JPanel rightPanel;
        if (userRightPanels.containsKey(userName)) {
            // 기존 패널 가져오기
            System.out.println("기존 userRightPanel 패널 가져옴");
            rightPanel = userRightPanels.get(userName);
        } else {
            // 새 패널 생성
            rightPanel = new JPanel();
            rightPanel.setBackground(new Color(242, 242, 242));
            rightPanel.setBorder(BorderFactory.createLineBorder(new Color(64, 48, 47), 2)); // 테두리

            userRightPanels.put(userName, rightPanel); // userRightPanels에 저장해서 관리
        }

        panel.add(leftPanel);
        panel.add(rightPanel);

        return panel;
    }


    private JPanel createRightPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout()); // 위아래로 나눔
        panel.setPreferredSize(new Dimension(170, 0));


        panel.add(readyPanel, BorderLayout.NORTH);

        // 가운데 채팅 패널
        JPanel chatPanel = ChatPanel();
        panel.add(chatPanel, BorderLayout.CENTER);

        // 아래쪽 이모티콘 패널
        JPanel imgPanel = ImgPanel();
        panel.add(imgPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createReadyPanel() {
        JPanel panel = new JPanel(new GridLayout(2,1));
        panel.setPreferredSize(new Dimension(0, 80));

        JPanel welcomePanel = new JPanel();
        welcomePanel.add(new JLabel("환영합니다! " + gameMsg.user.name + " 님"));


        if(ready == true) {
            JPanel buttonPanel = new JPanel(new GridLayout(1,2));
            JButton readyButton = new JButton("준비");
            JButton unReadyButton = new JButton("준비 해제");
            unReadyButton.setEnabled(false);
//            buttonPanel.add(new JLabel("gif 추가 예정"));
            buttonPanel.add(readyButton);
            buttonPanel.add(unReadyButton);

            readyButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    readyButton.setEnabled(false);
                    unReadyButton.setEnabled(true);
                    clientManager.sendReady(gameMsg.user);
                }
            });
            unReadyButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    readyButton.setEnabled(true);
                    unReadyButton.setEnabled(false);
                    clientManager.sendUnReady(gameMsg.user);
                }
            });

            panel.add(welcomePanel);
            panel.add(buttonPanel);
        } else {
            panel.add(welcomePanel);
        }


        return panel;
    }

    private JPanel createAlarmPanel() {
        JPanel alarmPanel = new JPanel();
        alarmPanel.setPreferredSize(new Dimension(0, 80));
        alarmPanel.setBackground(Color.pink);
        //JLabel alarm = new JLabel("시계");

        JLabel alarmLabel = new JLabel("남은 시간: 준비 중...");
        alarmLabel.setFont(new Font("Arial", Font.BOLD, 16));
        alarmPanel.add(alarmLabel);

        // GameRoomPanel에 JLabel 참조 저장 (UI 갱신에 필요)
        this.alarmLabel = alarmLabel;
        return alarmPanel;
    }

    private JPanel ChatPanel(){
        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new BorderLayout());
        chatPanel.setPreferredSize(new Dimension(0, 300));

        chatPanel.add(ChatDisplayPanel(), BorderLayout.CENTER);
        chatPanel.add(ChatInputPanel(), BorderLayout.SOUTH);

        return chatPanel;
    }

    private JPanel ChatDisplayPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(0, 270));

        document = new DefaultStyledDocument();
        chat_display = new JTextPane(document);
        chat_display.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(chat_display);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel ChatInputPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(0, 30));

        JTextField chat_input = new JTextField();
//        chat_input.setEnabled(false);
        chat_input.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String msg = chat_input.getText();
//                clientManager.sendChat(msg, userNames);
                clientManager.sendChat(msg);
                chat_input.setText("");
            }
        });

        JButton b_send = new JButton("전송");
//        b_send.setEnabled(false);
        b_send.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String msg = chat_input.getText();
                clientManager.sendChat(msg);
                chat_input.setText("");
            }
        });

        panel.add(chat_input, BorderLayout.CENTER);
        panel.add(b_send, BorderLayout.EAST);

        return panel;
    }

    public void showChat(String msg) {
        System.out.println("showChat에 " + msg);
//        SwingUtilities.invokeLater(() -> {
            int len = chat_display.getDocument().getLength();
            try {
                document.insertString(len, msg + "\n", null);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
            chat_display.setCaretPosition(len);
//        });
    }

    private JPanel ImgPanel() {
        JPanel imgPanel = new JPanel(new GridLayout(2, 3));
        imgPanel.setPreferredSize(new Dimension(0, 120));
        imgPanel.setBackground(new Color(64, 48, 47));

        // 각각의 GIF를 생성
        JLabel likeLabel = createClickableLabel("/images/like-resize-50.gif", "like");
        JLabel smileLabel = createClickableLabel("/images/smile.gif", "smile");
        JLabel sleepyLabel = createClickableLabel("/images/sleepy.gif", "sleepy");
        JLabel douptLabel = createClickableLabel("/images/doupt.gif", "doubt");
        JLabel frustratedLabel = createClickableLabel("/images/frustrated.gif", "frustrated");
        JLabel angryLabel = createClickableLabel("/images/angry.gif", "angry");

        // 패널에 추가
        imgPanel.add(likeLabel);
        imgPanel.add(smileLabel);
        imgPanel.add(sleepyLabel);
        imgPanel.add(douptLabel);
        imgPanel.add(frustratedLabel);
        imgPanel.add(angryLabel);

        return imgPanel;
    }

    private JLabel createClickableLabel(String resourcePath, String emoticonName) {
        // 이미지 로드
        ImageIcon icon = new ImageIcon(getClass().getResource(resourcePath));
        JLabel label = new JLabel(icon);

        // MouseListener 추가
        label.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                System.out.println(emoticonName + " GIF clicked!");
                clientManager.sendEmoticon(gameMsg.user, emoticonName);
            }
        });

        return label;
    }


    public void updateAlarmLabel(int remainingTime) {
        if (alarmLabel != null) {
            alarmLabel.setText("Time: " + remainingTime);
            //System.out.println("알람 업데이트: 남은 시간 -> " + remainingTime + "초");
        }
    }

    public JLabel getAlarmLabel() {
        return alarmLabel;
    }

}
