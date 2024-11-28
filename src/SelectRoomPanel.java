import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class SelectRoomPanel extends JPanel {
    private JButton foodRoomButton;
    private JButton placeRoomButton;
    private JButton animalRoomButton;
    private JButton characterRoomButton;
    private Client client;

    public SelectRoomPanel(Client client) {
        this.client = client;
        setLayout(new BorderLayout());


        ImageIcon roomLogoIcon = new ImageIcon(SelectRoomPanel.class.getResource("/images/roomLogo.png"));
        Image originalImage = roomLogoIcon.getImage(); // 원본 이미지 가져오기
        Image resizedImage = originalImage.getScaledInstance(400, 130, Image.SCALE_SMOOTH); // 새 크기로 조정
        ImageIcon resizedIcon = new ImageIcon(resizedImage); // 크기 조정된 이미지를 새로운 ImageIcon으로 설정
        JLabel title = new JLabel(resizedIcon); // JLabel에 설정
        title.setOpaque(true);
        title.setBackground(new Color(60, 30, 30)); // 배경색 설정
        title.setHorizontalAlignment(JLabel.CENTER);
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0)); // 여백 추가
        add(title, BorderLayout.NORTH);


        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 2, 20, 20)); // 2x2 배열, 간격 20px
        buttonPanel.setBackground(new Color(60, 30, 30)); // 배경색 설정

        // 각 방 버튼 생성
        foodRoomButton = createRoomButton("키워드: 음식\n1번방");
        placeRoomButton = createRoomButton("키워드: 명소\n2번방");
        animalRoomButton = createRoomButton("키워드: 동물\n3번방");
        characterRoomButton = createRoomButton("키워드: 캐릭터\n4번방");

        foodRoomButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
        placeRoomButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
        animalRoomButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
        characterRoomButton.setAlignmentX(JButton.CENTER_ALIGNMENT);

        foodRoomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String roomName = "food";
                sendRoomSelection(roomName); // 방 선택 서버로 전송

                Container parent = getParent();
                if (parent instanceof JPanel) {
                    //CardLayout cl = (CardLayout) parent.getLayout();
                    // GameRoomPanel을 생성하여 방 이름 전달
                    JPanel mainPanel = (JPanel) parent;
                    JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(parent);

                    GameRoomPanel gameRoomPanel = new GameRoomPanel(frame, roomName);
                    mainPanel.add(gameRoomPanel, "gameRoomPanel");

                    CardLayout cl = (CardLayout) parent.getLayout();
                    cl.show(parent, "gameRoomPanel");
                    // GameRoomPanel로 이동 후 MessageDialog 호출
//                    SwingUtilities.invokeLater(() -> MessageDialog.showRandomMessage((JFrame) SwingUtilities.getWindowAncestor(parent)));
                }
            }
        });
        placeRoomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String roomName = "place";
                sendRoomSelection(roomName); // 방 선택 서버로 전송

                Container parent = getParent();
                if (parent instanceof JPanel) {
                    JPanel mainPanel = (JPanel) parent;
                    JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(parent);

                    GameRoomPanel gameRoomPanel = new GameRoomPanel(frame, roomName);
                    mainPanel.add(gameRoomPanel, "gameRoomPanel");

                    CardLayout cl = (CardLayout) parent.getLayout();
                    cl.show(parent, "gameRoomPanel");
                    // GameRoomPanel로 이동 후 MessageDialog 호출
//                    SwingUtilities.invokeLater(() -> MessageDialog.showRandomMessage((JFrame) SwingUtilities.getWindowAncestor(parent)));
                }
            }
        });
        animalRoomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String roomName = "animal";
                sendRoomSelection(roomName); // 방 선택 서버로 전송

                Container parent = getParent();
                if (parent instanceof JPanel) {
                    JPanel mainPanel = (JPanel) parent;
                    JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(parent);

                    GameRoomPanel gameRoomPanel = new GameRoomPanel(frame, roomName);
                    mainPanel.add(gameRoomPanel, "gameRoomPanel");

                    CardLayout cl = (CardLayout) parent.getLayout();
                    cl.show(parent, "gameRoomPanel");
                    // GameRoomPanel로 이동 후 MessageDialog 호출
//                    SwingUtilities.invokeLater(() -> MessageDialog.showRandomMessage((JFrame) SwingUtilities.getWindowAncestor(parent)));
                }
            }
        });
        characterRoomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String roomName = "animal";
                sendRoomSelection(roomName); // 방 선택 서버로 전송

                Container parent = getParent();
                if (parent instanceof JPanel) {
                    JPanel mainPanel = (JPanel) parent;
                    JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(parent);

                    GameRoomPanel gameRoomPanel = new GameRoomPanel(frame, roomName);
                    mainPanel.add(gameRoomPanel, "gameRoomPanel");

                    CardLayout cl = (CardLayout) parent.getLayout();
                    cl.show(parent, "gameRoomPanel");
                    // GameRoomPanel로 이동 후 MessageDialog 호출
//                    SwingUtilities.invokeLater(() -> MessageDialog.showRandomMessage((JFrame) SwingUtilities.getWindowAncestor(parent)));
                }
            }
        });


        buttonPanel.add(foodRoomButton);
        buttonPanel.add(placeRoomButton);
        buttonPanel.add(animalRoomButton);
        buttonPanel.add(characterRoomButton);

        //add(title, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
    }
    private JButton createRoomButton(String text) {
        JButton button = new JButton("<html><center>" + text.replace("\n", "<br>") + "</center></html>"); // 줄바꿈 HTML 사용
        button.setFont(new Font("맑은 고딕", Font.BOLD, 20)); // 폰트 설정
        button.setFocusPainted(false);
        button.setBackground(Color.WHITE); // 버튼 배경색
        button.setForeground(Color.BLACK); // 버튼 텍스트 색상
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2)); // 테두리 추가
        button.setPreferredSize(new Dimension(200, 100)); // 크기 지정
        return button;

        //게임 시작 화면으로 이동
        //방 선택 패널 이동


    }
    private void sendRoomSelection(String roomName) {
        GameMsg roomSelectionMsg = new GameMsg(client.getUid(), GameMsg.ROOM_SELECT, roomName);
        client.send(roomSelectionMsg);
    }
}
