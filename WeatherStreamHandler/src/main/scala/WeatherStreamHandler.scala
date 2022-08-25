package sn.rachitech.weather

import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming._
import org.apache.spark.sql.cassandra._
import com.datastax.oss.driver.api.core.uuid.Uuids
import org.apache.log4j.{Level, Logger}

/**
 * Main Object/Class that handles stream weather data coming from scraping script and Kafka,
 * formalizes data structure and loads it to Cassandra
 */
object WeatherStreamHandler {
  def main(args: Array[String]): Unit = {

    // Reduce Spark logging verbosity
    Logger.getLogger("org.apache.spark").setLevel(Level.ERROR)

    /** Initialize Spark and Launch Cassandra Server Connection
     */
    val spark: SparkSession = SparkSession
      .builder()
      .master("local[*]")
      .appName("Stream Weather Scraper")
      .config("spark.cassandra.connection.host", "localhost")
      .getOrCreate()

    import spark.implicits._

    /** Formalize scraped weather data coming from kafka
     *
     * Creating the Kafka source for streaming queries
     * (Read from Kafka: Subcribe to the `weather` topic)
     */
    val inputWeatherDF = spark
      .readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092") // localhost:9092, edgenode:6667
      .option("subscribe", "weather")
      .option("startingOffsets", "earliest") // From starting
      .load()

    //inputWeatherDF.printSchema()

    val rawWeatherDF = inputWeatherDF.selectExpr("CAST(value AS STRING)").as[String] // Select only 'value' from the table and convert from bytes to string

    // Split each row on comma and load it to the case class
    val expendedWeatherDF = rawWeatherDF.map(row => row.split(","))
      .map(row => WeatherData(
        row(0), // location
        row(1), // temperature
        row(2), // description
        row(3), // day: String
        row(4)  // loadDate
      ))

    // User Defined Function that creates UUIDs
    val makeUUID = udf(() => Uuids.timeBased().toString)

    // Add the UUIDs and renamed the columns
    // This is necessary so that the dataframe matches the table schema in Cassandra
    val weatherExpendedWithIDs = expendedWeatherDF.withColumn("uuid", makeUUID())

    /** Write dataframe to Cassandra
     */
    /*
    val query = weatherExpendedWithIDs
      .writeStream
      .trigger(Trigger.ProcessingTime("5 seconds"))
      .outputMode("append")
      .format("console")
      .start() */
    val query = weatherExpendedWithIDs
      .writeStream
      .trigger(Trigger.ProcessingTime("5 seconds"))
      .foreachBatch {(batchDF: DataFrame, batchID: Long) =>
        println(s"Writing to Cassandra $batchID")
        batchDF.write
          .cassandraFormat("weathers", "stuff") // table, keyspace
          .mode("append")
          .save()
      }
      .outputMode("append")
      .start()
    
    // Writing until Ctrl+C
    query.awaitTermination()

  }
}

