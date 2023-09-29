package AggregationServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;

public class AggregationServer {

    private static void send_acknowledgement(String status, String content_length, PrintWriter out){

        String response;

        if(content_length.equals("0")){
            response = "HTTP/1.1 " + status;
        }
        else {
            response = "HTTP/1.1 " + status + "\nContent-Type: text/json" + "\nContent-Length: " + content_length;
        }

        out.println(response);
        out.println("-1");

    }

    private static void save_data(String jsonData, String destinationFile) throws IOException{

        // initialJsonWeather Data and newJsonWeatherData files are created.
        File jsonWeatherDataBackup = new File("AggregationServer/weather_backup.txt");
        File initialJsonWeatherData = new File("AggregationServer/" + destinationFile);
        File newJsonWeatherData = new File("AggregationServer/weather_temp.txt");

        if(jsonWeatherDataBackup.exists()){ // if weather_backup.txt exists
            if(!newJsonWeatherData.delete()){ // delete it
                System.err.println("weather_backup.txt cannot be deleted");
                System.exit(1);
            }
        }
        if(newJsonWeatherData.exists()){ // if weather_temp.txt exists
            if(!newJsonWeatherData.delete()){ // delete it
                System.err.println("weather_temp.txt cannot be deleted");
                System.exit(1);
            }
        }

        // copy initial json weather data into backup file
        Files.copy(initialJsonWeatherData.toPath(), jsonWeatherDataBackup.toPath());

        //create weather_temp file
        if(!newJsonWeatherData.createNewFile()){
            System.err.println("weather_temp.txt could not be created");
            System.exit(1);
        }

        Scanner initialJson = new Scanner(initialJsonWeatherData);
        PrintWriter newJson = new PrintWriter(newJsonWeatherData);
        String id = jsonData.substring(jsonData.indexOf("\"id\"") + 7, jsonData.indexOf("\"name\"")-3);
        String line;

        while(initialJson.hasNextLine()){
            line = initialJson.nextLine();
            if(!line.contains(id)){
                newJson.println(line);
            }
            else{
                newJson.print(jsonData.substring(2));
                while(!line.contains("}")){
                    line = initialJson.nextLine();
                }
            }
        }
        newJson.close();
        initialJson.close();

        if(!initialJsonWeatherData.delete()){
            System.err.println("Original weather.txt could not be deleted");
            System.exit(1);
        }
        if(!newJsonWeatherData.renameTo(initialJsonWeatherData)){
            System.err.println("Temp weather file wasn't renamed");
            System.exit(1);
        }
        if(!jsonWeatherDataBackup.delete()){
            System.err.println("weather_backup.txt could not be deleted");
            System.exit(1);
        }

    }

    private static void print_data(String requestedData, PrintWriter out) throws IOException{

        BufferedReader data = new BufferedReader(new FileReader("AggregationServer/weather.txt"));
        String line;
        boolean dataFound = false;

        if(requestedData.equals("/")){
            while((line = data.readLine()) != null){
                out.println(line);
            }
            out.println("-1");
        }
        else{
            while((line = data.readLine()) != null){
                if(line.contains(requestedData)){
                    dataFound = true;
                    out.println(line);
                    while(((line = data.readLine()) != null) && !(line.trim().equals("}"))){
                        out.println(line);
                    }
                    out.println("}");
                    out.println("-1");
                }
            }
        }

        if(!dataFound){
            out.println("No relevant ID data");
        }
        data.close();
    }

    public static void main(String[] args){

        // initial code to check if backup file is present and overwriting weather.txt with it if so.
        File jsonWeatherDataBackup = new File("AggregationServer/weather_backup.txt");
        File initialJsonWeatherData = new File("AggregationServer/weather.txt");
        if(jsonWeatherDataBackup.exists()){
            try {
                Files.copy(jsonWeatherDataBackup.toPath(), initialJsonWeatherData.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                System.err.println("Could not restore backup weather data");
                System.exit(1);
            }
        }

        int port = 0;

        //if statement to set server port to 4567 if a port number is not present in the arguments
        if(args.length < 1) {
            port = 4567;
        }
        else if(args.length == 1){
            try { // a try-catch statement to ensure the port number entered is valid
                port = Integer.parseInt(args[0]);

            } catch (NumberFormatException e) {
                System.err.println("Please enter a valid port number");
                System.exit(1);
            }
        }
        else{
            System.err.println("Usage: java AggregationServer <port number>");
            System.exit(1);
        }

        // a try-catch statement to ensure all socket operation runs smoothly
        try (ServerSocket serverSocket = new ServerSocket(port);
             Socket clientSocket = serverSocket.accept();
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
        )
        {
            // defining strings for later use
            String inputLine;
            String requestedData;
            String destinationFile;
            String content_length;
            while((inputLine = in.readLine()) != null){ // read in input

                if(inputLine.startsWith("GET")){ // if the input line starts with GET, data is being given to client

                    //delete GET and HTTP/1.1 from start and end of string to isolate requested Data
                    requestedData = (inputLine.substring(3, inputLine.length() - 9)).trim();

                    print_data(requestedData, out); //call print_data function to send data to client

                }
                else if(inputLine.startsWith("PUT")){ //else if the input line starts with PUT, a content server wants to store data

                    System.out.println(inputLine);

                    // Get the name of the file where the weather json data is being stored from the PUT request
                    destinationFile = (inputLine.substring(3, inputLine.length() - 9)).trim();

                    // Cycle through the received lines until the content-length line has been read into inputLine
                    while((inputLine = in.readLine()) != null){
                        if(inputLine.startsWith("Content-Length")){break;}
                    }

                    // Set content_length string to value in inputLine or to 0 if inputLine is null
                    content_length = inputLine != null ? inputLine.substring(16) : "0";

                    if(content_length.equals("0")){ // if content length is 0 (no content arrives)
                        send_acknowledgement("204 NO_CONTENT", content_length, out); //send a 204 acknowledgment back to the content server
                    }
                    else { // otherwise (content length isn't 0)

                        StringBuilder fullJsonData = new StringBuilder();

                        try{ // try to save data
                            while((inputLine = in.readLine()) != null) {
                                fullJsonData.append(inputLine).append("\n");
                                if(inputLine.contains("}")){break;}
                            }
                            save_data(fullJsonData.toString(), destinationFile);
                        }
                        catch(IOException e){ // catch IOException if there are issues saving data
                            send_acknowledgement("500 INTERNAL_SERVER_ERROR", "0", out); // send bad json acknowledgment
                        }

                        //if saving data succeeds and catch section isn't called, send acknowledgment 201 (will change later)
                        if(initialJsonWeatherData.exists()){
                            send_acknowledgement("200 OK", content_length, out);
                        }
                        else {
                            send_acknowledgement("201 HTTP_CREATED", content_length, out); // add status 200 functionality
                        }

                    }


                }
                else if(inputLine.isEmpty()){ //else if the input line received is empty, acknowledgment 204 is sent to content server
                    send_acknowledgement("204 NO_CONTENT", "0", out);
                }
                else{ // else, send status 400 back to content server
                    send_acknowledgement("400 BAD_REQUEST", "0", out);
                }

            }
        }
        catch(IOException e){
            System.out.println("Exception caught when trying to listen on port " + port + " or listening for a connection");
            System.exit(1);
        }
    }
}
