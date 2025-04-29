/*
 * Copyright 2016-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package com.tencent.kuikly.core.coroutines

/**
 * This exception gets thrown if an exception is caught while processing [CompletionHandler] invocation for [Job].
 *
 * @suppress **This an internal API and should not be used from general code.**
 */

public class CompletionHandlerException(message: String, cause: Throwable) : RuntimeException(message, cause)

public  open class CancellationException(message: String?) : IllegalStateException() {

}

@Suppress("FunctionName", "NO_ACTUAL_FOR_EXPECT")
public expect fun CancellationException(message: String?, cause: Throwable?) : CancellationException

//internal  class JobCancellationException(
//    message: String,
//    cause: Throwable?,
//    job: Job
//) : CancellationException {
//    internal val job: Job
//}
//
//internal class CoroutinesInternalError(message: String, cause: Throwable) : Error(message, cause)
//
//internal fun Throwable.addSuppressedThrowable(other: Throwable)
//// For use in tests
//internal val RECOVER_STACK_TRACES: Boolean
