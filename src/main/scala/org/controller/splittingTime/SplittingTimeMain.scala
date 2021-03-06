package org.controller.splittingTime

//import org.util.SparkOpener
//import java.text.SimpleDateFormat
//import java.sql.Timestamp
import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import java.sql.Timestamp
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import scala.collection.mutable.{ListBuffer,ArrayBuffer}
//import java.util.Date
import org.constants.projectConstants
import org.util.{SparkOpener,readWriteUtil}

object SplittingTimeMain extends SparkOpener  {

val spark=SparkSessionLoc("Testing Minute split")
val sc =spark.sparkContext
  import spark.implicits._
  def main(args: Array[String]): Unit = {
    val inputMap:collection.mutable.Map[String,String]= collection.mutable.Map[String,String]()
    inputMap.put(projectConstants.headerArgConstant,projectConstants.stringTrue)
    inputMap.put(projectConstants.inferSchemaArgConstant,projectConstants.stringFalse)
    inputMap.put(projectConstants.delimiterArgConstant,projectConstants.delimiterOr)
    inputMap.put(projectConstants.basePathArgConstant,System.getProperty("user.dir")+"/Input/")
    inputMap.put(projectConstants.fileTypeArgConstant,projectConstants.fileTypeCsvValue)
    inputMap.put(projectConstants.fileFormatArg,projectConstants.csvFormat)
    inputMap.put(projectConstants.filePathArgValue,System.getProperty("user.dir")+"/Input/InputForSplit.txt")

    val TripRecord=readWriteUtil.readDF(spark,inputMap)
    //val TripRecord=spark.read.format("csv").option("header","true").option("inferSchema","false").option("delimiter","|").load("C:\\Users\\RAPTOR\\IdeaProjects\\SparkLearning\\Input\\InputForSplit.txt")

    //val ReferenceTrip=spark.read.format("csv").option("header","true").option("inferSchema","true").option("delimiter","|").load("C:\\Users\\RAPTOR\\IdeaProjects\\SparkLearning\\Input\\ReferenceForSplit.txt")
    val tripRecordSplitted =TripRecord.flatMap(row => Splitting(row)) // -- only works on repl
    val finalDfTemp=tripRecordSplitted.selectExpr("value[0] as StateID","value[1] as VehicleID","value[2] as Model","cast (value[3] as Timestamp) as StartQhr","cast(value[4] as int) as M1","cast(value[5] as int) as M2","cast (value[6] as int) as M3","cast(value[7] as int) as M4","cast (value[8] as int) as M5","cast (value[9] as int) as M6","cast (value[10] as int) as M7","cast (value[11] as int) as M8","cast (value[12] as int) as M9","cast (value[13] as int) as M10","cast (value[14] as int) as M11","cast (value[15] as int) as M12","cast (value[16] as int) as M13","cast (value[17] as int) as M14","cast (value[18] as int) as M15","cast (value[19] as int) as Total")
    //combining over lapping records
    val finalDf=finalDfTemp.drop("Total").groupBy("StateID","VehicleID","Model","StartQhr").agg(
       max("M1").as("M1")
      ,max("M2").as("M2")
      ,max("M3").as("M3")
      ,max("M4").as("M4")
      ,max("M5").as("M5")
      ,max("M6").as("M6")
      ,max("M7").as("M7")
      ,max("M8").as("M8")
      ,max("M9").as("M9")
      ,max("M10").as("M10")
      ,max("M11").as("M11")
      ,max("M12").as("M12")
      ,max("M13").as("M13")
      ,max("M14").as("M14")
      ,max("M15").as("M15")).withColumn("totalMins",col("M1")+col("M2")+col("M3")+col("M4")+col("M5")+col("M6")+col("M7")+col("M8")+col("M9")+col("M10")+col("M11")+col("M12")+col("M13")+col("M14")+col("M15")).orderBy("StateID","VehicleID","Model","StartQhr")
    finalDf.show

    val tripRecordCaseClassSplitted =TripRecord.flatMap(SplittingCaseClass(_)).toDF
    val finalDfCaseClass=tripRecordCaseClassSplitted.drop("totalMinutes").groupBy("StateID","VehicleID","Model","StartQhr").agg(
      max("M1").as("M1")
      ,max("M2").as("M2")
      ,max("M3").as("M3")
      ,max("M4").as("M4")
      ,max("M5").as("M5")
      ,max("M6").as("M6")
      ,max("M7").as("M7")
      ,max("M8").as("M8")
      ,max("M9").as("M9")
      ,max("M10").as("M10")
      ,max("M11").as("M11")
      ,max("M12").as("M12")
      ,max("M13").as("M13")
      ,max("M14").as("M14")
      ,max("M15").as("M15")).withColumn("totalMins",col("M1")+col("M2")+col("M3")+col("M4")+col("M5")+col("M6")+col("M7")+col("M8")+col("M9")+col("M10")+col("M11")+col("M12")+col("M13")+col("M14")+col("M15")).orderBy("StateID","VehicleID","Model","StartQhr")
    finalDfCaseClass.show


  }

// uses listBuffer and returns list of string
  def Splitting(TripRecord:Row) =
  {
    val dateFormat="YYYY-MM-DD HH:mm:ss"
    val OutputList= new ListBuffer[List[String]]
    val startTimeSource=TripRecord.getString(3)
    val endTimeSource=TripRecord.getString(4)
    val startTime=DateTime.parse(startTimeSource,DateTimeFormat.forPattern(dateFormat))
    val endTime=DateTime.parse(endTimeSource,DateTimeFormat.forPattern(dateFormat))
    var newStartTime=startTime
    var startMinute=0
    var endMinute=15
    val minuteArray=new Array[Int](15)
    var splitNeeded=true
    while(splitNeeded)
    {
      var totalMinute=0
      startMinute=newStartTime.getMinuteOfDay%15
      var newEndTime=newStartTime.plusMinutes(15-startMinute)
      newStartTime.getMinuteOfDay%15 match
      {
        case 0 => newStartTime=newStartTime
        case _ => newStartTime=newStartTime.minusMinutes(startMinute)
      }
      endMinute=newEndTime.getMinuteOfDay%15
      /*if(newEndTime.compareTo(endTime) > 0)
       {
         splitNeeded=false
         newEndTime=endTime
         endMinute=endTime.getMinuteOfDay%15
       }*/
      newEndTime.compareTo(endTime) match
        {
        case value if value>0 => splitNeeded=false;newEndTime=endTime;endMinute=endTime.getMinuteOfDay%15
        case _ =>   splitNeeded=true
        }
      if (splitNeeded || (endMinute ==0))
        endMinute=15
      for (i <- 0 to 14 )
        i match {
          case value if value >= startMinute && value < endMinute => minuteArray(i)=1;totalMinute+=1
          case _ =>minuteArray(i)=0
        }
      OutputList+=List(TripRecord.getString(0),TripRecord.getString(1),TripRecord.getString(2),newStartTime.toString(),minuteArray(0).toString,minuteArray(1).toString,minuteArray(2).toString,minuteArray(3).toString,minuteArray(4).toString,minuteArray(5).toString,minuteArray(6).toString,minuteArray(7).toString,minuteArray(8).toString,minuteArray(9).toString,minuteArray(10).toString,minuteArray(11).toString,minuteArray(12).toString,minuteArray(13).toString,minuteArray(14).toString,totalMinute.toString)
      newStartTime=newEndTime
    }
    OutputList.toList
  }

//uses arrayBuffer[Case classes] -- case class for schema
  def SplittingCaseClass(TripRecord:Row) =
  {
    val dateFormat="YYYY-MM-DD HH:mm:ss"
    val outputArrayBuffer= new ArrayBuffer[TripRecordCaseClass]
    val startTimeSource=TripRecord.getString(3)
    val endTimeSource=TripRecord.getString(4)
    val startTime=DateTime.parse(startTimeSource,DateTimeFormat.forPattern(dateFormat))
    val endTime=DateTime.parse(endTimeSource,DateTimeFormat.forPattern(dateFormat))
    var newStartTime=startTime
    var startMinute=0
    var endMinute=15
    val minuteArray=new Array[Int](15)
    var splitNeeded=true
    while(splitNeeded)
    {
      var totalMinute=0
      startMinute=newStartTime.getMinuteOfDay%15
      var newEndTime=newStartTime.plusMinutes(15-startMinute)
      newStartTime.getMinuteOfDay%15 match
      {
        case 0 => newStartTime=newStartTime
        case _ => newStartTime=newStartTime.minusMinutes(startMinute)
      }
      endMinute=newEndTime.getMinuteOfDay%15
      /*if(newEndTime.compareTo(endTime) > 0)
       {
         splitNeeded=false
         newEndTime=endTime
         endMinute=endTime.getMinuteOfDay%15
       }*/
      newEndTime.compareTo(endTime) match
      {
        case value if value>0 => splitNeeded=false;newEndTime=endTime;endMinute=endTime.getMinuteOfDay%15
        case _ =>   splitNeeded=true
      }
      if (splitNeeded || (endMinute ==0))
        endMinute=15
      for (i <- 0 to 14 )
        i match {
          case value if value >= startMinute && value < endMinute => minuteArray(i)=1;totalMinute+=1
          case _ =>minuteArray(i)=0
        }
      outputArrayBuffer+=TripRecordCaseClass(TripRecord.getString(0),TripRecord.getString(1),TripRecord.getString(2),new Timestamp(newStartTime.getMillis),minuteArray(0).toInt,minuteArray(1).toInt,minuteArray(2).toInt,minuteArray(3).toInt,minuteArray(4).toInt,minuteArray(5).toInt,minuteArray(6).toInt,minuteArray(7).toInt,minuteArray(8).toInt,minuteArray(9).toInt,minuteArray(10).toInt,minuteArray(11).toInt,minuteArray(12).toInt,minuteArray(13).toInt,minuteArray(14).toInt,totalMinute.toInt)
      newStartTime=newEndTime
    }
    outputArrayBuffer
  }

/*  val oneMinutesInMilliSeonds=60000
val dateFormat = "YYYY-MM-DD HH:mm:ss"
val formatter = new SimpleDateFormat(dateFormat)

def Splitting(TripRecord:Row) = {  // format in  default one. Joda for any required format
    var OutputList = new ListBuffer[List[String]]
    val startTimeSource =  TripRecord.getString(3)
    val endTimeSource = TripRecord.getString(4)
    println("source start"+startTimeSource)
    println("End start"+endTimeSource)
    val startTime = formatter.parse(startTimeSource)
    val endTime = formatter.parse(endTimeSource)
    println("startTime" + startTime)
    println("EndTime" +endTimeSource)
    var newStartTime = startTime
    var startMinute = 0
    var endMinute = 15
    var minuteArray = new Array[Int](15)
    var splitNeeded = true
    while (splitNeeded) {
      startMinute = milliToMins(newStartTime.getTime) % 15
      val tempMinsToAddToGetEndTime = 15 - milliToMins(newStartTime.getTime) % 15
      var newEndTime = formatter.parse(plusMinutesFun(dateFormat, newStartTime.toString, tempMinsToAddToGetEndTime))
      if (newEndTime.getTime == endTime.getTime)
        splitNeeded = false
      endMinute = milliToMins(newEndTime.getTime) % 15
      if (splitNeeded || (endMinute == 0))
        endMinute = 15
      for (i <- 0 to 14)
        i match {
          case value if value >= startMinute && value < endMinute => minuteArray(i) = 1
          case _ => 0
        }
      OutputList += List(TripRecord.getString(0), TripRecord.getString(1), TripRecord.getString(2), newStartTime.toString(), minuteArray(0).toString, minuteArray(1).toString, minuteArray(2).toString, minuteArray(3).toString, minuteArray(4).toString, minuteArray(5).toString, minuteArray(6).toString, minuteArray(7).toString, minuteArray(8).toString, minuteArray(9).toString, minuteArray(10).toString, minuteArray(11).toString, minuteArray(12).toString, minuteArray(13).toString, minuteArray(14).toString)
      newStartTime = newEndTime
      newEndTime match {
        case endTime => splitNeeded = false
        case _ => splitNeeded = true
      }
    }
    OutputList
 }

    val plusMinutesFun =minutesFun(_:String,_:String,_:Int,"Add")
    val minusMinutesFun =minutesFun(_:String,_:String,_:Int,"Sub")

  def minutesFun(dateFormat:String,dateToModify: String,minsToAppend:Int,functionality:String)=
  {
   val time=formatter.parse(dateToModify).getTime
   var modifiedTime:Long=time
   functionality match {
     case "Add" => modifiedTime=time+(minsToAppend*oneMinutesInMilliSeonds)
     case "Sub" => modifiedTime=time-(minsToAppend*oneMinutesInMilliSeonds)
     case _ =>  println("Wrong Parmeter")
   }
   val finalTime=formatter.format(modifiedTime)
   finalTime.toString
  }

  def milliToMins(timeInMilli:Long)={
    (timeInMilli/oneMinutesInMilliSeonds)toInt
  }

*/
}
