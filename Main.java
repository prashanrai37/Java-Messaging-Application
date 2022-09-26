package com.example.pm;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
	public static void main(String[] args) {
		MessageStorage messageStorage = new MessageStorage();

		Message sampleMessage = new Message();
		sampleMessage.setTimeSent(1614686400);
		sampleMessage.setFrom("example.example@example.co.uk");
		sampleMessage.setTopic("#announcements");
		sampleMessage.setSubject("Hello!");
		String contents = "";
		contents += "Hello everyone!\n";
		contents += "This is the first message sent using PM.";
		sampleMessage.setContents(contents);

		System.out.println("This is the sample message:");
		System.out.println(sampleMessage.toHeadersAndBody(true));
		messageStorage.save(sampleMessage);

		System.out.println("Launching peers...");
		Peer peer1 = new Peer("peer1", messageStorage, 20111);
		Peer peer2 = new Peer("peer2", messageStorage, 20112);
		peer1.start();
		peer2.start();

		System.out.println("Waiting 5 seconds for server start...");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}

		PeerClient client2 = peer2.getClient();
		client2.connectTo(peer1);

		// wait a bit after connecting
		try {
			Thread.sleep(3000);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}

		System.out.println("=== You are now controlling Peer 2 ===");
		System.out.println("=== Type a request and hit ENTER to see the response ===");

		Scanner scanner = new Scanner(System.in);
		client2.enableAutoReceive();
		while (true) {
			String request = scanner.nextLine();
			client2.sendRequest(request);
		}
	}

    public static void mainTest(String[] args) {
	    MessageStorage messageStorage = new MessageStorage();

	    Message sampleMessage = new Message();
	    sampleMessage.setTimeSent(1614686400);
	    sampleMessage.setFrom("example.example@example.co.uk");
	    sampleMessage.setTopic("#announcements");
	    sampleMessage.setSubject("Hello!");
	    String contents = "";
	    contents += "Hello everyone!\n";
	    contents += "This is the first message sent using PM.";
	    sampleMessage.setContents(contents);

	    System.out.println("This is the sample message:");
	    System.out.println(sampleMessage.toHeadersAndBody(true));
		messageStorage.save(sampleMessage);

		System.out.println("Launching peers...");
		Peer peer1 = new Peer("peer1", messageStorage, 20111);
		Peer peer2 = new Peer("peer2", messageStorage, 20112);
		peer1.start();
		peer2.start();

		System.out.println("Waiting 5 seconds for server start...");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}

		PeerClient client2 = peer2.getClient();
		client2.connectTo(peer1);
		client2.requestTime();

		// wait a bit after each test...
		try {
			Thread.sleep(3000);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}

		// try to get the sample message from the peer
		client2.getMessage(sampleMessage.getMessageId());

		// wait a bit after each test...
		try {
			Thread.sleep(3000);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}

		// try to get a nonexisting message
		client2.getMessage("unknownid");

		// wait a bit after each test...
		try {
			Thread.sleep(3000);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}

		Message testMessage = new Message();
		testMessage.setTimeSent(1614686499);
		testMessage.setFrom("somebody@somewhere.co.uk");
		testMessage.setTopic("#announcements");
		testMessage.setSubject("It works!");
		String longContents = "";
		longContents += "This is a test message.\n";
		longContents += "This is line 2.\n";
		longContents += "This is line 3.\n";
		longContents += "This is line 4.\n";
		longContents += "This is line 5.";
		testMessage.setContents(longContents);

		// try to send the test message to peer1
		client2.sendMessage(testMessage);

		// wait a bit after each test...
		try {
			Thread.sleep(3000);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}

		// test LIST? request without headers
		Map<String, String> headers = new HashMap<>();
		client2.getMessagesSince(123456, headers);

		// test LIST? request with headers
		headers.put("From", "example.example@example.co.uk");
		headers.put("Topic", "#announcements");
		client2.getMessagesSince(123456, headers);

		System.out.println("===== ALL TESTS COMPLETED =====");
    }
}
