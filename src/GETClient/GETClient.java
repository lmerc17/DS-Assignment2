package GETClient;

import java.io.InputStreamReader;
import java.net.Socket;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.regex.PatternSyntaxException;

public class GETClient {

    /** Method to turn JSON formatting supplied by AggregationServer into readable format.
    * @param  JsonLine: A string of data in Json form to be converted into a readable form.
    * @return A string consisting of the converted JsonLine.
    */
    public static String parseJson(String JsonLine){

        JsonLine = JsonLine.trim(); //whitespace is trimmed from the input string.
        boolean colon = false; //boolean is used to ensure only the first colon (:) is given a space after it

        StringBuilder output = new StringBuilder(); //string builder initialised to create output string

        for(char c : JsonLine.toCharArray()){ //for each character in the Json line
            if(c == ':' && !colon){ //if it is a colon and the first one hasn't been formatted
                output.append(":"); //add it and a space to the output string builder
                colon = true; //set the colon flag to true
            }
            else if(c != '"' && c != ',' && c != ' '){ //if the character is not '"', ',' or ' ' add it to the string builder
                output.append(c);
            }
            else if(c == ' ' && colon){
                output.append(c);
            }
        }

        return output.toString(); //return the string builder in string form

    }

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
                System.err.println("Please enter correct url format: <hostname>:<port number>");
                return;
            }
            hostName = args[0].split(":")[0];
            portNumber = Integer.parseInt(args[0].split(":")[1]);
        }
        catch (PatternSyntaxException e){ //catch when there are issues in splitting the first argument
            System.err.println("Please enter correct url format: <hostname>:<port number>");
            return;
        }
        catch (NumberFormatException e){ //catch when the port number is not valid
            System.err.println("Please enter a valid port number");
            return;
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

            String receivedLine;
            label:
            while((receivedLine = in.readLine()) != null) { //while receiving lines from the server
                switch (receivedLine.trim()) { //trim the line
                    case "No relevant ID data":
                        System.out.println("There is no weather data stored for the supplied ID number");
                        break label;
                    case "-1": //if it is -1, break the while loop
                        break label;
                    case "}": //if it is }, print a new line (this is to separate different weather stations)
                        System.out.println("\n");
                        break;
                    case "{": //if it is {, do nothing
                        break;
                    default: //when no cases are matched, take the receivedLine and print the parsed version of it
                        System.out.println(parseJson(receivedLine));
                }
            }

        }
        catch(UnknownHostException e){ //Exception catching for unknown host
            System.err.println("Unknown host " + hostName);
        }
        catch(IOException e){ //Exception catching for IOException
            System.err.println("Couldn't get IO for connection to " + hostName);
        }

    }
}
