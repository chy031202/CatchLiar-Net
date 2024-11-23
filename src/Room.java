import javax.swing.*;
import java.util.Vector;

public class Room {
    private String name; // 방 이름
    private Vector<String> members; // 방에 있는 클라이언트 닉네임 목록
    private GameRoomPanel gameRoomPanel;  // 게임 방 패널

    public Room(String name, JFrame parentFrame) {
        this.name = name;
        this.members = new Vector<>();
        this.gameRoomPanel = new GameRoomPanel(parentFrame);
    }

    public String getName() {
        return name;
    }

    public Vector<String> getMembers() {
        return members;
    }

    public void addMember(String member) {
        members.add(member);
        // 유저가 방에 들어올 때마다 패널을 업데이트
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                gameRoomPanel.addUserToPanel(member);  // 유저 패널 추가
            }
        });

        System.out.println("addMember" + member);
    }

    public void removeMember(String member) {
        members.remove(member);
        // 유저가 방을 나갈 때 패널에서 제거
    }

    public boolean isEmpty() {
        return members.isEmpty();
    }
    // 게임 방 패널 반환
    public GameRoomPanel getGameRoomPanel() {
        return gameRoomPanel;
    }
}