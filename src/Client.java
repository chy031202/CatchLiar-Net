import javax.swing.*;
import java.util.Vector;

public class Client extends JFrame {

    private ClientManager clientManager;
    private StartPanel startPanel;
    private SelectRoomPanel selectRoomPanel;
    private GameRoomPanel gameRoomPanel;
    private GamePanel gamePanel;

    public Client(String serverAddress, int serverPort){
        super("캐치 라이어");
        clientManager = new ClientManager(serverAddress, serverPort, this);

        startPanel = new StartPanel(clientManager);
        selectRoomPanel = new SelectRoomPanel(clientManager);
        gamePanel = new GamePanel(clientManager);

        buildGUI();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void buildGUI() {
        setBounds(50, 200, 800, 600);
        // JPanel을 생성하고 BoxLayout을 설정
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        mainPanel.add(startPanel);

        // mainPanel을 JFrame의 ContentPane으로 설정
        setContentPane(mainPanel);

    }

    public void changeSelectRoomPanel() {
        getContentPane().removeAll();
        getContentPane().add(selectRoomPanel);

        revalidate();
        repaint();
    }

    public void changeGameRoomPanel(GameMsg inMsg) {
        getContentPane().removeAll();
        gameRoomPanel = new GameRoomPanel(clientManager, inMsg);
        getContentPane().add(gameRoomPanel);

        revalidate();
        repaint();
    }

    public void updateUserToRoom(Vector<User> userNames) {
        if(gameRoomPanel != null) {
            System.out.println("updateUserToRoom : " + userNames);
            gameRoomPanel.updateUser(userNames);
        }
    }

    public void updateReadyToRoom(Vector<User> readyUsers, User user) {
        if(gameRoomPanel != null) {
            System.out.println("updateReady");
            gameRoomPanel.updateReadyUser(readyUsers, user);
        }
    }

    public void showReadyButton() {
        if(gameRoomPanel != null) {
            System.out.println("showReadyButton");
//            gameRoomPanel.ready = true;
            gameRoomPanel.settingReady();
        }
    }

    public void startGame() {
        if(gameRoomPanel != null) {
            System.out.println("startGame");
            gameRoomPanel.refreshStartGame();
        }
    }

    public void showDialog(GameMsg inMsg) {
        switch (inMsg.mode) {
            case GameMsg.ROOM_SELECT_DENIED:
                JOptionPane.showMessageDialog(
                        this,
                        "방이 꽉 찼습니다. 다른 방으로 입장해주세요!", // 메시지
                        "알림",              // 제목
                        JOptionPane.WARNING_MESSAGE // 경고 아이콘
                );
                break;

            case GameMsg.GAME_READY_OK:
                JOptionPane.showMessageDialog(
                        this,
                        "게임이 시작됩니다! (개발용 => 추후 삭제)", // 메시지
                        "알림",              // 제목
                        JOptionPane.PLAIN_MESSAGE // 경고 아이콘
                );
                break;

            case GameMsg.LIAR_NOTIFICATION:
                JOptionPane.showMessageDialog(
                        this,
                        "당신은 라이어입니다!", // 메시지
                        "알림",              // 제목
                        JOptionPane.PLAIN_MESSAGE // 경고 아이콘
                );
                break;

            case GameMsg.KEYWORD_NOTIFICATION:
                JOptionPane.showMessageDialog(
                        this,
                        "키워드 : " + inMsg.message, // 메시지
                        "알림",              // 제목
                        JOptionPane.PLAIN_MESSAGE // 경고 아이콘
                );
                break;

            default:
                System.out.println("showDialog default : " + inMsg);
                break;
        }
    }

    public GameRoomPanel getGameRoomPanel() {
        return gameRoomPanel;
    }

    public GamePanel getGamePanel() {
        if (gamePanel == null) {
            System.out.println("GamePanel is null!");
        }
        return gamePanel;
    }

    public void updateAlarmLabel(int remainingTime) {
        if (gameRoomPanel != null) {
            gameRoomPanel.updateAlarmLabel(remainingTime);
        }
    }

    public static void main(String[] args) {
        String serverAddress = "localhost";
        int serverPort = 54321;
        Client client = new Client(serverAddress, serverPort);
    }

    public String getUserName() {
        if (clientManager != null && clientManager.getUser() != null) {
            return clientManager.getUser().getName(); // ClientManager의 User 객체에서 이름 반환
        }
        return null; // 사용자 정보가 없을 경우
    }
}
