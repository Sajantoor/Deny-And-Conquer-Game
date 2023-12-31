import java.awt.Color;

public class Constants {
    // Commands
    public static final String cursorCommand = "cursor";
    public static final String startDrawCommand = "draw";
    public static final String endDrawCommand = "endDraw";
    public static final String endCommand = "end";
    public static final String captureCommand = "capture";
    public static final String playerIDCommand = "playerID";
    public static final String startCommand = "start";

    // Server
    public static final String serverIP = "localhost";
    public static final int serverPort = 3000;

    // Board
    public static final int boardRows = 8;
    public static final int boardCols = 8;

    // Player colors
    public static final String[] playerColorsStrings = { "Blue", "Red", "Green", "Yellow" };
    public static final Color[] playerColors = { Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW };
    public static final String[] ColorNames = { "blue", "red", "green", "yellow" };
}
