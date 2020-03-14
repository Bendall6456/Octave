package xyz.gnarbot.gnar.commands.template;

import xyz.gnarbot.gnar.Bot;
import xyz.gnarbot.gnar.BotLoader;
import xyz.gnarbot.gnar.commands.CommandExecutor;
import xyz.gnarbot.gnar.commands.Context;
import xyz.gnarbot.gnar.commands.template.annotations.Description;
import xyz.gnarbot.gnar.commands.template.annotations.Name;
import xyz.gnarbot.gnar.commands.template.parser.Parser;
import xyz.gnarbot.gnar.commands.template.parser.Parsers;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class CommandTemplate extends CommandExecutor implements Template {
    private final Map<String, Template> cursors;

    public CommandTemplate() {
        this(Parsers.PARSER_MAP);
    }

    public CommandTemplate(Map<Class<?>, Parser<?>> parserMap) {
        cursors = new LinkedHashMap<>();

        Method[] methods = getClass().getDeclaredMethods();
        Arrays.sort(methods, Comparator.comparing(Method::getName));
        for (Method method : methods) {
            if (!method.isAnnotationPresent(Description.class)) continue;

            if (method.getParameterCount() == 0 || method.getParameters()[0].getType() != Context.class) {
                throw new RuntimeException("First argument of " + method + " must be Context");
            }

            Name ann = method.getAnnotation(Name.class);
            String name = ann == null ? method.getName() : ann.value();

            String[] parts = name.split("_");

            Template current = this;
            for (int i = 0; i < parts.length - 1; i++) {
                String part = parts[i];

                Template next = current.getCursors().get(part);
                if (next == null) {
                    next = new StringTemplate();
                    current.getCursors().put(part, next);
                }

                current = next;
            }

            current.add(parts[parts.length - 1], new MethodTemplate(this, method.getAnnotation(Description.class), method, parserMap));
        }
    }

    @Override
    public Map<String, Template> getCursors() {
        return cursors;
    }

    @Override
    public void execute(Context context, String label, String[] args) {
        walk(context, args, 0);
    }

    @Override
    public void onWalkFail(Context context, String[] args, int depth, String title, String description) {
        Template.super.onWalkFail(context, args, depth,
                title == null ? Bot.getInstance().getConfiguration().getPrefix() + getInfo().aliases()[0] + " Command" : title,
                getInfo().description() + (description == null ? "" : description));
    }
}
