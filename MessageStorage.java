package com.example.pm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MessageStorage {
    private List<Message> messages;

    public MessageStorage() {
        this.messages = new ArrayList<>();
    }

    public void save(Message message) {
        System.out.println("Saving message from " + message.getFrom());
        messages.add(message);
    }

    public Message findById(String messageId) {
        System.out.println("Searching message with ID " + messageId);
        for (Message message : messages) {
            if (message.getMessageId().equals(messageId)) {
                System.out.println("Message found");
                return message;
            }
        }
        return null;
    }

    public List<Message> findSince(long since, Map<String, String> headers) {
        System.out.println("Searching messages since " + since + " with " + headers.size() + " header(s)");

        List<Message> foundMessages = new ArrayList<>();
        for (Message message : messages) {
            boolean matches = false;

            if (message.getTimeSent() >= since) {
                matches = true;
            } else {
                // no need to check the headers
                continue;
            }

            for (Map.Entry<String, String> header : headers.entrySet()) {
                if (header.getKey().equals("From") && !header.getValue().equals(message.getFrom())) {
                    matches = false;
                }
                else if (header.getKey().equals("To") && !header.getValue().equals(message.getTo())) {
                    matches = false;
                }
                else if (header.getKey().equals("Topic") && !header.getValue().equals(message.getTopic())) {
                    matches = false;
                }
                else if (header.getKey().equals("Subject") && !header.getValue().equals(message.getSubject())) {
                    matches = false;
                }
            }

            if (matches) {
                foundMessages.add(message);
            }
        }

        return foundMessages;
    }
}
