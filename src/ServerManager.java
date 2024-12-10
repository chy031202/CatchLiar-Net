import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

public class ServerManager {
    private int port;
    private Server server;

    private ServerSocket serverSocket;
    private Thread acceptThread = null;
    private Vector<ClientHandler> users = new Vector<ClientHandler>();
    private Vector<Room> rooms = new Vector<>();
    private Set<ClientHandler> clients = Collections.synchronizedSet(new HashSet<>());

    public ServerManager(int port, Server server) {
        this.port = port;
        this.server = server;
    }

    public void startServer() {
        acceptThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                server.printDisplay("서버가 시작되었습니다. 포트: " + port);

                while (acceptThread == Thread.currentThread()) {
                    Socket clientSocket = serverSocket.accept();
                    server.printDisplay("클라이언트가 연결되었습니다: " + clientSocket.getInetAddress().getHostAddress());

                    ClientHandler handler = new ClientHandler(clientSocket);
                    users.add(handler);
                    handler.start();
                }
            } catch (IOException e) {
                server.printDisplay("서버 소켓 종료: " + e.getMessage());
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
            server.printDisplay("서버가 종료되었습니다.");
        } catch (IOException e) {
            server.printDisplay("서버 종료 중 오류: " + e.getMessage());
        }
    }

    public void exit() {
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            System.err.println("서버 닫기 오류> " + e.getMessage());
        }
        System.exit(-1);
    }


    private class ClientHandler extends Thread {
        private Socket clientSocket;
        private ObjectInputStream in;
        private ObjectOutputStream out;
        private User user;
        private String userName;
        private Room currentRoom = null;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        // 그림 데이터를 처리하는 메서드 추가
        private void handleDrawAction(GameMsg inMsg) {
            Paint paintData = inMsg.getPaintData();
            server.printDisplay("DRAW_ACTION 수신: 시작(" + paintData.getStartX() + ", " + paintData.getStartY() +
                    "), 끝(" + paintData.getEndX() + ", " + paintData.getEndY() + "), 색상: " + paintData.getColor());
            broadcasting(new GameMsg(GameMsg.DRAW_ACTION, paintData)); // 그림 데이터를 다른 클라이언트들에게 전송
        }

        private void receiveMessage(Socket cs) {
            try {
                in = new ObjectInputStream(new BufferedInputStream(clientSocket.getInputStream()));
                out = new ObjectOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
                out.flush();

                GameMsg inMsg;
                while ((inMsg = (GameMsg) in.readObject()) != null) {
                    switch (inMsg.getMode()) {
                        case GameMsg.LOGIN:
                            user = inMsg.getUser();
                            userName = user.name;
                            server.printDisplay("새 참가자: " + userName);
                            sendGameMsg(new GameMsg(GameMsg.LOGIN_OK, user));
                            break;

                        case GameMsg.ROOM_SELECT:
                            user = inMsg.user;
                            enterRoom(inMsg.getMsg());
                            if(user.currentRoom.getMemberCount() > 4) {
                                user.leaveRoom();
                                sendGameMsg(new GameMsg(GameMsg.ROOM_SELECT_DENIED, user));
                                server.printDisplay(userName + "님이 " + inMsg.getMsg() + "방에 입장하지 못했습니다.");
                                break;
                            }
                            server.printDisplay(userName + "님이 " + inMsg.getMsg() + "방에 입장했습니다.");
                            sendGameMsg(new GameMsg(GameMsg.ROOM_SELECT_OK, user, inMsg.getMsg()));
                            broadcasting(new GameMsg(GameMsg.ROOM_NEW_MEMBER, user, inMsg.getMsg())); // currentRoom
                            break;

                        case GameMsg.LOGOUT:
                            server.printDisplay(userName + "님이 로그아웃했습니다.");
                            disconnectClient();
                            break;

                        case GameMsg.DRAW_ACTION:
                            handleDrawAction(inMsg);
                            break;

                        default:
                            server.printDisplay("서버 receiveMessage 알 수 없는 메시지 모드: " + inMsg.getMode());
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                server.printDisplay("서버 receiveMessage 클라이언트 연결 해제: " + e.getMessage());
            } finally {
                disconnectClient();
            }
        }

        private void sendGameMsg(GameMsg msg) {
            try {
                out.writeObject(msg);
                out.flush();
            } catch (IOException e) {
                System.out.println("서버 sendGameMsg 전송 오류>" + e.getMessage());
                e.printStackTrace();
            }
        }

        private void broadcasting(GameMsg msg) {
            if (currentRoom == null) {
                server.printDisplay("broadcasting 실패: 클라이언트가 방에 속해 있지 않습니다.");
                return;
            }
            // 같은 방에 있는 멤버들에게만 메시지를 전송
            synchronized (currentRoom) {
                for (String memberName : currentRoom.getMembers()) {
                    users.stream()
                            .filter(c -> c.userName.equals(memberName)) // 해당 이름의 클라이언트를 찾음
                            .forEach(c -> c.sendGameMsg(msg));

                }
            }
            synchronized (users) {
                users.forEach(c -> c.sendGameMsg(msg));
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
                            server.printDisplay("새 방 생성: " + roomName);
                            return newRoom; // 새로 만든 방 반환
                        });
                // 현재 클라이언트가 방에 속해있다면 제거
                if (currentRoom != null) {
                    currentRoom.removeMember(user);
                }

                // 유저의 방 정보 업데이트
                user.joinRoom(room);
                user.setCurrentRoom(room);
                currentRoom = room; // 현재 클라이언트의 방 업데이트

                server.printDisplay(userName + "님이 방 [" + user.getCurrentRoom().getRoomName() + "]에 입장했습니다. 현재 : " + room.getMemberCount() + "명");
            }
        }

        private void disconnectClient() {
            if (user != null) {
                user.leaveRoom(); // User 메서드 호출
            }

            users.remove(this); // 클라이언트 목록에서 제거
            try {
                clientSocket.close();
            } catch (IOException e) {
                server.printDisplay("클라이언트 소켓 닫기 오류: " + e.getMessage());
            }
        }

        @Override
        public void run() {
            receiveMessage(clientSocket);
        }

    }
}
