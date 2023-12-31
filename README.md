# Deny and Conquer Game

## Description of Game

The game we've created is called Deny and Conquer. Deny and Conquer is a strategic multiplayer board game played on an 8x8 square grid. Each player has a pen of a different colour, and the objective is to take over as many boxes as possible and deny opponents from filling the most number of boxes.

## Description of Design

The game is written in Java and using JavaFX for the frontend. The game uses sockets for client-server communication. Clients connect to the server using TCP sockets, allowing bidirectional communication between the clients and the central game server, where the server starts listening for incoming connections from clients. When a client wants to join the game, it establishes a TCP connection to the server by connecting to the server's IP address and port number. Upon successful connection, clients register with the server and are provided their playerID, which is used to distinguish players and which determines their pen colour.

The game starts when a player (the host) initiates the server and waits for other players to join. The server listens for incoming connections from clients. The server maintains the game state, including the status of the 8x8 grid (game board) and which boxes are claimed by which players.

The server updates and manages the game state based on the players' moves. The server checks if a box can be claimed based on the player's move. If the player colours at least 50% of the box, the server updates the game state to indicate that the box is claimed by that particular player.

When a player starts drawing on a box, the server marks that block as being drawn in by that player, if the player does not hit 50% the block is then released allowing for other players to draw on it. The server keeps track of the number of claimed boxes by each player. When all boxes are claimed, the server determines the winner(s) based on the number of boxes claimed.

After each move, the server sends updates to all connected clients, informing them of the current state of the game board and who has a claim on a block. The server keeps track of the number of claimed boxes by each player.

When all boxes are claimed, the server determines the winner(s) based on the number of boxes claimed. If 2 or more players claim the same block at the same time, whoever sends the message first will be the one to claim the block, the operation of claiming a block is atomic.

JavaFX is used for the front-end UI that is then used to interact with the game. Where it displays the player's pen icon, the variety of colours used to colour in the initially empty 8x8 board. The clients send messages to the server to let it know that that specific playerID will be colouring in on a box at coordinates (X,Y). The clients receive messages indicating the turn player, whether a move is legal or not, etc. The GUI reflects these changes by changing the state of the board for each client player's board.

Command tokens are used in order to establish the actions being taken place within the connected game board.

## How to Run the Game

### Server

Compile the server using the following command:

```bash
javac backend/*.java shared/*.java

cd backend

java Server
```

### Client

Compile the client using the following command:

```bash
javac frontend/*.java shared/*.java

cd frontend

java Client
```

This is to run locally, if you want to run on different machines, you will need to change the server IP address in the Constants.java file after hosting a TCP server.

## Description of Messaging Scheme

-   The `startCommand`: Display UI and start game, this is sent after one of the clients hit the "play" button on the UI.
-   The `endCommand`: End UI display and game. This is sent by the server after all blocks have been coloured.
-   The `playerIDCommand`: Set the playerID, this is sent by the server when the client connects to the server.
-   The `cursorCommand`: Call the appropriate cursor's move method here based on `<x position>`, `<y position>,` and `<player id>`. This is sent by the server when a player moves their cursor.
-   The `startDrawCommand`: Handle the player attempting to draw in a block, the server determines if the move is legal or not. If it is the server sends the same command to all clients. This command also sends the tile position and x and y coordinates of the player's cursor as they are colouring to show the colouring in on other clients' screens.
-   The `endDrawCommand`: Handle the player releasing the block. This is sent by the client, when it did not hit 50% of the block. The server then sends the same command to all clients and clears the block.
-   The `captureCommand`: Handle the player capturing a block. This is sent by the client, when it did hit 50% of the block. The server then sends the same command to all clients and sets the block as captured.

## Sockets

### `ClientSocket.java` (Client side)

Opening Sockets:

The opening of sockets happens in the connect() method. Here's the code snippet:

```java
public void connect() throws IOException {
    socket = new Socket(Constants.serverIP, Constants.serverPort);
    out = new PrintWriter(socket.getOutputStream(), true);
    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    this.recieveMessages();
}

```

Explanation:

1. socket = new Socket(Constants.serverIP, Constants.serverPort);: This line creates a new socket by specifying the server's IP address (Constants.serverIP) and the server's port number (Constants.serverPort).

2. out = new PrintWriter(socket.getOutputStream(), true);: This line creates a PrintWriter object (out) to write data to the server using the socket's output stream.

3. in = new BufferedReader(new InputStreamReader(socket.getInputStream()));: This line creates a BufferedReader object (in) to read data from the server using the socket's input stream.

4. this.recieveMessages();: This line starts the background thread to receive messages from the server continuously.

Handling the Shared Object:

