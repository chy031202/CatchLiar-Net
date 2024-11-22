import javax.swing.*;
import java.awt.*;


public class GameRoomPanel extends JPanel {
    public GameRoomPanel(JFrame parentFrame) {
        setLayout(new BorderLayout());



        JLabel title = new JLabel("00방(ex명소)", SwingConstants.CENTER);
        add(title, BorderLayout.NORTH);

        add(UserSidePanel(), BorderLayout.WEST);

        add(CenterPanel(), BorderLayout.CENTER);
        // 오른쪽 채팅 및 이모티콘 패널
        add(RightPanel(), BorderLayout.EAST);

        //MessageDialog.showRandomMessage(parentFrame);


    }
    private JPanel CenterPanel(){
        JPanel cneterpanel = new JPanel();
        cneterpanel.setLayout(new BorderLayout());

        JPanel gamepanel = GamePanel();
        JPanel Itempanel = ItemPanel();

        cneterpanel.add(gamepanel, BorderLayout.CENTER);
        cneterpanel.add(Itempanel, BorderLayout.SOUTH);

        return cneterpanel;
    }

    private JPanel RightPanel() {
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout()); // 위아래로 나눔

        // 위쪽 채팅 패널
        JPanel chatPanel = ChatPanel();
        rightPanel.add(chatPanel, BorderLayout.NORTH);

        // 아래쪽 이모티콘 패널
        JPanel imgPanel = ImgPanel();
        rightPanel.add(imgPanel, BorderLayout.SOUTH);

        return rightPanel;
    }

    private JPanel UserSidePanel(){
        JPanel userSidePanel = new JPanel();
        userSidePanel.setLayout(new BoxLayout(userSidePanel, BoxLayout.Y_AXIS));
        userSidePanel.setBackground(Color.CYAN);

        JLabel user = new JLabel("유저 패널");

        userSidePanel.add(user);
        return userSidePanel;
    }

    private JPanel ChatPanel(){
        JPanel chatPanel = new JPanel();
        JLabel user = new JLabel("채팅 패널");
        chatPanel.setBackground(Color.ORANGE);

        chatPanel.add(user);
        return chatPanel;
    }

    private JPanel ImgPanel(){
        JPanel imgPanel = new JPanel();
        JLabel user = new JLabel("이모티콘 패널");
        imgPanel.setBackground(Color.pink);

        imgPanel.add(user);
        return imgPanel;
    }

    //색깔 같은거 있는 패널
    private JPanel ItemPanel(){
        JPanel itemPanel = new JPanel();
        itemPanel.setLayout(new BoxLayout(itemPanel, BoxLayout.Y_AXIS));
        itemPanel.setBackground(Color.lightGray);
        JLabel user = new JLabel("색깔 선택");

        itemPanel.add(user);
        return  itemPanel;
    }

    private JPanel GamePanel(){
        JPanel gamePanel = new JPanel();
        gamePanel.setBackground(Color.WHITE);
        JLabel user = new JLabel("메인 패널");

        gamePanel.add(user);
        return  gamePanel;
    }
}
