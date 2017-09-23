package xyz.gnarbot.gnar.commands.template;

import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

public class StringTemplate implements Template {
    private final Map<String, Template> cursors = new LinkedHashMap<>();

    @Override
    public String description() {
        if (getCursors().isEmpty()) return "";

        StringBuilder builder = new StringBuilder(getCursors().size() * 16);
        for (Map.Entry<String, Template> cursor : getCursors().entrySet()) {
            builder.append("  - `").append(cursor.getKey()).append("` ");
            builder.append(StringUtils.truncate(cursor.getValue().description(), 80));
            builder.append('\n');
        }
        builder.substring(0, builder.length() - 1);

        return builder.toString();
    }

    @Override
    public Map<String, Template> getCursors() {
        return cursors;
    }
}
