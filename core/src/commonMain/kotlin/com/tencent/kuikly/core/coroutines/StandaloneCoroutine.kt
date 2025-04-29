package com.tencent.kuikly.core.coroutines

import kotlin.coroutines.CoroutineContext

open class StandaloneCoroutine(
    parentContext: CoroutineContext,
    active: Boolean
) : AbstractCoroutine<Unit>(parentContext, initParentJob = true, active = active) {

    override fun resumeWith(result: Result<Unit>) { }
}