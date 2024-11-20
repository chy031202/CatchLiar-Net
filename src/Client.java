import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.Socket;

public class Client {
    private JFrame frame;
    private Socket socket;
    private JTextField t_nickname;
    private JButton b_start;
    private JPanel mainPanel;

    public static void main(String[] args) {
        String serverAddress = "localhost";
        int serverPort = 54321;
        Client client = new Client(serverAddress, serverPort);
        client.buildGUI();
        client.frame.setVisible(true);
    }

    public Client(String serverAddress, int serverPort){
        frame = new JFrame("Client");

//        this.serverAddress = serverAddress;
//        this.serverPort = serverPort;
        frame.setBounds(50, 200, 600, 400);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void buildGUI() {
        //패널 관리를 위한 카드레이아웃
        mainPanel = new JPanel(new CardLayout());

        JPanel displaypanel = createDisplayPanel();
        mainPanel.add(displaypanel, "displayPanel");

        // Select Room Panel 생성 및 추가
        SelectRoomPanel selectRoomPanel = new SelectRoomPanel();
        mainPanel.add(selectRoomPanel, "selectRoomPanel");

        GameRoomPanel gameRoomPanel = new GameRoomPanel();
        mainPanel.add(gameRoomPanel, "gameRoomPanel");

        frame.add(mainPanel);

    }
    private JPanel createDisplayPanel(){
        JPanel display = new JPanel();
        display.setLayout(new BoxLayout(display, BoxLayout.Y_AXIS));

        //캐치라이어 제목. 나중에 이미지로 뽑아서
        //바꿀것 
        JLabel Title = new JLabel("캐치라이어");
        Title.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        
        JLabel nicknameLabel = new JLabel("닉네임");
        nicknameLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT); //중앙정렬

        t_nickname = new JTextField(15);
        t_nickname.setMaximumSize(t_nickname.getPreferredSize()); // 입력 필드의 크기를 고정
        t_nickname.setAlignmentX(JTextField.CENTER_ALIGNMENT); // 중앙 정렬

        b_start = new JButton("시작");
        b_start.setAlignmentX(JButton.CENTER_ALIGNMENT);

        //방 선택 화면 전환
        //소켓 연결 x
        b_start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CardLayout cl = (CardLayout)mainPanel.getLayout();
                cl.show(mainPanel, "selectRoomPanel");
            }
        });

        display.add(Title);
        //display.add(Box.createVerticalStrut(10)); // 위쪽 여백
        display.add(b_start);
        display.add(Box.createVerticalStrut(50));
        display.add(nicknameLabel);
        display.add(t_nickname);
        return display;
    }
    




}
