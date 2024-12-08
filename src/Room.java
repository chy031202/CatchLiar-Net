import javax.swing.*;
import java.io.Serializable;
import java.util.Vector;

public class Room implements Serializable {
    private String roomName; // 방 이름
    private Vector<User> members; // 방에 있는 클라이언트 닉네임 목록

    public Room(String name) {
        this.roomName = name;
        this.members = new Vector<>();
    }

    public void addMember(User user) {
        members.add(user);
        System.out.println("Room 에서 addMember : " + roomName + "에서 " + user.getName() + "님이 입장");
        System.out.println("현재 방에 있는 멤버들 : " + members);
    }

    public void removeMember(User user) {
        members.remove(user);
        System.out.println("Room 에서 removeMember : " + roomName + "에서 " + user.getName() + "님이 퇴장");
    }


    public String getRoomName() {
        return roomName;
    }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public Vector<User> getMembers() {
        return members;
    }
    public void setMembers(Vector<User> members) { this.members = members; }

    public int getMemberCount() { return members.size(); }
    public boolean isEmpty() {
        return members.isEmpty();
    }

}