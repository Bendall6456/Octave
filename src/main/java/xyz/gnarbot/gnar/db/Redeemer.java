package xyz.gnarbot.gnar.db;

import java.beans.ConstructorProperties;

public class Redeemer {
    public enum Type {
        GUILD,
        USER
    }

    private final Type type;
    private final String id;

    @ConstructorProperties({"type", "id"})
    public Redeemer(Type type, String id) {
        this.type = type;
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public String getId() {
        return id;
    }
}
