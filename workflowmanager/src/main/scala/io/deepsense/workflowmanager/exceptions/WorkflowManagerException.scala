/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.exceptions

import io.deepsense.commons.exception.DeepSenseException
import io.deepsense.commons.exception.FailureCode.FailureCode

/**
 * Base exception for all exceptions Workflow Manager
 */
abstract class WorkflowManagerException(
    code: FailureCode,
    title: String,
    message: String,
    cause: Option[Throwable] = None,
    details: Map[String, String] = Map())
  extends DeepSenseException(code, title, message, cause, details)