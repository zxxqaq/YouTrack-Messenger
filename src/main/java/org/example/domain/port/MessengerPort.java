package org.example.domain.port;

import java.io.IOException;

public interface MessengerPort {
    void sendToPm(String text) throws IOException;
}


