import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class StartPanel extends JPanel {
    private JTextField t_nickname;

    private ClientManager clientManager;

    public StartPanel(ClientManager clientManager) {
        this.clientManager = clientManager;

        buildGUI();
    }

    public class BackgroundPanel extends JPanel {
        private Image background;

        // 생성자에서 배경 이미지 설정
        public BackgroundPanel(String imagePath) {
            background = new ImageIcon(getClass().getResource(imagePath)).getImage();
            setLayout(null); // 컴포넌트 배치를 자유롭게 설정
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            // 배경 이미지 그리기
            g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
        }
    }

    private void buildGUI() {
        setBounds(50, 200, 800, 600);
        //setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setLayout(new BorderLayout());


        BackgroundPanel backgroundPanel = new BackgroundPanel("/images/main.png");
        backgroundPanel.setLayout(new BoxLayout(backgroundPanel, BoxLayout.Y_AXIS)); // BoxLayout 사용

        JLabel nicknameLabel = new JLabel("닉네임");
        nicknameLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        t_nickname = new JTextField(10);
        t_nickname.setMaximumSize(t_nickname.getPreferredSize()); // 입력 필드의 크기를 고정
        t_nickname.addActionListener(e -> connectAndSendNickname());
        t_nickname.setAlignmentX(JTextField.CENTER_ALIGNMENT);

        ImageIcon originalIcon = new ImageIcon(getClass().getResource("/images/start.png"));
        Image resizedImage = originalIcon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
        ImageIcon resizedIcon = new ImageIcon(resizedImage);
        JLabel startButton = new JLabel(resizedIcon);
        // MouseListener 추가
        startButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                connectAndSendNickname();
            }
        });
        startButton.setAlignmentX(JTextField.CENTER_ALIGNMENT);

        backgroundPanel.add(Box.createVerticalGlue()); // 컴포넌트를 수직으로 가운데 정렬
        backgroundPanel.add(Box.createVerticalStrut(180)); // 컴포넌트 간 여백 추가
        backgroundPanel.add(startButton);
        backgroundPanel.add(Box.createVerticalStrut(70));
        backgroundPanel.add(nicknameLabel);
        backgroundPanel.add(Box.createVerticalStrut(10));
        backgroundPanel.add(t_nickname);
        backgroundPanel.add(Box.createVerticalGlue());

        // 배경 패널을 프레임에 추가
        add(backgroundPanel, BorderLayout.CENTER);

    }

    private void connectAndSendNickname() {
        try {
            String nickname = t_nickname.getText();
            if (nickname.isEmpty()) {
                // JOptionPane.showMessageDialog(this, "닉네임을 입력해주세요!", "경고", JOptionPane.WARNING_MESSAGE);
                return;
            }
            clientManager.connectToServer();
            clientManager.sendNickname(nickname); // 닉네임 서버에 전송
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
            System.exit(-1);
        }
    }
}
