# Distributed Systems Assignment 2 README File

## Compiling the Servers and Clients
The servers and clients can all be compiled by use of the makefile. When in the same directory as the makefile, run the command "make" to compile each of the servers and clients. To delete the class files, the command "make clean" can be run.

## Running the Servers and Clients
When in the src directory, each client and server can be run with the following commands:
- GETClient: "java GETClient.GETClient [hostname]:[port number] [station ID (optional)]". 
- ContentServer: "java ContentServer.ContentServer [hostname]:[port number] [weather station data file name]". 
- AggregationServer: "java AggregationServer.AggregationServer [port number (optional)]". 

The definitions of each of the arguments are as follows:
- hostname: also known as the server name. It is used to find the server
- port number: the number used to connect to the right place on the server
- station ID: the ID number for a weather station
- weather station data file name: the filename of the stored weather data to be sent to the aggregation server

## How Each Component Acts
### GETClient:
The GETClient consists of two methods. A regular main method as well as a parseJson method. The parseJson method takes in a line of Json styled text and converts it into a readable format for the client to output.

The main method's arguments currently take in a URL with hostname and port number info as well as a stationID. Within this project, the format of the URL is "[hostname]:[port number]". The ID is also assumed to be entered in full, with the letters at the beginning. 

The main method firstly organises the arguments into variables stationID, hostName and portNumber. Then it creates a socket connected to the Aggregation Server using the hostName and portNumber information. From there it creates a GET request and sends it to the server. Then it takes in the output from the server, converts it to a readable form and sends it to stdout line by line. If there is no data for the stationID supplied, the Aggregation Server will tell the GETClient who will then send a message to stdout before shutting down. If no stationID is entered, the aggregation server will send all the current weather data.

### ContentServer:
The ContentServer consists of three methods. A regular main method as well as a createJSON method and createPUTRequest method. The createJSON method takes in weather information in text format and coverts it into JSON format. The createPUTRequest method takes in the json weather data and creates the PUT request to be sent to the aggregation server. 

The main method organises the arguments into variables dataPath, hostName and portNumber. After that it creates the json weather data from the file specified in dataPath. Once done, the ContentServer attempts to connect to the server with the hostname and port number supplied to it. The code then enters a loop where the PUT request is sent once every 25 seconds. This is to act as a heartbeat message so the AggregationServer knows that the ContentServer is still alive. Within this loop there is a while loop to check the acknowledgment from the server. If the acknowledgment shows a successful PUT request and the content length matches, then the ContentServer will wait 25 seconds before the next PUT request is sent. If the acknowledgment shows a failed PUT request, the ContentServer will attempt to send a request another two times before disconnecting from the AggregationServer. 25 seconds was chosen to avoid a clash between the aggregation server deleting weather data and creating weather data for the same station.

Something worth noting is that the content server will stop if there is no weather information after the ":" in its weather file.

### AggregationServer:
The aggregation server is much more complex than the content server and the get client. It involves one AggregationServer class and one ClientHandler class. In this section the word "client" will cover both the get client and the content server.

#### AggregationServer Class
The aggregation server class has only a main method. This main takes in one port number. 

The main method begins by defining two files, the main weather data file and a backup weather data file. The first thing the server does is check that a backup file exists. If it does, it replaces the main weather data file with the backup. Whenever a method manipulates file data, it always takes a backup of the weather data first and then deletes it once it has finished. Therefore, if a backup file exists when the server starts, it is fair to assume the server had previously crashed and the old data should be restored.

Once the weather file is in order, the server checks its arguments. If it has one, it will check that port number and use it, otherwise it uses the default 4567 port number. After that a server socket is created with the port number before a while loop is entered to continuously take in connection clients / content servers.

The while loop consists of a try and catch statement to handle creating the client socket, the input and output streams and the thread creation and starting. A thread is created by creating a new ClientHandler object, then the start() function of the ClientHandler object is run.

#### ClientHandler Class
The client handler class consists of 5 methods, a constructor and four variables. The four variables are as follows

- clientSocket: The Socket object in which the clientSocket is stored
- in: The BufferedReader in which the input stream of the client is stored
- out: The PrintWriter in which the output stream of the client is stored
- initialJsonWeatherData: The file object which tracks the weather data file

