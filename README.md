# Distributed Systems Assignment 2 README File

## How to Run the Servers and Clients

## How Each Component Acts

## Current Errors being handled by exceptions
### GETClient:

The GETClient arguments currently take in a URL with hostname and port number info as well as a stationID. Within this project, the format of the URL is "[hostname]:[port number]". Currently, there is an exception and an if statement in place to ensure that the URL is of the correct format. There is also an exception to ensure the port number is valid too.

- Need to add exceptions to ensure stationID is of the correct format too
- Need to talk about UnknownHostException and IOException

### Aggregation Server:

- There is only one optional argument for the aggregation server, this is the port number it should run on.
  - There is currently an if statement to check if this argument has been entered or not or if too many arguments were entered
  - If the argument hasn't been entered, there is than a try and catch statement used to ensure the argument given is a valid port number to be used.
  - If too many arguments are entered, an error message is given showing the format of the arguments.
