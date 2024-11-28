import java.io.Serializable;

public class GameMsg implements Serializable {
    private static final long serialVersionUID = 1L;

    public final static int LOGIN = 1;
    public final static int CHAT_MESSAGE = 2;
    //private static final int name = ;

    public int mode;   // 모드 값

    String code;
    String message; //메시지
    int time; //남은 시간(해당 라운드)
    User user;  //유저 정보


    public GameMsg(int mode, String msg, int time, User user) {
        this.mode = mode;
        this.message = msg;
        this.time = time;
        this.user = user;
    }

    public GameMsg(String uid, int mode, String message) {
        this.mode = mode;
        this.message = message;
        this.user = new User(uid); // uid를 이용해 User 생성
    }


    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public String getMsg() {
        return message;
    }

    public void setMsg(String msg) {
        this.message = msg;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
