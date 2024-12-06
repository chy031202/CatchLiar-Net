import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class StartPanel extends JPanel {
    private JTextField t_nickname;
    private JButton b_start;

    private ClientManager clientManager;

    public StartPanel(ClientManager clientManager) {
        this.clientManager = clientManager;

        buildGUI();
    }

    private void buildGUI() {
        setBounds(50, 200, 800, 600);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("캐치라이어");
        title.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        JLabel nicknameLabel = new JLabel("닉네임");
        nicknameLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        t_nickname = new JTextField(15);
        t_nickname.setMaximumSize(t_nickname.getPreferredSize()); // 입력 필드의 크기를 고정
        t_nickname.setAlignmentX(JTextField.CENTER_ALIGNMENT);

        b_start = new JButton("시작");
        b_start.setAlignmentX(JButton.CENTER_ALIGNMENT);
        b_start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String nickname = t_nickname.getText();
                    if (nickname.isEmpty()) {
//                        JOptionPane.showMessageDialog(frame, "닉네임을 입력해주세요!", "경고", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    clientManager.connectToServer();
                    clientManager.sendNickname(nickname); // 닉네임 서버에 전송

                } catch (IOException ex) {
                    System.err.println(ex.getMessage());
                    System.exit(-1);
                }

            }
        });


        add(title);
        add(b_start);
        add(nicknameLabel);
        add(t_nickname);

    }
}
