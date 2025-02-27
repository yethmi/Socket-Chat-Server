package chatserver;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.*;

public class ChatClient {

    BufferedReader in;
    PrintWriter out;
    JFrame frame = new JFrame("Chatter");
    JTextField textField = new JTextField(40);
    JTextArea messageArea = new JTextArea(8, 40);

    DefaultListModel<String> clientListModel;
    JList<String> clientList;
    JCheckBox broadcastCheckBox;

    public ChatClient() {

        // Layout GUI
        textField.setEditable(false);
        messageArea.setEditable(false);
        frame.getContentPane().add(textField, "North");
        frame.getContentPane().add(new JScrollPane(messageArea), "West");

        // client list box , Can Select multiple clients
        clientListModel = new DefaultListModel<>();
        clientList = new JList<>(clientListModel);
        clientList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        frame.getContentPane().add(new JScrollPane(clientList), "East");

        // Broadcast Check Box
        broadcastCheckBox = new JCheckBox("Broadcast");
        broadcastCheckBox.setSelected(false);
        frame.getContentPane().add(broadcastCheckBox, "South");

        frame.pack();

        // Hide client list , if broadcast check box checked
        broadcastCheckBox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                if (!broadcastCheckBox.isSelected()) {
                    clientList.setVisible(true);

                }else if(broadcastCheckBox.isSelected()){
                    clientList.setVisible(false);
                }

            }

        });

        textField.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                if (!broadcastCheckBox.isSelected()) {

                    String stringClientList = String.join(",", clientList.getSelectedValuesList());
                    out.println("DIRECT" + stringClientList + ":" + textField.getText());
                    textField.setText("");

                }else if(broadcastCheckBox.isSelected()){
                    out.println("BROADCAST" + textField.getText());
                    textField.setText("");
                }

            }

        });
        
        
    }

    private String getServerAddress() {
        return JOptionPane.showInputDialog(
            frame,
            "Enter IP Address of the Server:",
            "Welcome to the Chatter",
            JOptionPane.QUESTION_MESSAGE);
    }

    private String getName() {
        return JOptionPane.showInputDialog(
            frame,
            "Choose a screen name:",
            "Screen name selection",
            JOptionPane.PLAIN_MESSAGE);
    }

    private void run() throws IOException {

        // Make connection and initialize streams
        String serverAddress = getServerAddress();
        Socket socket = new Socket(serverAddress, 9001);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        while (true) {

            String line = in.readLine();

            if (line.startsWith("SUBMITNAME")) {

                String name = getName();

                if (name == null || name.equals("null")) {
                    frame.dispose();
                }else{
                    out.println(name);
                }

            } else if (line.startsWith("NAMEACCEPTED")) {

                // Enable to type a message in text feild , Set frame name to the client name
                textField.setEditable(true);
                frame.setTitle(line.substring("NAMEACCEPTED".length()));

            } else if (line.startsWith("MESSAGE")) {

                messageArea.append(line.substring("MESSAGE".length()) + "\n");

            }else if (line.startsWith("CLIENT_LIST")) {

                // Get client list and show in list
                String[] clientList = line.substring("CLIENT_LIST".length()).split(",");
                clientListModel.clear();
                for (String client : clientList) {
                    clientListModel.addElement(client);
                }

            }
        }
    }


    public static void main(String[] args) throws Exception {
        ChatClient client = new ChatClient();
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.run();
    }
}