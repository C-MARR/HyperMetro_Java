package metro;

public enum MenuCommands {
    ADD_HEAD("/add-head"),
    APPEND("/append"),
    CONNECT("/connect"),
    EXIT("/exit"),
    OUTPUT("/output"),
    REMOVE("/remove")
    ;

    private final String command;

    MenuCommands(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public static void invalidCommandError() {
        System.out.println("Invalid command");
    }
}
