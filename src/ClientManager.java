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
    private GameRoomPanel gameRoomPanel;

    private User user;
    private String userName;
    private String roomName;
    private Vector<User> userNames = new Vector<>();
    private Vector<User> readyUsers = new Vector<>();

    public ClientManager(String serverAddress, int serverPort, Client client) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.client = client;
        //this.gameRoomPanel = gameRoomPanel; // gameRoomPanel 설정
    }

//    public ClientManager(GameRoomPanel gameRoomPanel) {
//        this.gameRoomPanel = gameRoomPanel;
//    }
//
//    public void setGameRoomPanel(GameRoomPanel gameRoomPanel) {
//        this.gameRoomPanel = gameRoomPanel;
//    }

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
            disconnect(); // 스레드 종료 시 연결 해제
        }
    }

    private void receiveMessage() {
        try {
            GameMsg inMsg = (GameMsg) in.readObject();

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

                    case GameMsg.ROOM_SELECT:
                        user = inMsg.getUser();
                        roomName = inMsg.getMsg();
                        client.changeGameRoomPanel(inMsg);
                        synchronized (userNames) {
                            userNames = new Vector<>(inMsg.userNames); // 리스트 새로 갱신
                        }
                        synchronized (readyUsers) {
                            readyUsers = new Vector<>(inMsg.readyUsers);
                        }
                        client.updateUserToRoom(userNames);
                        client.updateReadyToRoom(readyUsers, null);

                        System.out.println("클라이언트 ROOM_SELECT 때 userNames : " + userNames);
//                        client.getGamePanel().clearLines();
                        break;

                    case GameMsg.ROOM_NEW_MEMBER:
                        System.out.println("새로운 유저 >" + inMsg.user.name + "가 들어옴");
//                        synchronized (userNames) {
////                            if (!userNames.contains(inMsg.user)) {
////                                userNames.add(inMsg.user);
////                                System.out.println("추가된 후 userNames : " + userNames);
////                            }
//                            userNames = new Vector<>(inMsg.userNames);
//                        }
//                        client.updateUserToRoom(userNames);
                        synchronized (userNames) {
                            userNames = new Vector<>(inMsg.userNames); // 리스트 새로 갱신
                        }
                        synchronized (readyUsers) {
                            readyUsers = new Vector<>(inMsg.readyUsers);
                        }
                        client.updateUserToRoom(userNames);
                        client.updateReadyToRoom(readyUsers, null);
                        break;

                    case GameMsg.ROOM_SELECT_DENIED:
                        client.showDialog(inMsg);
                        break;

                    case GameMsg.GAME_READY_AVAILABLE:
                        client.setReadyButtonVisibility(true);
                        break;

                    case GameMsg.GAME_READY_OK:
                        // readyUsers 업데이트
                        synchronized (readyUsers) {
                            readyUsers = new Vector<>(inMsg.readyUsers);
//                            if (!readyUsers.contains(inMsg.user)) { readyUsers.add(inMsg.user); }
//                            readyUsers = new Vector<>(inMsg.readyUsers);
                            System.out.println("readyUsers: " + readyUsers);
                        }
                        client.updateReadyToRoom(readyUsers, null);

                        // readyUsers 4명이 되면 게임 시작
                        if (readyUsers.size() == 4) {
                            System.out.println("겜 시작");
                            System.out.println("userNames : " + userNames);
                            // 첫 번째 사용자만 sendGameMsg 보내도록
                            User firstUser = readyUsers.get(0);
                            if (userName.equals(firstUser.name)) {
                                sendGameMsg(new GameMsg(GameMsg.GAME_START, readyUsers, userNames)); // 첫 번째 사용자만 실행
                            } else {
                                System.out.println("첫 번째 사용자가 아님, 메시지 전송 안 함");
                            }
                        }
                        break;

                    case GameMsg.GAME_UN_READY_OK:
                        System.out.println("GAME_UN_READY 받음 클라이언트");
                        synchronized (readyUsers) {
                            readyUsers = new Vector<>(inMsg.readyUsers != null ? inMsg.readyUsers : new Vector<>());
                            System.out.println("readyUsers: " + readyUsers);
                        }
//                        readyUsers.remove(inMsg.user);
                        client.updateReadyToRoom(readyUsers, inMsg.user);
                        break;

                    case GameMsg.LIAR_NOTIFICATION:
                        client.getGameRoomPanel().changeGameMsg(inMsg, inMsg.user.name);
                        client.startGame();
                        client.showDialog(inMsg);
                        break;

                    case GameMsg.KEYWORD_NOTIFICATION:
                        client.getGameRoomPanel().changeGameMsg(inMsg, userName);
                        client.startGame();
                        client.showDialog(inMsg);
                        break;

                    case GameMsg.TIME:
                        userNames = inMsg.userNames;
                        int remainingTime = inMsg.getTime(); // 서버에서 받은 남은 시간
                        User currentTurnUser = inMsg.getUser();

                        client.updateAlarmLabel(remainingTime); // 클라이언트 UI 갱신

                        if (currentTurnUser != null) {
                            if (!currentTurnUser.getName().equals(client.getGameRoomPanel().getCurrentTurnUserName())) {
                                client.getGameRoomPanel().updateTurnUser(currentTurnUser.getName());

                                if (currentTurnUser.getName().equals(userName)) {
                                    client.getGamePanel().setDrawingEnabled(true);
                                } else {
                                    client.getGamePanel().setDrawingEnabled(false);
                                }
                            }
                        }
                        //System.out.println("클라이언트: 남은 시간 업데이트 -> " + remainingTime + "초");

                        if (currentTurnUser != null) {
                            System.out.println("Time에서 userNames : " + userNames);
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
                            client.getGamePanel().setDrawingEnabled(false);
                            System.out.println("GameMsg.VOTE 수신. isVoteStart: " + inMsg.isVoteStart());
                            //client.startVote();
                            // 투표 모드 활성화
                            //gameRoomPanel.setVotingActive(true);
                            if (client.getGameRoomPanel() != null) {
                                client.getGameRoomPanel().setGameMsg(inMsg); // gameMsg를 설정하고 투표 상태를 제어
                                client.showDialog(inMsg); // 투표 시작 다이얼로그 표시
                            }else {
                                System.err.println("GameRoomPanel is null!");
                            }
                            //client.showVoteDialog(); // 투표 UI 표시
                        } else {
                            client.updateAlarmLabel(inMsg.getTime()); // 투표 타이머 업데이트
                        }
                        break;

//                    case GameMsg.VOTE_RESULT:
//                        //client.endVote(); // 투표 종료 처리
//                        break;

                    case GameMsg.GAME_END:
                        System.out.println("[DEBUG] GAME_END 메시지 수신!");
                        System.out.println("[DEBUG] inMsg.isWinner(): " + inMsg.isWinner());
                        System.out.println("[DEBUG] inMsg.getResultMessage(): " + inMsg.getResultMessage());

                        boolean isWinner = inMsg.isWinner();
                        String resultMessage = inMsg.getResultMessage();

                        // 결과 화면 표시
//                        client.getGameRoomPanel().showGameResult(isWinner, resultMessage);
                        client.endGame(isWinner, resultMessage);

//                        client.getGameRoomPanel().clearAllLeftBottomPanels();
//                        readyUsers = new Vector<>(); // 준비 초기화
//                        user.currentRoom.setReadyUsers(readyUsers);
//                        client.updateReadyToRoom(readyUsers, user);
//                        client.updateUserToRoom(userNames);
//                        client.getGameRoomPanel().clearAllLeftBottomPanels();
                        break;

                    case GameMsg.CHAT_MESSAGE:
                        System.out.println("클라이언트 CHAT_MESSAGE_OK : " + inMsg.user.name + "의 " + inMsg.message);
                        String chatUser = inMsg.user.name;
                        String chatMsg = inMsg.message;
                        client.getGameRoomPanel().showChat("[ " + chatUser + "] : " + chatMsg);
                        break;

                    case GameMsg.CHAT_EMOTICON:
                        System.out.println("클라이언트 CHAT_EMOTICON 받음 : " + inMsg.user.name + "의 " + inMsg.message);
                        client.updateEmoticonPanel(inMsg.user, inMsg.message);
                        break;

                    case GameMsg.ROOM_EXIT:
                        User exitUser = inMsg.user;
//                        if(inMsg.message.equals("finish")) {
//                            client.getGameRoomPanel().clearAllLeftBottomPanels();
//                        }
                        System.out.println("exit 때 remove 하기 전 updateUserToRoom : " + userNames);
                        userNames.remove(exitUser);
//                        exitUser.setCurrentRoom(null);
//                        readyUsers.remove(exitUser);
                        client.updateUserToRoom(userNames);
//                        client.updateReadyToRoom(readyUsers, inMsg.user);

                        if(exitUser.getName().equals(userName)) {
//                            client.updateUserToRoom(userNames);
                            client.changeSelectRoomPanel();
                        } else {
//                            client.updateUserToRoom(userNames);
                            if(readyUsers.size() < 4) {
                                client.setReadyButtonVisibility(false);
//                                client.restartGame();
                            }
                            if(readyUsers != null) { client.updateReadyToRoom(readyUsers, null); }
//                            sendGameMsg(new GameMsg(GameMsg.ROOM_NEW_MEMBER, user, null, userNames));
                        }
                        break;

                    case GameMsg.LOGOUT:
                        User logoutUser = inMsg.user;
                        userNames.remove(logoutUser);
                        readyUsers.remove(logoutUser);
                        client.updateReadyToRoom(readyUsers, inMsg.user);

                        if(logoutUser.getName().equals(userName)) {
                            client.updateUserToRoom(userNames);
                            client.changeStartPanel();
                        } else {
                            client.updateUserToRoom(userNames);
                            if(readyUsers.size() < 4) {
                                client.setReadyButtonVisibility(false);
                            }
                            if(readyUsers != null) { client.updateReadyToRoom(readyUsers, null); }
                        }
                        break;

                }
            });
        } catch (IOException e) {
            System.err.println("receiveMessage 서버 연결 종료: " + e.getMessage());
            disconnect();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void disconnect() {
        if(user != null) { sendGameMsg(new GameMsg(GameMsg.LOGOUT, user)); }
        try {
            if (in != null) { in.close(); }
            if (out != null) { out.close(); }
            if (socket != null && !socket.isClosed()) socket.close();
            receiveThread = null;
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
//        System.out.println("clientManage의 sendUnReady");
        sendGameMsg(new GameMsg(GameMsg.GAME_UN_READY, readyUser));
    }

    public void sendEmoticon(String emoticonName) {
//        System.out.println("clientManage의 sendEmoticon");
        sendGameMsg(new GameMsg(GameMsg.CHAT_EMOTICON, user, emoticonName));
    }

    public void sendVote(User user, String votedUserName) { sendGameMsg(new GameMsg(GameMsg.VOTE, user, votedUserName)); }

    public void sendLogout(User user) {
        sendGameMsg(new GameMsg(GameMsg.LOGOUT, user));
    }

    public void sendRoomExit(User user) {
        sendGameMsg(new GameMsg(GameMsg.ROOM_EXIT, user));
        // 2. 투표 상태 초기화
        client.getGameRoomPanel().resetVoteState();
        // 3. 라이어 상태 초기화
        user.currentRoom.setReadyUsers(new Vector<>()); // 준비 사용자 목록 초기화
        client.getGameRoomPanel().resetLiarState();
        client.getGamePanel().setDrawingEnabled(true); // 리스너까지 다시 등록
    }

    public void sendRetry(User user) {
        client.getGameRoomPanel().clearAllLeftBottomPanels();
//        user.currentRoom.setReadyUsers(new Vector<>());
//        sendGameMsg(new GameMsg(GameMsg.GAME_RETRY, user, readyUsers));
//        sendGameMsg(new GameMsg(GameMsg.ROOM_SELECT, user, user.currentRoom.getRoomName()));
//        sendGameMsg(new GameMsg(GameMsg.GAME_RETRY, user));
//        client.getGamePanel().removeAll();
//        client.getGameRoomPanel().remove(client.getGameRoomPanel().centerPanel);
//        client.getGameRoomPanel().add(client.getGamePanel().createCenterPanel());
        client.getGamePanel().clearLines();
//        client.getGameRoomPanel().remove(client.getGamePanel());
//        client.setGamePanel(this);

        // 2. 투표 상태 초기화
        client.getGameRoomPanel().resetVoteState();
        // 3. 라이어 상태 초기화
        user.currentRoom.setReadyUsers(new Vector<>()); // 준비 사용자 목록 초기화
        client.getGameRoomPanel().resetLiarState();

//        client.updateUserToRoom(userNames);
//        client.updateReadyToRoom(readyUsers, user);
        client.getGamePanel().setDrawingEnabled(true); // 리스너까지 다시 등록
        client.getGameRoomPanel().rightPannel.remove(client.getGameRoomPanel().alarmPanel);
        System.out.println("sendRetry usernames 수 : " + userNames.size());

        sendGameMsg(new GameMsg(GameMsg.GAME_RETRY, user, readyUsers));
        sendGameMsg(new GameMsg(GameMsg.ROOM_SELECT, user, roomName));

        client.setReadyButtonVisibility(true);
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
