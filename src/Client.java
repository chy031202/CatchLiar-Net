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
        setBounds(50, 200, 600, 400);
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

    public void updateUserToRoom(Vector<String> userNames) {
        if(gameRoomPanel != null) {
            System.out.println("updateUserToRoom : " + userNames);
            gameRoomPanel.updateUser(userNames);
        }
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
