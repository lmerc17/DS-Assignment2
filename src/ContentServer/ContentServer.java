package ContentServer;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.regex.PatternSyntaxException;

public class ContentServer {

    //EDIT THE BELOW METHOD TO ENSURE IT CAN SUPPORT MULTIPLE ENTRIES!!!!!!!!!!!!!
    private static String createJSON(String fileName) throws IOException{

        fileName = "AggregationServer/" + fileName;

        boolean colon = false;

        StringBuilder jsonData = new StringBuilder();
        BufferedReader weatherData = new BufferedReader(new FileReader(fileName));
        String line;

        jsonData.append("{\n");
        while((line = weatherData.readLine()) != null){
            jsonData.append("\t\"");
            for(char c : line.toCharArray()) {
                if(c == ':' && !colon){
                    jsonData.append("\" : \"");
                    colon = true;
                }
                else{
                    jsonData.append(c);
                }
            }
            jsonData.append("\",\n");
        }
        jsonData.append("}");
        weatherData.close();

        return jsonData.toString();

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
            String httpPutRequest = createPUTRequest(jsonWeatherData);
            out.println(httpPutRequest); //sending PUT Request to Aggregation Server

            //need to receive acknowledgement from the server
            //need to check this acknowledgement (check response code and maybe payload)

        }
        catch(UnknownHostException e){ //Exception catching for unknown host
            System.err.println("Unknown host " + hostName);
            System.exit(1);
        }
        catch(IOException e){ //Exception catching for IOException
            System.err.println("Couldn't get IO for connection to " + hostName);
            System.exit(1);
        }

    }

}
