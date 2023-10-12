package AggregationServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

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
                    return;
                }
            } catch (IOException e) {
                System.err.println("Could not restore backup weather data");
                return;
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
                return;
            }
        }
        else{
            System.err.println("Usage: java AggregationServer <port number>");
            return;
        }

        //server socket is created with port given above
        ServerSocket serverSocket = new ServerSocket(port);

        while(true) { //infinite loop to allow server to accept multiple clients

            // a try-catch statement to ensure all socket operation runs smoothly
            try{
                Socket clientSocket = serverSocket.accept();
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                //a thread object is created to take in all the client information and location of initial weather data
                Thread t = new ClientHandler(clientSocket, in, out);

                //thread is started
                t.start();

            } catch (IOException e) {
                System.out.println("Exception caught when trying to listen on port " + port + " or listening for a connection");
                return;
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
    public static void send_acknowledgement(String status, String content_length, PrintWriter out){

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

    /** Method to save jsonData to the weather file.
     * @param  jsonData: A string containing all the jsonData sent by the content server.
     * @param destinationFile: The path to the file where the weather data is stored.
     */
    public static void save_data(String jsonData, String destinationFile) throws IOException{

        // initialJsonWeather Data and newJsonWeatherData files are created.
        File jsonWeatherDataBackup = new File("AggregationServer/weather_backup.txt");
        File initialJsonWeatherData = new File("AggregationServer/" + destinationFile);
        File newJsonWeatherData = new File("AggregationServer/weather_temp.txt");

        if(jsonWeatherDataBackup.exists()){ // if weather_backup.txt exists
            if(!newJsonWeatherData.delete()){ // delete it
                System.err.println("weather_backup.txt cannot be deleted");
                return;
            }
        }
        if(newJsonWeatherData.exists()){ // if weather_temp.txt exists
            if(!newJsonWeatherData.delete()){ // delete it
                System.err.println("weather_temp.txt cannot be deleted");
                return;
            }
        }

        // copy initial json weather data into backup file
        Files.copy(initialJsonWeatherData.toPath(), jsonWeatherDataBackup.toPath());

        //create weather_temp file
        if(!newJsonWeatherData.createNewFile()){
            System.err.println("weather_temp.txt could not be created");
            return;
        }

        Scanner initialJson = new Scanner(initialJsonWeatherData); // to read from the current weather file
        PrintWriter newJson = new PrintWriter(newJsonWeatherData); // to write to the new weather file
        String id = jsonData.substring(jsonData.indexOf("\"id\"") + 6, jsonData.indexOf("\"name\"")-4); // id of the data being added
        String line;
        boolean dataAdded = false;

        while(initialJson.hasNextLine()){ // while there is still a line to be read in the current weather file
            line = initialJson.nextLine(); // read the line
            if(!line.contains(id)){ // if the line does not contain the id of the jsonData being added
                newJson.println(line); // print that line to the new weather data file
            }
            else{ // if the line does contain the id of the jsonData being added
                newJson.print(jsonData.substring(2)); // add that jsonData to the new weather file (minus the starting curly bracket)
                while(!line.contains("}")){ // while line does not contain the ending curly bracket
                    line = initialJson.nextLine(); // progress through the lines
                    dataAdded = true;
                }
            }
        }

        // if statement to print jsonData if the weather data file is empty (and none of the above conditions have been met)
        if(!dataAdded){
            newJson.print(jsonData);
        }

        newJson.close(); //close the PrintWriter
        initialJson.close(); //close the scanner

        // delete the initial weather data
        if(!initialJsonWeatherData.delete()){
            System.err.println("Original weather.txt could not be deleted");
            return;
        }
        // rename the new weather file to the same name as the initial one
        if(!newJsonWeatherData.renameTo(initialJsonWeatherData)){
            System.err.println("Temp weather file wasn't renamed");
            return;
        }
        // delete the backup weather file
        if(!jsonWeatherDataBackup.delete()){
            System.err.println("weather_backup.txt could not be deleted");
        }

    }

    /** Method to delete certain jsonData from the weather file.
     * @param  id: A string containing all the ID of the weather data to be deleted
     * @param destinationFile: The path to the file where the weather data is being deleted.
     */
    public static void delete_data(String id, String destinationFile) throws IOException{

        // initialJsonWeather Data and newJsonWeatherData files are created.
        File jsonWeatherDataBackup = new File("AggregationServer/weather_backup.txt");
        File initialJsonWeatherData = new File("AggregationServer/" + destinationFile);
        File newJsonWeatherData = new File("AggregationServer/weather_temp.txt");

        if(jsonWeatherDataBackup.exists()){ // if weather_backup.txt exists
            if(!newJsonWeatherData.delete()){ // delete it
                System.err.println("weather_backup.txt cannot be deleted");
                return;
            }
        }
        if(newJsonWeatherData.exists()){ // if weather_temp.txt exists
            if(!newJsonWeatherData.delete()){ // delete it
                System.err.println("weather_temp.txt cannot be deleted");
                return;
            }
        }

        // copy initial json weather data into backup file
        Files.copy(initialJsonWeatherData.toPath(), jsonWeatherDataBackup.toPath());

        //create weather_temp file
        if(!newJsonWeatherData.createNewFile()){
            System.err.println("weather_temp.txt could not be created");
            return;
        }

        Scanner initialJson = new Scanner(initialJsonWeatherData); // to read from the current weather file
        PrintWriter newJson = new PrintWriter(newJsonWeatherData); // to write to the new weather file
        String line;

        while(initialJson.hasNextLine()){ // while there is still a line to be read in the current weather file
            line = initialJson.nextLine(); // read the line
            if(line.contains("id") && !line.contains(id)){ // if the line contains "id" and doesn't contain the ID of data being deleted
                newJson.println("{"); // print { on a line
                newJson.println(line); // print the read line on a line
            }
            else if(!line.contains(id) && !line.contains("{")){ // else if the line doesn't contain the ID of the data being collected or a {
                newJson.println(line); // print the new line
            }
            else{ // if the line does contain the id of the jsonData being deleted
                while(!line.contains("}") && initialJson.hasNextLine()){ // skip to the next }
                    line = initialJson.nextLine(); // progress through the lines and don't read them
                }
            }
        }

        newJson.close(); //close the PrintWriter
        initialJson.close(); //close the scanner

        // delete the initial weather data
        if(!initialJsonWeatherData.delete()){
            System.err.println("Original weather.txt could not be deleted");
            return;
        }
        // rename the new weather file to the same name as the initial one
        if(!newJsonWeatherData.renameTo(initialJsonWeatherData)){
            System.err.println("Temp weather file wasn't renamed");
            return;
        }
        // delete the backup weather file
        if(!jsonWeatherDataBackup.delete()){
            System.err.println("weather_backup.txt could not be deleted");
        }

    }

    /** Method to print the jsonData requested by a GETClient.
     * @param requestedData: A string containing the station ID for the data requested by the GETClient
     * @param out: The PrintWriter associated with the client socket.
     */
    public static void print_data(String requestedData, PrintWriter out) throws IOException{

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

    //define variables for ClientHandler Object
    final Socket clientSocket;
    final BufferedReader in;
    final PrintWriter out;

    final File initialJsonWeatherData = new File("AggregationServer/weather.txt");

    /** Constructor for a client handler class
     * @param s: The clientSocket
     * @param in: The BufferedReader which is reading the GETClient information
     * @param out: The PrintWriter which is sending information to the GETClient
     */
    public ClientHandler(Socket s, BufferedReader in, PrintWriter out){
        clientSocket = s;
        this.in = in;
        this.out = out;
    }

    /** Run method for the thread ClientHandler object. */
    @Override
    public void run(){

        //defining strings for later use
        StringBuilder fullJsonData = null;
        String destinationFile = null;

        try {
            // defining strings for later use
            String inputLine;
            String requestedData;
            String content_length;
            while ((inputLine = in.readLine()) != null) { // read in input

                if (inputLine.startsWith("GET")) { // if the input line starts with GET, data is being given to client

                    //delete GET and HTTP/1.1 from start and end of string to isolate requested Data
                    requestedData = (inputLine.substring(3, inputLine.length() - 9)).trim();

                    print_data(requestedData, out); //call print_data function to send data to client

                } else if (inputLine.startsWith("PUT")) { //else if the input line starts with PUT, a content server wants to store data

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

                        fullJsonData = new StringBuilder();
                        String status;

                        //if initial file is there set status as 200 otherwise set it as 201
                        if (initialJsonWeatherData.exists()) {
                            status = "200 OK";
                        } else {
                            status = "201 HTTP_CREATED";
                        }

                        try { // try to save data
                            while ((inputLine = in.readLine()) != null) { //while there is data to read
                                fullJsonData.append(inputLine).append("\n"); //append the input line to fullJsonData
                                if (inputLine.contains("}")) { //if the input contains }, stop reading
                                    break;
                                }
                                else if (inputLine.contains("\"id\"")){ // if the input contains id:
                                    if(inputLine.contains("IDS")) { // if the id contains IDS
                                        try { // try turning the ID after it into a number
                                            Integer.parseInt(inputLine.substring(10, inputLine.length()-2));
                                        } catch (NumberFormatException e) { // if it is not a valid number, end the connection and stop the thread
                                            System.err.println("Invalid ID in Data received");
                                            clientSocket.close();
                                            in.close();
                                            out.close();
                                            return; // close the thread
                                        }
                                    }
                                    else{ // if IDS isn't present, end the connection and stop the thread.
                                        System.err.println("Invalid ID in Data received");
                                        clientSocket.close();
                                        in.close();
                                        out.close();
                                        return;
                                    }
                                }
                            }
                            save_data(fullJsonData.toString(), destinationFile);
                        }
                        catch (IOException e) { // catch IOException if there are issues saving data
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
        catch(IOException e){ // if the server loses connection with the client (GETClient or Content Server)
            if(fullJsonData != null && destinationFile != null) { // if these two variables have been used (meaning Content Server has been used)
                String jsonData = fullJsonData.toString(); //get the Json data
                String id = jsonData.substring(jsonData.indexOf("\"id\"") + 6, jsonData.indexOf("\"name\"") - 4); // get the id of the data being deleted
                try {
                    TimeUnit.SECONDS.sleep(30); //run a 30s timer
                    delete_data(id, destinationFile); // call delete data function for specified id and weather data file
                }
                catch (InterruptedException | IOException ex) { // if the data couldn't be deleted, show an error message
                    System.err.println("Could not delete weather data from disconnected content server with ID: " + id);
                }

            }


        }

    }

}
