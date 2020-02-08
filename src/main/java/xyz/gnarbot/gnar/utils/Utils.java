package xyz.gnarbot.gnar.utils;

import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.json.JSONTokener;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    private static final Pattern TIME_PATTERN =
            Pattern.compile("(-?\\d+)\\s*((?:d(?:ay(?:s)?)?)|(?:h(?:our(?:s)?)?)|(?:m(?:in(?:ute(?:s)?)?)?)|(?:s(?:ec(?:ond(?:s)?)?)?))?");

    // https://regex101.com/r/VXEl27/1/
    private static final Pattern ARGUMENT_PATTERN = Pattern.compile("`{3}(?:\\w+\\n)?([\\s\\S]*?)`{3}|`([^`]+)`|(\\S+)");

    public static String getTime(long ms) {
        long s = ms / 1000;
        long m = s / 60;
        long h = m / 60;
        long d = h / 24;

        return d + "d " + h % 24 + "h " + m % 60 + "m " + s % 60 + "s";
    }

    public static long parseTime(String s) {
        s = s.toLowerCase();
        long ms = 0;

        Matcher matcher = TIME_PATTERN.matcher(s);

        while (matcher.find()) {
            String numStr = matcher.group(1);
            String unitStr = matcher.group(2);

            TimeUnit unit;
            if (unitStr == null) {
                unit = TimeUnit.SECONDS;
            } else switch (unitStr) {
                case "d":
                case "day":
                case "days":
                    unit = TimeUnit.DAYS;
                    break;
                case "h":
                case "hour":
                case "hours":
                    unit = TimeUnit.HOURS;
                    break;
                case "m":
                case "min":
                case "minute":
                case "minutes":
                    unit = TimeUnit.MINUTES;
                    break;
                case "s":
                case "sec":
                case "second":
                case "seconds":
                default:
                    unit = TimeUnit.SECONDS;
                    break;
            }
            ms += unit.toMillis(Long.parseLong(numStr));
        }
        return ms;
    }

    public static String[] stringSplit(String s) {
        List<String> parts = new ArrayList<>();

        Matcher matcher = ARGUMENT_PATTERN.matcher(s);
        outer: while (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                String match = matcher.group(i);
                if (match != null) {
                    parts.add(StringUtils.stripEnd(match, null));
                    continue outer;
                }
            }
        }

        return parts.toArray(new String[parts.size()]);
    }

    public static String getTimestamp(long ms) {
        long s = ms / 1000;
        long m = s / 60;
        long h = m / 60;

        if (h > 0) {
            return String.format("%02d:%02d:%02d", h, m % 60, s % 60);
        } else {
            return String.format("%02d:%02d", m, s % 60);
        }
    }

    @Nullable
    public static String hasteBin(String content) {
        Request request = new Request.Builder().url("https://hastebin.com/documents")
                .header("User-Agent", "Gnar")
                .header("Content-Type", "text/plain")
                .post(RequestBody.create(null, content))
                .build();

        try (Response response = HttpUtils.CLIENT.newCall(request).execute()) {
            ResponseBody body = response.body();
            if (body == null) return null;

            JSONObject jso = new JSONObject(new JSONTokener(body.byteStream()));

            response.close();

            return "https://hastebin.com/" + jso.get("key");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
