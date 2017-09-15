/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.rest

import java.util.UUID

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, duration}

import com.google.common.base.Function
import com.google.inject.{Key, TypeLiteral}
import org.jclouds.ContextBuilder
import org.jclouds.compute.ComputeServiceContext
import org.jclouds.domain.Credentials
import org.jclouds.openstack.keystone.v2_0.domain.Access
import org.scalatest.BeforeAndAfter
import spray.routing.Route

import io.deepsense.experimentmanager.ExperimentManagerIntegTestSupport
import io.deepsense.experimentmanager.models.Experiment
import io.deepsense.experimentmanager.storage.ExperimentStorage
import io.deepsense.graph.Graph

class ExperimentsApiIntegSpec
  extends ExperimentsApiSpec
  with ExperimentManagerIntegTestSupport
  with BeforeAndAfter {

  var experimentA: Experiment = null
  var experimentB: Experiment = null
  var experimentStorage: ExperimentStorage = null

  lazy val validTokenA = validAuthToken("userA")
  lazy val validTokenB = validAuthToken("userB")
  lazy val apiPrefixFromConfig = getConfig.getString("experiments.api.prefix")
  lazy val tenantA = tenantId("userA")
  lazy val tenantB = tenantId("userB")

  override def experimentOfTenantA = experimentA
  override def experimentOfTenantB = experimentB

  override val tenantAId: String = tenantA

  override val tenantBId: String = tenantB

  /**
   * A valid Auth Token of a user of tenant A. This user has to have roles
   * for all actions in ExperimentManager
   */
  override val validAuthTokenTenantA: String = validTokenA

  /**
   * A valid Auth Token of a user of tenant B. This user has to have no roles.
   */
  override val validAuthTokenTenantB: String = validTokenB

  override protected def testRoute: Route = getRestServiceInstance

  override val apiPrefix: String = apiPrefixFromConfig

  before {
    experimentA = Experiment(
      UUID.randomUUID(),
      tenantAId,
      "Experiment of Tenant A",
      Graph())

    experimentB = Experiment(
      UUID.randomUUID(),
      tenantBId,
      "Experiment of Tenant B",
      Graph())

    experimentStorage = getInstance[ExperimentStorage]
    Await.ready(experimentStorage.save(experimentA), FiniteDuration(2, duration.SECONDS))
    Await.ready(experimentStorage.save(experimentB), FiniteDuration(2, duration.SECONDS))
  }

  after {
    experimentStorage.delete(experimentA.id)
    experimentStorage.delete(experimentB.id)
  }

  private def tenantId(user: String): String = accessForUser(user).getToken.getTenant.get.getId

  private def validAuthToken(user: String): String = accessForUser(user).getToken.getId

  private def accessForUser(user: String): Access = {
    val identity = getConfig.getString(s"test.restapi.$user.identity")
    val password = getConfig.getString(s"test.restapi.$user.password")
    val context = ContextBuilder.newBuilder("openstack-nova")
      .endpoint(getConfig.getString("auth-service.endpoint"))
      .credentials(identity, password)
      .buildView(classOf[ComputeServiceContext])

    val auth = context
      .utils()
      .injector()
      .getInstance(Key.get(new TypeLiteral[Function[Credentials, Access]](){}))

    auth(
      new Credentials.Builder[Credentials]()
        .identity(identity).credential(password).build())
  }
}
