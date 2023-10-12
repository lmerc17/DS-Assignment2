package AggregationServer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static AggregationServer.AggregationServer.main;


class AggregationServerTest {
    private static final ByteArrayOutputStream err = new ByteArrayOutputStream(); //have access to error stream

    //set up error stream
    @BeforeAll
    public static void setUpStreams() {
        System.setErr(new PrintStream(err));
    }

    // Testing port number validity in main
    // This is the only unit test as all that can be changed
    // is the port number and when it is normal, the test runs infinitely
    @Test
    void mainChecksForInvalidPortNumber() throws IOException {
        String[] arguments = {"45adf67"};
        main(arguments);
        Assertions.assertEquals("Please enter a valid port number\n",
                err.toString());
    }
}