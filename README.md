# Distributed Systems Assignment 2 README File

## How to Run the Servers and Clients

## How Each Component Acts
### GETClient:
The GETClient consists of two methods. A regular main method as well as a parseJson method. The parseJson method takes in a line of Json styled text and converts it into a readable format for the client to output.

The main method's arguments currently take in a URL with hostname and port number info as well as a stationID. Within this project, the format of the URL is "[hostname]:[port number]". The ID is also assumed to be entered in full, with the letters at the beginning. 

The main method firstly organises the arguments into variables stationID, hostName and portNumber. Then it creates a socket connected to the Aggregation Server using the hostName and portNumber information. From there it creates a GET request and sends it to the server. Then it takes in the output from the server, converts it to a readable form and sends it to stdout line by line. If there is no data for the stationID supplied, the Aggregation Server will tell the GETClient who will then send a message to stdout before shutting down.

### ContentServer:
The ContentServer consists of three methods. A regular main method as well as a createJSON method and createPUTRequest method. The createJSON method takes in weather information in text format and coverts it into JSON format. The createPUTRequest method takes in the json weather data and creates the PUT request to be sent to the aggregation server. 

The main method organises the arguments into variables dataPath, hostName and portNumber. After that it creates the json weather data from the file specified in dataPath. Once done, the ContentServer attempts to connect to the server with the hostname and port number supplied to it. The code then enters a loop where the PUT request is sent once every 25 seconds. This is to act as a heartbeat message so the AggregationServer knows that the ContentServer is still alive. Within this loop there is a while loop to check the acknowledgment from the server. If the acknowledgment shows a successful PUT request and the content length matches then the ContentServer will wait 25 seconds before the next PUT request is sent. If the acknowledgment shows a failed PUT request, the ContentServer will attempt to send a request another two times before disconnecting from the AggregationServer.

### AggregationServer:


## Current Errors being handled by exceptions
### GETClient:

The GETClient arguments currently take in a URL with hostname and port number info as well as a stationID. Within this project, the format of the URL is "[hostname]:[port number]". Currently, there is an exception and an if statement in place to ensure that the URL is of the correct format. There is also an exception to ensure the port number is valid too.

- Need to talk about UnknownHostException and IOException

### ContentServer:


### Aggregation Server:

- There is only one optional argument for the aggregation server, this is the port number it should run on.
  - There is currently an if statement to check if this argument has been entered or not or if too many arguments were entered
  - If the argument hasn't been entered, there is than a try and catch statement used to ensure the argument given is a valid port number to be used.
  - If too many arguments are entered, an error message is given showing the format of the arguments.
