import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.Vector;

public class Server {
    private int port;
    private ServerSocket serverSocket = null;
    private JFrame frame;
    private JTextArea t_display;
    private GameRoomPanel gameRoomPanel;

    private JButton b_connect, b_disconnect, b_exit;
    private Thread acceptThread = null;

    private Vector<ClientHandler> users = new Vector<ClientHandler>();
    private Vector<Room> rooms = new Vector<>();

    //소켓 설정
    private ObjectInputStream in;

    public Server(int port){
        this.port = port;
        frame = new JFrame( "P2P ChatServer");
        frame.setBounds(700, 200, 600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameRoomPanel = new GameRoomPanel(frame); //
    };

    public static void main(String[] args){
        int port = 54321;
        Server server = new Server(port);
        server.buildGUI();
        server.frame.setVisible(true);
    }

    private void buildGUI(){
        JPanel displaypanel = createDisplayPanel();
        frame.add(displaypanel);
        frame.add(createControlPanel(), BorderLayout.SOUTH);
    }

    private JPanel createDisplayPanel() {
        JPanel display = new JPanel();
        display.setLayout(new BorderLayout());
        t_display = new JTextArea();
        t_display.setEditable(false);
        JScrollPane sp = new JScrollPane(t_display);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);


        display.add(sp, BorderLayout.CENTER);

        return display;
    }

