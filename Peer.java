package com.example.pm;

public class Peer {
    private String peerId;
    private MessageStorage messageStorage;
    private int serverPort;
    private PeerServer server;
    private PeerClient client;
    private Thread serverThread;
    private Thread clientThread;

    public Peer(String peerId, MessageStorage messageStorage, int serverPort) {
        this.peerId = peerId;
        this.messageStorage = messageStorage;
        this.serverPort = serverPort;
        server = new PeerServer(this, serverPort);
        client = new PeerClient(this);
    }

    public String getPeerId() {
        return peerId;
    }

    public MessageStorage getMessageStorage() {
        return messageStorage;
    }

    public int getServerPort() {
        return serverPort;
    }

    public PeerClient getClient() {
        return client;
    }

    public void start() {
        serverThread = new Thread(server);
        serverThread.start();
        clientThread = new Thread(client);
        clientThread.start();
    }
}
