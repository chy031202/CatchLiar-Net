import javax.swing.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

public class Client extends JFrame {

    private ClientManager clientManager;
    private StartPanel startPanel;
    private SelectRoomPanel selectRoomPanel;
    private GameRoomPanel gameRoomPanel;
    private GamePanel gamePanel;

    public Client(){
        super("캐치 라이어");

        // 서버 설정 파일 읽기
        String[] serverConfig = readServerConfig();
        String serverAddress = serverConfig[0];
        int serverPort = Integer.parseInt(serverConfig[1]);

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

    public void restartGame() {
        gameRoomPanel.refreshReadyGame();
    }

    public void endGame(boolean isWinner, String resultMessage) {
        if(gameRoomPanel != null) {
            System.out.println("endGame");
            gameRoomPanel.showGameResult(isWinner, resultMessage);
            gameRoomPanel.refreshEndGame();
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

    public void setGamePanel(ClientManager clientManager) {
        getGameRoomPanel().gamePanel = new GamePanel(clientManager);
    }

    public void updateAlarmLabel(int remainingTime) {
        if (gameRoomPanel != null) {
            gameRoomPanel.updateAlarmLabel(remainingTime);
        }
    }

    private String[] readServerConfig() {
        String defaultAddress = "localhost";
        int defaultPort = 54321;

        try (BufferedReader br = new BufferedReader(new FileReader("server.txt"))) {
            String serverAddress = br.readLine(); // 첫 번째 줄: 서버 주소
            String serverPort = br.readLine();    // 두 번째 줄: 포트 번호
            return new String[]{serverAddress, serverPort};
        } catch (IOException e) {
            System.err.println("서버 설정 파일을 읽을 수 없습니다. 기본 설정을 사용합니다.");
            return new String[]{defaultAddress, String.valueOf(defaultPort)};
        }
    }

    public static void main(String[] args) {
        // 서버 주소와 포트를 파일에서 읽음
        new Client();
    }

}
