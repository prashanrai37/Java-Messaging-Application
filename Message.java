package com.example.pm;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Message {
    private long timeSent;
    private String from;
    private String to;
    private String subject;
    private String topic;
    private String contents;

    public long getTimeSent() {
        return timeSent;
    }

    public void setTimeSent(long timeSent) {
        this.timeSent = timeSent;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public String getMessageId() {
        try {
            String headersAndBody = toHeadersAndBody(false);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(headersAndBody.getBytes(StandardCharsets.UTF_8));

            String hexHash = "";
            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) {
                    hexHash += '0';
                }
                hexHash += hex;
            }
            return hexHash;
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException("SHA-256 not supported");
        }
    }

    public String toHeadersAndBody(boolean withId) {
        String out = "";
        String lines[] = contents.split("\\n");

        if (withId) {
            out += "Message-id: SHA-256 " + getMessageId() + "\n";
        }
        out += "Time-sent: " + timeSent + "\n";
        out += "From: " + from + "\n";

        if (to != null && !to.isEmpty()) {
            out += "To: " + to + "\n";
        }
        if (topic != null && !topic.isEmpty()) {
            out += "Topic: " + topic + "\n";
        }
        if (subject != null && !subject.isEmpty()) {
            out += "Subject: " + subject + "\n";
        }

        out += "Contents: " + lines.length + "\n";
        for (String line : lines) {
            out += line + "\n";
        }

        return out;
    }
}
