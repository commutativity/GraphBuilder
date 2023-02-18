package utils

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.{SQLContext, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}

object Spark {
  Logger.getLogger("main").setLevel(Level.ERROR)

  val conf: SparkConf = new SparkConf().setAppName("SparkTest").setMaster("local[*]").set("spark.executor.memory", "16g")
  val sparkContext = new SparkContext(conf)
  val sparkSession: SparkSession = SparkSession.builder.config(sparkContext.getConf)
    .config("spark.local.dir", "D://spark").getOrCreate()
  val sqlContext: SQLContext = sparkSession.sqlContext

}
