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

    private JButton b_connect, b_disconnect, b_exit;
    private Thread acceptThread = null;

    private Vector<ClientHandler> users = new Vector<ClientHandler>();

    public Server(int port){
        this.port = port;
        frame = new JFrame( "P2P ChatServer");
        frame.setBounds(700, 200, 600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

                ClientHandler cHandler = new ClientHandler(clientSocket);
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
        private BufferedWriter out;

        private String uid;

        public ClientHandler (Socket clientSocket) {
            this.clientSocket = clientSocket;

        }

        private void receiveMessage(Socket cs) {

            /* 클라이언트 소켓에서 입력 스트림 생성 */
            try {

                BufferedReader in = new BufferedReader(new InputStreamReader(cs.getInputStream(), "UTF-8"));
                out = new BufferedWriter(new OutputStreamWriter(cs.getOutputStream(), "UTF-8"));
                String message;


                while ((message = in.readLine()) !=null ) {
                    if(message.contains("/uid:")) {
                        String[] tok = message.split(":");
                        uid = tok[1];
                        printDisplay("새 참가자" + uid);
                        printDisplay("현재 참가자 수" + users.size());
                        continue;
                    }

                    message = uid + ": "+ message;
                    printDisplay(message);
                    broadcasting(message);

                }

                users.removeElement(this);
                printDisplay(uid+"퇴장. 현재 참가자 수 : "+ users.size());
            } catch (IOException e) {
                users.removeElement(this);
                System.err.println("연결 끊김. 현재 참가자 수 : " + users.size());
            }finally {
                try {
                    cs.close();
                } catch (IOException e) {
                    System.err.println("서버 닫기오류> " + e.getMessage());
                    System.exit(-1);
                }
            }
        }


        private void sendMessage(String msg) {
            try {
                //int message = Integer.parseInt(messageText);
                ((BufferedWriter) out).write(msg+"\n");
                out.flush();
                System.out.println(out);
            } catch (IOException e) {
                System.err.println("서버 반향 전송 오류> "+ e.getMessage());
            }
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
}
