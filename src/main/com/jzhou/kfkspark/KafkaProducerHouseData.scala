package com.jzhou.kfkspark


import java.util.HashMap

import org.apache.kafka.clients.producer.{ProducerConfig, KafkaProducer, ProducerRecord}

import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka._
import org.apache.spark.SparkConf
import scala.collection.mutable.ArrayBuffer
import scala.io.Source



object KafkaProducerHouseData  {


  def main(args: Array[String]) {
    if (args.length < 2) {
      System.err.println("Usage: KafkaProducerHouseData <messagesPerSec> <inputFile>")
      System.exit(1)
    }

    val Array(messagesPerSec, inputFile) = args


    // Zookeeper connection properties
    val props = new HashMap[String, Object]()
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, ""broker1:9092,broker2:9092")
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
      "org.apache.kafka.common.serialization.StringSerializer")
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
      "org.apache.kafka.common.serialization.StringSerializer")

    val producer = new KafkaProducer[String, String](props)

    var topic:String = _
    var timeObs = ArrayBuffer[(String,String)]()
    var topicAndContent = ArrayBuffer[(String, String)]
  

    // Parse the xml file and save the data in buffer
    for (line <- Source.fromFile(inputFile).getLines) {
       if (line.trim().startWith("<cansim:Series ")) {
           topic = parseXmlGeo(scala.xml.XML.loadString(line)))
       }
       else if (line.trim().startWith("<cansim:Obs "))  {
           val timeAndObs = parseXmlObs(scala.xml.XML.loadString(line)))
           timeObs += timeAndObs     
       }
       else if (line.trim().startWith("</cansim:Series>")) {
           val strTimeObs = timeObs.mkString(",")
           topicAndContent += (topic, strTimeObs)
           
           // Reset timeObs
           timeObs.clear()

       }
       else None 

    }


    // Send message
    for (i <- 0 until topicAndContent.length()) {
       (1 to messagePerSec.toInt).foreach { messageNum =>
    
          if (i < topicAndContent.length()) {
             val message = new ProducerRecord[String, String](topicAndContent[i]._1, null, topicAndContent[i]._2)
     
             producer.send(message)
             i++
          }
          else  None
            
       }
     
       Thread.sleep(1000)
    }


    // Some functions below
    def parseXMLGeo(x: scala.xml.Elem): String = {
        // Return geo+index
        (x \ "@GEO").text + "+" + (x \ ":@INDEX").text
    }


    def parseXMLObs(x: scala.xml.Elem): (String,String) = {
        // Return (time_period, obs_value)
        ((x \ "@TIME_PERIOD").text, (x \ ":@OBS_VALUE").text )
    }
 

}
