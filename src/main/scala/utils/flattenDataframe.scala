package utils

import org.apache.spark.sql.Column
import org.apache.spark.sql.functions.{col, lit}
import org.apache.spark.sql.types.StructType

object flattenDataframe {
  // first function is not utilized
  def getNewColumns(column: Set[String], merged_cols: Set[String]): Seq[Column] = {
    merged_cols.toList.map(x => x match {
      case x if column.contains(x) => col(x)
      case _ => lit(null).as(x)
    })
  }

  def flattenSchema(schema: StructType, prefix: String = null): Array[Column] = {
    schema.fields.flatMap(f => {
      val colName = if (prefix == null) f.name else (prefix + "." + f.name)

      f.dataType match {
        case st: StructType => flattenSchema(st, colName)
        case _ => Array(col(colName))
      }
    })
  }

  def flattenStructSchema(schema: StructType, prefix: String = null): Array[Column] = {
    schema.fields.flatMap(f => {
      val columnName = if (prefix == null) f.name else (prefix + "." + f.name)

      f.dataType match {
        case st: StructType => flattenStructSchema(st, columnName)
        case _ => Array(col(columnName).as(columnName.replace(".", "_")))
      }
    })
  }
}
