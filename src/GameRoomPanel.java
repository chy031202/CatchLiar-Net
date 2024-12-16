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
import java.util.stream.Collectors;

public class GameRoomPanel extends JPanel {
    private ClientManager clientManager;
    private GameMsg gameMsg;
    private Vector<User> userNames = new Vector<>();  // 방에 들어온 유저 저장
    private Vector<User> readyUsers = new Vector<>(); // 준비 완료한 유저 저장

    private JTextPane chat_display;
    private DefaultStyledDocument document;

    private JPanel userSidePanel; // 전역 변수로 저장
    private HashMap<String, JPanel> userLeftTopPanels = new HashMap<>();
    private HashMap<String, JPanel> userLeftBottomPanels = new HashMap<>();
    private HashMap<String, JPanel> userRightPanels = new HashMap<>();
    public JPanel rightPannel;
    private JPanel readyPanel;
    public JPanel alarmPanel;
    public GamePanel gamePanel;
    public JPanel centerPanel;
    private JLabel alarmLabel;
    private JComboBox<String> userDropdown;
    private String currentTurnUserName; // 현재 턴 사용자 이름

    public boolean ready = false;
    private boolean start = false;
    // 투표 상태 플래그
    private boolean hasVoted = false; // 이미 투표했는지 여부 추적
    private boolean isVotingActive; // 플래그는 private로 설정
    //펜 아이콘
    private ImageIcon penIcon;
    private ImageIcon voteIcon;

    public GameRoomPanel(ClientManager clientManager, GameMsg gameMsg) {
        this.clientManager = clientManager;
        this.gameMsg = gameMsg;

        gamePanel = new GamePanel(clientManager);
        centerPanel = gamePanel.createCenterPanel();

        userSidePanel = createUserSidePanel();
        readyPanel = createReadyPanel();
        rightPannel = createRightPanel();
        alarmPanel = createAlarmPanel();
        buildGUI();
    }

    public void changeGameMsg(GameMsg gameMsg, String userName) {
        this.gameMsg = gameMsg;
        gameMsg.user.name = userName;
    }

    // 턴 사용자 업데이트 및 그림 그리기 활성화/비활성화 제어
    public void updateTurnUser(String userName) {
        currentTurnUserName = userName;
        // 자신의 턴이 아닐 경우 GamePanel 비활성화
        if (!userName.equals(clientManager.getUser().getName())) {
            gamePanel.setDrawingEnabled(false);
            System.out.println("다른 사용자의 턴입니다. GamePanel 비활성화.");
        } else {
            gamePanel.setDrawingEnabled(true);
            System.out.println("내 턴입니다. GamePanel 활성화.");
        }
        System.out.println("턴 변경::현재 턴: " + userName);
        nowDrawingUser(userName);

        revalidate();
        repaint();
    }

    public void removePanelSave() {
        // 없어진 유저의 패널은 지움
        Set<String> currentUserNames = userNames.stream()
                .map(User::getName)
                .collect(Collectors.toSet());
        // userLeftTopPanels에서 제거
        userLeftTopPanels.keySet().removeIf(username -> !currentUserNames.contains(username));
        // userLeftBottomPanels에서 제거
        userLeftBottomPanels.keySet().removeIf(username -> !currentUserNames.contains(username));
        // userRightPanels에서 제거
        userRightPanels.keySet().removeIf(username -> !currentUserNames.contains(username));

        refreshUserSidePanel();  // 유저 목록 UI 갱신
    }

    public void updateUser(Vector<User> userNames) {
        this.userNames = userNames;
        refreshUserSidePanel();  // 유저 목록 UI 갱신
        updateUserDropdown();    // 토글 유저 목록 갱신
    }

    public void updateReadyUser(Vector<User> readyUsers, User user) {
        this.readyUsers = readyUsers;
//        System.out.println("updateReadyUser : " + readyUsers);
        refreshLeftBottomPanel(user);
    }

    // 유저 새로 들어오면 UserSidePanel 갱신
    private void refreshUserSidePanel() {
        remove(userSidePanel); // 기존 userSidePanel 제거
        // 새로운 userSidePanel 붙이기
        JPanel newUserSidePanel = createUserSidePanel();
        userSidePanel = newUserSidePanel; // 새로운 참조 유지
        add(userSidePanel, BorderLayout.WEST);

        revalidate();  // 레이아웃 갱신
        repaint();  // 화면 갱신
    }

