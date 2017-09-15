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

package io.deepsense.graph

import scala.reflect.runtime.{universe => ru}

import org.mockito.Mockito._
import org.scalatest.{Matchers, WordSpec}

import io.deepsense.deeplang._
import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.deeplang.exceptions.DeepLangException
import io.deepsense.deeplang.inference.{InferContext, InferenceWarning, InferenceWarnings}
import io.deepsense.deeplang.params.exceptions.ValidationException


class AbstractInferenceSpec
  extends WordSpec
  with DeeplangTestSupport
  with Matchers {

  import io.deepsense.graph.DClassesForDOperations._
  import io.deepsense.graph.DOperationTestClasses._

  val hierarchy = new DOperableCatalog
  hierarchy.registerDOperable[A1]()
  hierarchy.registerDOperable[A2]()

  val knowledgeA1: DKnowledge[DOperable] = DKnowledge(A1())
  val knowledgeA2: DKnowledge[DOperable] = DKnowledge(A2())
  val knowledgeA12: DKnowledge[DOperable] = DKnowledge(A1(), A2())

  val typeInferenceCtx: InferContext = createInferContext(hierarchy, fullInference = false)
  val fullInferenceCtx: InferContext = typeInferenceCtx.copy(fullInference = true)

  /**
   * This operation can be set to:
   *  - have invalid parameters
   *  - throw inference errors
   * By default it infers A1 on its output port.
   */
  case class DOperationA1A2ToFirst()
      extends DOperation2To1[A1, A2, A]
      with DOperationBaseFields {
    import DOperationA1A2ToFirst._

    override protected def _execute(context: ExecutionContext)(t1: A1, t2: A2): A = ???

    override def validateParams: Vector[DeepLangException] = {
      if (paramsValid) Vector.empty else Vector(parameterInvalidError)
    }

    private var paramsValid: Boolean = _
    def setParamsValid(): Unit = paramsValid = true
    def setParamsInvalid(): Unit = paramsValid = false

    private var inferenceShouldThrow = false

    def setInferenceErrorThrowing(): Unit = inferenceShouldThrow = true

    override protected def _inferTypeKnowledge(
      context: InferContext)(
      k0: DKnowledge[A1],
      k1: DKnowledge[A2]): (DKnowledge[A], InferenceWarnings) = {
      if (inferenceShouldThrow) {
        throw inferenceError
      }
      (k0, InferenceWarnings(warning))
    }

    override lazy val tTagTI_0: ru.TypeTag[A1] = ru.typeTag[A1]
    override lazy val tTagTO_0: ru.TypeTag[A] = ru.typeTag[A]
    override lazy val tTagTI_1: ru.TypeTag[A2] = ru.typeTag[A2]
  }

  object DOperationA1A2ToFirst {
    val parameterInvalidError = mock[ValidationException]
    val inferenceError = mock[DeepLangException]
    val warning = mock[InferenceWarning]
  }

  val idCreateA1 = Node.Id.randomId
  val idA1ToA = Node.Id.randomId
  val idAToA1A2 = Node.Id.randomId
  val idA1A2ToFirst = Node.Id.randomId

  protected def nodeCreateA1 = Node(idCreateA1, DOperationCreateA1())
  protected def nodeA1ToA = Node(idA1ToA, DOperationA1ToA())
  protected def nodeAToA1A2 = Node(idAToA1A2, DOperationAToA1A2())
  protected def nodeA1A2ToFirst = Node(idA1A2ToFirst, DOperationA1A2ToFirst())

  def validGraph: DirectedGraph = DirectedGraph(
    nodes = Set(nodeCreateA1, nodeAToA1A2, nodeA1A2ToFirst),
    edges = Set(
      Edge(nodeCreateA1, 0, nodeAToA1A2, 0),
      Edge(nodeAToA1A2, 0, nodeA1A2ToFirst, 0),
      Edge(nodeAToA1A2, 1, nodeA1A2ToFirst, 1))
  )

  def setParametersValid(node: Node): Unit = {
    node.operation.asInstanceOf[DOperationA1A2ToFirst].setParamsValid()
  }

  def setInferenceErrorThrowing(node: Node): Unit = {
    node.operation.asInstanceOf[DOperationA1A2ToFirst].setInferenceErrorThrowing()
  }

  def setParametersInvalid(node: Node): Unit = {
    node.operation.asInstanceOf[DOperationA1A2ToFirst].setParamsInvalid()
  }

  def setParametersValid(graph: DirectedGraph): Unit = setInGraph(graph, _.setParamsValid())

  def setInGraph(graph: DirectedGraph, f: DOperationA1A2ToFirst => Unit): Unit = {
    val node = graph.node(idA1A2ToFirst)
    f(node.operation.asInstanceOf[DOperationA1A2ToFirst])
  }
}
