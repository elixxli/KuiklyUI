package com.tencent.kuikly.core.coroutines

import kotlin.coroutines.CoroutineContext
/**
 * Defines a scope for new coroutines. Every **coroutine builder** (like [launch], [async], etc.)
 * is an extension on [CoroutineScope] and inherits its [coroutineContext][CoroutineScope.coroutineContext]
 * to automatically propagate all its elements and cancellation.
 *
 * The best ways to obtain a standalone instance of the scope are [CoroutineScope()] and [MainScope()] factory functions,
 * taking care to cancel these coroutine scopes when they are no longer needed (see section on custom usage below for
 * explanation and example).
 *
 * Additional context elements can be appended to the scope using the [plus][CoroutineScope.plus] operator.
 *
 * ### Convention for structured concurrency
 *
 * Manual implementation of this interface is not recommended, implementation by delegation should be preferred instead.
 * By convention, the [context of a scope][CoroutineScope.coroutineContext] should contain an instance of a
 * [job][Job] to enforce the discipline of **structured concurrency** with propagation of cancellation.
 *
 * Every coroutine builder (like [launch], [async], and others)
 * and every scoping function (like [coroutineScope] and [withContext]) provides _its own_ scope
 * with its own [Job] instance into the inner block of code it runs.
 * By convention, they all wait for all the coroutines inside their block to complete before completing themselves,
 * thus enforcing the structured concurrency. See [Job] documentation for more details.
 *
 * ### Android usage
 *
 * Android has first-party support for coroutine scope in all entities with the lifecycle.
 * See [the corresponding documentation](https://developer.android.com/topic/libraries/architecture/coroutines#lifecyclescope).
 *
 * ### Custom usage
 *
 * `CoroutineScope` should be declared as a property on entities with a well-defined lifecycle that are
 * responsible for launching child coroutines. The corresponding instance of `CoroutineScope` shall be created
 * with either `CoroutineScope()` or `MainScope()`:
 *
 * * `CoroutineScope()` uses the [context][CoroutineContext] provided to it as a parameter for its coroutines
 *   and adds a [Job] if one is not provided as part of the context.
 * * `MainScope()` uses [Dispatchers.Main] for its coroutines and has a [SupervisorJob].
 *
 * **The key part of custom usage of `CoroutineScope` is cancelling it at the end of the lifecycle.**
 * The [CoroutineScope.cancel] extension function shall be used when the entity that was launching coroutines
 * is no longer needed. It cancels all the coroutines that might still be running on behalf of it.
 *
 * For example:
 *
 * ```
 * class MyUIClass {
 *     val scope = MainScope() // the scope of MyUIClass, uses Dispatchers.Main
 *
 *     fun destroy() { // destroys an instance of MyUIClass
 *         scope.cancel() // cancels all coroutines launched in this scope
 *         // ... do the rest of cleanup here ...
 *     }
 *
 *     /*
 *      * Note: if this instance is destroyed or any of the launched coroutines
 *      * in this method throws an exception, then all nested coroutines are cancelled.
 *      */
 *     fun showSomeData() = scope.launch { // launched in the main thread
 *        // ... here we can use suspending functions or coroutine builders with other dispatchers
 *        draw(data) // draw in the main thread
 *     }
 * }
 * ```
 */
public interface CoroutineScope {
    /**
     * The context of this scope.
     * Context is encapsulated by the scope and used for implementation of coroutine builders that are extensions on the scope.
     * Accessing this property in general code is not recommended for any purposes except accessing the [Job] instance for advanced usages.
     *
     * By convention, should contain an instance of a [job][Job] to enforce structured concurrency.
     */
    public val coroutineContext: CoroutineContext
}