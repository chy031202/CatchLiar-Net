import javax.swing.*;
import java.awt.*;
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

    private GamePanel gamePanel;

    private User user;
    private String userName;
    private Vector<User> userNames = new Vector<>();
    private Vector<User> readyUsers = new Vector<>();

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
//                        client.changeGameRoomPanel(inMsg, user.currentRoom.getReadyUsers());
                        client.changeGameRoomPanel(inMsg);
                        userNames = user.currentRoom.getMembers();
                        System.out.println("클라이언트 ROOM_SELECT_OK 이후 userNames 세팅 : " + userNames);
//                        readyUsers = user.currentRoom.getReadyUsers();
                        client.getGamePanel().clearLines();
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

                    case GameMsg.ROOM_SELECT_DENIED:
//                      user = inMsg.getUser();
                        client.showDialog(inMsg);
                        break;

                    case GameMsg.GAME_READY_AVAILABLE:
                        client.showReadyButton();
                        break;

                    case GameMsg.GAME_READY_OK:
                        if(!readyUsers.contains(inMsg.user)) {
                            readyUsers.add(inMsg.user);
                        }
//                        readyUsers = inMsg.readyUsers;
                        client.updateReadyToRoom(readyUsers, null);
                        // readyUsers 4명되면 게임 시작
                        if(readyUsers.size() == 4) {
                            System.out.println("겜 시작");
                            // 첫 번째 사용자만 sendGameMsg 보내도록
                            User firstUser = readyUsers.get(0);
                            if (userName.equals(firstUser.name)) {
                                sendGameMsg(new GameMsg(GameMsg.GAME_START, readyUsers)); // 첫 번째 사용자만 실행
                            } else {
                                System.out.println("첫 번째 사용자가 아님, 메시지 전송 안 함");
                            }
                        }
                        break;

                    case GameMsg.GAME_UN_READY_OK:
                        System.out.println("GAME_UN_READY 받음 클라이언트");
                        readyUsers.remove(inMsg.user);
                        client.updateReadyToRoom(readyUsers, inMsg.user);
                        break;

                    case GameMsg.LIAR_NOTIFICATION:
                        client.showDialog(inMsg);
                        client.startGame();
                        break;

                    case GameMsg.KEYWORD_NOTIFICATION:
                        client.startGame();
                        client.showDialog(inMsg);
                        break;

                    case GameMsg.TIME:
                        int remainingTime = inMsg.getTime(); // 서버에서 받은 남은 시간
                        User currentTurnUser = inMsg.getUser();

                        client.updateAlarmLabel(remainingTime); // 클라이언트 UI 갱신

                         //자신의 턴 여부 확인
                        if (currentTurnUser != null && currentTurnUser.getName().equals(client.getUserName())) {
                            client.getGamePanel().setDrawingEnabled(true); // 그림 그리기 활성화
                        } else {
                            client.getGamePanel().setDrawingEnabled(false); // 그림 그리기 비활성화
                        }
                        //System.out.println("클라이언트: 남은 시간 업데이트 -> " + remainingTime + "초");

                        if (currentTurnUser != null) {
                            client.getGameRoomPanel().updateTurnUser(currentTurnUser.getName());
                        }

                        // 알람 UI 업데이트
                        //client.updateAlarmLabel(remainingTime);

                        break;

                    case GameMsg.DRAW_ACTION:
                        Paint paintData = inMsg.getPaintData();

                        //드로잉 확인 콘솔
//                        System.out.println("DRAW_ACTION 수신: " +
//                                "시작(" + paintData.getStartX() + ", " + paintData.getStartY() +
//                                "), 끝(" + paintData.getEndX() + ", " + paintData.getEndY() +
//                                "), 색상: " + paintData.getColor() +
//                                ", 지우개 모드: " + paintData.isErasing());
                        client.getGamePanel().receiveRemoteDrawing(
                                paintData.getStartX(),
                                paintData.getStartY(),
                                paintData.getEndX(),
                                paintData.getEndY(),
                                paintData.getColor()
                        );
                        break;

                    case GameMsg.VOTE:
                        if (inMsg.isVoteStart()) {
                            client.showDialog(inMsg); // 투표 시작 다이얼로그
                            client.startVote();
                            //client.showVoteDialog(); // 투표 UI 표시
                        } else {
                            client.updateAlarmLabel(inMsg.getTime()); // 투표 타이머 업데이트
                        }


                    case GameMsg.CHAT_MESSAGE_OK:
                        System.out.println("클라이언트 CHAT_MESSAGE_OK : " + inMsg.user.name + "의 " + inMsg.message);
                        String chatUser = inMsg.user.name;
                        String chatMsg = inMsg.message;
                        client.getGameRoomPanel().showChat("[ " + chatUser + "] : " + chatMsg);
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

    //투표 다이얼로그
    private void showVoteDialog() {

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

    public void sendChat(String message) {
        System.out.println("clientManage의 sendChat : " + message );
        sendGameMsg(new GameMsg(GameMsg.CHAT_MESSAGE, user, message));
    }

    public void sendReady(User readyUser) {
        System.out.println("clientManage의 sendReady");
        sendGameMsg(new GameMsg(GameMsg.GAME_READY, readyUser));
    }

    public void sendUnReady(User readyUser) {
        System.out.println("clientManage의 sendUnReady");
        sendGameMsg(new GameMsg(GameMsg.GAME_UN_READY, readyUser));
    }

    public User getUser() {
        return this.user; // 현재 사용자 객체 반환
    }

    public void sendDrawingData(int startX, int startY, int endX, int endY, Color color, boolean isErasing) {
        if (out == null) {
            System.err.println("출력 스트림이 초기화되지 않았습니다. 데이터를 전송할 수 없습니다.");
            return;
        }
        try {
            // Color를 RGB 값으로 변환
            //int colorRGB = color.getRGB();
            // Paint 객체 생성 시 현재 색상과 지우개 상태 전달
            //System.out.println("전송 데이터 디버깅: 색상 RGB - " + color.getRGB());
            Paint paintData = new Paint(startX, startY, endX, endY, color, isErasing);
            GameMsg msg = new GameMsg(GameMsg.DRAW_ACTION, paintData);
            
            //드로잉 관련
//            System.out.println(String.format(
//                    "클라이언트 전송 - 시작(%d, %d), 끝(%d, %d), 색상: R:%d, G:%d, B:%d, 지우개: %b",
//                    startX, startY, endX, endY,
//                    color.getRed(), color.getGreen(), color.getBlue(),
//                    isErasing
//            ));
            sendGameMsg(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
