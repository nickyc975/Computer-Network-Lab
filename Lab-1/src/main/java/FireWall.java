import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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

    /**
     * Parse given file to build fire wall.
     *
     * @param filePath file that contains fire wall rules.
     * @throws IOException read file failed.
     */
    public static void parseFireWall(String filePath) throws IOException {
        String line;
        BufferedReader reader = new BufferedReader(new FileReader(new File(filePath)));
        while ((line = reader.readLine()) != null) {
            String[] content = line.split("\\s");
            if (content.length != 3) {
                continue;
            }

            switch (content[0]) {
                case "host":
                    switch (content[1]) {
                        case "white":
                            addHostWhiteListItem(content[2]);
                            break;
                        case "black":
                            addHostBlackListItem(content[2]);
                            break;
                        default:
                            break;
                    }
                    break;
                case "user":
                    switch (content[1]) {
                        case "white":
                            addUserWhiteListItem(content[2]);
                            break;
                        case "black":
                            addUserBlackListItem(content[2]);
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Validate user host of a request.
     *
     * @param user user host.
     * @return true if valid, false otherwise.
     */
    public static boolean validateUser(String user) {
        if (USER_WHITE_LIST.size() > 0) {
            for (Pattern pattern : USER_WHITE_LIST) {
                if (pattern.matcher(user).find()) {
                    return true;
                }
            }
            return false;
        } else if (USER_BLACK_LIST.size() > 0) {
            for (Pattern pattern : USER_BLACK_LIST) {
                if (pattern.matcher(user).find()) {
                    return false;
                }
            }
            return true;
        }
        return true;
    }

    /**
     * Validate server host of a request.
     *
     * @param host server host.
     * @return true if valid, false otherwise.
     */
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
