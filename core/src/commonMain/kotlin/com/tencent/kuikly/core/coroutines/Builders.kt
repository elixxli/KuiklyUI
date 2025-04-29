package com.tencent.kuikly.core.coroutines

import com.tencent.kuikly.core.log.KLog
import com.tencent.kuikly.core.manager.BridgeManager
import com.tencent.kuikly.core.timer.setTimeout
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn

// --------------- launch ---------------

/**
 * Launches a new coroutine without blocking the current thread and returns a reference to the coroutine as a [Job].
 * The coroutine is cancelled when the resulting job is [cancelled][Job.cancel].
 *
 * The coroutine context is inherited from a [CoroutineScope]. Additional context elements can be specified with [context] argument.
 * If the context does not have any dispatcher nor any other [ContinuationInterceptor], then [Dispatchers.Default] is used.
 * The parent job is inherited from a [CoroutineScope] as well, but it can also be overridden
 * with a corresponding [context] element.
 *
 * By default, the coroutine is immediately scheduled for execution.
 * Other start options can be specified via `start` parameter. See [CoroutineStart] for details.
 * An optional [start] parameter can be set to [CoroutineStart.LAZY] to start coroutine _lazily_. In this case,
 * the coroutine [Job] is created in _new_ state. It can be explicitly started with [start][Job.start] function
 * and will be started implicitly on the first invocation of [join][Job.join].
 *
 * Uncaught exceptions in this coroutine cancel the parent job in the context by default
 * (unless [CoroutineExceptionHandler] is explicitly specified), which means that when `launch` is used with
 * the context of another coroutine, then any uncaught exception leads to the cancellation of the parent coroutine.
 *
 * See [newCoroutineContext] for a description of debugging facilities that are available for a newly created coroutine.
 *
 * @param context additional to [CoroutineScope.coroutineContext] context of the coroutine.
 * @param start coroutine start option. The default value is [CoroutineStart.DEFAULT].
 * @param block the coroutine code which will be invoked in the context of the provided scope.
 **/
public fun CoroutineScope.launch(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.ATOMIC,
    block: suspend CoroutineScope.() -> Unit
): Job {
    val job = StandaloneCoroutine(context, true)
    job.start(start, this) {
        try {
            block.invoke(this)
        } catch (e: Throwable) {
            throwCoroutineScopeException(e)
        }
    }
    return job
}

public fun <T> CoroutineScope.async(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.ATOMIC,
    block: suspend CoroutineScope.() -> T
): Deferred<T> {
    val job = DeferredCoroutine<T>(context, true)
    job.start(start, this) {
        try {
            block.invoke(this)
        } catch (e: Throwable) {
            throwCoroutineScopeException(e)
            val res: T? = null
            res!!// 解决编译返回，不影响报错结果
        }
    }
    return job
}

public suspend fun CoroutineScope.delay(timeMs: Int): Unit {
    if (BridgeManager.currentPageId == null) {
        return
    }
    suspendCoroutine<Unit> {
        setTimeout(BridgeManager.currentPageId!!, timeMs) {
            try {
                it.resume(Unit)
            } catch (e: Throwable) {
                throwCoroutineScopeException(e)
            }
        }
    }
}

/**
 * 协程内的异常统一使用该接口抛出，否则在协程内异常抛出无效。
 */
public fun throwCoroutineScopeException(e: Throwable) {
    setTimeout { // 解决协程作用域内异常throw无效，故下一帧非协程内抛出
        throw  e
    }
}

// --------------- withContext ---------------

/**
 * Calls the specified suspending block with a given coroutine context, suspends until it completes, and returns
 * the result.
 *
 * The resulting context for the [block] is derived by merging the current [coroutineContext] with the
 * specified [context] using `coroutineContext + context` (see [CoroutineContext.plus]).
 * This suspending function is cancellable. It immediately checks for cancellation of
 * the resulting context and throws [CancellationException] if it is not [active][CoroutineContext.isActive].
 *
 * Calls to [withContext] whose [context] argument provides a [CoroutineDispatcher] that is
 * different from the current one, by necessity, perform additional dispatches: the [block]
 * can not be executed immediately and needs to be dispatched for execution on
 * the passed [CoroutineDispatcher], and then when the [block] completes, the execution
 * has to shift back to the original dispatcher.
 *
 * Note that the result of `withContext` invocation is dispatched into the original context in a cancellable way
 * with a **prompt cancellation guarantee**, which means that if the original [coroutineContext]
 * in which `withContext` was invoked is cancelled by the time its dispatcher starts to execute the code,
 * it discards the result of `withContext` and throws [CancellationException].
 *
 * The cancellation behaviour described above is enabled if and only if the dispatcher is being changed.
 * For example, when using `withContext(NonCancellable) { ... }` there is no change in dispatcher and
 * this call will not be cancelled neither on entry to the block inside `withContext` nor on exit from it.
 */
public suspend fun <T> withContext(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> T
): T {
    return GlobalScope.async {
        block()
    }.await()
}



public object GlobalScope : CoroutineScope {
    /**
     * Returns [EmptyCoroutineContext].
     */
    override val coroutineContext: CoroutineContext
        get() = EmptyCoroutineContext
}


public class LifecycleScope : CoroutineScope {
    /**
     * Returns [EmptyCoroutineContext].
     */
    override val coroutineContext: CoroutineContext
        get() = EmptyCoroutineContext
}





