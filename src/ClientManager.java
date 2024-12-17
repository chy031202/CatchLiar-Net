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

    private User user;
    private String userName;
    private String roomName;
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
            SwingUtilities.invokeLater(() -> handleMessage(inMsg));
        } catch (IOException e) {
            System.err.println("receiveMessage 서버 연결 종료: " + e.getMessage());
            disconnect();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleMessage(GameMsg inMsg) {
        switch (inMsg.mode) {
            case GameMsg.LOGIN_OK:
                handleLoginOk(inMsg);
                break;
            case GameMsg.ROOM_SELECT:
                handleRoomSelect(inMsg);
                break;
            case GameMsg.ROOM_NEW_MEMBER:
                handleRoomNewMember(inMsg);
                break;
            case GameMsg.ROOM_SELECT_DENIED:
                handleRoomSelectDenied(inMsg);
                break;
            case GameMsg.GAME_READY_AVAILABLE:
                handleGameReadyAvailable();
                break;
            case GameMsg.GAME_READY_OK:
                handleGameReadyOk(inMsg);
                break;
            case GameMsg.GAME_UN_READY_OK:
                handleGameUnReadyOk(inMsg);
                break;
            case GameMsg.LIAR_NOTIFICATION:
                handleLiarNotification(inMsg);
                break;
            case GameMsg.KEYWORD_NOTIFICATION:
                handleKeywordNotification(inMsg);
                break;
            case GameMsg.TIME:
                handleTime(inMsg);
                break;
            case GameMsg.DRAW_ACTION:
                handleDrawAction(inMsg);
                break;
            case GameMsg.VOTE:
                handleVote(inMsg);
                break;
            case GameMsg.GAME_END:
                handleGameEnd(inMsg);
                break;
            case GameMsg.CHAT_MESSAGE:
                handleChatMessage(inMsg);
                break;
            case GameMsg.CHAT_EMOTICON:
                handleChatEmoticon(inMsg);
                break;
            case GameMsg.ROOM_EXIT:
                handleRoomExit(inMsg);
                break;
            case GameMsg.ROOM_EXIT_OK:
                handleRoomExitOk();
                break;
            case GameMsg.LOGOUT:
                handleLogout();
                break;
            default:
                System.err.println("처리할 수 없는 메시지 유형: " + inMsg.mode);
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

    private void handleLoginOk(GameMsg inMsg) {
        user = inMsg.getUser(); // user 여기에서 저장해야 유지됨
        client.changeSelectRoomPanel();
        System.out.println("클라이언트 receiveMessage 로그인OK: " + inMsg.mode + "," + inMsg.user.name);
    }

    private void handleRoomSelect(GameMsg inMsg) {
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
        client.getGamePanel().clearLines();
    }

    private void handleRoomNewMember(GameMsg inMsg) {
        System.out.println("새로운 유저 >" + inMsg.user.name + "가 들어옴");
        synchronized (userNames) {
            userNames = new Vector<>(inMsg.userNames); // 리스트 새로 갱신
        }
        synchronized (readyUsers) {
            readyUsers = new Vector<>(inMsg.readyUsers);
        }
        client.updateUserToRoom(userNames);
        client.updateReadyToRoom(readyUsers, null);
    }

    private void handleRoomSelectDenied(GameMsg inMsg) {
        client.showDialog(inMsg);
    }

    private void handleGameReadyAvailable() {
        client.setReadyButtonVisibility(true);
    }

    private void handleGameReadyOk(GameMsg inMsg) {
        synchronized (readyUsers) {
            readyUsers = new Vector<>(inMsg.readyUsers);
            System.out.println("readyUsers: " + readyUsers);
        }
        client.updateReadyToRoom(readyUsers, null);

        // readyUsers 4명이 되면 게임 시작
        if (readyUsers.size() == 4) {
            System.out.println("겜 시작");
            // 한 명만 '게임 시작' 서버에 보내도록
            User firstUser = readyUsers.get(0);
            if (userName.equals(firstUser.name)) {
                sendGameMsg(new GameMsg(GameMsg.GAME_START, user, userNames, readyUsers)); // 첫 번째 사용자만 실행
            } else {
                System.out.println("첫 번째 사용자가 아님, 메시지 전송 안 함");
            }
        }
    }

    private void handleGameUnReadyOk(GameMsg inMsg) {
        synchronized (readyUsers) {
            readyUsers = new Vector<>(inMsg.readyUsers != null ? inMsg.readyUsers : new Vector<>());
            System.out.println("readyUsers: " + readyUsers);
        }
        client.updateReadyToRoom(readyUsers, inMsg.user);
    }

    private void handleLiarNotification(GameMsg inMsg) {
        client.getGameRoomPanel().changeGameMsg(inMsg, inMsg.user.name);
        client.startGame();
        client.showDialog(inMsg);
    }

    private void handleKeywordNotification(GameMsg inMsg) {
        client.getGameRoomPanel().changeGameMsg(inMsg, userName);
        client.startGame();
        client.showDialog(inMsg);
    }

    private void handleTime(GameMsg inMsg) {
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
    }

    private void handleDrawAction(GameMsg inMsg) {
        Paint paintData = inMsg.getPaintData();
        client.getGamePanel().receiveRemoteDrawing(
                paintData.getStartX(),
                paintData.getStartY(),
                paintData.getEndX(),
                paintData.getEndY(),
                paintData.getColor()
        );
        //드로잉 확인 콘솔
//                        System.out.println("DRAW_ACTION 수신: " +
//                                "시작(" + paintData.getStartX() + ", " + paintData.getStartY() +
//                                "), 끝(" + paintData.getEndX() + ", " + paintData.getEndY() +
//                                "), 색상: " + paintData.getColor() +
//                                ", 지우개 모드: " + paintData.isErasing());
    }

    private void handleVote(GameMsg inMsg) {
        if (inMsg.isVoteStart()) {
            client.getGamePanel().setDrawingEnabled(false);
            // 투표 모드 활성화
//                            System.out.println("GameMsg.VOTE 수신. isVoteStart: " + inMsg.isVoteStart());
            if (client.getGameRoomPanel() != null) {
                client.getGameRoomPanel().setGameMsg(inMsg); // gameMsg를 설정하고 투표 상태를 제어
                client.showDialog(inMsg); // 투표 시작 다이얼로그 표시
            }else {
                System.err.println("GameRoomPanel is null!");
            }
        } else {
            client.updateAlarmLabel(inMsg.getTime()); // 투표 타이머 업데이트
        }
    }

    private void handleGameEnd(GameMsg inMsg) {
        boolean isWinner = inMsg.isWinner();
        String resultMessage = inMsg.getResultMessage();
        client.endGame(isWinner, resultMessage);

        readyUsers = new Vector<>(); // 준비 유저 초기화
        user.currentRoom.setReadyUsers(readyUsers);
    }

    private void handleChatMessage(GameMsg inMsg) {
        String chatMsg = inMsg.message;
        if(inMsg.user != null) {
            String chatUser = inMsg.user.name;
            client.getGameRoomPanel().showChat("\uD83D\uDC64 " + chatUser + " : " + chatMsg);
        } else {
            client.getGameRoomPanel().showChat("\uD83D\uDD14 알림 : " + chatMsg);
        }

    }

    private void handleChatEmoticon(GameMsg inMsg) {
        client.updateEmoticonPanel(inMsg.user, inMsg.message);
    }

    private void handleRoomExit(GameMsg inMsg) {
        User exitUser = inMsg.user;
        synchronized (userNames) {
            userNames = new Vector<>(inMsg.userNames);
            System.out.println("userNames: " + userNames);
        }
        synchronized (readyUsers) {
            readyUsers = new Vector<>(inMsg.readyUsers);
            System.out.println("readyUsers: " + readyUsers);
        }
        client.updateReadyToRoom(readyUsers, exitUser);
        client.updateUserToRoom(userNames);

        if(readyUsers.size() < 4) {
            client.setReadyButtonVisibility(false);
        }
    }

    private void handleRoomExitOk() {
        synchronized (userNames) {
            userNames = new Vector<>();
        }
        synchronized (readyUsers) {
            readyUsers = new Vector<>();
        }
        client.changeSelectRoomPanel();
    }

    private void handleLogout() {
        synchronized (userNames) {
            userNames = new Vector<>();
        }
        synchronized (readyUsers) {
            readyUsers = new Vector<>();
        }
        client.changeStartPanel();
    }

    //

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
        sendGameMsg(new GameMsg(GameMsg.GAME_UN_READY, readyUser));
    }

    public void sendEmoticon(String emoticonName) {
        sendGameMsg(new GameMsg(GameMsg.CHAT_EMOTICON, user, emoticonName));
    }

    public void sendDrawingData(int startX, int startY, int endX, int endY, Color color, boolean isErasing) {
        if (out == null) {
            System.err.println("출력 스트림이 초기화되지 않았습니다. 데이터를 전송할 수 없습니다.");
            return;
        }
        try {
            Paint paintData = new Paint(startX, startY, endX, endY, color, isErasing);
            GameMsg msg = new GameMsg(GameMsg.DRAW_ACTION, paintData);
            sendGameMsg(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendVote(User user, String votedUserName) { sendGameMsg(new GameMsg(GameMsg.VOTE, user, votedUserName)); }

    public void sendLogout(User user) {
        sendGameMsg(new GameMsg(GameMsg.LOGOUT, user, userNames, readyUsers));

        client.getGameRoomPanel().resetVoteState(); // 투표 상태 초기화
        client.getGameRoomPanel().resetLiarState(); // 라이어 상태 초기화
        client.getGamePanel().setDrawingEnabled(true); // 리스너까지 다시 등록
    }

    public void sendRoomExit(User user) {
        sendGameMsg(new GameMsg(GameMsg.ROOM_EXIT, user, userNames, readyUsers));

        client.getGameRoomPanel().resetVoteState(); // 투표 상태 초기화
        client.getGameRoomPanel().resetLiarState(); // 라이어 상태 초기화
        client.getGamePanel().setDrawingEnabled(true); // 리스너까지 다시 등록
    }

    public void sendRetry(User user) {
        client.getGameRoomPanel().clearAllLeftBottomPanels();
        client.getGamePanel().clearLines();

        client.getGameRoomPanel().resetVoteState(); // 투표 상태 초기화
        client.getGameRoomPanel().resetLiarState(); // 라이어 상태 초기화
        client.getGamePanel().setDrawingEnabled(true); // 리스너까지 다시 등록
        // 다시 게임 대기 상태로 전환
        client.getGameRoomPanel().rightPannel.remove(client.getGameRoomPanel().alarmPanel);
        client.setReadyButtonVisibility(true);

        sendGameMsg(new GameMsg(GameMsg.GAME_RETRY, user, readyUsers));
        sendGameMsg(new GameMsg(GameMsg.ROOM_SELECT, user, roomName));
    }

    public User getUser() {
        return this.user; // 사용자 객체 반환
    }
}
