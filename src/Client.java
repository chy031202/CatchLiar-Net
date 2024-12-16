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
