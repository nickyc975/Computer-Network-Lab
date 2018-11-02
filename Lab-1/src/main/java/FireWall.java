import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class FireWall {
    private static final Set<Pattern> HOST_WHITE_LIST = new HashSet<>();
    private static final Set<Pattern> HOST_BLACK_LIST = new HashSet<>();
    private static final Set<Pattern> USER_WHITE_LIST = new HashSet<>();
    private static final Set<Pattern> USER_BLACK_LIST = new HashSet<>();

    public static void addHostWhiteListItem(String item) {
        HOST_WHITE_LIST.add(Pattern.compile(item));
    }

    public static void addHostBlackListItem(String item) {
        HOST_BLACK_LIST.add(Pattern.compile(item));
    }

    public static void addUserWhiteListItem(String item) {
        USER_WHITE_LIST.add(Pattern.compile(item));
    }

    public static void addUserBlackListItem(String item) {
        USER_BLACK_LIST.add(Pattern.compile(item));
    }

    public static boolean validateUser(String client) {
        if (USER_WHITE_LIST.size() > 0) {
            for (Pattern pattern : USER_WHITE_LIST) {
                if (pattern.matcher(client).find()) {
                    return true;
                }
            }
            return false;
        } else if (USER_BLACK_LIST.size() > 0) {
            for (Pattern pattern : USER_BLACK_LIST) {
                if (pattern.matcher(client).find()) {
                    return false;
                }
            }
            return true;
        }
        return true;
    }

    public static boolean validateHost(String host) {
        if (HOST_WHITE_LIST.size() > 0) {
            for (Pattern pattern : HOST_WHITE_LIST) {
                if (pattern.matcher(host).find()) {
                    return true;
                }
            }
            return false;
        } else if (HOST_BLACK_LIST.size() > 0) {
            for (Pattern pattern : HOST_BLACK_LIST) {
                if (pattern.matcher(host).find()) {
                    return false;
                }
            }
            return true;
        }
        return true;
    }
}
