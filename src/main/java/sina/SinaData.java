package sina;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <p>类描述</p>
 * 创建日期 2017/11/11
 *
 * @author tianshangdeyun(wangbo@eefung.com)
 * @since 1.0.1
 */
public class SinaData {
    /**
     * 微博唯一的id
     */
    String id;
    String userId;
    String username;
    long time;
    boolean isBigV;
    int commentNum;
    int transmitNum;
    int likeNum;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getLikeNum() {
        return likeNum;
    }

    public void setLikeNum(int likeNum) {
        this.likeNum = likeNum;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isBigV() {
        return isBigV;
    }

    public void setBigV(boolean bigV) {
        isBigV = bigV;
    }

    public int getCommentNum() {
        return commentNum;
    }

    public void setCommentNum(int commentNum) {
        this.commentNum = commentNum;
    }

    public int getTransmitNum() {
        return transmitNum;
    }

    public void setTransmitNum(int transmitNum) {
        this.transmitNum = transmitNum;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String toString() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");

        return String.format("id:" + id + "\n" +
                                 "userId:" + userId + "\n" +
                                 "username:" + username + "\n" +
                                 "isBigV:" + isBigV + "\n" +
                                 "commentNum:" + commentNum + "\n" +
                                 "transmitNum:" + transmitNum + "\n" +
                                 "likeNum:" + likeNum + "\n" +
                                 "time:" + format.format(new Date(time)));

    }
}
