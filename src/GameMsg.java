import java.awt.*;
import java.io.Serializable;
import java.util.Vector;

public class GameMsg implements Serializable {
    private static final long serialVersionUID = 1L;

    public final static int LOGIN = 1;
    public final static int LOGIN_OK = 2;
    public final static int LOGOUT = 4; // 로그아웃 모드

    public final static int ROOM_SELECT = 11;//방 선택
    public final static int ROOM_NEW_MEMBER = 12;
    public final static int ROOM_SELECT_DENIED = 13;
    public final static int ROOM_EXIT = 14;

    public final static int CHAT_MESSAGE = 21;
    public final static int CHAT_EMOTICON = 22;

    public final static int GAME_READY_AVAILABLE = 31;
    public final static int GAME_READY = 32;
    public final static int GAME_UN_READY = 33;
    public final static int GAME_READY_OK = 34;
    public final static int GAME_UN_READY_OK = 35;

    public final static int DRAW_ACTION = 41;

    public final static int GAME_START = 51;
    public final static int LIAR_NOTIFICATION = 52;
    public final static int KEYWORD_NOTIFICATION = 53;
    public final static int GAME_END = 54;
    public final static int TIME = 55;
    public final static int GAME_RETRY = 56;

    public final static int VOTE = 61;
    public final static int VOTE_RESULT = 62;

    public int mode;   // 모드 값
    User user;  //유저 정보
    Room room;
    Vector<User> readyUsers;
    Vector<User> userNames;
    String message; //방 이름 or 채팅 메시지
    int time; //남은 시간(해당 라운드)
    private Paint paintData; // 그림 데이터용 필드 추가
    String votedUser; //투표된 사용자 이름
    private String resultMessage; // 최종 결과 메시지

    private boolean isWinner; // 승리 여부
    private boolean isLiar;   // 라이어 여부
    //private String resultMessage; // 결과 메시지

    //투표 관련 플래그
    private boolean isVoteStart; // 투표 시작 여부

    public GameMsg(int mode, Paint paintData) {
        this.mode = mode;
        this.paintData = paintData;
    }

    public Paint getPaintData() {
        return paintData;
    }

    public GameMsg(int mode, User user, String message, int time, Vector<User> userNames) {
        this.mode = mode;
        this.user = user;
        this.message = message;
        this.time = time;
        this.userNames = userNames;
    }

    // LOGIN
    public GameMsg(int mode, String name) {
        this.mode = mode;
        this.user = new User(name); // 이름으로 User 객체 생성
        System.out.println("로그인시 User 초기화되는지 확인 " + user.getName());
    }

    // LOGIN_OK, ROOM_SELECT_DENIED, GAME_READY, LIAR_NOTIFICATION, GAME_READY_OK, GAME_UN_READY_OK, LOGOUT
    public GameMsg(int mode, User user) {
        this.mode = mode;
        this.user = user;
    }

    // ROOM_SELECT, ROOM_SELECT_OK, NEW_MEMBER, CHAT_MESSAGE, KEYWORD_NOTIFICATION, CHAT_EMOTICON
    public GameMsg(int mode, User user, String message) {
        this.mode = mode;
        this.user = user; // 전에 생성한 User 객체 사용할 것
        this.message = message;

//        if (mode == ROOM_SELECT) {
//            this.message = message;
//            // 방 선택 요청 처리
//            System.out.println("방 선택 요청: " + user.getName() + message);
//        } else if (mode == ROOM_SELECT_OK) {
//            this.message = message;
//            // 방 선택 성공 메시지 처리
//            System.out.println("방 선택 성공: " + user.getName() + " , 들어간 방 : " + message);
//        }
//        else if (mode == VOTE){
//            this.votedUser = message;
//            System.out.println("투표 값" + message);
//        }
    }

//    public GameMsg(int mode, User user, String message, Vector<User> userNames) {
//        this.mode = mode;
//        this.user = user; // 전에 생성한 User 객체 사용할 것
//        this.message = message;
//        this.userNames = userNames;
//    }

    // GAME_READY_AVAILABLE
    public GameMsg(int mode) {
        this.mode = mode;
    }

    public GameMsg(int mode, User user, Room room) {
        this.mode = mode;
        this.user = user;
        this.room = room;
    }

    public GameMsg(int mode, User user, Vector<User> userNames, Vector<User> readyUsers) {
        this.mode = mode;
        this.user = user;
        this.userNames = userNames;
        this.readyUsers = readyUsers;
    }

    // GAME_READY, GAME_UN_READY
    public GameMsg(int mode, User user, Vector<User> readyUsers) {
        this.mode = mode;
        this.user = user;
        this.readyUsers = readyUsers;
    }

    // GAME_START
    public GameMsg(int mode, Vector<User> readyUsers, Vector<User> userNames) {
        this.mode = mode;
        this.readyUsers = readyUsers;
        this.userNames = userNames;
    }

    //-------투표 관련
    public GameMsg(int mode, User user, String resultMessage, boolean isWinner) {
        this.mode = mode;
        this.user = user;
        this.resultMessage = resultMessage;
        this.isWinner = isWinner;
    }

    public String getVotedUser() {
        return votedUser;
    }

    public void setVotedUser(String votedUser) {
        this.votedUser = votedUser;
    }

    // 결과 메시지 반환
    public String getResultMessage() {
        return resultMessage;
    }

    public boolean isWinner() {
        return isWinner;
    }

    public boolean isVoteStart() {
        return isVoteStart;
    }

    public void setVoteStart(boolean isVoteStart) {
        this.isVoteStart = isVoteStart;
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



    public static class Builder {
        private int mode;
        private User user;
        private String votedUser;
        private String message;
        private int time;
        private boolean isVoteStart;


    }

}
