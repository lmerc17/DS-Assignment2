package main;

import java.io.InputStreamReader;
import java.net.Socket;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.regex.PatternSyntaxException;

public class GETClient {
    public static void main(String[] args){
        if(args.length > 2){
            System.err.println("Usage: java GETClient <url> <station ID>");
            System.exit(1);
        }

        String hostName = null;
        int portNumber = 0;

        //try and catch statement to ensure arg 0 of input is properly split and the port number is valid
        try{
            if(args[0].split(":").length != 2){
                System.err.print("Please enter correct url format: <hostname>:<port number>");
                System.exit(1);
            }
            hostName = args[0].split(":")[0];
            portNumber = Integer.parseInt(args[0].split(":")[1]);
        }
        catch (PatternSyntaxException e){
            System.err.println("Please enter correct url format: <hostname>:<port number>");
            System.exit(1);
        }
        catch (NumberFormatException e){
            System.err.println("Please enter a valid port number");
            System.exit(1);
        }


        try (Socket clientSocket = new Socket(hostName, portNumber);
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader((new InputStreamReader(clientSocket.getInputStream())));
             BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))
        )
        {
            String userInput;
            while((userInput = stdIn.readLine()) != null){
                out.println(userInput);
                System.out.println("echo: " + in.readLine());
            }
        }
        catch(UnknownHostException e){
            System.err.println("Unknown host " + hostName);
            System.exit(1);
        }
        catch(IOException e){
            System.err.println("Couldn't get IO for connection to " + hostName);
            System.exit(1);
        }

    }
}
