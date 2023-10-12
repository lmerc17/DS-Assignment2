all: compile clean

compile: src/AggregationServer/AggregationServer.java src/ContentServer/ContentServer.java src/GETClient/GETClient.java
	javac src/AggregationServer/AggregationServer.java
	javac src/ContentServer/ContentServer.java
	javac src/GETClient/GETClient.java

clean:
	rm -f src/AggregationServer/AggregationServer.class
	rm -f src/ContentServer/ContentServer.class
	rm -f src/GETClient/GETClient.class