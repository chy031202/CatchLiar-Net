import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class Server extends JFrame {
    private JTextArea t_display;
    private JButton b_connect, b_disconnect, b_exit;
    private ServerManager serverManager;

    public Server(int port) {
        super("캐치 라이어 서버");
        serverManager = new ServerManager(port, this);
        buildGUI();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void buildGUI() {
        setBounds(700, 200, 600, 400);
        add(createDisplayPanel(), BorderLayout.CENTER);
        add(createControlPanel(), BorderLayout.SOUTH);
    }

    private JPanel createDisplayPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        t_display = new JTextArea();
        t_display.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(t_display);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new GridLayout(1,0)); // 한 줄에 모든 영역을 붙여라.

        b_connect = new JButton("서버 시작");
        b_connect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 접속하기
                serverManager.startServer();

                b_connect.setEnabled(false);
                b_disconnect.setEnabled(true);
                b_exit.setEnabled(false);
            }
        });

        b_disconnect = new JButton("서버 종료");
        b_disconnect.setEnabled(false);
        b_disconnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 접속 끊기
                serverManager.stopServer();

                b_connect.setEnabled(true);
                b_disconnect.setEnabled(false);
                b_exit.setEnabled(true);
            }
        });

        b_exit = new JButton("종료");
        b_exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                // 종료
                serverManager.exit();
            }
        });

        panel.add(b_connect);
        panel.add(b_disconnect);
        panel.add(b_exit);

        return panel;
    }

    public void printDisplay(String msg) {
        t_display.append(msg + "\n");
        t_display.setCaretPosition(t_display.getDocument().getLength());
    }

    public static void main(String[] args){
        int port = 54321;
        Server server = new Server(port);
    }
}
