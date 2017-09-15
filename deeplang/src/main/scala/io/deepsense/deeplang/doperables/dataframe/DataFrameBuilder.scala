/**
 * Copyright 2015, deepsense.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.deepsense.deeplang.doperables.dataframe

import org.apache.spark.rdd.RDD
import org.apache.spark.sql
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructType

import io.deepsense.sparkutils.SparkSQLSession

/**
 * DeepSense DataFrame builder.
 * @param sparkSQLSession Spark sql context.
 */
case class DataFrameBuilder(sparkSQLSession: SparkSQLSession) {

  def buildDataFrame(schema: StructType, data: RDD[Row]): DataFrame = {
    val dataFrame: sql.DataFrame = sparkSQLSession.createDataFrame(data, schema)
    DataFrame.fromSparkDataFrame(dataFrame)
  }
}

object DataFrameBuilder
