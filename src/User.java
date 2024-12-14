import java.io.Serializable;

public class User implements Serializable {

    private static final long serialVersionUID = 1L;
    private boolean isWinner = false;

    String name; //닉네임
    Room currentRoom;  //속한 방 정보
    boolean ready;  //준비 여부 // 0: 대기 중 // 1: 준비완료
    boolean isLiar = false;
//    private boolean isLoggedIn = true;

    public User(String name){
        this.name = name;
    }

    public void joinRoom(Room room) {
        if (currentRoom != null) {
            System.out.println(this.name + " currentRoom이 남아있어서 나감");
            currentRoom.removeMember(this);
        }
        currentRoom = room; // 유저가 특정 방에 들어감
        currentRoom.addMember(this); // 방에 유저를 추가
        System.out.println("User joinRoom : " + this.name + " , room : " + currentRoom.getRoomName());
    }

    public void leaveRoom() {
        if (currentRoom != null) {
            currentRoom.removeMember(this); // 방에서 유저 제거
            currentRoom = null; // 방 정보 초기화
        }
    }

    public void setReady() {
        if (currentRoom != null) {
            currentRoom.removeReadyUser(this);
        }
        ready = true;
        currentRoom.addReadyUser(this);
        System.out.println("User setReady : " + this.name + " , room : " + currentRoom.getRoomName());
    }

    public void setUnReady() {
        if (currentRoom != null) {
            currentRoom.removeReadyUser(this);
        }
        ready = false;
        currentRoom.removeReadyUser(this);
        System.out.println("User setNotReady : " + this.name + " , room : " + currentRoom.getRoomName());
    }

    public String getName() {
        return name;
    }
    public void setName(String name) { this.name = name; }

    public Room getCurrentRoom() { return currentRoom; }
    public void setCurrentRoom(Room room) { this.currentRoom = room; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return name != null && name.equals(user.name); // 이름이 같으면 동일한 객체로 간주
    }

//    public boolean getReady() { return ready; }
//    public void setReady(boolean ready) { this.ready = ready; }


//    public boolean isLoggedIn() {
//        return isLoggedIn;
//    }
//
//    public void logout() {
//        this.isLoggedIn = false;
//    }

    public boolean isWinner() {
        return isWinner;
    }

    // 승리 여부 설정
    public void setWinner(boolean winner) {
        this.isWinner = winner;
    }


}