```java
case (Constants.startDrawCommand):
    handleDrawing(tokens);
    break;
case (Constants.endDrawCommand):
    tilePositionX = Integer.parseInt(tokens[1]);
    tilePositionY = Integer.parseInt(tokens[2]);

    BlockManager.getInstance().clearBlock(tilePositionX, tilePositionY);
    break;

case (Constants.captureCommand):
    // A player captures a tile
    // Tokens are <tile x> <tile y> <player id>
    int playerID = Integer.parseInt(tokens[3]);
    int tileX = Integer.parseInt(tokens[1]);
    int tileY = Integer.parseInt(tokens[2]);
    BlockManager.getInstance().setBlockAsCaptured(tileX, tileY, playerID);
    break;



private void handleDrawing(String[] tokens) {
        tilePositionX = Integer.parseInt(tokens[1]);
        tilePositionY = Integer.parseInt(tokens[2]);
        int x = Integer.parseInt(tokens[3]);
        int y = Integer.parseInt(tokens[4]);
        int playerID = Integer.parseInt(tokens[5]);
        BlockManager.getInstance().setBlockAsDrawing(this.tilePositionX, tilePositionY, x, y, playerID);
    }
```

Explanation:

1.Handling Drawing: When the server sends a message with Constants.startDrawCommand, the handleDrawing(tokens) method is called. This method processes the message and updates the state of the shared object by invoking setBlockAsDrawing().

2.Handling Ending Drawing: When the server sends a message with Constants.endDrawCommand, the BlockManager is updated by calling clearBlock() to clear the drawing on a specific tile.

3.Handling Capturing: When the server sends a message with Constants.captureCommand, the BlockManager is updated by calling setBlockAsCaptured() to indicate that a player has captured a specific tile.

```java
private void handleEndDraw(String[] tokens) {
    int tilePositionX = Integer.parseInt(tokens[1]);
    int tilePositionY = Integer.parseInt(tokens[2]);
    int playerID = Integer.parseInt(tokens[3]);

    BlockManager.getInstance().clearBlock(tilePositionX, tilePositionY);
    CursorManager.getInstance().getCursor(playerID).show();
}
```

Explanation:

1. When the server receives a message indicating the end of drawing (Constants.endDrawCommand), the handleEndDraw(tokens) method is called.

2. It parses the tile position (tilePositionX and tilePositionY) and the player ID from the message tokens.

3. The method uses BlockManager.getInstance().clearBlock(tilePositionX, tilePositionY) to clear the block at the specified tile position on the shared canvas.

4. It then calls CursorManager.getInstance().getCursor(playerID).show() to show the cursor of the corresponding player.

```java
private void handleCapture(String[] tokens) {
    int userPlayerID = Integer.parseInt(tokens[3]);
    int tileX = Integer.parseInt(tokens[1]);
    int tileY = Integer.parseInt(tokens[2]);
    int playerID = Integer.parseInt(tokens[3]);

    BlockManager.getInstance().setBlockAsCaptured(tileX, tileY, userPlayerID);
    CursorManager.getInstance().getCursor(playerID).show();
}
```

Explanation:

1. When the server receives a message indicating a tile capture (Constants.captureCommand), the handleCapture(tokens) method is called.

2. It parses the user's player ID (userPlayerID), the tile position (tileX and tileY), and the player ID from the message tokens.

3. The method uses BlockManager.getInstance().setBlockAsCaptured(tileX, tileY, userPlayerID) to mark the specified tile as captured by the corresponding player.

4. It then calls CursorManager.getInstance().getCursor(playerID).show() to show the cursor of the capturing player.

```java
private void recieveMessages() {
    new Thread(() -> {
        while (!isClosed) {
            try {
                String message = in.readLine();
                handleMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }).start();
}
```

Explanation:

1. The recieveMessages() method is responsible for continuously receiving messages from the server in a separate thread.

2. Within the loop, it reads the incoming messages using in.readLine() and passes each message to the handleMessage(message) method for processing.

### `ClientHandler.java` (Server Side)

Opening Sockets:
The opening of sockets happens when a client connects to the server. The constructor of the ClientHandler class is responsible for this. Here's the code snippet:

```java
public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private int playerID;
    private boolean isClientConnected;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.isClientConnected = true;

        playerID = findAvailablePlayerID();
        if (playerID != -1) {
            Server.players[playerID] = 1;
        }
    }
    // ...
}
```

Explanation:

1. Socket clientSocket: The constructor receives the client's socket as a parameter and assigns it to the clientSocket member variable.

2. isClientConnected: A boolean variable to keep track of whether the client is connected or not. It is initially set to true.

3. findAvailablePlayerID(): A method that finds an available player ID to assign to the client. If a player ID is available, it assigns the ID to the playerID member variable. The player ID is used to track which tiles are captured by which players.

4. Server.players[playerID] = 1;: The Server.players array keeps track of which player is capturing a specific tile. When a client connects, the server marks the corresponding player index as "1" in the players array, indicating that the player is active and has connected to the game.

Handling the Shared Object:

