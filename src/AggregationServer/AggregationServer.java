package AggregationServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;

public class AggregationServer {

    public static void main(String[] args) throws IOException {

        // initial code to check if backup file is present and overwriting weather.txt with it if so.
        File jsonWeatherDataBackup = new File("AggregationServer/weather_backup.txt");
        File initialJsonWeatherData = new File("AggregationServer/weather.txt");
        if(jsonWeatherDataBackup.exists()){
            try {
                Files.copy(jsonWeatherDataBackup.toPath(), initialJsonWeatherData.toPath(), StandardCopyOption.REPLACE_EXISTING);
                if(!jsonWeatherDataBackup.delete()){
                    System.err.println("Could not delete backup file after restoring backup");
                    System.exit(1);
                }
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

        ServerSocket serverSocket = new ServerSocket(port);

        while(true) {

            // a try-catch statement to ensure all socket operation runs smoothly
            try (Socket clientSocket = serverSocket.accept();
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
            ) {

                Thread t = new ClientHandler(clientSocket, in, out, initialJsonWeatherData);

                t.start();

            } catch (IOException e) {
                System.out.println("Exception caught when trying to listen on port " + port + " or listening for a connection");
                System.exit(1);
            }
        }
    }
}

class ClientHandler extends Thread{
    /** Method to send an acknowledgement back to the client.
     * @param  status: The status to be returned to the client.
     * @param content_length: The content_length given by the client in their request (if applicable).
     * @param out: The PrintWriter associated with the client socket.
     */
    private static void send_acknowledgement(String status, String content_length, PrintWriter out){

        String response;

        //if content length is 0, the extra information in the acknowledgement isn't needed.
        if(content_length.equals("0")){
            response = "HTTP/1.1 " + status;
        }
        else {
            response = "HTTP/1.1 " + status + "\nContent-Type: text/json" + "\nContent-Length: " + content_length;
        }

        out.println(response); // send to client / content server
        out.println("-1"); // send terminating character

    }

    /** Method to save the jsonData to the weather files.
     * @param  jsonData: A string containing all the jsonData sent by the content server.
     * @param destinationFile: The path to the file where the weather data is stored.
     */
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

        Scanner initialJson = new Scanner(initialJsonWeatherData); // to read from the current weather file
        PrintWriter newJson = new PrintWriter(newJsonWeatherData); // to write to the new weather file
        String id = jsonData.substring(jsonData.indexOf("\"id\"") + 7, jsonData.indexOf("\"name\"")-4); // id of the data being added
        System.out.println("THIS IS THE ID: " + id);
        String line;

        while(initialJson.hasNextLine()){ // while there is still a line to be read in the current weather file
            line = initialJson.nextLine(); // read the line
            if(!line.contains(id)){ // if the line does not contain the id of the jsonData being added
                newJson.println(line); // print that line to the new weather data file
            }
            else{ // if the line does contain the id of the jsonData being added
                newJson.print(jsonData.substring(2)); // add that jsonData to the new weather file (minus the starting curly bracket)
                while(!line.contains("}")){ // while line does not contain the ending curly bracket
                    line = initialJson.nextLine(); // progress through the lines
                }
            }
        }
        newJson.close(); //close the PrintWriter
        initialJson.close(); //close the scanner

        // delete the initial weather data
        if(!initialJsonWeatherData.delete()){
            System.err.println("Original weather.txt could not be deleted");
            System.exit(1);
        }
        // rename the new weather file to the same name as the initial one
        if(!newJsonWeatherData.renameTo(initialJsonWeatherData)){
            System.err.println("Temp weather file wasn't renamed");
            System.exit(1);
        }
        // delete the backup weather file
        if(!jsonWeatherDataBackup.delete()){
            System.err.println("weather_backup.txt could not be deleted");
            System.exit(1);
        }

    }

    /** Method to print the jsonData requested by a GETClient.
     * @param requestedData: A string containing the station ID for the data requested by the GETClient
     * @param out: THe PrintWriter associated with the client socket.
     */
    private static void print_data(String requestedData, PrintWriter out) throws IOException{

        BufferedReader data = new BufferedReader(new FileReader("AggregationServer/weather.txt"));
        String line;
        boolean dataFound = false;

        if(requestedData.equals("/")){ // if requested data is all stations
            while((line = data.readLine()) != null){ // send the entire file to the client
                out.println(line);
            }
            out.println("-1"); // send terminating character
        }
        else{ // otherwise (if a specific station is requested)
            while((line = data.readLine()) != null){ // read the lines of data
                if(line.contains(requestedData)){ // if the line read contains the ID requested
                    dataFound = true; // set dataFound flag to true
                    out.println(line); // send the line that was found
                    while(((line = data.readLine()) != null) && !(line.trim().equals("}"))){ // while there are lines to read, and it has not yet reached the ending curly brace
                        out.println(line); // send the line to the client
                    }
                    out.println("}"); // send the client the final curly brace
                    out.println("-1"); // send the terminating character
                }
            }
        }

        // if the data was not found, send a message to the client
        if(!dataFound){
            out.println("No relevant ID data");
        }
        data.close();
    }

    final Socket clientSocket;
    final BufferedReader in;
    final PrintWriter out;

    File initialJsonWeatherData;

    public ClientHandler(Socket s, BufferedReader in, PrintWriter out, File initialJsonWeatherData){
        clientSocket = s;
        this.in = in;
        this.out = out;
        this.initialJsonWeatherData = initialJsonWeatherData;
    }

    @Override
    public void run(){

        try {
            // defining strings for later use
            String inputLine;
            String requestedData;
            String destinationFile;
            String content_length;
            while ((inputLine = in.readLine()) != null) { // read in input

                if (inputLine.startsWith("GET")) { // if the input line starts with GET, data is being given to client

                    //delete GET and HTTP/1.1 from start and end of string to isolate requested Data
                    requestedData = (inputLine.substring(3, inputLine.length() - 9)).trim();

                    print_data(requestedData, out); //call print_data function to send data to client

                } else if (inputLine.startsWith("PUT")) { //else if the input line starts with PUT, a content server wants to store data

                    System.out.println(inputLine);

                    // Get the name of the file where the weather json data is being stored from the PUT request
                    destinationFile = (inputLine.substring(3, inputLine.length() - 9)).trim();

                    // Cycle through the received lines until the content-length line has been read into inputLine
                    while ((inputLine = in.readLine()) != null) {
                        if (inputLine.startsWith("Content-Length")) {
                            break;
                        }
                    }

                    // Set content_length string to value in inputLine or to 0 if inputLine is null
                    content_length = inputLine != null ? inputLine.substring(16) : "0";

                    if (content_length.equals("0")) { // if content length is 0 (no content arrives)
                        send_acknowledgement("204 NO_CONTENT", content_length, out); //send a 204 acknowledgment back to the content server
                    } else { // otherwise (content length isn't 0)

                        StringBuilder fullJsonData = new StringBuilder();
                        String status;

                        //if initial file is there set status as 200 otherwise set it as 201
                        if (initialJsonWeatherData.exists()) {
                            status = "200 OK";
                        } else {
                            status = "201 HTTP_CREATED";
                        }

                        try { // try to save data
                            while ((inputLine = in.readLine()) != null) {
                                fullJsonData.append(inputLine).append("\n");
                                if (inputLine.contains("}")) {
                                    break;
                                }
                            }
                            save_data(fullJsonData.toString(), destinationFile);
                        } catch (IOException e) { // catch IOException if there are issues saving data
                            send_acknowledgement("500 INTERNAL_SERVER_ERROR", "0", out); // send bad json acknowledgment
                        }

                        //if data is saved and catch statement isn't entered, send acknowledgement
                        send_acknowledgement(status, content_length, out);

                    }


                } else if (inputLine.isEmpty()) { //else if the input line received is empty, acknowledgment 204 is sent to content server
                    send_acknowledgement("204 NO_CONTENT", "0", out);
                } else { // else, send status 400 back to content server
                    send_acknowledgement("400 BAD_REQUEST", "0", out);
                }

            }
        }
        catch(IOException e){
            System.err.println("IOException in ClientHandler");
            System.exit(1);
        }

    }

}
