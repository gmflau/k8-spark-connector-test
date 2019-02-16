package datastax

import com.datastax.spark.connector._
import org.apache.spark._

object HelloWorld {

  def main(args: Array[String]) {
    // only setting app name, all other properties will be specified at runtime for flexibility

    val conf = new SparkConf().setAppName("cassandra-example-hello")

    val sc = new SparkContext(conf)

    val hello = sc.cassandraTable[(String, String)]("test", "hello")

    val first = hello.first

    sc.stop

    println(first)

  }

}