    private JPanel createControlPanel() {
        JPanel controlpanel = new JPanel();
        controlpanel.setLayout(new GridLayout(1, 0));

        b_connect = new JButton("서버 시작");
        b_disconnect = new JButton("서버 종료");

        b_disconnect.setEnabled(false);
        b_connect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                acceptThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        startServer();
                    }

                });
                acceptThread.start();

                //버튼 비활성화
                b_connect.setEnabled(false);
                b_exit.setEnabled(false);
                b_disconnect.setEnabled(true);
            }

        });

        //접속 끊기 버튼
        //sendMessage를 수정한다.
        b_disconnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                disconnect();
                b_connect.setEnabled(true);
                b_exit.setEnabled(true);
            }

        });

        //종료 버튼

        b_exit = new JButton("종료");

        b_exit.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(-1);
            }

        });
        controlpanel.add(b_connect);
        controlpanel.add(b_disconnect);
        controlpanel.add(b_exit);

        return controlpanel;
    }

    private String getLocalAddr() {
        InetAddress local = null;
        String addr = "";
        try {
            local = InetAddress.getLocalHost();
            addr = local.getHostAddress();
            System.out.println(addr);
        } catch(UnknownHostException e) {
            e.printStackTrace();
        }
        return addr;
    }

    private void startServer() {
        Socket clientSocket = null;

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("서버가 시작되었습니다.");
            printDisplay("서버가 시작되었습니다." + getLocalAddr());

            while (acceptThread == Thread.currentThread()) {
                clientSocket = serverSocket.accept();

                String cAddr = clientSocket.getInetAddress().getHostAddress();

                printDisplay("클라이언트가 연결되었습니다." + cAddr + "\n");

                ClientHandler cHandler = new ClientHandler(clientSocket, frame);
                users.add(cHandler);
                cHandler.start();

            }
        }catch (SocketException e) {
            printDisplay("서버 소켓 종료");
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if(clientSocket != null) clientSocket.close();
                if(serverSocket !=null) serverSocket.close();
            } catch (IOException e) {
                System.err.println("서버 닫기 오류> "+e.getMessage());
                System.exit(-1);
            }
        }
    } //startserver


    private class ClientHandler extends Thread{
        private Socket clientSocket;
        private ObjectOutputStream out;
        //private BufferedWriter out;

        private String uid;
        private Room currentRoom = null; // 현재 클라이언트가 속한 방
        private JFrame parentFrame;

        public ClientHandler (Socket clientSocket, JFrame parentFrame) {
            this.clientSocket = clientSocket;
            this.parentFrame = parentFrame;
        }

        private void receiveMessage(Socket cs) {

            /* 클라이언트 소켓에서 입력 스트림 생성 */
            try {
//                BufferedReader in = new BufferedReader(new InputStreamReader(cs.getInputStream(), "UTF-8"));
//                out = new BufferedWriter(new OutputStreamWriter(cs.getOutputStream(), "UTF-8"));
                out = new ObjectOutputStream(new BufferedOutputStream(cs.getOutputStream()));
                out.flush();
                in = new ObjectInputStream(new BufferedInputStream(cs.getInputStream()));

                //String message;
                GameMsg msg;


                while  ((msg = (GameMsg) in.readObject()) != null) {
                    switch (msg.getMode()){
                        case GameMsg.LOGIN :
                            uid = msg.user.name;
                            printDisplay("새 참가자" + uid);
                            printDisplay("현재 참가자 수" + users.size());
                            break;

                        case GameMsg.ROOM_SELECT: // 방 입장 처리
                            String roomName = msg.getMsg(); // 방 이름은 msg.msg에 저장
                            if (currentRoom != null) {
                                leaveRoom(); // 이전 방에서 퇴장
                            }
                            joinRoom(roomName); // 새로운 방에 입장
                            printDisplay(uid + "님이 " + roomName + " 방에 입장했습니다.");
                            broadcasting(uid + "님이 " + roomName + " 방에 입장했습니다.");
                            break;

                        default: // 알 수 없는 모드 처리
                            printDisplay("알 수 없는 모드: " + msg.mode);
                            break;

                    }

//                    else if (message.startsWith("/room:")) {
//                        String roomName = message.split(":")[1];
//                        // 이전 방에서 퇴장
//                        if (currentRoom != null) {
//                            leaveRoom();
//                        }
//                        // 새로운 방에 입장
//                        joinRoom(roomName);
//                        // 패널 부착
////                        gameRoomPanel.createUserPanel(uid);
//
//                        printDisplay(uid + "님이 " + roomName + " 방에 입장했습니다.");
//                        broadcasting(uid + "님이 " + roomName + " 방에 입장했습니다.");
//                    } else {
//                        message = uid + ": " + message;
//                        printDisplay(message);
//                        broadcasting(message);
//                    }
                }
                users.removeElement(this);
                printDisplay(uid+"퇴장. 현재 참가자 수 : "+ users.size());
            } catch (IOException e) {
                users.removeElement(this);
                System.err.println("연결 끊김. 현재 참가자 수 : " + users.size());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    cs.close();
                } catch (IOException e) {
                    System.err.println("서버 닫기오류> " + e.getMessage());
                    System.exit(-1);
                }
            }
        }

        private void joinRoom(String roomName) {
            // 방 검색 또는 생성
            currentRoom = findOrCreateRoom(roomName);

            // 방에 클라이언트 추가
            currentRoom.addMember(uid);

            // 방 전체에 입장 메시지 전송
            broadcastToRoom(currentRoom, uid + "님이 " + roomName + " 방에 입장했습니다.(현재 인원: \" + currentRoom.getMembers().size() + \"명)");
            System.out.println(roomName + " 방에 " + uid + "님 입장 (현재 인원: " + currentRoom.getMembers().size() + "명)");
            printDisplay(roomName + " 방에 " + uid + "님 입장 (현재 인원: " + currentRoom.getMembers().size() + "명)");
        }

        private Room findOrCreateRoom(String roomName) {
            synchronized (rooms) {
                for (Room room : rooms) {
                    if (room.getName().equals(roomName)) {
                        return room;
                    }
                }

                // 새로운 방 생성
                Room newRoom = new Room(roomName, parentFrame);
                rooms.add(newRoom);
                System.out.println("새로운 방 생성: " + roomName);
                printDisplay("새로운 방 생성: " + roomName);
                return newRoom;
            }
        }
        private void leaveRoom() {
            if (currentRoom == null) return;

            currentRoom.removeMember(uid);
            broadcastToRoom(currentRoom, uid + "님이 방을 퇴장했습니다.");

            // 방이 비었으면 삭제
            if (currentRoom.isEmpty()) {
                rooms.remove(currentRoom);
                System.out.println("방 [" + currentRoom.getName() + "]이 삭제되었습니다.");
            }

            currentRoom = null;
        }

        private void broadcastToRoom(Room room, String message) {
            // 방에 있는 모든 클라이언트에게 메시지 전송
            for (String memberUid : room.getMembers()) {
                ClientHandler member = findClientByUid(memberUid);
                if (member != null) {
                    System.out.println(memberUid + "에게 " + message + "전송");
                    member.sendMessage(message);
                }
            }
        }

        private ClientHandler findClientByUid(String uid) {
            // 벡터에서 해당 UID를 가진 클라이언트를 찾아 반환
            for (ClientHandler c : users) {
                if (c.uid.equals(uid)) {
                    return c;
                }
            }
            return null;
        }
        private void send(GameMsg msg) {
            try {
                out.writeObject(msg);
                out.flush();
            } catch (IOException e) {
                System.err.println("클라이언트 일반 전송 오류: " + e.getMessage());
            }
        }

        private void sendMessage(String msg) {
            //GameMsg gameMessage = new GameMsg(GameMsg.CHAT_MESSAGE, message, 0, null);
            //send(new GameMsg(uid, GameMsg.CHAT_MESSAGE, msg));
            User currentUser = new User(uid); // uid로 User 생성
            send(new GameMsg(GameMsg.CHAT_MESSAGE, msg, 0, currentUser));
        } //sendMessgae

        private void broadcasting(String msg) {
            for(ClientHandler c: users) {
                c.sendMessage(msg);
            }
        }

        @Override
        public void run() {
            receiveMessage(clientSocket);
        }
    } //Client핸들러


    private void disconnect() {
        try {
            acceptThread = null;
            serverSocket.close();
        } catch(IOException e ) {
            System.err.println("서버 소켓 닫기 오류> "+e.getMessage());
            System.exit(-1);
        }
    }

    private void printDisplay(String msg) {
        t_display.append(msg + "\n");
        t_display.setCaretPosition(t_display.getDocument().getLength());

    }

//    private class Room {
//        private String name; // 방 이름
//        private Vector<String> members; // 방에 있는 클라이언트 닉네임 목록
//
//        public Room(String name) {
//            this.name = name;
//            this.members = new Vector<>();
//        }
//
//        public String getName() {
//            return name;
//        }
//
//        public Vector<String> getMembers() {
//            return members;
//        }
//
//        public void addMember(String member) {
//            members.add(member);
//        }
//
//        public void removeMember(String member) {
//            members.remove(member);
//        }
//
//        public boolean isEmpty() {
//            return members.isEmpty();
//        }
//    }
}
