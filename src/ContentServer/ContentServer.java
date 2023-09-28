package ContentServer;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.regex.PatternSyntaxException;
import java.util.concurrent.TimeUnit;

public class ContentServer {

    private static String createJSON(String fileName) throws IOException{

        //create the proper path for the fileName
        fileName = "ContentServer/" + fileName;
        System.out.println(fileName);

        boolean colon;

        StringBuilder jsonData = new StringBuilder(); //create string builder for final jsonData
        BufferedReader weatherData = new BufferedReader(new FileReader(fileName)); //create buffered reader to read file
        String line;

        jsonData.append("{\n"); //insert the original { with a new line
        while((line = weatherData.readLine()) != null){ //while there is still a weatherData line to read
            colon = false;
            jsonData.append("\t\""); //add a tab and a " to the line
            for(char c : line.toCharArray()) {
                if(c == ':' && !colon){ //if the read character is a colon and this if statement hasn't been entered already for this line
                    jsonData.append("\" : \""); //add " : " to the line
                    colon = true; //set flag to true
                }
                else{
                    jsonData.append(c); //otherwise append the character to the line as normal
                }
            }
            jsonData.append("\",\n"); //once a line is complete add a final ", comma and newline
        }
        jsonData.append("}"); //when the file is done add a final } symbol
        weatherData.close();

        return jsonData.toString(); //return the new string of jsonData

    }

    private static String createPUTRequest(String jsonWeatherData) {
        String contentType = "text/json";
        String contentLength = Integer.toString(jsonWeatherData.getBytes().length);

        //creation of HTTP PUT Request to be sent to Aggregation Server
        String httpPutRequest = "PUT /weather.json HTTP/1.1\nUser-Agent: ATOMClient/1/0\n";
        httpPutRequest = httpPutRequest + "Content-Type: " + contentType + "\n";
        httpPutRequest = httpPutRequest + "Content-Length: " + contentLength + "\n";
        httpPutRequest = httpPutRequest + jsonWeatherData;
        return httpPutRequest;
    }

    public static void main(String[] args){
        String dataPath = "/";
        String hostName = null;
        int portNumber = 0;

        // initial if statements to check the arguments and save the stationID
        if(args.length != 2){
            System.err.println("Usage: java ContentServer <url> <data file location>");
            System.exit(1);
        }
        else{
            dataPath = args[1];
        }

        //try and catch statement to ensure arg 0 of input is properly split and the port number is valid
        try{
            if(args[0].split(":").length != 2){ //if the first argument doesn't split into two components
                System.err.println("Please enter correct url format: <hostname>:<port number>");
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

        String jsonWeatherData = null;
        try{
            jsonWeatherData = createJSON(dataPath);
        }
        catch(IOException e){
            System.err.println("Could not create JSON file from local weather data");
            System.exit(1);
        }

        // try and catch statement to create socket, writer and reader and ensure they act properly
        try (Socket serverSocket = new Socket(hostName, portNumber);
             PrintWriter out = new PrintWriter(serverSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader((new InputStreamReader(serverSocket.getInputStream())))
        )
        {
            String httpPutRequest;
            String receivedLine;
            String status = "400";
            int content_length = 0;
            int counter = 0;

            for(int i=0; i<5; i++){

                status = "400";
                content_length = 0;
                counter = 0;

                //while loop with condition that checks values from server acknowledgement
                while((!(status.equals("201"))) && (!(status.equals("200"))) || content_length != jsonWeatherData.getBytes().length){
                    if(counter == 2){System.err.println("Two PUT attempts in a row have failed, stopping content server"); System.exit(1); break;}

                    httpPutRequest = createPUTRequest(jsonWeatherData);
                    out.println(httpPutRequest); //sending PUT Request to Aggregation Server

                    //code to receive acknowledgement from server
                    while((receivedLine = in.readLine()) != null) {
                        if(receivedLine.equals("-1")){break;}
                        if(receivedLine.startsWith("HTTP/1.1")){status = receivedLine.substring(9,12);}
                        if(receivedLine.startsWith("Content-Length")){content_length = Integer.parseInt(receivedLine.substring(16,19));}
                    }

                    counter++;
                }

                TimeUnit.SECONDS.sleep(5);

            }


        }
        catch(UnknownHostException e){ //Exception catching for unknown host
            System.err.println("Unknown host " + hostName);
            System.exit(1);
        }
        catch(IOException e){ //Exception catching for IOException
            System.err.println("Couldn't get IO for connection to " + hostName);
            System.exit(1);
        }
        catch(InterruptedException e){
            System.err.println("Interrupted Exception");
            System.exit(1);
        }

    }

}
