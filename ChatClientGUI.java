import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClientGUI extends Frame implements ActionListener, WindowListener {
    private TextArea chatTextArea;
    private TextArea connectedUsersTextArea;
    private TextField userInputField;
    private Button sendButton;
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private String clientName;

    public ChatClientGUI() {
        super("Chat Client");

        // Prompt for user's name
        promptForUserName();

        // Initialize components
        chatTextArea = new TextArea();
        connectedUsersTextArea = new TextArea();
        userInputField = new TextField();
        sendButton = new Button("Send");

        // Set layout
        setLayout(new BorderLayout());

        // Add components to the frame
        add(chatTextArea, BorderLayout.CENTER);
        add(connectedUsersTextArea, BorderLayout.WEST);
        Panel bottomPanel = new Panel(new BorderLayout());
        bottomPanel.add(userInputField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        // Add action listener to the send button
        sendButton.addActionListener(this);

        // Add window listener to handle window close event
        addWindowListener(this);

        // Set frame properties
        setSize(600, 300);
        setVisible(true);

        // Connect to the server
        connectToServer();
    }

    private void promptForUserName() {
        Frame frame = new Frame("Enter Your Name");
        TextField textField = new TextField();
        Button button = new Button("Submit");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clientName = textField.getText();
                frame.dispose();
                setTitle("Chat Client - " + clientName); // Set title with username
            }
        });
        frame.setLayout(new BorderLayout());
        frame.add(new Label("Enter your name:"), BorderLayout.NORTH);
        frame.add(textField, BorderLayout.CENTER);
        frame.add(button, BorderLayout.SOUTH);
        frame.pack();
        frame.setVisible(true);
    }

    private void connectToServer() {
        try {
            socket = new Socket("localhost", 5000);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);

            // Start a separate thread to listen for incoming messages
            new Thread(new ClientRunnable()).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Handle send button click
        if (e.getSource() == sendButton) {
            String message = userInputField.getText();
            if (!message.isEmpty()) {
                output.println("(" + clientName + ") message: " + message);
                userInputField.setText("");
            }
        }
    }

    private class ClientRunnable implements Runnable {
        @Override
        public void run() {
            try {
                String message;
                while ((message = input.readLine()) != null) {
                    if (message.startsWith("[USERLIST]")) {
                        updateConnectedUsers(message.substring(10));
                    } else {
                        chatTextArea.append(message + "\n");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateConnectedUsers(String userListString) {
        connectedUsersTextArea.setText(userListString.replace(",", "\n"));
    }

    // WindowListener methods
    @Override
    public void windowClosing(WindowEvent e) {
        // Close the socket and exit the application when the window is closed
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.exit(0);
    }

    @Override
    public void windowOpened(WindowEvent e) {}
    @Override
    public void windowClosed(WindowEvent e) {}
    @Override
    public void windowIconified(WindowEvent e) {}
    @Override
    public void windowDeiconified(WindowEvent e) {}
    @Override
    public void windowActivated(WindowEvent e) {}
    @Override
    public void windowDeactivated(WindowEvent e) {}

    public static void main(String[] args) {
        new ChatClientGUI();
    }
}
