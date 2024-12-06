import java.io.Serializable;
import java.util.Vector;

public class GameMsg implements Serializable {
    private static final long serialVersionUID = 1L;

    public final static int LOGIN = 1;
    public final static int LOGIN_OK = 2;
    public final static int CHAT_MESSAGE = 3;
    public final static int LOGOUT = 4; // 로그아웃 모드

    public final static int ROOM_SELECT = 11;//방 선택
    public final static int ROOM_SELECT_OK = 12;
    public final static int ROOM_NEW_MEMBER = 13;
    public final static int USER_LIST_UPDATE = 14;
//    public static final int ROOM_MEMBER_LIST = 14;

    public int mode;   // 모드 값
    User user;  //유저 정보
    String message; //방 이름 or 채팅 메시지
    int time; //남은 시간(해당 라운드)

    public GameMsg(int mode, User user, String message, int time) {
        this.mode = mode;
        this.user = user;
        this.message = message;
        this.time = time;
    }

    // 로그인!! 로그아웃?
    public GameMsg(int mode, String name) {
        this.mode = mode;
        this.user = new User(name); // 이름으로 User 객체 생성
        System.out.println("로그인시 User 초기화 " + user.getName());
    }

    // LOGIN_OK
    public GameMsg(int mode, User user) {
        this.mode = mode;
        this.user = user;
    }

    //방 선택, ROOM_SELECT_OK
    public GameMsg(int mode, User user, String message) {
        this.mode = mode;
        this.user = user; // 이미 존재하는 User 객체 사용
        this.message = message; // 방 이름

        if (mode == ROOM_SELECT) {
            // 방 선택 요청 처리
            System.out.println("방 선택 요청: " + user.getName() + message);
        } else if (mode == ROOM_SELECT_OK) {
            // 방 선택 성공 메시지 처리
            System.out.println("방 선택 성공: " + user.getName() + " , 들어간 방 : " + message);
        }

//        user.setName(name);
//        this.user = new User(name); // uid를 이용해 User 생성
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

    @Override
    public String toString() {
        return "GameMsg{" +
                "mode=" + mode +
                ", message='" + message + '\'' +
                ", time=" + time +
                ", user=" + user +
                '}';
    }
}
