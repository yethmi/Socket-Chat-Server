package chatserver;

/*
*in begining we use Hash Set because we only check whether it's unique, but then we use HashMap to map key - value
**/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class ChatServer {

    private static final int PORT = 9001;

    /**
     * The HashMap for map,
     * client name as the "key" and print writer as the "value"
     */
    private static HashMap<String,PrintWriter> clients = new HashMap<>();

    public static void main(String[] args) throws Exception {
        System.out.println("The chat server is running.");
        ServerSocket listener = new ServerSocket(PORT);
        try {
            while (true) {
            	Socket socket  = listener.accept();
                Thread handlerThread = new Thread(new Handler(socket));
                handlerThread.start();
            }
        } finally {
            listener.close();
        }
    }

    private static class Handler implements Runnable {
        private String name;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;


        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {

                // Create character streams for the socket.
                in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                while (true) {
                    out.println("SUBMITNAME");
                    name = in.readLine();
                    if (name == null) {
                        return;
                    }

                    synchronized(clients) {
                        if (!clients.containsKey(name)) {
                            clients.put(name , out);
                            break;
                        }
                    }
                    
                 }

                out.println("NAMEACCEPTED");

                String clientList = String.join( "," , clients.keySet());
                for (PrintWriter writer : clients.values()) {
                    writer.println("MESSAGE" + name + " Joined the chat..");
                    writer.println("CLIENT_LIST" + clientList);
                }


                while (true) {
                    String input = in.readLine();
                    if (input == null) {
                        return;
                    }

                    if(input.startsWith("DIRECT")) {

                        //Remove "DIRECT"
                        input = input.substring("DIRECT".length());
                        String[] stringClientList = (input.substring( 0 , input.indexOf(":"))).split(",");
                        String message = input.substring(input.indexOf(":") + 1 );

                        for (String client : stringClientList) {

                            if(clients.containsKey(client)) {
                                PrintWriter writer = clients.get(client);
                                writer.println("MESSAGE" + name + ": " + input);
                            }

                        }
                        out.println("MESSAGE" + name + ": " + message);

                    }else if(input.startsWith("BROADCAST")) {

                        input = input.substring("BROADCAST".length());
                        for (PrintWriter writer : clients.values()) {
                            writer.println("MESSAGE" + name + ": " + input);
                        }
                    }

                }
            }// TODO: Handle the SocketException here to handle a client closing the socket
            catch (IOException e) {
                System.out.println(e);
            } finally {

                if (name != null) {
                    clients.remove(name);
                }
                if (out != null) {
                    clients.remove(out);
                }
                try {
                    String clientList = String.join( "," , clients.keySet());
                    for (PrintWriter writer : clients.values()) {
                        writer.println("MESSAGE" + name + " Left the chat..");
                        writer.println("CLIENT_LIST " + clientList);
                    }
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }
}