##### Constructor
The constructor takes in a socket, buffered reader and print writer and sets the ClientHandler variables to them.

##### run()
This function checks the request received and does its corresponding action. Firstly, it defines needed strings to store jsonData and inputLines etc. In then goes into a try and catch statement which lasts for the rest of the method. In this method a while loop is used to read in the first line from the client. An if statement is then used to see which command was called. If a GET or PUT request was called that process would be performed. If the input was empty a 204 acknowledgement would be sent and the thread ends. Otherwise, a 400 acknowledgement is sent to the client and the thread ends.

When a GET request is received, the filename of the data the client requested is stored and the print_data method is called.

When a PUT request is received, the weather data file from the request is stored and the request is read through to receive content length. If the content length received is null or 0, a 204 acknowledgement is sent to the client, otherwise the code continues. From here the json data is read in by the server and the ID is checked in this process. If ID is invalid, a message is printed to the server and thread ends. Otherwise, the data read in is saved in the main weather file using the save_data method. If an issue occurs while saving data a 500 error is sent to the client. Upon successful data storage 200 or 201 is sent to the server. Finally, when the thread loses connection to the client, it waits 30 seconds before deleting the clients data with the delete_data function.

#### send_acknowledgement()
All acknowledgements are sent with this function. It takes in the status, content_length and the output stream. If the content_length is 0 a shorter response is sent, otherwise a longer one is.

#### save_data()
This function takes in the jsonData to be stored as well as a string for where to store this data. Firstly, 3 File objects are made, the initial weather data, a backup and a temporary one. If a backup or temporary exists, they are deleted. The initial weather data is copied into a backup and the temp file created. The initalJson file is then read through and checked to see if any IDs match the new Json Data. If there is no match it prints at the end, otherwise it overwrites the old data. From there the initial weather file is deleted, the new one is renamed to the initial one and the backup is deleted. The backup being deleted indicates to the server on startup that it didn't crash and no data needs to be restored.

#### delete_data()
This function works similarly to the save_data() function. The process is the same with the 3 files up until transferring the files from the initial weather to the new weather file. All weather data not matching the ID of that getting deleted is not printed to the new file. Then the same copying and renaming process is done.

#### print_data()
Lastly, print_data(). This function takes in a string of the requestedData and the output stream. It checks to see if the requested data is for all the data or part of the data and acts accordingly, sending it to the output stream.

### Lamport Clocks and Server Replicas
The initial idea was to implement Lamport clocks and an aggregation server replica into this assignment. Unfortunately, due to time constraints and general complexity of the theory, they were not done in time. 

## Current Errors being handled by exceptions
### GETClient:

The GETClient arguments currently take in a URL with hostname and port number info as well as a stationID. Within this project, the format of the URL is "[hostname]:[port number]". Currently, there is an exception and an if statement in place to ensure that the URL is of the correct format. There is also an exception to ensure the port number is valid too. 

To handle any issues that may come up with the sockets, an UnknownHostException and an IOException are both utilised as well.

### ContentServer:
Like the GETClient, there is also a PatternSyntaxException and NumberFormatException to check the URL format and port number validity. There is also an IOException and ArrayIndexOutOfBoundsException for when creating the JSON data.

Lastly, a few more exceptions are added toward the end to account for the sockets.


### Aggregation Server:
The first try and catch statement in the server makes sure the backup weather file copies to the normal file properly. The next ensures the correct port number validity and argument usage. The last one ensures the sockets and threads are running accordingly. 

When running the thread (and its methods), there is one main try and catch statement. This makes sure the threads run accordingly and also let the program know when a thread has disconnected. There is another used later to ensure the ID number of the data being saved is valid. Lastly, there is one to ensure the data has been saved properly. 

## Testing
The testing process for this project was quite difficult. As the methods in this project made use of File, PrintWriter and other objects, it was difficult to pass them into functions for testing and receive results. Also, most of the methods were void because they were sending output directly to the outward socket which made it difficult to check what was being sent. 

A few unit tests were still performed though as well as a few exception tests. Unfortunately, for the exception tests, the text wouldn't match because it would run with CRLF line spacings and JUnit would only take in LF line spacings for some reason. So despite the expected and actual text being the same, it was still marked as wrong. 