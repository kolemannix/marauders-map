package kolemannix.com.marauderandroid;

/**
 * Created by knix on 9/21/15.
 */
public class MarauderProfile {
    public final String email;
    public final String password;
    public final String nickname;
    public final int icon;

    public MarauderProfile() {
        this("hp@gmail.com", "jameslily420", "hp", 0);
    }

    public MarauderProfile(String email, String password, String nickname, int icon) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.icon = icon;
    }
}
