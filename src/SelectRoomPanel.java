import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SelectRoomPanel extends JPanel {
    private JButton foodRoomButton;
    private JButton placeRoomButton;
    private JButton animalRoomButton;
    private JButton characterRoomButton;

    public SelectRoomPanel() {
        setLayout(new BorderLayout());


        JLabel title = new JLabel("키워드(방) 선택");
        title.setHorizontalAlignment(JLabel.CENTER);
        title.setFont(new Font("맑은 고딕", Font.BOLD, 30)); // 폰트 크기 조정
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0)); // 여백 추가
        title.setForeground(Color.WHITE); // 글자 색상

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
                Container parent = getParent();
                if (parent instanceof JPanel) {
                    CardLayout cl = (CardLayout) parent.getLayout();
                    cl.show(parent, "gameRoomPanel"); // "gameRoomPanel"로 이동
                }
            }
        });


        buttonPanel.add(foodRoomButton);
        buttonPanel.add(placeRoomButton);
        buttonPanel.add(animalRoomButton);
        buttonPanel.add(characterRoomButton);

        add(title, BorderLayout.NORTH);
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
}
