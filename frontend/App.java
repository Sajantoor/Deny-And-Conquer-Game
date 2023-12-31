import javax.swing.*;

public class App {

    public static void main(String[] args) {
        try {
            ClientSocket.getInstance().connect();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        SwingUtilities.invokeLater(() -> Screens.getInstance().start());
    }

}
