import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * TCP Server used to accept connections when hosting a game. .
 */

public class Server {
    private static final int PORT = 3000;
    private static final int MAX_PLAYERS = 4;
    private static ServerSocket serverSocket = null;
    private static int playerCount = 0;
    private static List<ClientHandler> clientSockets = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        serverSocket = null;
        try {
            // Initialize the game board and players (same as previous code)

            // Create the server socket
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server listening on port " + PORT);

            // Accept connections from clients and handle them
            while (playerCount < MAX_PLAYERS) {
                Socket newSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(newSocket);
                Server.addClientSocket(clientHandler);

                // threads for the server to handle multiple clients simultaneously.
                new Thread(clientHandler).start();
            }
            startFaultTolerance(); 

        } catch (IOException e) {
            if (playerCount > MAX_PLAYERS) {
                throw new RuntimeException("The number of target connections cannot exceed  " + MAX_PLAYERS);
            }
        } finally {
            // Close the server socket
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    System.err.println("Error closing server socket: " + e.getMessage());
                    clear();
                }
            }
        }
    }

    public static void clear() throws IOException {
        if (!serverSocket.isClosed()) {
            serverSocket.close();
        }
        for (ClientHandler clientHandler : clientSockets) {
            removeClientSocket(clientHandler);
        }
        clientSockets.clear();
        System.out.println("Server cleared. All existing connections terminated.");
    }    

    private static void startFaultTolerance() {
    // Start the heartbeat timer
        Timer heartbeatTimer = new Timer();
        //check the status of each client every 5 seconds to see if they're still running
        heartbeatTimer.schedule(new HeartbeatTask(), 0, 5000); // Every 5 seconds

    // Run a shutdown hook to stop the fault tolerance on program exit
        Runtime.getRuntime().addShutdownHook(new Thread(() -> { //allows program to shut down gracefully after termination
            heartbeatTimer.cancel();
        }));
    
    }

    private static class HeartbeatTask extends TimerTask {
        @Override
        public void run() { //run whenever a client is not responsive in its 5 second ping
            for (ClientHandler clientHandler : clientSockets) {
                if (!clientHandler.getClientAlive()) {//added client status to client handler
                    removeClientSocket(clientHandler);//
                }
            }
        }
    }


    public synchronized static void addClientSocket(ClientHandler socket) {
        clientSockets.add(socket);
        playerCount++;
    }

    public synchronized static void removeClientSocket(ClientHandler socket) {
        clientSockets.remove(socket);
        playerCount--;
    }

    public static List<ClientHandler> getClientSockets() {
        return clientSockets;
    }

    public static boolean isPlayersLeft() {
        return playerCount != 0;
    }

    public synchronized static int getPlayerCount() {
        return playerCount;
    }
}