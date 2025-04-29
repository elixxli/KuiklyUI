package com.tencent.kuikly.core.coroutines

import com.tencent.kuikly.core.collection.fastArrayListOf
import kotlin.coroutines.*

open class DeferredCoroutine<T>(
    parentContext: CoroutineContext,
    active: Boolean
) : AbstractCoroutine<T>(parentContext, true, active = active), Deferred<T> {
    private var suspendCoroutineResumeTasks = fastArrayListOf< (T)->Unit >()
    override suspend fun await(): T = awaitInternal()
    private var didSetResultValue = false
    private var resumeResultValue: T? = null
         set(value) {
             field = value
             didSetResultValue = true
         }
    override fun resumeWith(result: Result<T>) {
        if (result.isSuccess) {
            resumeResultValue = result.getOrNull()
            suspendCoroutineResumeTasks.forEach { callback ->
                callback.invoke(resumeResultValue as T)
            }
            suspendCoroutineResumeTasks.clear()
        } else {
            throw RuntimeException("result failure:" + result.exceptionOrNull())
        }
    }

    private suspend fun awaitInternal(): T {
        if (didSetResultValue) {
            return resumeResultValue as T
        }
        return awaitSuspend() // slow-path
    }

    private suspend fun awaitSuspend(): T = suspendCoroutine {
          this.suspendCoroutineResumeTasks.add { value ->
              it.resume(value)
          }
    }


}