```java
@Override
public void run() {
    try {
        // ...
        OutputStream os = clientSocket.getOutputStream();
        InputStream is = clientSocket.getInputStream();
        out = new PrintWriter(os, true);
        in = new BufferedReader(new InputStreamReader(is));

        // Send playerID to the client when connecting
        String sendPlayerID = "playerID " + (Server.getPlayerCount() - 1);
        sendMessage(sendPlayerID);

        String message;

        while (Server.isPlayersLeft()) {
            message = in.readLine();
            handleMessage(message);
        }

        // Remove the client socket from the list of active client sockets upon
        // disconnection
        clientSocket.close();
        Server.players[playerID] = 0;
        Server.removeClientSocket(this);
    } catch (IOException e) {
        System.err.println("Error handling client: " + e.getMessage());
        isClientConnected = false;
    }
}

private void handleMessage(String message) {
    if (message == null) {
        return;
    }

    String[] tokens = message.split(" ");

    if (tokens.length == 0) {
        return;
    }

    System.out.println("Received message: " + message);

    String commandToken = tokens[0];

    switch (commandToken) {
        case (Constants.captureCommand):
            handleCapture(tokens);
            break;
        case (Constants.startDrawCommand):
            handleStartDraw(tokens);
            break;
        case (Constants.endDrawCommand):
            handleEndDraw(tokens);
            break;

        // ...
    }
}
```

Explanation:

1. run(): This method is the entry point for the client handling thread. It sets up the input and output streams to communicate with the client and sends the playerID to the client upon connection. Then, it enters a loop to continuously listen for messages from the client and calls handleMessage(message) to process each received message.

2. handleMessage(String message): This method is responsible for processing the messages received from the client. It splits the message into tokens and checks the command token (first token) to determine the action to be taken. Based on the command token, different methods like handleCapture(), handleStartDraw(), and handleEndDraw() are called to update the shared object (game board) accordingly.

```java
case (Constants.captureCommand):
    handleCapture(tokens);
    break;

```

Explanation:

1. When the server receives a message with Constants.captureCommand, the handleCapture(tokens) method is called. This method attempts to capture a tile on the shared game board.

2. It checks if the tile is available for capture (ServerBoard.getInstance().attemptCaptureTile(tileX, tileY, playerID)).
3. If the tile can be captured, it marks the tile as captured and broadcasts the capture message to all other clients using broadcastMessage(String.join(" ", tokens)).

```java
case (Constants.startDrawCommand):
    handleStartDraw(tokens);
    break;
```

Explanation:

1. When the server receives a message with Constants.startDrawCommand, the handleStartDraw(tokens) method is called. This method attempts to start drawing on a tile on the shared game board.

2. It checks if the tile is available for drawing (ServerBoard.getInstance().attemptDrawTile(tileX, tileY, playerID)).

3. If the tile can be drawn upon, it marks the tile as being drawn and broadcasts the drawing message to all other clients using broadcastMessage(String.join(" ", tokens)).

```
case (Constants.endDrawCommand):
    handleEndDraw(tokens);
    break;
```

Explanation:

1. When the server receives a message with Constants.endDrawCommand, the handleEndDraw(tokens) method is called. This method marks the tile as no longer being drawn by the player.

2. It calls ServerBoard.getInstance().releaseTile(tileX, tileY, playerID) to unmark the tile as being drawn and broadcasts the end draw message to all other clients using broadcastMessage(String.join(" ", tokens)).

`ServerBoard.java`

In all the cases the server eventually calls the ServerBoard class to update the shared object (game board).

```java
    public synchronized boolean attemptDrawTile(int row, int col, int playerID) {
        if (isTileBeingDrawnBy(row, col, playerID)) {
            drawTile(row, col, playerID);
            return true;
        }

        return false;
    }
```

Explanation:

1. This is an atomic method since it is synchronized, only 1 thread can access it at a time.

2. The attemptDrawTile() method checks if the tile is being drawn by the player. If it is, it calls the drawTile() method to mark the tile as being drawn by the player and returns true. Otherwise, it returns false.

```java
    public synchronized void drawTile(int row, int col, int playerID) {
        board[row][col] = playerID;
    }
```

```java
private void broadcastMessage(String message) {
    for (ClientHandler socket : Server.getClientSockets()) {
        if (socket != this && socket.getSocket().isConnected()) {
            socket.sendMessage(message);
        }
    }
}

private void broadcastMessageToAll(String message) {
    for (ClientHandler socket : Server.getClientSockets()) {
        if (socket != this && socket.getSocket().isConnected()) {
            socket.sendMessage(message);
        }
        out.println(message);
    }
}
```

Explanation:

1. These methods are responsible for broadcasting messages to all other clients connected to the server.

2. broadcastMessage(String message) sends the provided message to all other connected clients except the current client (represented by this ClientHandler instance).

3. broadcastMessageToAll(String message) additionally sends the message to the current client itself using out.println(message), in addition to broadcasting to other clients.

4. Both methods iterate through all the connected clients using Server.getClientSockets() and use sendMessage(message) to send the message to each client.

# Group Members

Arjun Singh

Gurinder Bhogal

Jessy Chahal

Sajan Toor

Yousef Haiba 

# Video of Demo

With the server: https://youtu.be/QEFrlTVCZQ8

Another perspective without the server: https://www.youtube.com/watch?v=QR9gGSQRvzY
