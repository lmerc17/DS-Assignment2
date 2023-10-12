package GETClient;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static GETClient.GETClient.main;
import static GETClient.GETClient.parseJson;



class GETClientTest{
    private static final ByteArrayOutputStream err = new ByteArrayOutputStream(); //have access to error stream

    //set up error stream
    @BeforeAll
    public static void setUpStreams(){
        System.setErr(new PrintStream(err));
    }


    //Testing parseJson to make sure it returns the right string
    @Test
    void parseJsonReturnsCorrectString(){
        Assertions.assertEquals("id:IDS60902", parseJson("\"id\":\"IDS60902\","));
    }
    @Test
    void parseJsonReturnsCorrectStringForIntInfo(){
         Assertions.assertEquals("lon:138.6", parseJson("\"lon\":138.6,"));
    }
    @Test
    void parseJsonReturnsCorrectStringForNoInfo(){
        Assertions.assertEquals("lon:", parseJson("\"lon\":"));
    }

    // Testing catch statements in main
    @Test
    void mainChecksForIncorrectURLFormat(){
        String[] arguments = {"localhost"};
        main(arguments);
        Assertions.assertEquals("Please enter correct url format: <hostname>:<port number>\n",
                err.toString());
    }
    @Test
    void mainChecksForIncorrectURLFormat2(){
        String[] arguments = {"localhost:23343:dfdfd"};
        main(arguments);
        Assertions.assertEquals("Please enter correct url format: <hostname>:<port number>\n",
                err.toString());
    }
    @Test
    void mainChecksForCorrectURLFormat(){ // IOException triggers meaning URL format must be right
        String[] arguments = {"localhost:4567"};
        main(arguments);
        Assertions.assertEquals("Couldn't get IO for connection to localhost\n",
                err.toString());
    }
    @Test
    void mainChecksForInvalidPortNumber(){
        String[] arguments = {"localhost:d28f"};
        main(arguments);
        Assertions.assertEquals("Please enter a valid port number\n",
                err.toString());
    }
}