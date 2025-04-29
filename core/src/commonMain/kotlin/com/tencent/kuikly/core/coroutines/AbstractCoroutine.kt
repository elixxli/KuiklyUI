
package com.tencent.kuikly.core.coroutines

import kotlin.coroutines.*

/**
 * Abstract base class for implementation of coroutines in coroutine builders.
 *
 * This class implements completion [Continuation], [Job], and [CoroutineScope] interfaces.
 * It stores the result of continuation in the state of the job.
 * This coroutine waits for children coroutines to finish before completing and
 * fails through an intermediate _failing_ state.
 *
 * The following methods are available for override:
 *
 * * [onStart] is invoked when the coroutine was created in non-active state and is being [started][Job.start].
 * * [onCancelling] is invoked as soon as the coroutine starts being cancelled for any reason (or completes).
 * * [onCompleted] is invoked when the coroutine completes with a value.
 * * [onCancelled] in invoked when the coroutine completes with an exception (cancelled).
 *
 * @param parentContext the context of the parent coroutine.
 * @param initParentJob specifies whether the parent-child relationship should be instantiated directly
 *               in `AbstractCoroutine` constructor. If set to `false`, it's the responsibility of the child class
 *               to invoke [initParentJob] manually.
 * @param active when `true` (by default), the coroutine is created in the _active_ state, otherwise it is created in the _new_ state.
 *               See [Job] for details.
 *
 * @suppress **This an internal API and should not be used from general code.**
 */

public abstract class AbstractCoroutine<in T>(
    parentContext: CoroutineContext,
    initParentJob: Boolean,
    active: Boolean
) : Job, Continuation<T>, CoroutineScope {
    init {

    }
    public final override val context: CoroutineContext = parentContext + this
    final override val key: CoroutineContext.Key<*> get() = Job
    /**
     * The context of this scope which is the same as the [context] of this coroutine.
     */
    public override val coroutineContext: CoroutineContext get() = context


    /**
     * Starts this coroutine with the given code [block] and [start] strategy.
     * This function shall be invoked at most once on this coroutine.
     *
     * * [DEFAULT] uses [startCoroutineCancellable].
     * * [ATOMIC] uses [startCoroutine].
     * * [UNDISPATCHED] uses [startCoroutineUndispatched].
     * * [LAZY] does nothing.
     */
     open fun <R> start(start: CoroutineStart, receiver: R, block: suspend R.() -> T) {
        start(block, receiver, this)
    }
}
