package org.constants

object projectConstants {
  val delimiterArgConstant="delimiter"
  val inferSchemaArgConstant="inferSchema"
  val outputModeArg="outputMode"
  val csvFormat="com.databricks.spark.csv"
  val kafkaFormat="kafka"
  val kafkaBootStrapServersArg="kafka.bootstrap.servers"
  val kafkaValueDeserializerArg="value.deserializer"
  val kafkaKeyDeserializerArg="key.deserializer"
  val deltaReplaceWhereClause="replaceWhere"
  val deltaMergeSchemaClause="mergeSchema"
  val deltaOverWriteSchemaClause="overwriteSchema"
  val checkPointLocationArg="checkpointLocation"
  val pathArg="path"
  val startingOffsetsArg="startingOffsets"
  val subscribeArg="subscribe"
  val deltaFormat="delta"
  val rddPartitionArg="rddPartitionArg"
  val fileTypeDeltaValue="delta"
  val fileFormatArg="format"
  val columnNameArg="columnNames"
  val columnNameSepArg="columnNameSeparator"
  val fileTypeArgConstant="fileType"
  val fileTypeParquetValue="parquet"
  val fileTypeAvroValue="avro"
  val fileTypeOrcValue="orc"
  val fileTypeJsonValue="json"
  val pathSep="/"
  val fileTypeXmlValue="xml"
  val fileRootTagXmlArg ="rootTag"
  val fileRowTagXmlArg ="rowTag"
  val hbaseFormat="org.apache.spark.sql.execution.datasources.hbase"
  val fileTypeXmlFormatValue="com.databricks.spark.xml"
  val fileTypeAvroFormatValue="com.databricks.spark.avro"
  val fileTypeCsvValue="csvHeader"
  val fileTypeCsvHeaderColumnPassedValue="csvNoHeaderColumnPassed"
  val filePathArgValue="inputPath"
  val headerArgConstant="header"
  val emptyValueArg="emptyValue"
  val fileOverwriteValue="overwrite"
  val fileAppendValue="append"
  val fileOverwriteAppendArg="fileOverwriteAppendArg"
  val delimiterOr="|"
  val delimiterNot="!"
  val delimiterComma=","
  val delimiterTilde="~"
  val booleanTrue=true
  val booleanFalse=false
  val stringTrue="true"
  val stringFalse="false"
  val basePathArgConstant="basePath"
  val basePathValueConstant="basePath"
  def trueFalseFinder(value:String)={
    value  match {case value if value==stringTrue => projectConstants.booleanTrue ; case value  if value== stringFalse  => projectConstants.booleanFalse ; case _ => projectConstants.booleanFalse }
  }
}

