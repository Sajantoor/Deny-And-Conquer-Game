import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;

public class App {
    static ClientSocket clientSocket;

    public static void main(String[] args) {
        clientSocket = new ClientSocket();
        try {
            clientSocket.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> createAndShowGUI());
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Deny and Conquer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(400, 400));

        JPanel containerPanel = new JPanel();
        containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Deny and Conquer");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        containerPanel.add(title);

        JPanel boardPanel = new JPanel(new GridLayout(4, 4, 5, 5)) {
            @Override
            public Insets getInsets() {
                return new Insets(0, 0, 0, 0);
            }
        };

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                try {
                    clientSocket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        for (int i = 0; i < 16; i++) {
            Block block = new Block();
            boardPanel.add(block);
        }
        containerPanel.add(boardPanel);

        Cursor cursor = new Cursor();
        containerPanel.add(cursor);

        frame.add(containerPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
