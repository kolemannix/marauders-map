package kolemannix.com.marauderandroid;

/**
 * Created by knix on 9/21/15.
 */
public class MarauderProfile {
    public final String email;
    public final String nickname;
    public final int icon;

    public MarauderProfile() {
        this("hp@gmail.com", "hp", 0);
    }

    public MarauderProfile(String email, String nickname, int icon) {
        this.email = email;
        this.nickname = nickname;
        this.icon = icon;
    }

    public String[] toStringArray() {
        String[] result = new String[3];
        result[0] = email;
        result[1] = nickname;
        result[2] = Integer.toString(icon);
        return result;
    }

    public static MarauderProfile fromStringArray(String[] s) {
        return new MarauderProfile(s[0], s[1], Integer.parseInt(s[2]));
    }

}
