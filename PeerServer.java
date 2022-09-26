package com.example.pm;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PeerServer implements Runnable {
    private Peer peer;
    private int port;
    private boolean protocolRequestReceived = false;
    private String expectedId;
    private Message incomingMessage;
    private int remainingContents;
    private String contents;

    // for LIST
    private long listSince;
    private int remainingHeaders;
    private Map<String, String> receivedHeaders;

    public PeerServer(Peer peer, int port) {
        this.peer = peer;
        this.port = port;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(this.port)) {
            System.out.println("Server is listening on port " + this.port);

            while (true) {
                Socket socket = serverSocket.accept();

                try {
                    System.out.println("New client connected");

                    OutputStream output = socket.getOutputStream();
                    PrintWriter writer = new PrintWriter(output, true);

                    InputStream input = socket.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                    String request;
                    while ((request = reader.readLine()) != null) {
                        System.out.println("Incoming request: " + request);
                        processRequest(request, writer);
                    }
                } catch (IOException ex) {
                    System.out.println("IO exception: " + ex.getMessage());
                    ex.printStackTrace();
                } catch (RuntimeException ex) {
                    System.out.println("Runtime exception: " + ex.getMessage());
                } finally {
                    System.out.println("Closing socket");
                    socket.close();
                }
            }
        } catch (IOException ex) {
            System.out.println("IO exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void processRequest(String request, Writer writer) throws IOException {
        String[] parts = request.split(" ");

        if (!protocolRequestReceived) {
            if (parts[0].equals("PROTOCOL?")) {
                if (parts.length >= 3) {
                    System.out.print("PROTOCOL OK\n");
                    System.out.printf("VERSION: %s\n", parts[1]);
                    System.out.printf("IDENTIFIER: %s\n", parts[2]);
                    protocolRequestReceived = true;
                } else {
                    throw new RuntimeException("PROTOCOL request is missing some parts");
                }
            } else {
                throw new RuntimeException("First request must be PROTOCOL");
            }
        }
        else if (parts[0].equals("PROTOCOL?")) {
            throw new RuntimeException("PROTOCOL request already received");
        }
        else if (parts[0].equals("TIME?")) {
            writer.write("NOW ");
            writer.write(String.valueOf(Instant.now().getEpochSecond()));
            writer.write('\n');
            writer.flush();
        }
        else if (parts[0].equals("GET?")) {
            if (parts.length >= 2) {
                String messageId = parts[1];
                Message foundMessage = peer.getMessageStorage().findById(messageId);

                if (foundMessage == null) {
                    writer.write("SORRY");
                    writer.write('\n');
                    writer.flush();
                } else {
                    writer.write("FOUND");
                    writer.write('\n');
                    writer.write(foundMessage.toHeadersAndBody(true));
                    writer.flush();
                }
            } else {
                throw new RuntimeException("GET request is missing message ID");
            }
        }
        else if (parts[0].equals("LIST?")) {
            if (parts.length >= 3) {
                listSince = Integer.parseInt(parts[1]);
                remainingHeaders = Integer.parseInt(parts[2]);
                receivedHeaders = new HashMap<>();

                if (remainingHeaders == 0) {
                    List<Message> foundMessages = peer.getMessageStorage().findSince(listSince, receivedHeaders);
                    writer.write("MESSAGES " + foundMessages.size() + "\n");
                    for (Message message : foundMessages) {
                        writer.write(message.getMessageId() + "\n");
                    }
                    writer.flush();
                }
            } else {
                throw new RuntimeException("LIST request is missing some parts");
            }
        }
        else if (remainingHeaders > 0) {
            String headerKey = parts[0].replace(":", "");
            String headerValue = request.substring((parts[0] + " ").length());

            receivedHeaders.put(headerKey, headerValue);
            remainingHeaders--;

            System.out.println("Remaining Headers: " + remainingHeaders);

            if (remainingHeaders == 0) {
                List<Message> foundMessages = peer.getMessageStorage().findSince(listSince, receivedHeaders);
                writer.write("MESSAGES " + foundMessages.size() + "\n");
                for (Message message : foundMessages) {
                    writer.write(message.getMessageId() + "\n");
                }
                writer.flush();
            }
        }
        else if (parts[0].equals("BYE!")) {
            throw new RuntimeException("BYE request received!");
        }
        else if (parts[0].equals("Message-id:") && parts[1].equals("SHA-256") && parts.length == 3) {
            System.out.println("New incoming message with ID " + parts[2]);
            expectedId = parts[2];
            incomingMessage = new Message();
        }
        else if (parts[0].equals("Time-sent:")) {
            incomingMessage.setTimeSent(Long.parseLong(parts[1]));
        }
        else if (parts[0].equals("From:")) {
            incomingMessage.setFrom(request.substring("From: ".length()));
        }
        else if (parts[0].equals("To:")) {
            incomingMessage.setTo(request.substring("To: ".length()));
        }
        else if (parts[0].equals("Topic:")) {
            incomingMessage.setTopic(request.substring("Topic: ".length()));
        }
        else if (parts[0].equals("Subject:")) {
            incomingMessage.setSubject(request.substring("Subject: ".length()));
        }
        else if (parts[0].equals("Contents:")) {
            remainingContents = Integer.parseInt(parts[1]);
            System.out.println("Expecting " + remainingContents + " lines");
            contents = "";
        }
        else if (remainingContents > 0) {
            contents += request + "\n";
            remainingContents--;

            if (remainingContents == 0) {
                incomingMessage.setContents(contents);

                System.out.println("===== Received message =====");
                System.out.println(incomingMessage.toHeadersAndBody(true));
                System.out.println("===== End of message =====");

                peer.getMessageStorage().save(incomingMessage);
            }
        }
        else {
            throw new RuntimeException("Unknown request");
        }
    }
}
