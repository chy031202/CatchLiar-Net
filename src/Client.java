import javax.swing.*;
import java.util.Map;
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
        setBounds(50, 200, 700, 500);
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
//        clientManager.setGameRoomPanel(gameRoomPanel);
        getContentPane().add(gameRoomPanel);

        revalidate();
        repaint();
    }

    public void changeStartPanel() {
        try {
            // 새로운 ClientManager 생성
            clientManager = new ClientManager("localhost", 54321, this);
            startPanel = new StartPanel(clientManager);
            selectRoomPanel = new SelectRoomPanel(clientManager);

            getContentPane().removeAll();
            getContentPane().add(startPanel);

            revalidate();
            repaint();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "초기화 중 문제가 발생했습니다!", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 방 유저 목록 업데이트
    public void updateUserToRoom(Vector<User> userNames) {
        if(gameRoomPanel != null) {
//            System.out.println("updateUserToRoom : " + userNames);
            gameRoomPanel.updateUser(userNames);
        }
    }

    // 방 준비 완료한 유저 목록 업데이트
    public void updateReadyToRoom(Vector<User> readyUsers, User user) {
        if(gameRoomPanel != null) {
//            System.out.println("updateReady");
            gameRoomPanel.updateReadyUser(readyUsers, user);
        }
    }

    // 이모티콘 출력
    public void updateEmoticonPanel(User user, String emoticon) {
        if(gameRoomPanel != null) {
//            System.out.println("updateEmoticon");
            gameRoomPanel.refreshUserRightPanel(user, emoticon);
        }
    }

    public void setReadyButtonVisibility(boolean visible) {
        if (gameRoomPanel != null) {
            if (visible) {
                gameRoomPanel.settingReady();
            } else {
                gameRoomPanel.settingUnReady();
            }
        }
    }

    public void startGame() {
        if(gameRoomPanel != null) {
            System.out.println("startGame");
            gameRoomPanel.refreshStartGame();
        }
    }

    public void showDialog(GameMsg inMsg) {
        String message = "";
        String title = "알림";
        int messageType = JOptionPane.PLAIN_MESSAGE;

        switch (inMsg.mode) {
            case GameMsg.ROOM_SELECT_DENIED:
                message = "방이 꽉 찼습니다. 다른 방으로 입장해주세요!";
                messageType = JOptionPane.WARNING_MESSAGE;
                break;
            case GameMsg.GAME_READY_OK:
                message = "게임이 시작됩니다! (개발용 => 추후 삭제)";
                break;
            case GameMsg.LIAR_NOTIFICATION:
                message = "당신은 라이어입니다!";
                break;
            case GameMsg.KEYWORD_NOTIFICATION:
                message = "키워드 : " + inMsg.message;
                break;
            case GameMsg.VOTE:
                message = "라이어 투표를 시작합니다.";
                gameRoomPanel.clearAllLeftBottomPanels();
                break;
            default:
                System.out.println("showDialog default : " + inMsg);
                return;
        }

        JOptionPane.showMessageDialog(this, message, title, messageType);
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

}
