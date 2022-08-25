### Real-Time Weather Scraping : using Python, Spark Structured-Streaming, Kafka and Cassandra

---

#### Resume :

It is a weather data scraping tool that captures data in real time from Google and then sends it to a Kafka topic.
This data recorded in Kafka will then be processed and structured in streaming by Spark and finally saved in Cassandra.

- Collect (Scrape) real-time weather data (Region to check, Temperature and Units, Weather Description, Day) with Python (`requests_html` library)
- Send scraped data to Kafka topic (`weather`)
- Structure scraped data in streaming with Spark Streaming (subscribe to `weather` topic and process data)
- Store final data in Cassandra (keyspace : `stuff`, table : `weathers`)

| **Scraped weather data** | **Kafka** | **Spark** | **Cassandra** |

---

#### Quickstart :

1. Start a Kafka server and create a Kafka topic :

  - Starting Kafka server :

    ```shell
    # Start Zookeeper
    zookeeper-server-start.sh $KAFKA_HOME/config/zookeeper.properties
    ```

    ```shell
    # Start Kafka
    kafka-server-start.sh $KAFKA_HOME/config/server.properties
    ```

  * Creating a topic called `weather` :

    ```shell
    # Create weather topic
    kafka-topics.sh --create --partitions 1 --replication-factor 1 --topic weather --bootstrap-server localhost:9092
    ```

    ```shell
    # Describe created topic
    kafka-topics.sh --describe --topic weather --bootstrap-server localhost:9092
    ```

2. Start a Cassandra database

  * Starting Cassandra server :

    ```shell
    cassandra -f
    ```

    ```shell
    cqlsh
    ```

  * Create a keyspace called `stuff` (SimpleStrategy, replication=1)

    ```cassandra
    CREATE KEYSPACE stuff
    WITH replication = {'class': 'SimpleStrategy, 'replication_factor' : 1};
    ```

    ```cassandra
    USE stuff;
    ```

  * Create a table called `weathers` with the following schema

    ```cassandra
    CREATE TABLE weathers ( 
        uuid uuid primary key, 
        location text,  
        temperature text,  
        description text,  
        day text,  
        loadDate text 
    );
    ```

3. Inside the `WeatherStreamHandler` directory, package up the Scala file:

   ```shell
   sbt package
   ```

4. Then run

   ```shell
   spark-submit --class sn.rachitech.StreamHandler \
   --master local[*] \
   --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.4.8,\
   	com.datastax.cassandra:cassandra-driver-core:4.0.0,\
   	com.datastax.spark:spark-cassandra-connector_2.11:2.5.2 \
   target/scala-2.11/stream-handler_2.11-1.0.jar
   ```

   

  > Instead of doing steps 3 and 4, you can also run the app with IntelliJ



5. From root directory, start the python scraping script :

  ```shell
  python ./weatherscraping.py
  ```

6. `select * from weathers` from CQLSH to see if the data is being processed saved correctly!