    // 유저왼쪽하단 준비 완료 화면 갱신
    private void refreshLeftBottomPanel(User user) {
//        System.out.println("refreshLeftBottomPanel");
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
                    leftBottomPanel.setBackground(new Color(242, 242, 242));
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
                    Timer timer = new Timer(3000, e -> {
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

    public void settingReady() {
        ready = true;
        rightPannel.remove(readyPanel);
        JPanel newReadyPanel = createReadyPanel();
        readyPanel = newReadyPanel;
        rightPannel.add(readyPanel, BorderLayout.NORTH);

        revalidate();
        repaint();
    }

    public void settingUnReady() {
        ready = false;
        rightPannel.remove(readyPanel);
        JPanel newReadyPanel = createReadyPanel();
        readyPanel = newReadyPanel;
        rightPannel.add(readyPanel, BorderLayout.NORTH);

        revalidate();
        repaint();
    }

    private ImageIcon getPenIcon() {
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

    private ImageIcon getVoteICon(){
        if (voteIcon == null) {
            URL iconURL = getClass().getResource("/images/Vote.png");
            if (iconURL != null) {
                ImageIcon originalIcon = new ImageIcon(iconURL);
                Image scaledImage = originalIcon.getImage().getScaledInstance(17, 17, Image.SCALE_SMOOTH);
                voteIcon = new ImageIcon(scaledImage);
            } else {
                System.out.println("이미지 파일을 찾을 수 없습니다.");
                voteIcon = null; // 로드 실패 시 null 처리
            }
        }
        return voteIcon;
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
                leftBottomPanel.setBackground(new Color(201,208,191)); // 강조 배경색
            } else {
                // 기본 상태 유지
                leftBottomPanel.add(new JLabel("대기 중"));
                leftBottomPanel.setBackground(new Color(242, 242, 242)); // 기본 배경색
            }

            leftBottomPanel.revalidate();
            leftBottomPanel.repaint();
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

        if(gamePanel.keywordPanel != null) {
            gamePanel.southPanel.remove(gamePanel.keywordPanel);
            gamePanel.keywordPanel.removeAll();
        }
        gamePanel.southPanel.remove(gamePanel.exitPanel);

        // 라이어 빼고 화면에 키워드 추가
        if(!gameMsg.user.isLiar) {
            System.out.println("refreshStartGame 나머지 : " + gameMsg.user.name);
            gamePanel.addKeyword(gameMsg.user.currentRoom.getKeyword());
        } else {
//            gamePanel.southPanel.remove(gamePanel.exitPanel);
            System.out.println("refreshStartGame 라이어 : " + gameMsg.user.name);
            JPanel panel= new JPanel(); panel.setBackground(new Color(64,48,47));
            gamePanel.southPanel.add(panel);
        }
        rightPannel.remove(readyPanel);
        rightPannel.add(alarmPanel, BorderLayout.NORTH);

        gamePanel.revalidate();
        gamePanel.repaint();
        revalidate();
        repaint();
    }

    public void refreshEndGame() {
        System.out.println("refreshEndGame");
        gamePanel.endGameSouthPanel(gameMsg.user.currentRoom.getKeyword());

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
        }
        return panel;
    }

    private void setupClickEventForPanel(JPanel userPanel, String userName) {
        // 클릭 이벤트 추가
        if (isVotingActive) {
            userPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    handleVote(userName, userPanel);
                }
            });
        }
    }

    public void setVotingActive(boolean active) {
        isVotingActive = active;
        System.out.println("setVotingActive 확인  :: "+isVotingActive);

        if (active) {
            // 투표 모드 활성화 시 모든 패널에 클릭 이벤트 추가
            for (Map.Entry<String, JPanel> entry : userLeftBottomPanels.entrySet()) {
                setupClickEventForPanel(entry.getValue(), entry.getKey());
            }
        } else {
            // 투표 모드 비활성화 시 기존 이벤트 제거
            for (JPanel panel : userLeftBottomPanels.values()) {
                for (MouseListener listener : panel.getMouseListeners()) {
                    panel.removeMouseListener(listener);
                }
            }
        }
        revalidate();
        repaint();
    }

    public void setGameMsg(GameMsg inMsg) {
        // gameMsg 모드에 따라 투표 상태를 설정
        if (inMsg != null && inMsg.mode == GameMsg.VOTE) {
            setVotingActive(true); // 투표 모드 활성화
        } else {
            setVotingActive(false); // 투표 모드 비활성화
        }
    }


    private JPanel createIndividualUserPanel(String userName) {
        JPanel panel = new JPanel(new GridLayout(1, 2));
        JPanel leftPanel = new JPanel(new GridLayout(2, 1));

        JPanel leftTopPanel;
        if(userLeftTopPanels.containsKey(userName)) {
            // 기존 패널 가져오기
//            System.out.println("기존 leftBottomPanel 패널 가져옴");
            leftTopPanel = userLeftTopPanels.get(userName);
        } else {
            // 새 패널 생성
            leftTopPanel = new JPanel();
            if(userName.equals(gameMsg.user.name)) {
                leftTopPanel.setBackground(new Color(201,208,191));
            } else {
                leftTopPanel.setBackground(new Color(242, 242, 242));
            }
            leftTopPanel.add(new JLabel(userName + " 님"));
            leftTopPanel.setBorder(BorderFactory.createLineBorder(new Color(64, 48, 47), 2)); // 테두리

            userLeftTopPanels.put(userName, leftTopPanel); // userLeftBottomPanels에 저장해서 관리
        }
        leftPanel.add(leftTopPanel);

        JPanel leftBottomPanel;
        if (userLeftBottomPanels.containsKey(userName)) {
            // 기존 패널 가져오기
//            System.out.println("기존 leftBottomPanel 패널 가져옴");
            leftBottomPanel = userLeftBottomPanels.get(userName);
        } else {
            // 새 패널 생성
            leftBottomPanel = new JPanel();
            leftBottomPanel.setBackground(new Color(242, 242, 242));
            leftBottomPanel.setBorder(BorderFactory.createLineBorder(new Color(64, 48, 47), 2)); // 테두리

            userLeftBottomPanels.put(userName, leftBottomPanel); // userLeftBottomPanels에 저장해서 관리
        }
        leftPanel.add(leftBottomPanel);

        // setupClickEventForPanel을 leftBottomPanel에 적용
        setupClickEventForPanel(leftBottomPanel, userName);

        JPanel rightPanel;
        if (userRightPanels.containsKey(userName)) {
            // 기존 패널 가져오기
//            System.out.println("기존 userRightPanel 패널 가져옴");
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

    private void handleVote(String votedUserName, JPanel leftBottomPanel) {
        // 이미 투표를 한 경우
        if (hasVoted) {
            //JOptionPane.showMessageDialog(this, "이미 투표했습니다!", "투표 불가", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // 이미 투표한 경우 투표 불가능 처리
        if (!leftBottomPanel.isEnabled()) {
            //JOptionPane.showMessageDialog(this, "이미 투표했습니다!", "투표 불가", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 투표 상태 업데이트
        hasVoted = true;
        // UI 갱신 // 이미지를 JLabel에 추가
        try {
            // getVoteIcon 메서드를 통해 아이콘 가져오기
            ImageIcon voteIcon = getVoteICon(); // 클래스의 getVoteICon 메서드 사용
            if (voteIcon != null) {
                JLabel voteLabel = new JLabel(voteIcon, JLabel.CENTER); // 아이콘으로 JLabel 생성
                leftBottomPanel.add(voteLabel); // 아이콘 추가
            }
            else {
                System.err.println("Vote 아이콘을 가져올 수 없습니다.");
            }
        } catch (Exception e) {
            System.err.println("이미지 로드 실패: " + e.getMessage());
        }
        leftBottomPanel.setEnabled(false); // 중복 클릭 방지
        // 투표 요청 서버로 전송
        clientManager.sendVote(gameMsg.user, votedUserName);
        System.out.println("투표 요청 전송: " + votedUserName);

        revalidate();
        repaint();
        // 투표 성공 알림
        JOptionPane.showMessageDialog(this, votedUserName + " 님에게 투표했습니다!", "투표 완료", JOptionPane.INFORMATION_MESSAGE);
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
        panel.setBackground(new Color(64,48,47));

        JPanel welcomePanel = new JPanel();
        welcomePanel.setBackground(new Color(64,48,47));
        JLabel welcomeLabel = new JLabel("환영합니다! " + gameMsg.user.name + " 님");
        welcomeLabel.setForeground(Color.WHITE); // 텍스트 색상을 흰색으로 설정
        welcomePanel.add(welcomeLabel);

        if(ready == true) {
            JPanel buttonPanel = new JPanel(new GridLayout(1,2));
            JButton readyButton = new JButton("준비");
            JButton unReadyButton = new JButton("준비 해제");
            unReadyButton.setEnabled(false);
            buttonPanel.add(readyButton);
            buttonPanel.add(unReadyButton);

            readyButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    readyButton.setEnabled(false);
                    unReadyButton.setEnabled(true);
                    clientManager.sendReady(clientManager.getUser());
                }
            });
            unReadyButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    readyButton.setEnabled(true);
                    unReadyButton.setEnabled(false);
                    clientManager.sendUnReady(clientManager.getUser());
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
        JPanel alarmPanel = new JPanel(new BorderLayout()) {
            private Image backgroundImage = new ImageIcon(getClass().getResource("/images/alarm.png")).getImage();

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // 이미지 그리기
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        };
        alarmPanel.setPreferredSize(new Dimension(0, 80));
        alarmPanel.setBackground(new Color(64,48,47));

        JLabel alarmLabel = new JLabel("남은 시간: 준비 중...");
        alarmLabel.setFont(new Font("Arial", Font.BOLD, 16));
        alarmLabel.setForeground(Color.black);
        alarmLabel.setHorizontalAlignment(SwingConstants.CENTER);
        alarmPanel.add(alarmLabel, BorderLayout.CENTER);

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
        panel.setPreferredSize(new Dimension(0, 40));

        JTextField chat_input = new JTextField();
        chat_input.setPreferredSize(new Dimension(100, 40));
        chat_input.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String msg = chat_input.getText();
//                clientManager.sendChat(msg, userNames);
                clientManager.sendChat(msg);
                chat_input.setText("");
            }
        });

        // 유저 목록 드롭다운 초기화
        userDropdown = new JComboBox<>();
        userDropdown.addItem("모두"); // 기본값
        userDropdown.setPreferredSize(new Dimension(40, 40));

        JButton b_send = new JButton("전송");
        b_send.setPreferredSize(new Dimension(30, 40));
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
//        JPanel controlsPanel = new JPanel();
//        controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.X_AXIS));
//
//        controlsPanel.add(userDropdown);
//        controlsPanel.add(Box.createHorizontalStrut(5)); // 간격 추가
//        controlsPanel.add(chat_input);
//        controlsPanel.add(Box.createHorizontalStrut(5)); // 간격 추가
//        controlsPanel.add(b_send);
//
//        panel.add(controlsPanel, BorderLayout.CENTER);

        return panel;
    }

    private void updateUserDropdown() {
        if (userDropdown != null) {
            userDropdown.removeAllItems(); // 기존 항목 모두 제거
            userDropdown.addItem("모두");  // 기본값 추가
            for (User user : userNames) {
                userDropdown.addItem(user.getName()); // 유저 이름 추가
            }
        }
    }

    public void showChat(String msg) {
        System.out.println("showChat에 " + msg);
        int len = chat_display.getDocument().getLength();
        try {
            document.insertString(len, msg + "\n", null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        chat_display.setCaretPosition(len);
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
//                System.out.println(emoticonName + " GIF clicked!");
                clientManager.sendEmoticon(emoticonName);
            }
        });
        return label;
    }

    public void updateAlarmLabel(int remainingTime) {
        if (alarmLabel != null) {
            alarmLabel.setText(" " + remainingTime);
            alarmLabel.setForeground(Color.black);
            alarmLabel.setHorizontalAlignment(SwingConstants.CENTER);
            //System.out.println("알람 업데이트: 남은 시간 -> " + remainingTime + "초");
        }
    }

    public void resetVoteState() {
        hasVoted = false; // 투표 상태 초기화
        for (JPanel panel : userLeftBottomPanels.values()) {
            panel.setEnabled(true); // 모든 패널을 활성화
            panel.removeAll(); // 기존 컴포넌트 제거
            panel.setBackground(new Color(242, 242, 242)); // 초기 배경색 설정
            panel.revalidate();
            panel.repaint();
        }
        System.out.println("투표 상태 초기화 완료");
    }

    public void resetLiarState() {
        // 각 사용자 라이어 상태 초기화
        for (User user : userNames) {
            user.isLiar = false; // 모든 사용자 라이어 상태 해제
        }
        System.out.println("라이어 상태 및 결과 화면 초기화 완료.");
    }

    public void showGameResult(boolean isWinner, String resultMessage) {
        // 디버깅용 메시지 출력
        System.out.println("[Gam_DEBUG] showGameResult 호출됨!");
        System.out.println("[Gam_DEBUG] isWinner: " + isWinner);
        System.out.println("[Gam_DEBUG] resultMessage: " + resultMessage);
        // 배경 이미지 결정
        String imagePath = isWinner ? "/images/result_win.png" : "/images/result_lost.png";
//        System.out.println("[DEBUG] changeGameResultWithOverlay 호출 직전");
        gamePanel.changeGameResultWithOverlay(imagePath, resultMessage);
        // 기존 패널 갱신
        revalidate();
        repaint();
    }
    public String getCurrentTurnUserName() {
        return currentTurnUserName;
    }
}
