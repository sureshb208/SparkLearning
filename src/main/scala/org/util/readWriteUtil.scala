package org.util
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.SparkSession
import org.constants.projectConstants
import io.delta.tables._
import org.apache.spark.sql.execution.datasources.hbase.HBaseTableCatalog
object readWriteUtil {
  def readDF(spark:SparkSession,inputMap:collection.mutable.Map[String,String]):DataFrame ={
     var dfTemp=spark.createDataFrame(Seq(("invalid Scn"," Error"))).toDF("Scn","Desc")
    inputMap(projectConstants.fileTypeArgConstant)  match
      {
      case value if value==projectConstants.fileTypeParquetValue =>dfTemp= spark.read.option(projectConstants.basePathArgConstant,inputMap(projectConstants.basePathValueConstant)).load(inputMap(projectConstants.filePathArgValue))
      case value if value== projectConstants.fileTypeCsvValue =>dfTemp= spark.read.format(inputMap(projectConstants.fileFormatArg)).option(projectConstants.delimiterArgConstant,inputMap(projectConstants.delimiterArgConstant)).option(projectConstants.headerArgConstant,inputMap(projectConstants.headerArgConstant)).option(projectConstants.inferSchemaArgConstant,inputMap(projectConstants.inferSchemaArgConstant)).option(projectConstants.basePathArgConstant,inputMap(projectConstants.basePathValueConstant)).load(inputMap(projectConstants.filePathArgValue))
      case value if value==projectConstants.fileTypeCsvHeaderColumnPassedValue =>
        {
        inputMap.put(projectConstants.fileTypeArgConstant,projectConstants.fileTypeCsvValue)
        inputMap.put(projectConstants.headerArgConstant,projectConstants.stringFalse)
        val  df = readDF(spark,inputMap)
      //  val columnNames=inputMap(projectConstants.columnNameArg)
        val columnNameSeq=inputMap(projectConstants.columnNameArg).split(inputMap(projectConstants.columnNameSepArg)).toSeq
        dfTemp=df.toDF(columnNameSeq:_*)
        }
      case value if value== projectConstants.fileTypeJsonValue => dfTemp= spark.read.json(inputMap(projectConstants.filePathArgValue))
      case value if value== projectConstants.fileTypeAvroValue => dfTemp= spark.read.format(projectConstants.fileTypeAvroFormatValue).load(inputMap(projectConstants.filePathArgValue))
      case value if value== projectConstants.fileTypeOrcValue => dfTemp= spark.read.format(value).option(projectConstants.inferSchemaArgConstant,inputMap(projectConstants.inferSchemaArgConstant)).load(inputMap(projectConstants.filePathArgValue))
      case value if value== projectConstants.fileTypeXmlValue => dfTemp= spark.read.format(projectConstants.fileTypeXmlFormatValue).option(projectConstants.fileRootTagXmlArg,inputMap(projectConstants.fileRootTagXmlArg)).option(projectConstants.fileRowTagXmlArg,inputMap(projectConstants.fileRowTagXmlArg)).load(inputMap(projectConstants.filePathArgValue))
      case value if value== projectConstants.fileTypeDeltaValue => dfTemp= spark.read.format(projectConstants.fileTypeDeltaValue).load(inputMap(projectConstants.filePathArgValue))
      case _ => println("Invalid selection")
      }
    dfTemp
  }

  //Hbase table read
  def withCatalog(spark:SparkSession,catalog:String): DataFrame ={
    spark.read.options(Map(HBaseTableCatalog.tableCatalog->catalog)).format(projectConstants.hbaseFormat).load()
  }

  def deltaTableRead(spark:SparkSession,inputMap:collection.mutable.Map[String,String]) ={
    DeltaTable.forPath(spark,inputMap(projectConstants.filePathArgValue))
  }

  def deltaTableWriteFromDeltaTable(spark:SparkSession,inputMap:collection.mutable.Map[String,String],detlaTable:DeltaTable) ={
    inputMap.put(projectConstants.fileTypeArgConstant,projectConstants.fileTypeDeltaValue)
    writeDF(inputMap,detlaTable.toDF) // deltatable without any partition
  }

  def execSparkSql(spark:SparkSession,sql:String):DataFrame={
    spark.sql(sql)
  }

