import javax.swing.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Vector;

public class ClientManager {
    private String serverAddress;
    private int serverPort;
    private Client client;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Thread receiveThread;

    private User user;
    private String userName;
    private Vector<User> userNames = new Vector<>();

    public ClientManager(String serverAddress, int serverPort, Client client) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.client = client;
    }

    public void connectToServer() throws IOException {
        socket = new Socket();
        SocketAddress sa = new InetSocketAddress(serverAddress, serverPort);
        socket.connect(sa, 3000);

        out = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        out.flush();
        in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));

        receiveThread = new Thread(this::run);
        receiveThread.start();
    }

    private void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                receiveMessage();
            }
        } catch (Exception e) {
            System.err.println("메시지 수신 중 오류 발생: " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    private void receiveMessage() {
        try {
            GameMsg inMsg = (GameMsg) in.readObject();
//            user = inMsg.getUser();

            if (user == null) {
                System.err.println("클라이언트에서 받은 User 객체가 null입니다.");
            } else {
//                System.out.println("User currentRoom: " +user.getName() + ", 현재 방 : " + user.getCurrentRoom().getRoomName()); // currentRoom 확인
            }

            if (inMsg == null) {
                disconnect();
                System.err.println("receiveMessage 서버 연결 끊김");
                return;
            }
            SwingUtilities.invokeLater(() -> {
                switch (inMsg.mode) {
                    case GameMsg.LOGIN_OK:
                        user = inMsg.getUser(); // user 여기에서 저장해야 유지됨
                        client.changeSelectRoomPanel();
                        System.out.println("클라이언트 receiveMessage 로그인OK: " + inMsg.mode + "," + inMsg.user.name);
                        break;
                    case GameMsg.ROOM_SELECT_OK:
                        user = inMsg.getUser();
                        System.out.println("클라이언트 receiveMessage 방선택OK : " + inMsg.mode + "," + inMsg.user.name + "," + inMsg.message);
                        client.changeGameRoomPanel(inMsg);
                        userNames = user.currentRoom.getMembers();
                        System.out.println("클라이언트 ROOM_SELECT_OK 이후 userNames 세팅 : " + userNames);
                        break;
                    case GameMsg.ROOM_NEW_MEMBER:
                        System.out.println("새로운 유저 >" + inMsg.user.name + "가 들어옴");
                        System.out.println("추가되기 전 userNames : " + userNames);
//                        userNames = inMsg.getUser().currentRoom.getMembers();
//                        System.out.println("추가된 후 userNames : " + userNames);
//                        client.updateUserToRoom(userNames);
                        // 새로들어온 유저의 user.getCurrentRoom.getMembers를 userNames에 넣어. 그러고 client.업데이트함수 불러서 그걸로 userData 업데이트하게해
                        if (!userNames.contains(inMsg.user)) { // 목록에 없는 유저가 들어올 때만 리프레쉬
                            userNames.add(inMsg.user);
                            inMsg.user.currentRoom.setMembers(userNames);
                            System.out.println("추가된 후 userNames : " + userNames);
                        }
                        client.updateUserToRoom(userNames);
                        break;
                    //채팅 모드 등...
                    case GameMsg.CHAT_MESSAGE:
                        System.out.println("receiveMessage 서버로부터 메시지 수신: ");
                        break;
                    //이모티콘 전송 모드 등...
//                case GameMsg.MODE_TX_IMAGE :
//                    printDisplay(inMsg.userID + ": " + inMsg.message);
//                    printDisplay(inMsg.image);
//                    break;
                }
            });
        } catch (IOException e) {
            System.err.println("receiveMessage 서버 연결 종료: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void disconnect() {
        sendGameMsg(new GameMsg(GameMsg.LOGOUT, userName));
        try {
            receiveThread = null;
            socket.close();
        } catch (IOException e) {
            System.err.println("클라이언트 disconnect 닫기 오류> "+e.getMessage());
            System.exit(-1);
        }
    }

    void sendGameMsg(GameMsg msg) {
        try {
            if (out != null) {
                out.writeObject(msg); // 객체 전송
                out.flush();
            } else {
                System.err.println("sendGameMsg 출력 스트림이 초기화되지 않았습니다.");
            }
        } catch (IOException e) {
            System.err.println("클라이언트 sendGameMsg 전송 오류: " + e.getMessage());
        }
    }

    public void sendNickname(String nickname) {
        this.userName = nickname;
        sendGameMsg(new GameMsg(GameMsg.LOGIN, userName));
    }

    public void sendRoomSelection(String roomName) {
        sendGameMsg(new GameMsg(GameMsg.ROOM_SELECT, user, roomName));
    }

}
