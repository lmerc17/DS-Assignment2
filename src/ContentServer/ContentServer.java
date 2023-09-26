package ContentServer;

import java.util.regex.PatternSyntaxException;
import java.net.UnknownHostException;
import java.util.regex.PatternSyntaxException;

public class ContentServer {

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

    }
}
