package org.example.domain.port;

import java.io.IOException;

public interface MessengerPort {
    void sendToGroup(String text) throws IOException;
    void sendToPm(String text) throws IOException;
}


