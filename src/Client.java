import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

public class Client {

    Server server = new Server(0);
    private JFrame frame;
    private Socket socket;
    private String uid;

    private JTextField t_nickname;
    private JTextField t_hostAddr;
    private JTextField t_portNum;

    private Thread receiveThread = null;

    private JButton b_start;
    private JPanel mainPanel;

    private String serverAddress ="localhost";
    private int serverPort;

    public static ObjectOutputStream out = null; // 객체 출력 스트림
    private ObjectInputStream in = null;

    public static void main(String[] args) {
        String serverAddress = "localhost";
        int serverPort = 54321;
        Client client = new Client(serverAddress, serverPort);
        client.buildGUI();
        client.frame.setVisible(true);
    }

    public Client(String serverAddress, int serverPort){
        frame = new JFrame("Client");

        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        frame.setBounds(50, 200, 600, 400);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void buildGUI() {
        //패널 관리를 위한 카드레이아웃
        mainPanel = new JPanel(new CardLayout());
        //GameRoomPanel gameRoomPanel = new GameRoomPanel(frame);

        JPanel displaypanel = createDisplayPanel();
        mainPanel.add(displaypanel, "displayPanel");

        // Select Room Panel 생성 및 추가
        SelectRoomPanel selectRoomPanel = new SelectRoomPanel(this);
        mainPanel.add(selectRoomPanel, "selectRoomPanel");

//        GameRoomPanel gameRoomPanel = new GameRoomPanel(frame);
//        mainPanel.add(gameRoomPanel, "gameRoomPanel");

        frame.add(mainPanel);

    }
    private JPanel createDisplayPanel(){
        JPanel display = new JPanel();
        display.setLayout(new BoxLayout(display, BoxLayout.Y_AXIS));

        t_hostAddr = new JTextField("localhost", 15);
        t_portNum = new JTextField("54321", 15);

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

        //방 선택 화면
        //소켓 연결 와뇰
        b_start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String nickname = t_nickname.getText();
                    if (nickname.isEmpty()) {
                        JOptionPane.showMessageDialog(frame, "닉네임을 입력해주세요!", "경고", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    Client.this.serverAddress = t_hostAddr.getText();
                    Client.this.serverPort = Integer.parseInt(t_portNum.getText());

                    connectToServer();
                    sendNickname(); // 닉네임 서버에 전송

                    //방 선택 패널 이동
                    CardLayout cl = (CardLayout)mainPanel.getLayout();
                    cl.show(mainPanel, "selectRoomPanel");
                } catch (IOException ex) {
                    //throw new RuntimeException(ex);
                    System.err.println(ex.getMessage());
                    System.exit(-1);
                }

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


//    private Writer out = null;
//private void printDisplay(ImageIcon icon) {
//    t_display.setCaretPosition(t_display.getDocument().getLength());
//
//    if (icon.getIconWidth() > 400) {
//        Image img = icon.getImage();
//        Image changeImg = img.getScaledInstance(400, -1, Image.SCALE_SMOOTH);
//        icon = new ImageIcon(changeImg);
//    }
//
//    t_display.insertIcon(icon);
//
//    printDisplay("");
//    t_input.setText("");
//}

    private void connectToServer() throws UnknownHostException, IOException {

        socket = new Socket();
        SocketAddress sa = new InetSocketAddress(serverAddress, serverPort);
        socket.connect(sa, 3000);

        out = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        out.flush();
        in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));

        receiveThread = new Thread(() -> receiveMessage());
        receiveThread.start();
    }

    private void receiveMessage() {
        try {
            GameMsg inMsg = (GameMsg) in.readObject();
            //String message;
//            while ((message = in.readObject()) != null) {
//                System.out.println("서버로부터 메시지 수신: " + message);
//            }
            if (inMsg == null) {
                disconnect();
                System.err.println("서버 연결 끊김");
                return;
            }
            switch (inMsg.mode) {
                //채팅 모드 등...
                case GameMsg.CHAT_MESSAGE:
                    System.out.println("서버로부터 메시지 수신: ");
                    break;
                    //이모티콘 전송 모드 등...
//                case GameMsg.MODE_TX_IMAGE :
//                    printDisplay(inMsg.userID + ": " + inMsg.message);
//                    printDisplay(inMsg.image);
//                    break;
            }
        } catch (IOException e) {
            System.err.println("서버 연결 종료: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void disconnect() {
        send(new GameMsg(uid, GameMsg.EXIT));
        try {
            receiveThread = null;
            socket.close();
        } catch (IOException e) {
            System.err.println("클라이언트 닫기 오류> "+e.getMessage());
            System.exit(-1);
        }
    }
    void send(GameMsg msg) {
        try {
            if (out != null) {
                out.writeObject(msg); // 객체 전송
                out.flush();
            } else {
                System.err.println("출력 스트림이 초기화되지 않았습니다.");
            }
        } catch (IOException e) {
            System.err.println("클라이언트 일반 전송 오류: " + e.getMessage());
        }
    }

//    private void sendNickname(String nickname) {
//        try {
//            if (out != null) {
//                out.write("/uid:" + nickname + "\n");
//                out.flush();
//            }
//        } catch (IOException e) {
//            System.err.println("닉네임 전송 오류: " + e.getMessage());
//        }
//    }

    //객체전송으로 바꿈
    private void sendNickname() {
        uid = t_nickname.getText();
        send(new GameMsg(uid, GameMsg.LOGIN));
    }

    public String getUid() {
        return uid;
    }

}
