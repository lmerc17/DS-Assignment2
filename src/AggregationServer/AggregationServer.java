package AggregationServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class AggregationServer {

    private static void save_data(String jsonData) throws IOException{

        File intermediate = new File("AggregationServer/intermediate_weather.json");

        if(intermediate.exists()) {
            intermediate.delete();
        }

        intermediate.createNewFile();

    }

    private static void print_data(String requestedData, PrintWriter out) throws IOException{

        BufferedReader data = new BufferedReader(new FileReader("AggregationServer/weather.json"));
        String line;

        if(requestedData.equals("/")){
            while((line = data.readLine()) != null){
                out.println(line);
            }
            out.println("-1");
        }
        else{
            while((line = data.readLine()) != null){
                if(line.contains(requestedData)){
                    while(((line = data.readLine()) != null) && !(line.trim().equals("}"))){
                        out.println(line);
                    }
                    out.println("}");
                    out.println("-1");
                }
            }
        }
        data.close();
    }

    public static void main(String[] args){

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
            System.out.println("Client connected on port " + port);
            String inputLine;
            String requestedData;
            while((inputLine = in.readLine()) != null){

                if(inputLine.startsWith("GET")){

                    //delete GET and HTTP/1.1 from start and end of string to isolate requested Data
                    requestedData = (inputLine.substring(3, inputLine.length() - 9)).trim();
                    System.out.println("Received message: " + inputLine + " from " + clientSocket);

                    print_data(requestedData, out);

                }
                else if(inputLine.startsWith("PUT")){

                    System.out.println(inputLine);


                }

            }
        }
        catch(IOException e){
            System.out.println("Exception caught when trying to listen on port " + port + " or listening for a connection");
            System.exit(1);
        }
    }
}