  def writeDF(inputMap:collection.mutable.Map[String,String],df:DataFrame)={
    inputMap(projectConstants.fileTypeArgConstant)  match
      {
      case value if value==projectConstants.fileTypeParquetValue =>df.write.mode(inputMap(projectConstants.fileOverwriteAppendArg)).option(projectConstants.emptyValueArg,inputMap(projectConstants.emptyValueArg)).parquet(inputMap(projectConstants.filePathArgValue))
      case value if value== projectConstants.fileTypeCsvValue =>df.write.mode(inputMap(projectConstants.fileOverwriteAppendArg)).option(projectConstants.delimiterArgConstant,inputMap(projectConstants.delimiterArgConstant)).option(projectConstants.headerArgConstant,inputMap(projectConstants.headerArgConstant)).csv(inputMap(projectConstants.filePathArgValue))
      case value if value== projectConstants.fileTypeAvroValue =>df.write.mode(inputMap(projectConstants.fileOverwriteAppendArg)).format(projectConstants.fileTypeAvroValue).save(inputMap(projectConstants.filePathArgValue))
      case value if value== projectConstants.fileTypeJsonValue =>df.write.mode(inputMap(projectConstants.fileOverwriteAppendArg)).json(inputMap(projectConstants.filePathArgValue))
      case value if value== projectConstants.fileTypeXmlValue =>df.write.mode(inputMap(projectConstants.fileOverwriteAppendArg)).format(projectConstants.fileTypeXmlFormatValue).option(projectConstants.fileRowTagXmlArg,inputMap(projectConstants.fileRowTagXmlArg)).option(projectConstants.fileRootTagXmlArg,inputMap(projectConstants.fileRootTagXmlArg)).save(inputMap(projectConstants.filePathArgValue))
      case value if value== projectConstants.fileTypeOrcValue =>df.write.format( projectConstants.fileTypeOrcValue ).mode(inputMap(projectConstants.fileOverwriteAppendArg)).save(inputMap(projectConstants.filePathArgValue))
      case value if value== projectConstants.fileTypeDeltaValue =>df.write.format( projectConstants.fileTypeDeltaValue ).mode(inputMap(projectConstants.fileOverwriteAppendArg)).save(inputMap(projectConstants.filePathArgValue))

      //default TextFile
      case _=>df.write.mode(inputMap(projectConstants.fileOverwriteAppendArg)).option(projectConstants.delimiterArgConstant,inputMap(projectConstants.delimiterArgConstant)).option(projectConstants.headerArgConstant,inputMap(projectConstants.headerArgConstant)).text(inputMap(projectConstants.filePathArgValue))
    }
  }

  def readRdd(spark:SparkSession,inputMap:collection.mutable.Map[String,String])=
    {
      spark.sparkContext.textFile(inputMap(projectConstants.filePathArgValue),inputMap(projectConstants.rddPartitionArg).toInt)

    }

  def readStreamFunction(spark:SparkSession,inputMap:collection.mutable.Map[String,String])= {
    inputMap(projectConstants.fileFormatArg) match {
      case value if value== projectConstants.kafkaFormat =>spark.readStream.format (inputMap (projectConstants.fileFormatArg) ).option(projectConstants.checkPointLocationArg,inputMap(projectConstants.checkPointLocationArg)).option (projectConstants.kafkaBootStrapServersArg, inputMap (projectConstants.kafkaBootStrapServersArg) ).option (projectConstants.kafkaValueDeserializerArg, inputMap (projectConstants.kafkaValueDeserializerArg) ).option (projectConstants.kafkaKeyDeserializerArg, inputMap (projectConstants.kafkaKeyDeserializerArg) ).option (projectConstants.startingOffsetsArg, inputMap (projectConstants.startingOffsetsArg) ).option (projectConstants.subscribeArg, inputMap (projectConstants.subscribeArg) ).load ()
      case value if value== projectConstants.deltaFormat =>spark.readStream.format (inputMap (projectConstants.fileFormatArg) ).load(inputMap(projectConstants.filePathArgValue))
      case _ => println ("Default value for stream read function ");spark.readStream.format (inputMap (projectConstants.fileFormatArg) ).load(inputMap(projectConstants.filePathArgValue))
    }
  }

  def writeStreamFunction(spark:SparkSession,inputMap:collection.mutable.Map[String,String],DataframeToStream: DataFrame)=DataframeToStream.writeStream.outputMode(inputMap(projectConstants.outputModeArg)).format(inputMap(projectConstants.fileFormatArg)).option(projectConstants.checkPointLocationArg,inputMap(projectConstants.checkPointLocationArg)).option(projectConstants.pathArg, inputMap(projectConstants.pathArg))
  //readStreamDF.writeStream.outputMode("append").format(projectConstants.deltaFormat).option("checkpointLocation",checkPointLocation).option("path",outputPath)

  def writeStreamAsDelta(spark:SparkSession,inputMap:collection.mutable.Map[String,String],DataframeToStream: DataFrame)=DataframeToStream.writeStream.outputMode(inputMap(projectConstants.outputModeArg)).format(inputMap(projectConstants.fileFormatArg)).option(projectConstants.checkPointLocationArg,inputMap(projectConstants.checkPointLocationArg)).option(projectConstants.deltaMergeSchemaClause, inputMap(projectConstants.deltaMergeSchemaClause)).option(projectConstants.deltaOverWriteSchemaClause, inputMap(projectConstants.deltaOverWriteSchemaClause)).option(projectConstants.pathArg, inputMap(projectConstants.pathArg))

}

/*df.coalesce(1).write.mode("overwrite").format("csv")
    .option("delimiter", ",")
    .option("nullValue", "unknown")
    .option("treatEmptyValuesAsNulls", "false")
    .save(s"$path/test")*/