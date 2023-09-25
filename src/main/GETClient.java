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
        //Definition of stationID and hostname strings as well as port number integer
        String stationID = "/";
        String hostName = null;
        int portNumber = 0;

        // initial if statements to check the arguments and save the stationID
        if(args.length > 2){
            System.err.println("Usage: java GETClient <url> <station ID>");
            System.exit(1);
        }
        else if(args.length == 2){
            stationID = args[1];
        }

        //try and catch statement to ensure arg 0 of input is properly split and the port number is valid
        try{
            if(args[0].split(":").length != 2){ //if the first argument doesn't split into two components
                System.err.print("Please enter correct url format: <hostname>:<port number>");
                System.exit(1);
            }
            hostName = args[0].split(":")[0];
            portNumber = Integer.parseInt(args[0].split(":")[1]);
        }
        catch (PatternSyntaxException e){ //catch when there are issues in splitting the first argument
            System.err.println("Please enter correct url format: <hostname>:<port number>");
            System.exit(1);
        }
        catch (NumberFormatException e){ //catch when the port number is not valid
            System.err.println("Please enter a valid port number");
            System.exit(1);
        }

        // try and catch statement to create socket, writer and reader and ensure they act properly
        try (Socket serverSocket = new Socket(hostName, portNumber);
             PrintWriter out = new PrintWriter(serverSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader((new InputStreamReader(serverSocket.getInputStream())))
        )
        {
            //creation of HTTP GET Request to be sent to Aggregation Server
            String httpGetRequest = "GET " + stationID + " HTTP/1.0";

            out.println(httpGetRequest); //sending GET Request to Aggregation Server
            String output;
            while((output = in.readLine()) != null) {
                if(output.trim().equals("-1")){break;}
                System.out.println(output); //currently printing information received from server
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
