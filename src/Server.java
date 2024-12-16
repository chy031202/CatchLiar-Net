import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.List;
import java.io.IOException;

public class Server extends JFrame {
    private JTextArea t_display;
    private JButton b_connect, b_disconnect, b_exit;
    private ServerManager serverManager;
    private String currentFilter = "모두"; // 현재 필터 상태
    private Map<String, List<String>> logsByType; // 로그를 종류별로 저장

    public Server(int port) {
        super("캐치 라이어 서버");
        serverManager = new ServerManager(port, this);
        logsByType = new HashMap<>();
        initializeLogTypes();
        buildGUI();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void buildGUI() {
        setBounds(700, 200, 600, 400);
        add(createFilterPanel(), BorderLayout.NORTH);
        add(createDisplayPanel(), BorderLayout.CENTER);
        add(createControlPanel(), BorderLayout.SOUTH);
    }

    private void initializeLogTypes() {
        String[] logTypes = {"모두", "접속", "게임상태", "채팅+이모티콘", "페인팅", "투표"};
        for (String type : logTypes) {
            logsByType.put(type, new ArrayList<>());
        }
    }

    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT)); // 상단 필터 패널
        ButtonGroup group = new ButtonGroup();
        String[] logTypes = {"모두", "접속", "게임상태", "채팅+이모티콘", "페인팅", "투표"};

        for (String logType : logTypes) {
            JRadioButton radioButton = new JRadioButton(logType);
            if (logType.equals("모두")) radioButton.setSelected(true); // 기본 선택
            group.add(radioButton);
            panel.add(radioButton);

            radioButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    currentFilter = logType; // 선택된 필터 업데이트
                    updateDisplay(); // 화면 갱신
                }
            });
        }

        return panel;
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

    // 로그 출력 메서드
    public void printDisplay(String msg, String type) {
        logsByType.get("모두").add(msg); // 모든 로그에 추가
        if (logsByType.containsKey(type)) {
            logsByType.get(type).add(msg); // 해당 타입에 추가
        }

        if (currentFilter.equals("모두") || currentFilter.equals(type)) {
            t_display.append(msg + "\n"); // 현재 필터에 맞는 로그 출력
            t_display.setCaretPosition(t_display.getDocument().getLength());
        }
    }

    // 필터링된 로그를 출력
    private void updateDisplay() {
        t_display.setText(""); // 기존 로그 지우기
        List<String> filteredLogs = logsByType.get(currentFilter); // 선택된 필터의 로그 가져오기
        if (filteredLogs != null) {
            for (String log : filteredLogs) {
                t_display.append(log + "\n");
            }
        }
        t_display.setCaretPosition(t_display.getDocument().getLength());
    }

    public static void main(String[] args){
        String ip = "localhost"; // 기본 IP
        int port = 54321;        // 기본 포트

        // server.txt에서 서버 설정 읽기
        try (BufferedReader br = new BufferedReader(new FileReader("server.txt"))) {
            ip = br.readLine(); // 첫 번째 줄: IP 주소
            port = Integer.parseInt(br.readLine()); // 두 번째 줄: 포트 번호
            System.out.println("서버 IP: " + ip + ", 포트: " + port);
        } catch (IOException e) {
            System.err.println("서버 설정 파일을 읽을 수 없습니다. 기본 설정을 사용합니다.");
        }

        Server server = new Server(port);
        server.printDisplay("서버 IP: " + ip + ", 포트: " + port);
    }
}
