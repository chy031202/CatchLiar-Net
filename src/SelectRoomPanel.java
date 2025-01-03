import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SelectRoomPanel extends JPanel {
    private ClientManager clientManager;
    private JButton foodRoomButton, placeRoomButton, animalRoomButton, characterRoomButton;

    public SelectRoomPanel(ClientManager clientManager) {
        this.clientManager = clientManager;
        buildGUI();
    }

    private void buildGUI() {
        setBounds(50, 200, 800, 600);
        setLayout(new BorderLayout());
        add(createTitlePanel(), BorderLayout.NORTH);
        add(createButtonPanel(), BorderLayout.CENTER);
    }

    private JPanel createTitlePanel() {
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(64,48,47)); // 배경색 설정

        ImageIcon roomLogoIcon = new ImageIcon(SelectRoomPanel.class.getResource("/images/roomLogo.png"));
        Image originalImage = roomLogoIcon.getImage(); // 원본 이미지 가져오기
        Image resizedImage = originalImage.getScaledInstance(400, 95, Image.SCALE_SMOOTH); // 새 크기로 조정
        ImageIcon resizedIcon = new ImageIcon(resizedImage); // 크기 조정된 이미지를 새로운 ImageIcon으로 설정
        JLabel title = new JLabel(resizedIcon); // JLabel에 설정
        title.setOpaque(true);
        title.setBackground(new Color(64,48,47)); // 배경색 설정
        title.setHorizontalAlignment(JLabel.CENTER);
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0)); // 여백 추

        titlePanel.add(title);
        return titlePanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 2, 20, 20)); // 2x2 배열, 간격 20px
        buttonPanel.setBackground(new Color(64,48,47)); // 배경색 설정

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
                clientManager.sendRoomSelection(roomName); // 방 선택 서버로 전송
            }
        });
        placeRoomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String roomName = "place";
                clientManager.sendRoomSelection(roomName); // 방 선택 서버로 전송
            }
        });
        animalRoomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String roomName = "animal";
                clientManager.sendRoomSelection(roomName); // 방 선택 서버로 전송
            }
        });
        characterRoomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String roomName = "character";
                clientManager.sendRoomSelection(roomName); // 방 선택 서버로 전송
            }
        });

        buttonPanel.add(foodRoomButton);
        buttonPanel.add(placeRoomButton);
        buttonPanel.add(animalRoomButton);
        buttonPanel.add(characterRoomButton);

        return buttonPanel;
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
    }
}
