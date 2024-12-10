import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

public class GameRoomPanel extends JPanel {
    private ClientManager clientManager;
    private GameMsg gameMsg;
    private Vector<User> userNames = new Vector<>();  // 방에 들어온 유저 이름 저장

    private JTextPane chat_display;
    private DefaultStyledDocument document;

    private JPanel userSidePanel; // 전역 변수로 저장

    public GameRoomPanel(ClientManager clientManager, GameMsg gameMsg) {
        this.clientManager = clientManager;
        this.gameMsg = gameMsg;

        userSidePanel = createUserSidePanel();
        buildGUI();
    }

    public void updateUser(Vector<User> userNames) {
        System.out.println("들어온 유저 목록: " + userNames);

        this.userNames = userNames;
        System.out.println("현재 userNames : " + userNames);

        refreshUserSidePanel();  // 유저 목록 UI 갱신
    }


    // 유저 목록 User Side Panel UI 갱신
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


    private void buildGUI() {
        setBounds(50, 200, 800, 600);
        setLayout(new BorderLayout());

        add(createTopPanel(), BorderLayout.NORTH);
        add(userSidePanel, BorderLayout.WEST);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createRightPanel(), BorderLayout.EAST);
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
//        panel.setLayout(new GridLayout(0, 1));  // 1열 4행으로 세로로 나열
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // 세로 방향 정렬
        panel.setPreferredSize(new Dimension(180, 0));
        panel.setBackground(new Color(64,48,47));


        for (User userName : userNames) {
            JPanel userPanel = createIndividualUserPanel(userName.getName());
            userPanel.setMaximumSize(new Dimension(160, 110)); // 크기 고정

            panel.add(userPanel);
            panel.add(Box.createRigidArea(new Dimension(0, 20))); // 간격 추가
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

        JPanel leftBottomPanel = new JPanel();
        leftBottomPanel.setBackground(new Color(242,242,242));
        leftBottomPanel.setBorder(BorderFactory.createLineBorder(new Color(64,48,47), 2)); // 테두리
        leftPanel.add(leftBottomPanel);

        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(new Color(242,242,242));
        rightPanel.setBorder(BorderFactory.createLineBorder(new Color(64,48,47), 2)); // 테두리

        panel.add(leftPanel);
        panel.add(rightPanel);

        return panel;
    }

    private JPanel createCenterPanel(){
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JPanel gamepanel = GamePanel();
        JPanel Itempanel = ItemPanel();

        panel.add(gamepanel, BorderLayout.CENTER);
        panel.add(Itempanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel GamePanel(){
        JPanel gamePanel = new JPanel();
        gamePanel.setBackground(Color.BLACK);
        JLabel text = new JLabel("메인 패널");
        text.setForeground(Color.WHITE);

        gamePanel.add(text);
        return gamePanel;
    }

    private JPanel ItemPanel(){
        JPanel itemPanel = new JPanel();
        itemPanel.setLayout(new BoxLayout(itemPanel, BoxLayout.Y_AXIS));
        itemPanel.setPreferredSize(new Dimension(0, 120));
        itemPanel.setBackground(Color.lightGray);
        JLabel user = new JLabel("색깔 선택");

        itemPanel.add(user);
        return itemPanel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout()); // 위아래로 나눔
        panel.setPreferredSize(new Dimension(180, 0));

        JPanel alarmPanel = AlarmPanel();
        panel.add(alarmPanel, BorderLayout.NORTH);

        // 가운데 채팅 패널
        JPanel chatPanel = ChatPanel();
        panel.add(chatPanel, BorderLayout.CENTER);

        // 아래쪽 이모티콘 패널
        JPanel imgPanel = ImgPanel();
        panel.add(imgPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel AlarmPanel() {
        JPanel alarmPanel = new JPanel();
        alarmPanel.setPreferredSize(new Dimension(0, 80));
        alarmPanel.setBackground(Color.pink);
        JLabel alarm = new JLabel("시계");
        alarmPanel.add(alarm);

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
        panel.setPreferredSize(new Dimension(0, 250));

        document = new DefaultStyledDocument();
        chat_display = new JTextPane(document);
        chat_display.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(chat_display);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel ChatInputPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(0, 50));

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


    private JPanel ImgPanel(){
        JPanel imgPanel = new JPanel();
        imgPanel.setPreferredSize(new Dimension(0, 120));
        JLabel user = new JLabel("이모티콘 패널");
        imgPanel.setBackground(Color.pink);

        imgPanel.add(user);
        return imgPanel;
    }

}
