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
        setContentPane(mainPanel); // mainPanel을 JFrame의 ContentPane으로 설정
    }

    // 방 선택 화면으로 전환
    public void changeSelectRoomPanel() {
        getContentPane().removeAll();
        getContentPane().add(selectRoomPanel);

        revalidate();
        repaint();
    }

    // 게임 화면으로 전환
    public void changeGameRoomPanel(GameMsg inMsg) {
        getContentPane().removeAll();
        gameRoomPanel = new GameRoomPanel(clientManager, inMsg);
        getContentPane().add(gameRoomPanel);

        revalidate();
        repaint();
    }

    // 시작 화면으로 전환
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

    // 게임 시작 화면으로 리프레쉬
    public void startGame() {
        if(gameRoomPanel != null) {
            System.out.println("startGame");
            gameRoomPanel.refreshStartGame();
        }
    }

    // 게임 종료 화면으로 리프레쉬
    public void endGame(boolean isWinner, String resultMessage) {
        if(gameRoomPanel != null) {
            System.out.println("endGame");
            gameRoomPanel.showGameResult(isWinner, resultMessage);
            gameRoomPanel.refreshEndGame();
        }
    }

    // 방 유저 목록 업데이트
    public void updateUserToRoom(Vector<User> userNames) {
        if(gameRoomPanel != null) {
            gameRoomPanel.updateUser(userNames);
        }
    }

    // 준비 완료 유저 목록 업데이트
    public void updateReadyToRoom(Vector<User> readyUsers, User user) {
        if(gameRoomPanel != null) {
            gameRoomPanel.updateReadyUser(readyUsers, user);
        }
    }

    // 이모티콘 출력
    public void updateEmoticonPanel(User user, String emoticon) {
        if(gameRoomPanel != null) {
            gameRoomPanel.refreshUserRightPanel(user, emoticon);
        }
    }

    // 타이머 출력
    public void updateAlarmLabel(int remainingTime) {
        if (gameRoomPanel != null) {
            gameRoomPanel.updateAlarmLabel(remainingTime);
        }
    }

    // 방인원 다 쳤을 때만 준비 패널 출력
    public void setReadyButtonVisibility(boolean visible) {
        if (gameRoomPanel != null) {
            if (visible) {
                gameRoomPanel.settingReady();
            } else {
                gameRoomPanel.settingUnReady();
            }
        }
    }

    // 다이얼로그
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

    public static void main(String[] args) {
        String serverAddress = "localhost";
        int serverPort = 54321;
        Client client = new Client(serverAddress, serverPort);
    }

}
