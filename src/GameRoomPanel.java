import javax.sound.sampled.Line;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Vector;

public class GameRoomPanel extends JPanel {
    private ClientManager clientManager;
    private GameMsg gameMsg;
    private Vector<User> userNames = new Vector<>();  // 방에 들어온 유저 이름 저장

    private GamePanel gamePanel;

    public GameRoomPanel(ClientManager clientManager, GameMsg gameMsg) {
        this.clientManager = clientManager;
        this.gameMsg = gameMsg;
        gamePanel = new GamePanel(clientManager);

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

        // 기존에 있던 다른 패널들은 그대로 두고 userSidePanel만 갱신하기
        JPanel userSidePanel = createUserSidePanel();
        JPanel centerPanel = gamePanel.createCenterPanel();
        JPanel rightPanel = createRightPanel();

        // 기존 패널을 모두 삭제하고 다시 추가하는 대신, 필요한 부분만 갱신
        removeAll();  // 기존 UI를 삭제

        // userSidePanel만 갱신
        buildGUI();

        revalidate();  // 레이아웃 갱신
        repaint();  // 화면 갱신
    }


    private void buildGUI() {
        setBounds(50, 200, 800, 600);
        setLayout(new BorderLayout());

        add(createTopPanel(), BorderLayout.NORTH);
        add(createUserSidePanel(), BorderLayout.WEST);
        add(gamePanel.createCenterPanel(), BorderLayout.CENTER);
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

        // 현재 유저 목록을 JLabel로 표시
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
        JLabel user = new JLabel("채팅 패널");
        chatPanel.setBackground(Color.white);

        chatPanel.add(user);
        return chatPanel;
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
