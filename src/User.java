import java.io.Serializable;

public class User implements Serializable {

    //public final static int ROOM_SELECT = 11;

    private static final long serialVersionUID = 1L;

    String name; //닉네임
    boolean ready;  //준비 여부
    Room room;  //속한 방 정보
    int loca; //플레이어 방 위치
    boolean result; //승패

    public User(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    private boolean isLoggedIn = true;

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void logout() {
        this.isLoggedIn = false;
    }

}
