import java.io.Serializable;

public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    String name; //닉네임
    Room currentRoom;  //속한 방 정보
//    boolean ready;  //준비 여부
//    private boolean isLoggedIn = true;

    public User(String name){
        this.name = name;
    }

    public void joinRoom(Room room) {
        if (currentRoom != null) {
            currentRoom.removeMember(this.name);
        }
        currentRoom = room; // 유저가 특정 방에 들어감
        currentRoom.addMember(this.name); // 방에 유저를 추가
        System.out.println("User joinRoom : " + this.name + " , room : " + currentRoom.getRoomName());
    }

    public void leaveRoom() {
        if (currentRoom != null) {
            currentRoom.removeMember(this.getName()); // 방에서 유저 제거
            currentRoom = null; // 방 정보 초기화
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) { this.name = name; }

    public Room getCurrentRoom() { return currentRoom; }

    public void setCurrentRoom(Room room) { this.currentRoom = room; }


//    public boolean isLoggedIn() {
//        return isLoggedIn;
//    }
//
//    public void logout() {
//        this.isLoggedIn = false;
//    }

}
