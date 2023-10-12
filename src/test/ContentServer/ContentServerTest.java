package ContentServer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static ContentServer.ContentServer.main;
import static ContentServer.ContentServer.createJSON;


class ContentServerTest{
    private static final ByteArrayOutputStream err = new ByteArrayOutputStream(); //have access to error stream

    //set up error stream
    @BeforeAll
    public static void setUpStreams(){
        System.setErr(new PrintStream(err));
    }


    //Testing createJSON to check for IOException
    @Test
    void createJSONThrowsIOException() {
        Assertions.assertThrows(IOException.class,
                () -> createJSON("weather294.txt"));
    }

    // Testing catch statements in main
    @Test
    void mainChecksForCorrectNumberOfArguments(){
        String[] arguments = {"localhost:4567f:342", "weather.txt"};
        main(arguments);
        Assertions.assertEquals("Please enter correct url format: <hostname>:<port number>\n",
                err.toString());
    }
    @Test
    void mainChecksForIncorrectNumberOfArguments(){
        String[] arguments = {"localhost"};
        main(arguments);
        Assertions.assertEquals("Usage: java ContentServer <url> <data file location>\n",
                err.toString());
    }
    @Test
    void mainChecksForIncorrectURLFormat(){
        String[] arguments = {"localhost:23343:dfdfd", "weather.txt"};
        main(arguments);
        Assertions.assertEquals("Please enter correct url format: <hostname>:<port number>\n",
                err.toString());
    }
    @Test
    void mainChecksForCorrectURLFormat(){ // IOException triggers meaning URL format must be right
        String[] arguments = {"localhost:4567", "weather.txt"};
        main(arguments);
        Assertions.assertEquals("Could not create JSON file from local weather data\n",
                err.toString());
    }
    @Test
    void mainChecksForInvalidPortNumber(){
        String[] arguments = {"localhost:d28f", "weather.txt"};
        main(arguments);
        Assertions.assertEquals("Please enter a valid port number\n",
                err.toString());
    }
}