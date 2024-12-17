import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.Vector;

public class ServerManager {
    private int port;
    private Server server;

    private ServerSocket serverSocket;
    private Thread acceptThread = null;
    private Vector<ClientHandler> users = new Vector<ClientHandler>();
    private Vector<Room> rooms = new Vector<>();

    private static final int DRAWING_TIME=60; //60
    private static final int DRAWING_PERTIME=DRAWING_TIME/4;
    private static final int VOTE_TIME=30;

    public ServerManager(int port, Server server) {
        this.port = port;
        this.server = server;
    }

    public void startServer() {
        acceptThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                server.printDisplay("[접속] 서버가 시작되었습니다. 포트 : " + port, "접속");

                while (acceptThread == Thread.currentThread()) {
                    Socket clientSocket = serverSocket.accept();
                    server.printDisplay("[접속] 새로운 클라이언트가 연결되었습니다  (" + clientSocket.getInetAddress().getHostAddress() + ")", "접속");

                    ClientHandler handler = new ClientHandler(clientSocket);
                    users.add(handler);
                    handler.start();
                }
            } catch (IOException e) {
                server.printDisplay("[접속] 서버 소켓 종료 : " + e.getMessage(), "접속");
            } finally {
                stopServer();
            }
        });
        acceptThread.start();
    }

    public void stopServer() {
        try {
            if (serverSocket != null) serverSocket.close();
            acceptThread = null;
//            server.printDisplay("[접속] 서버가 종료되었습니다.", "접속");
        } catch (IOException e) {
            server.printDisplay("[접속] 서버 종료 중 오류 : " + e.getMessage(), "접속");
        }
    }

    public void exit() {
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            server.printDisplay("[접속] 서버 닫기 오류 : " + e.getMessage(), "접속");
            System.err.println("서버 닫기 오류> " + e.getMessage());
        }
        System.exit(-1);
    }


    private class ClientHandler extends Thread {
        private Socket clientSocket;
        private ObjectInputStream in;
        private ObjectOutputStream out;
        private User user;
        public String userName;
        private Room currentRoom = null;
        public boolean isLiar = false;
        private Vector<User> readyUsers = new Vector<>();
        public User liar;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        private void receiveMessage() {
            try {
                in = new ObjectInputStream(new BufferedInputStream(clientSocket.getInputStream()));
                out = new ObjectOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
                out.flush();

                GameMsg inMsg;
                while ((inMsg = (GameMsg) in.readObject()) != null) {
                    switch (inMsg.getMode()) {
                        case GameMsg.LOGIN:
                            handleLogin(inMsg);
                            break;
                        case GameMsg.ROOM_SELECT:
                            handleRoomSelect(inMsg);
                            break;
                        case GameMsg.CHAT_MESSAGE:
                            handleChatMessage(inMsg);
                            break;
                        case GameMsg.CHAT_EMOTICON:
                            handleChatEmoticon(inMsg);
                            break;
                        case GameMsg.GAME_READY:
                            handleGameReady(inMsg);
                            break;
                        case GameMsg.GAME_UN_READY:
                            handleGameUnReady(inMsg);
                            break;
                        case GameMsg.GAME_START:
                            handleGameStart(inMsg);
                            break;
                        case GameMsg.VOTE:
                            handleVote(inMsg);
                            break;
                        case GameMsg.DRAW_ACTION:
                            handleDrawAction(inMsg);
                            break;
                        case GameMsg.GAME_RETRY:
                            handleGameRetry(inMsg);
                            break;
                        case GameMsg.ROOM_EXIT:
                            handleRoomExit(inMsg);
                            break;
                        case GameMsg.LOGOUT:
                            handleLogout(inMsg);
                            break;
                        default:
                            server.printDisplay("[접속][에러] 서버 receiveMessage 알 수 없는 메시지 모드: " + inMsg.getMode(), "접속");
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                disconnectClient();
                server.printDisplay("[접속][에러] 서버 receiveMessage 클라이언트 연결 해제: " + e.getMessage(), "접속");
                broadcasting(new GameMsg(GameMsg.LOGOUT, user));
            } finally {
                disconnectClient();
            }
        }

        private void handleLogin(GameMsg inMsg) {
            user = inMsg.getUser();
            userName = user.name;
            server.printDisplay("[접속][로그인] " + userName + "님이 로그인하였습니다.", "접속");
            sendGameMsg(new GameMsg(GameMsg.LOGIN_OK, user));
        }

        private void handleRoomSelect(GameMsg inMsg) {
            user = inMsg.user;
            enterRoom(inMsg.getMsg());
            user.setCurrentRoom(currentRoom);
//            user.currentRoom.setReadyUsers(currentRoom.getReadyUsers());
            user.joinRoom(currentRoom);
            if(user.currentRoom.getMemberCount() > 4) {
                user.leaveRoom();
                sendGameMsg(new GameMsg(GameMsg.ROOM_SELECT_DENIED, user));
                server.printDisplay("[" + currentRoom.getRoomName() + "][방 입장 실패] " + userName + "님이 " + inMsg.getMsg() + "방에 입장하지 못했습니다.", "접속");
                return;
            }
            server.printDisplay("[" + currentRoom.getRoomName() + "][방 입장] " + userName + "님 " + user.getCurrentRoom().getRoomName() + " 방 입장. 현재 : " + user.currentRoom.getMemberCount() + "명", "접속");
            sendGameMsg(new GameMsg(GameMsg.ROOM_SELECT, user, currentRoom.getMembers(), currentRoom.getReadyUsers(), inMsg.getMsg()));
            broadcastExceptUser(user, new GameMsg(GameMsg.ROOM_NEW_MEMBER, user, currentRoom.getMembers(), currentRoom.getReadyUsers(), inMsg.getMsg())); // currentRoom

            // 4명 다 들어오면 준비 가능하도록
            if(user.currentRoom.getMemberCount() == 4) {
                broadcasting(new GameMsg(GameMsg.GAME_READY_AVAILABLE));
            }
        }

        private void handleChatMessage(GameMsg inMsg) {
            user = inMsg.user;
            broadcasting(new GameMsg(GameMsg.CHAT_MESSAGE, user, inMsg.getMsg()));
            server.printDisplay("[채팅][" + currentRoom.getRoomName() + "] " + inMsg.user.name + "님 : " + inMsg.getMsg(), "채팅+이모티콘");
        }

        private void handleChatEmoticon(GameMsg inMsg) {
            broadcasting(new GameMsg(GameMsg.CHAT_EMOTICON, inMsg.user, inMsg.getMsg()));
            server.printDisplay("[이모티콘][" + user.currentRoom.getRoomName() + "] " + inMsg.user.name + "님 : \"" + inMsg.getMsg() + "\" 이모티콘 전송", "채팅+이모티콘");
        }

        private void handleGameReady(GameMsg inMsg) {
            inMsg.user.setCurrentRoom(currentRoom);
            inMsg.user.setReady();
            currentRoom = inMsg.user.getCurrentRoom();
            server.printDisplay("[" + currentRoom.getRoomName() + "][준비]" + user.name + " 님 준비 완료", "게임상태");
            broadcasting(new GameMsg(GameMsg.GAME_READY_OK, inMsg.user, currentRoom.getReadyUsers()));
        }

        private void handleGameUnReady(GameMsg inMsg) {
            server.printDisplay("[" + currentRoom.getRoomName() + "][준비 해제]" + user.name + " 님 준비 해제", "게임상태");
            inMsg.user.setCurrentRoom(currentRoom);
            inMsg.user.setUnReady();
            currentRoom = inMsg.user.getCurrentRoom();
            broadcasting(new GameMsg(GameMsg.GAME_UN_READY_OK, inMsg.user, currentRoom.getReadyUsers()));
        }

        private void handleGameStart(GameMsg inMsg) {
            currentRoom = inMsg.user.getCurrentRoom();
            server.printDisplay("[" + currentRoom.getRoomName() + "][시작] 게임이 시작됩니다.", "게임상태");
//            currentRoom = inMsg.user.currentRoom;
            readyUsers = inMsg.readyUsers;
            liar = selectLiar(inMsg.readyUsers);
            liar.isLiar = true;
            if(liar == null) {
                System.out.println("라이어가 뽑히지 않았습니다.");
                server.printDisplay("[" + currentRoom.getRoomName() + "][에러] 라이어가 뽑히지 않았습니다.", "게임상태");
            } else {
                server.printDisplay("[" + currentRoom.getRoomName() + "][라이어] 라이어 : "+ liar.name, "게임상태");
                System.out.println("뽑힌 라이어 이름 : " + liar.name);
            }
            currentRoom.setMembers(inMsg.userNames);
            //턴 초기화
            currentRoom.resetTurns();
            System.out.println("setMembers 함 : " + currentRoom.getMembers());

            broadcastIndividualUser(liar, new GameMsg(GameMsg.LIAR_NOTIFICATION, liar, currentRoom.getKeyword()));
            broadcastExceptUser(liar, new GameMsg(GameMsg.KEYWORD_NOTIFICATION, user, currentRoom.getKeyword()));
            // 타이머 시작
            server.printDisplay("[" + currentRoom.getRoomName() + "][타이머] 타이머 시작", "게임상태");
            startRoomTimer(currentRoom, DRAWING_TIME);
        }

        private void handleVote(GameMsg inMsg) {
            String votedUser = inMsg.getMsg();
            if (votedUser != null) {
                String message = userName + "님이 투표를 완료했습니다.";
                server.printDisplay("[" + currentRoom.getRoomName() + "][투표] " + userName + "님이 " + votedUser + "에게 투표했습니다.", "투표");
                broadcasting(new GameMsg(GameMsg.CHAT_MESSAGE, null, message));
                currentRoom.addVote(votedUser);
            } else {
                server.printDisplay("[" + currentRoom.getRoomName() + "][투표] 투표 값이 null입니다.", "투표");
            }
        }

        // 그림 데이터를 처리하는 메서드
        private void handleDrawAction(GameMsg inMsg) {
            Paint paintData = inMsg.getPaintData();
            Color color = inMsg.getPaintData().getColor() != null ? inMsg.getPaintData().getColor() : Color.BLACK;
            //드로잉 확인 패널
            server.printDisplay("[페인팅][" + currentRoom.getRoomName()+ "][" + user.name + "]" + "시작(" + paintData.getStartX() + ", " + paintData.getStartY() +
                    "), 끝(" + paintData.getEndX() + ", " + paintData.getEndY() + "), 색상: " + paintData.getColor() +
                    ", 지우개 모드: " + paintData.isErasing(), "페인팅");
            broadcasting(new GameMsg(GameMsg.DRAW_ACTION, paintData)); // 그림 데이터를 다른 클라이언트들에게 전송
        }

        private void handleGameRetry(GameMsg inMsg) {
            server.printDisplay("[" + currentRoom.getRoomName() + "][재시작] " + userName + "님이 다시 시작을 눌렀습니다.", "게임상태");
            user.isLiar = false;
            currentRoom.setReadyUsers(inMsg.readyUsers);
            inMsg.user.setCurrentRoom(currentRoom);
            inMsg.user.setUnReady();
            broadcasting(new GameMsg(GameMsg.GAME_UN_READY_OK, user, inMsg.user.currentRoom.getReadyUsers()));
        }

        private void handleRoomExit(GameMsg inMsg) {
//            user = inMsg.user;
            currentRoom.setReadyUsers(inMsg.readyUsers);
            currentRoom.removeReadyUser(inMsg.user);
            currentRoom.setMembers(inMsg.userNames);
            currentRoom.removeMember(inMsg.user);
            broadcastExceptUser(inMsg.user, new GameMsg(GameMsg.ROOM_EXIT, inMsg.user, currentRoom.getMembers(), currentRoom.getReadyUsers()));
            server.printDisplay("[" + currentRoom.getRoomName() + "][방 퇴장] " + userName + "님이 " + currentRoom.getRoomName() + "방을 나갔습니다. 현재 인원 : " + currentRoom.getMemberCount() +"명", "접속");

            inMsg.user.setCurrentRoom(currentRoom);
//            inMsg.user.setUnReady(); // inMsg.user.currentRoom 이미 null임 여기서
//            inMsg.user.leaveRoom(); // user의 currentRoom null됨
            sendGameMsg(new GameMsg(GameMsg.ROOM_EXIT_OK, inMsg.user));
//            currentRoom = null;
        }

        private void handleLogout(GameMsg inMsg) {
            currentRoom.setReadyUsers(inMsg.readyUsers);
            currentRoom.removeReadyUser(inMsg.user);
            currentRoom.setMembers(inMsg.userNames);
            currentRoom.removeMember(inMsg.user);

            broadcastExceptUser(inMsg.user, new GameMsg(GameMsg.ROOM_EXIT, inMsg.user, currentRoom.getMembers(), currentRoom.getReadyUsers()));
            server.printDisplay("[" + currentRoom.getRoomName() + "][방 퇴장] " + userName + "님이 " + currentRoom.getRoomName() + "방을 나갔습니다. 현재 인원 : " + currentRoom.getMemberCount() +"명", "접속");
            server.printDisplay("[" + currentRoom.getRoomName() + "][로그아웃] " + userName + "님이 로그아웃했습니다.", "접속");

            inMsg.user.setCurrentRoom(currentRoom);
//            inMsg.user.setUnReady();
//            inMsg.user.leaveRoom(); // user의 currentRoom null됨
            sendGameMsg(new GameMsg(GameMsg.LOGOUT, inMsg.user));
//            currentRoom = null;
        }

        //

        private void sendGameMsg(GameMsg msg) {
            try {
                out.writeObject(msg);
                out.flush();
            } catch (IOException e) {
                System.out.println("[접속][에러] 서버 sendGameMsg 전송 오류>" + e.getMessage());
                e.printStackTrace();
            }
        }

        private void startRoomTimer(Room room, int totalTime) {
            new Thread(() -> {
                int remainingTime = totalTime;
                int turns = 0; // 현재 턴 횟수
                int totalTurns = room.getMembers().size(); // 총 턴 횟수
                int turnTimeRemaining = DRAWING_PERTIME; // 각 턴의 남은 시간 초기화
                try {
                    // 첫 번째 사용자 알림
                    room.nextTurn(); // 첫 사용자 설정
                    User currentUser = room.getCurrentTurnUser();
                    if (currentUser != null) {
                        GameMsg firstTurnMsg = new GameMsg(GameMsg.TIME, currentUser, "Your turn!", remainingTime, room.getMembers());
                        broadcasting(firstTurnMsg);
                        server.printDisplay("[" + room.getRoomName() + "][턴] " + currentUser.getName() + " 님이 그림을 그릴 차례입니다.", "게임상태");
                    }

                    while (remainingTime > 0) {
                        Thread.sleep(1000); // 1초 간격으로 실행
                        remainingTime--;
                        turnTimeRemaining--; // 현재 턴의 남은 시간 감소
                        // 턴 종료 조건 확인
                        if (turnTimeRemaining <= 0 && remainingTime > 0) {
                            turnTimeRemaining = DRAWING_PERTIME; // 다음 턴 시간 초기화
                            room.nextTurn(); // 다음 사용자로 턴 전환
                            currentUser = room.getCurrentTurnUser();

                            // 다음 사용자 알림
                            if (currentUser != null) {
                                GameMsg turnMsg = new GameMsg(GameMsg.TIME, currentUser, "Your turn!", remainingTime, room.getMembers());
                                broadcasting(turnMsg);
                                server.printDisplay("[" + room.getRoomName() + "][턴] " + currentUser.getName() + " 님이 그림을 그릴 차례입니다.", "게임상태");
                            }
                        }
                        // TIME 메시지를 생성하여 브로드캐스트
                        GameMsg timeMsg = new GameMsg(GameMsg.TIME, null, null, remainingTime, currentRoom.getMembers());
                        broadcasting(timeMsg);
                    }
                    //시간 종료되면 투표 모드 전환
                    server.printDisplay("[" + room.getRoomName() + "][타이머] 타이머 종료", "게임상태");
                    // 투표 타이머:
                    GameMsg voteStartMsg = new GameMsg(GameMsg.VOTE, null, "투표를 시작하세요!", VOTE_TIME, currentRoom.getMembers());
                    voteStartMsg.setVoteStart(true); // 투표 시작 메시지로 설정
                    broadcasting(voteStartMsg);
                    // 투표 타이머 시작
                    startVoteTimer(room, VOTE_TIME); // 20초 동안 투표 실행
                    System.out.println("타이머 종료 - 방 [" + room.getRoomName() + "]");
                } catch (InterruptedException e) {
                    System.err.println("타이머 중단 - 방 [" + room.getRoomName() + "], 오류: " + e.getMessage());
                }
            }).start();
        }

        // 투표 타이머 실행
        private void startVoteTimer(Room room, int voteTime) {
            new Thread(() -> {
                int remainingTime = voteTime;
                try {
                    while (remainingTime > 0) {
                        Thread.sleep(1000);
                        remainingTime--;
                        // 타이머 메시지 전송
                        GameMsg voteTimeMsg = new GameMsg(GameMsg.VOTE, null, null, remainingTime, currentRoom.getMembers());
                        voteTimeMsg.setVoteStart(false); // 타이머 메시지
                        broadcasting(voteTimeMsg);
                    }
                    collectVoteResults(room);
                    // 투표 결과 집계
                    server.printDisplay("[" + room.getRoomName() + "][타이머] 투표 시간 종료", "게임상태");
                } catch (InterruptedException e) {
                    System.err.println("투표 타이머 중단 - 방 [" + room.getRoomName() + "], 오류: " + e.getMessage());
                }
            }).start();
        }

        //투표 결과 집계
        private void collectVoteResults(Room room) {
            Map<String, Integer> voteCounts = room.getVoteCounts();
            // 아무도 투표하지 않은 경우 처리
            if (voteCounts.isEmpty()) {
                String liarVictoryMessage = "라이어: " + liar.name;
                System.out.println(liarVictoryMessage);
                // 라이어에게 메시지 전송
                broadcastIndividualUser(
                        liar,
                        new GameMsg(GameMsg.GAME_END, liar, liarVictoryMessage, true) // true = 라이어 승리
                );
                // 라이어가 아닌 사람들에게 메시지 전송
                broadcastExceptUser(
                        liar,
                        new GameMsg(GameMsg.GAME_END, liar, liarVictoryMessage, false) // false = 라이어가 승리
                );
                // 투표 상태 및 게임 상태 초기화
                room.resetVoteCounts();
                server.printDisplay("[" + room.getRoomName() + "][투표] 아무도 투표하지 않음. 게임 상태 초기화 완료", "투표");
                return; // 조기 종료
            }

            // 최다 득표자 계산
            String liarCandidate = voteCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .get()
                    .getKey();

            // 라이어 승리 여부 판단
            boolean liarVictory = !liarCandidate.equals(liar.name);

            // 서버 패널 표시용 ----------
            String resultMessage = liarVictory
                    ? "라이어가 승리했습니다! 라이어는 " + liar.name + "입니다."
                    : "라이어가 패배했습니다! " + liarCandidate + "님이 지목되었습니다.";

            String liarWinMessage = "라이어: " + liar.name ;
            String liarLoseMessage = "라이어: " + liar.name ;

            // 라이어에게 메시지 전송
            String liarResultMessage = liarVictory ? liarWinMessage : liarLoseMessage;
            System.out.println("[DEBUG] 라이어에게 전송할 메시지: " + liarResultMessage);
            System.out.println("[DEBUG] 라이어의 승리 여부: " + liarVictory);

            broadcastIndividualUser(
                    liar,
                    new GameMsg(GameMsg.GAME_END, liar, liarResultMessage, liarVictory)
            );
            //라이어 아닌사람 메시지 전송
            boolean isWinner = !liarVictory; // 라이어 승리 여부의 반대
            String userResultMessage = liarVictory ? liarWinMessage : liarLoseMessage;
            //System.out.println("[DEBUG] 사용자 " + member.name + "에게 전송할 메시지: " + userResultMessage);
            //System.out.println("[DEBUG] 승리 여부: " + isWinner);

            broadcastExceptUser(
                    liar,
                    new GameMsg(GameMsg.GAME_END, liar, userResultMessage, isWinner)
            );

            server.printDisplay("[" + room.getRoomName() + "][투표] 결과 : " + liarCandidate, "투표");
            server.printDisplay("[" + room.getRoomName() + "][투표] 결과 : " + resultMessage, "게임상태");
            room.resetVoteCounts(); // 투표 초기화
            room.resetVoteCounts(); // 라이어 초기화
            server.printDisplay("[" + room.getRoomName() + "][게임상태] 투표 값, 라이어 초기화", "게임상태");
        }

        private void broadcasting(GameMsg msg) {
            if (currentRoom == null) {
                server.printDisplay("[접속] broadcasting 실패: " + msg.user.name + " 클라이언트가 방에 속해 있지 않습니다.", "접속");
                return;
            }
            // 같은 방에 있는 멤버들에게만 메시지를 전송
            synchronized (currentRoom) {
                for (User member : currentRoom.getMembers()) {
//                    System.out.println("Broadcast 대상: " + member.name);
                    ClientHandler handler = findHandlerByUser(member);
                    if (handler != null) { // 핸들어 있을때
                        handler.sendGameMsg(msg);
                    }
                }
            }
        }

        private ClientHandler findHandlerByUser(User user) {
            for (ClientHandler handler : users) {
                if (handler.userName.equals(user.name)) {
                    return handler;
                }
            }
            return null;
        }

        private void broadcastIndividualUser(User liar, GameMsg msg) {
            if (currentRoom == null) {
                server.printDisplay("[접속] broadcasting 실패: " + msg.user.name + " 클라이언트가 방에 속해 있지 않습니다.", "접속");
                return;
            }
            // 같은 방에 있는 멤버들에게만 메시지를 전송
            synchronized (currentRoom) {
                for (User member : currentRoom.getMembers()) {
                    System.out.println("Broadcast 대상: " + member.name);
                    ClientHandler handler = findHandlerByUser(member);
                    if (handler.userName.equals(liar.name)) { // 라이어만
                        handler.isLiar = true;
                        handler.sendGameMsg(msg);
                    }
                }
            }
        }

        private void broadcastExceptUser(User liar, GameMsg msg) {
            if (currentRoom == null) {
                server.printDisplay("[접속] broadcasting 실패: " + msg.user.name + " 클라이언트가 방에 속해 있지 않습니다.", "접속");
                return;
            }
            // 같은 방에 있는 멤버들에게만 메시지를 전송
            synchronized (currentRoom) {
                for (User member : currentRoom.getMembers()) {
                    ClientHandler handler = findHandlerByUser(member);
                    if (!handler.userName.equals(liar.name)) { // 라이어 빼고
                        System.out.println("broadcastExceptUser 대상: " + member.name);
                        handler.sendGameMsg(msg);
                    }
                }
            }
        }

        private Room findRoom(String roomName) {
            synchronized (rooms) {
                // 방 검색
                Room room = rooms.stream()
                        .filter(r -> r.getRoomName().equals(roomName)) // 이름이 같은 방 필터링
                        .findFirst() // 첫 번째 방 반환
                        .orElseGet(() -> { // 방이 없으면 새로 생성
                            Room newRoom = new Room(roomName);
                            rooms.add(newRoom); // 새 방을 목록에 추가
                            server.printDisplay("[접속] 새 방 생성 : " + roomName, "접속");
                            return newRoom; // 새로 만든 방 반환
                        });
                return room;
            }
        }

        private void enterRoom(String roomName) {
            synchronized (rooms) {
                // 방 검색
                Room room = rooms.stream()
                        .filter(r -> r.getRoomName().equals(roomName)) // 이름이 같은 방 필터링
                        .findFirst() // 첫 번째 방 반환
                        .orElseGet(() -> { // 방이 없으면 새로 생성
                            Room newRoom = new Room(roomName);
                            rooms.add(newRoom); // 새 방을 목록에 추가
                            server.printDisplay("[접속] 새 방 생성 : " + roomName, "접속");
                            return newRoom; // 새로 만든 방 반환
                        });
                // 현재 클라이언트가 방에 속해있다면 제거
                if (currentRoom != null) {
                    currentRoom.removeMember(user);
                }

                // 유저의 방 정보 업데이트
//                user.joinRoom(room);
//                user.setCurrentRoom(room);
                currentRoom = room; // 현재 클라이언트의 방 업데이트

                // 방 이름에 따라 키워드 설정
                switch (currentRoom.getRoomName()) {
                    case "food":
                        currentRoom.setKeyword("햄버거");
                        break;
                    case "place":
                        currentRoom.setKeyword("에펠탑");
                        break;
                    case "animal":
                        currentRoom.setKeyword("사자");
                        break;
                    case "character":
                        currentRoom.setKeyword("뽀로로");
                        break;
                    default:
                        currentRoom.setKeyword("마카롱");
                }
            }
        }

        private void exitRoom() {
            synchronized (rooms) {
                if (currentRoom != null) {
                    // 현재 방에서 유저 제거
//                    currentRoom.removeMember(user);
                    user.leaveRoom();
                    // 방의 멤버가 아무도 없다면 방 삭제
                    if (currentRoom.getMembers().isEmpty()) {
                        rooms.remove(currentRoom);
                        server.printDisplay("[접속] 빈 방 삭제: " + currentRoom.getRoomName(), "접속");
                    }
                    // 현재 방 정보 초기화
                    currentRoom = null;
                }
            }
        }

        private User selectLiar(Vector<User> readyUsers) {
            // 랜덤으로 라이어 선택
            Random random = new Random();
            int liarIndex = random.nextInt(readyUsers.size());
            User liarUser = readyUsers.get(liarIndex);

            return liarUser;
        }

        private void disconnectClient() {
            if (user != null) {
                user.leaveRoom(); // User 메서드 호출
            }

            users.remove(this); // 클라이언트 목록에서 제거
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (clientSocket != null) clientSocket.close();
            } catch (IOException e) {
                server.printDisplay("[접속][에러]클라이언트 소켓 닫기 오류: " + e.getMessage(), "접속");
            }
        }

        @Override
        public void run() {
            receiveMessage();
        }
    }
}
