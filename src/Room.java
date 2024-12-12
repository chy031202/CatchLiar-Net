import javax.swing.*;
import java.io.Serializable;
import java.util.Vector;

public class Room implements Serializable {
    private String roomName; // 방 이름
    private Vector<User> members; // 방에 있는 클라이언트 닉네임 목록
    private Vector<User> readyUsers;
    public String Keywords;

    private int currentTurnIndex = 0; // 현재 그림을 그릴 사용자 인덱스

    public Room(String name) {
        this.roomName = name;
        this.members = new Vector<>();
        this.readyUsers = new Vector<>();
    }

    public void addMember(User user) {
        members.add(user);
        if (members.size() == 1) {
            // 첫 번째 사용자가 방에 들어오면 첫 턴으로 설정
            currentTurnIndex = 0;
        }
        System.out.println("Room 에서 addMember : " + roomName + "에서 " + user.getName() + "님이 입장");
        System.out.println("현재 방에 있는 멤버들 : " + members);
    }

    public void removeMember(User user) {
        members.remove(user);
        System.out.println("Room 에서 removeMember : " + roomName + "에서 " + user.getName() + "님이 퇴장");
    }

    public void addReadyUser(User user) {
        readyUsers.add(user);
        System.out.println("Room 에서 addReadyUser : " + roomName + "에서 " + user.getName() + "님이 준비");
        System.out.println("현재 준비한 멤버들 : " + members);
    }

    public void removeReadyUser(User user) {
        readyUsers.remove(user);
        System.out.println("Room 에서 removeReadyUser : " + roomName + "에서 " + user.getName() + "님이 준비 해제");
        System.out.println("현재 준비한 멤버들 : " + members);
    }


    public String getRoomName() {
        return roomName;
    }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public Vector<User> getMembers() { return members; }
    public void setMembers(Vector<User> members) { this.members = members; }
    public int getMemberCount() { return members.size(); }

    public Vector<User> getReadyUsers() { return readyUsers; }
    public void setReadyUsers(Vector<User> readyUsers) { this.readyUsers = readyUsers; }

    public String getKeyword() { return Keywords; }
    public void setKeyword(String keywords) { Keywords = keywords; }

    public boolean isEmpty() {
        return members.isEmpty();
    }

    public User getCurrentTurnUser() {
        if (members.isEmpty()) return null;
        return members.get(currentTurnIndex); // 현재 그림 그릴 사용자 반환
    }

    public void nextTurn() {
        if (!members.isEmpty()) {
            currentTurnIndex = (currentTurnIndex + 1) % members.size(); // 순환 인덱스
        }
    }

}