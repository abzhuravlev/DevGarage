package ru.zhuravlev.service.enums;

public enum ServiceCommands {
    HELP("/help"),
    REGISTRATION("/registration"),
    CANCEL("/cancel"),
    START("/start"),
    JOKE("/"),
    ANSWER(""),
    MEET("/date"),
    PLACE("/place"),
    ELSE("/else");
    private final String cmd;

    ServiceCommands(String cmd) {
        this.cmd = cmd;
    }

    @Override
    public String toString() {
        return cmd;
    }
    public boolean equals(String cmd) {
        return this.toString().equals(cmd);

    }
    public static ServiceCommands fromValue(String v) {
        for (ServiceCommands c : ServiceCommands.values()) {
            if (c.cmd.equals(v)) {
                return c;
            }
        }
        return null;
    }
}
