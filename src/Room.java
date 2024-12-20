import java.io.Serializable;
import java.util.*;

public class Room implements Serializable {
    private String roomName; // 방 이름
    private Vector<User> members; // 방에 있는 클라이언트 닉네임 목록
    private Vector<User> readyUsers;
    public String Keywords;

    private int currentTurnIndex = -1; // 현재 그림을 그릴 사용자 인덱스
    private User currentTurnUser = null;
    private Map<String, Integer> voteCounts = new HashMap<>();

    public Room(String name) {
        this.roomName = name;
        this.members = new Vector<>();
        this.readyUsers = new Vector<>();
    }

    public void addMember(User user) {
        synchronized (members) {
            members.add(user);
            if (members.size() == 1) {
                // 첫 번째 사용자가 방에 들어오면 첫 턴으로 설정
                currentTurnIndex = 0;
            }
        }
        System.out.println("Room 에서 addMember : " + roomName + "에서 " + user.getName() + "님이 입장");
        System.out.println("현재 방에 있는 멤버들 : " + members);
    }

    public void removeMember(User user) {
        synchronized (members) {
            members.remove(user);
        }
        System.out.println("Room 에서 removeMember : " + roomName + "에서 " + user.getName() + "님이 퇴장");
        System.out.println("현재 방에 있는 멤버들 : " + members);
    }

    public void addReadyUser(User user) {
        synchronized (readyUsers) {
            readyUsers.add(user);
        }
        System.out.println("Room 에서 addReadyUser : " + roomName + "에서 " + user.getName() + "님이 준비");
        System.out.println("현재 준비한 멤버들 : " + readyUsers);
    }

    public void removeReadyUser(User user) {
        synchronized (readyUsers) {
            readyUsers.remove(user);
        }
        System.out.println("Room 에서 removeReadyUser : " + roomName + "에서 " + user.getName() + "님이 준비 해제");
        System.out.println("현재 준비한 멤버들 : " + readyUsers);
    }


    public String getRoomName() {
        return roomName;
    }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public Vector<User> getMembers() {
        synchronized (members) {
            return new Vector<>(members);
        }
    }
    public void setMembers(Vector<User> members) { this.members = members; }
    public int getMemberCount() { return members.size(); }

    public Vector<User> getReadyUsers() {
        synchronized (readyUsers) {
            return new Vector<>(readyUsers);
        }
    }

    public void setReadyUsers(Vector<User> readyUsers) {
        synchronized (readyUsers) {
            this.readyUsers = readyUsers;
        }
    }

    public String getKeyword() { return Keywords; }
    public void setKeyword(String keywords) { Keywords = keywords; }

    public boolean isEmpty() {
        return members.isEmpty();
    }

    public User getCurrentTurnUser() {
        if (members.isEmpty()) return null;
        if (currentTurnIndex == -1) return null;// 첫 턴이 설정되지 않았다면 null 반환
        System.out.println("getCurrentTurnUser에서 현재 members : " + members);
        return members.get(currentTurnIndex); // 현재 그림 그릴 사용자 반환
    }

    public void nextTurn() {
        if (members.isEmpty()) return; // 멤버가 없을 때 방어 코드

        currentTurnIndex = (currentTurnIndex + 1) % members.size(); // 다음 턴으로 전환
    }

    public void resetTurns() {
        currentTurnIndex = -1; // 턴 인덱스 초기화
        currentTurnUser = null;
        System.out.println("턴 초기화 완료: currentTurnIndex = " + currentTurnIndex);
    }

    //----------투표 관련
    public void addVote(String userName) {
        voteCounts.put(userName, voteCounts.getOrDefault(userName, 0) + 1);
    }

    public Map<String, Integer> getVoteCounts() {
        return voteCounts;
    }

    // 기존 투표 결과 초기화
    public void resetVoteCounts() {
        voteCounts.clear();
        System.out.println("[Room] 투표 결과 초기화 완료");
    }
}