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

package io.deepsense.deeplang.doperables

import scala.reflect.runtime.universe._

import org.apache.spark.ml.UnaryTransformer
import org.apache.spark.sql.types.StructType

import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.inference.exceptions.SparkTransformSchemaException
import io.deepsense.deeplang.params.Param
import io.deepsense.deeplang.{ExecutionContext, TypeUtils}

/**
 * This class creates a Deeplang MultiColumnTransformer
 * from a Spark ML UnaryTransformer.
 * We assume that every Spark UnaryTransformer has a no-arg constructor.
 *
 * @tparam T Wrapped Spark UnaryTransformer type
 */
abstract class SparkUnaryTransformerAsMultiColumnTransformer[T <: UnaryTransformer[_, _, T]]
    (implicit tag: TypeTag[T])
  extends MultiColumnTransformer {

  lazy val sparkTransformer: T = TypeUtils.instanceOfType(tag)

  override protected def getSpecificParams: Array[Param[_]] = Array()

  override def transformSingleColumn(
      inputColumn: String,
      outputColumn: String,
      context: ExecutionContext,
      dataFrame: DataFrame): DataFrame = {
    val transformer = transformerCopy()
    transformer.setInputCol(inputColumn)
    transformer.setOutputCol(outputColumn)
    DataFrame.fromSparkDataFrame(transformer.transform(dataFrame.sparkDataFrame))
  }

  override def transformSingleColumnSchema(
      inputColumn: String,
      outputColumn: String,
      schema: StructType): Option[StructType] = {
    val transformer = transformerCopy()
    transformer.setInputCol(inputColumn)
    transformer.setOutputCol(outputColumn)
    try {
      val transformedSchema = transformer.transformSchema(schema)
      Some(transformedSchema)
    } catch {
      case e: Exception => throw new SparkTransformSchemaException(e)
    }
  }


  def transformerCopy(): T = {
    sparkTransformer.copy(sparkTransformer.extractParamMap())
  }
}
