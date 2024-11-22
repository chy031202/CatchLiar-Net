import javax.swing.*;
import java.awt.*;
import java.util.Random;

//다이얼로그로 라이어나 일반인을 뽑음
public class MessageDialog extends JDialog {
    public MessageDialog(JFrame parent) {
        super(parent, "키워드 제공", true);

        // 랜덤 메시지 생성
        String[] messages = {
                "당신은 라이어 입니다",
                "에펠탑",
                "피사의 사탑",
                "경복궁"
        };

        Random random = new Random();
        String randomMessage = messages[random.nextInt(messages.length)];

        JLabel label = new JLabel(randomMessage, SwingConstants.CENTER);
        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setLayout(new BorderLayout());
        add(label, BorderLayout.CENTER);


        setSize(300, 200);
        setLocationRelativeTo(parent);
    }

    public static void showRandomMessage(JFrame parent) {
        new MessageDialog(parent).setVisible(true);
    }
}
