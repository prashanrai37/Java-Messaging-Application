package com.example.pm;

import java.io.*;
import java.net.Socket;
import java.util.Map;

public class PeerClient implements Runnable {
    private Peer peer;
    private Socket socket;
    private BufferedReader reader;
    private Writer writer;
    private boolean autoReceive;

    public PeerClient(Peer peer) {
        this.peer = peer;
    }

    public void enableAutoReceive() {
        this.autoReceive = true;
    }

    @Override
    public void run() {
        while (true) {
            if (autoReceive) {
                // automatically receive and print lines from peer
                try {
                    String response = reader.readLine();
                    if (response != null) {
                        System.out.printf("Response: %s\n", response);
                    } else {
                        break; // server disconnected?
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                // do nothing
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void connectTo(Peer otherPeer) {
        System.out.println("Connecting to " + otherPeer.getPeerId());

        try {
            socket = new Socket("localhost", otherPeer.getServerPort());
            System.out.println("Connected!");

            InputStream input = socket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(input));

            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output);

            writer.write("PROTOCOL? 1 " + peer.getPeerId() + "\n");
            writer.flush();
        } catch (IOException ex) {
            System.out.println("IO exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void sendRequest(String request) {
        try {
            writer.write(request);
            writer.write('\n');
            writer.flush();
        }  catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void requestTime() {
        try {
            writer.write("TIME?\n");
            writer.flush();

            String response = reader.readLine();
            System.out.println("Peer response: " + response);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void getMessage(String messageId) {
        try {
            writer.write("GET? " + messageId + "\n");
            writer.flush();

            String response = reader.readLine();
            String parts[] = response.split(" ");

            if (response.equals("SORRY")) {
                System.out.println("Peer said SORRY");
            }
            else {
                int remainingLines = -1;
                while (remainingLines != 0) {
                    if (remainingLines == -1) {
                        System.out.println("Peer is sending the message headers");
                    } else {
                        System.out.println("Peer is sending the message contents - Remaining Lines: " + remainingLines);
                    }

                    String line = reader.readLine();
                    parts = line.split(" ");

                    if (remainingLines != -1) {
                        remainingLines--;
                    }
                    if (parts[0].equals("Contents:")) {
                        remainingLines = Integer.parseInt(parts[1]);
                    }
                }
                System.out.println("Peer finished sending");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void getMessagesSince(long since, Map<String, String> headers) {
        try {
            writer.write("LIST? " + since + " " + headers.size() + "\n");
            for (Map.Entry<String, String> header : headers.entrySet()) {
                writer.write(header.getKey() + ": " + header.getValue() + "\n");
            }
            writer.flush();

            String response = reader.readLine();
            String parts[] = response.split(" ");

            // MESSAGES <count>
            int count = Integer.parseInt(parts[1]);

            System.out.println("Peer found " + count + " message(s)");
            for (int i = 0; i < count; i++) {
                System.out.println("Message ID: " + reader.readLine());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void sendMessage(Message message) {
        try {
            System.out.println("Sending message to peer...");
            writer.write(message.toHeadersAndBody(true));
            writer.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
