package com.tencent.kuikly.core.coroutines

import kotlin.coroutines.Continuation
import kotlin.coroutines.startCoroutine

/**
 * Defines start options for coroutines builders.
 * It is used in `start` parameter of [launch][CoroutineScope.launch], [async][CoroutineScope.async], and other coroutine builder functions.
 *
 * The summary of coroutine start options is:
 * * [DEFAULT] -- immediately schedules coroutine for execution according to its context;
 * * [LAZY] -- starts coroutine lazily, only when it is needed;
 * * [ATOMIC] -- atomically (in a non-cancellable way) schedules coroutine for execution according to its context;
 * * [UNDISPATCHED] -- immediately executes coroutine until its first suspension point _in the current thread_.
 */
public enum class CoroutineStart {
    /**
     * Default -- immediately schedules the coroutine for execution according to its context.
     *
     * If the [CoroutineDispatcher] of the coroutine context returns `true` from [CoroutineDispatcher.isDispatchNeeded]
     * function as most dispatchers do, then the coroutine code is dispatched for execution later, while the code that
     * invoked the coroutine builder continues execution.
     *
     * Note that [Dispatchers.Unconfined] always returns `false` from its [CoroutineDispatcher.isDispatchNeeded]
     * function, so starting a coroutine with [Dispatchers.Unconfined] by [DEFAULT] is the same as using [UNDISPATCHED].
     *
     * If coroutine [Job] is cancelled before it even had a chance to start executing, then it will not start its
     * execution at all, but will complete with an exception.
     *
     * Cancellability of a coroutine at suspension points depends on the particular implementation details of
     * suspending functions. Use [suspendCancellableCoroutine] to implement cancellable suspending functions.
     */
    ATOMIC;

    public operator fun <R, T> invoke(block: suspend R.() -> T, receiver: R, completion: Continuation<T>): Unit =
        when (this) {
            ATOMIC -> block.startCoroutine(receiver, completion)
        }
}