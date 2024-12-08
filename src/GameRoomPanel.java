import javax.swing.*;
import java.awt.*;
import java.util.Vector;

public class GameRoomPanel extends JPanel {
    private ClientManager clientManager;
    private GameMsg gameMsg;
    private Vector<String> userNames = new Vector<>();  // 방에 들어온 유저 이름 저장

    public GameRoomPanel(ClientManager clientManager, GameMsg gameMsg) {
        this.clientManager = clientManager;
        this.gameMsg = gameMsg;

        buildGUI();
    }

    public void updateUser(Vector<String> userNames) {
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
        JPanel centerPanel = createCenterPanel();
        JPanel rightPanel = createRightPanel();

        // 기존 패널을 모두 삭제하고 다시 추가하는 대신, 필요한 부분만 갱신
        removeAll();  // 기존 UI를 삭제

        // userSidePanel만 갱신
        JLabel title = new JLabel(gameMsg.user.name + "님의 " + gameMsg.message + "방", SwingConstants.CENTER);
        add(title, BorderLayout.NORTH);
        add(userSidePanel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        revalidate();  // 레이아웃 갱신
        repaint();  // 화면 갱신
    }


    private void buildGUI() {
        setBounds(50, 200, 800, 600);
        setLayout(new BorderLayout());

        JLabel title = new JLabel(gameMsg.user.name + "님의 " + gameMsg.message + "방", SwingConstants.CENTER);
        add(title, BorderLayout.NORTH);
        add(createUserSidePanel(), BorderLayout.WEST);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createRightPanel(), BorderLayout.EAST);
    }

    private JPanel createUserSidePanel(){
        JPanel panel = new JPanel();
//        panel.setLayout(new GridLayout(0, 1));  // 1열 4행으로 세로로 나열
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // 세로 방향 정렬

        panel.setBackground(Color.CYAN);

        // 현재 유저 목록을 JLabel로 표시
        for (String userName : userNames) {
            JPanel userPanel = createIndividualUserPanel(userName);
            userPanel.setMaximumSize(new Dimension(300, 100)); // 패널의 최대 크기 설정
            panel.add(userPanel);
            panel.add(Box.createRigidArea(new Dimension(0, 10))); // 간격 추가
//            panel.add(createIndividualUserPanel(userName));
        }

        return panel;
    }


    private JPanel createIndividualUserPanel(String userName) {
        JPanel panel = new JPanel(new GridLayout(1, 2));
//        panel.setBackground(Color.CYAN);

        JPanel leftPanel = new JPanel(new GridLayout(2, 1));
        JPanel leftTopPanel = new JPanel();
        leftTopPanel.add(new JLabel(userName + " 님"));
        leftTopPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2)); // 테두리
        leftPanel.add(leftTopPanel);

        JPanel leftBottomPanel = new JPanel();
        leftBottomPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2)); // 테두리
        leftPanel.add(leftBottomPanel);

        JPanel rightPanel = new JPanel();
        rightPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2)); // 테두리

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
        JLabel user = new JLabel("메인 패널");

        gamePanel.add(user);
        return gamePanel;
    }

    private JPanel ItemPanel(){
        JPanel itemPanel = new JPanel();
        itemPanel.setLayout(new BoxLayout(itemPanel, BoxLayout.Y_AXIS));
        itemPanel.setBackground(Color.lightGray);
        JLabel user = new JLabel("색깔 선택");

        itemPanel.add(user);
        return itemPanel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout()); // 위아래로 나눔

        // 위쪽 채팅 패널
        JPanel chatPanel = ChatPanel();
        panel.add(chatPanel, BorderLayout.NORTH);

        // 아래쪽 이모티콘 패널
        JPanel imgPanel = ImgPanel();
        panel.add(imgPanel, BorderLayout.SOUTH);

        return panel;
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

}
