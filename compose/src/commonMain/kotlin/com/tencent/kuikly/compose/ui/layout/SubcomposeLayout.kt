/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:OptIn(InternalComposeUiApi::class)

package com.tencent.kuikly.compose.ui.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.ComposeNodeLifecycleCallback
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReusableComposeNode
import androidx.compose.runtime.ReusableComposition
import androidx.compose.runtime.ReusableContentHost
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collection.mutableVectorOf
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.currentCompositeKeyHash
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import com.tencent.kuikly.compose.KuiklyApplier
import com.tencent.kuikly.compose.extension.shouldWrapShadowView
import com.tencent.kuikly.compose.foundation.gestures.Orientation
import com.tencent.kuikly.compose.foundation.gestures.ScrollableState
import com.tencent.kuikly.compose.foundation.pager.PagerState
import com.tencent.kuikly.compose.ui.ExperimentalComposeUiApi
import com.tencent.kuikly.compose.ui.InternalComposeUiApi
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.UiComposable
import com.tencent.kuikly.compose.ui.geometry.Size
import com.tencent.kuikly.compose.ui.internal.checkPrecondition
import com.tencent.kuikly.compose.ui.materialize
import com.tencent.kuikly.compose.ui.node.ComposeUiNode.Companion.SetCompositeKeyHash
import com.tencent.kuikly.compose.ui.node.ComposeUiNode.Companion.SetModifier
import com.tencent.kuikly.compose.ui.node.ComposeUiNode.Companion.SetResolvedCompositionLocals
import com.tencent.kuikly.compose.ui.node.KNode
import com.tencent.kuikly.compose.ui.node.LayoutNode
import com.tencent.kuikly.compose.ui.node.TraversableNode
import com.tencent.kuikly.compose.ui.node.checkMeasuredSize
import com.tencent.kuikly.compose.ui.node.requireOwner
import com.tencent.kuikly.compose.ui.node.traverseDescendants
import com.tencent.kuikly.compose.ui.platform.LocalConfiguration
import com.tencent.kuikly.compose.ui.platform.LocalDensity
import com.tencent.kuikly.compose.ui.platform.createSubcomposition
import com.tencent.kuikly.compose.ui.unit.Constraints
import com.tencent.kuikly.compose.ui.unit.LayoutDirection
import com.tencent.kuikly.compose.ui.util.fastForEach
import com.tencent.kuikly.compose.views.KuiklyInfoKey
import com.tencent.kuikly.compose.views.VirtualNodeView
import com.tencent.kuikly.compose.layout.checkOffScreenNode
import com.tencent.kuikly.compose.scroller.calculateContentSize
import com.tencent.kuikly.compose.scroller.isAtTop
import com.tencent.kuikly.compose.scroller.kuiklyInfo
import com.tencent.kuikly.compose.scroller.kuiklyOnScroll
import com.tencent.kuikly.compose.scroller.kuiklyOnScrollEnd
import com.tencent.kuikly.compose.scroller.kuiklyWillDragEnd
import com.tencent.kuikly.compose.scroller.tryExpandStartSize
import com.tencent.kuikly.compose.ui.node.ComposeUiNode.Companion.ShadowLayoutConstructor
import com.tencent.kuikly.compose.ui.scaleWithDensity
import com.tencent.kuikly.core.base.DeclarativeBaseView
import com.tencent.kuikly.core.base.event.layoutFrameDidChange
import com.tencent.kuikly.core.views.ScrollerAttr
import com.tencent.kuikly.core.views.ScrollerEvent
import com.tencent.kuikly.core.views.ScrollerView
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Analogue of [Layout] which allows to subcompose the actual content during the measuring stage
 * for example to use the values calculated during the measurement as params for the composition
 * of the children.
 *
 * Possible use cases:
 * * You need to know the constraints passed by the parent during the composition and can't solve
 * your use case with just custom [Layout] or [LayoutModifier].
 * See [androidx.compose.foundation.layout.BoxWithConstraints].
 * * You want to use the size of one child during the composition of the second child.
 * * You want to compose your items lazily based on the available size. For example you have a
 * list of 100 items and instead of composing all of them you only compose the ones which are
 * currently visible(say 5 of them) and compose next items when the component is scrolled.
 *
 *
 * @param modifier [Modifier] to apply for the layout.
 * @param measurePolicy Measure policy which provides ability to subcompose during the measuring.
 */
@Composable
fun SubcomposeLayout(
    modifier: Modifier = Modifier,
    measurePolicy: SubcomposeMeasureScope.(Constraints) -> MeasureResult,
    scrollableState: ScrollableState? = null,
    orientation: Orientation = Orientation.Vertical,
) {
    SubcomposeLayout(
        state = remember { SubcomposeLayoutState() },
        modifier = modifier,
        measurePolicy = measurePolicy,
        scrollableState = scrollableState,
        orientation = orientation,
    )
}

/**
 * Analogue of [Layout] which allows to subcompose the actual content during the measuring stage
 * for example to use the values calculated during the measurement as params for the composition
 * of the children.
 *
 * Possible use cases:
 * * You need to know the constraints passed by the parent during the composition and can't solve
 * your use case with just custom [Layout] or [LayoutModifier].
 * See [androidx.compose.foundation.layout.BoxWithConstraints].
 * * You want to use the size of one child during the composition of the second child.
 * * You want to compose your items lazily based on the available size. For example you have a
 * list of 100 items and instead of composing all of them you only compose the ones which are
 * currently visible(say 5 of them) and compose next items when the component is scrolled.
 *
 *
 * @param modifier [Modifier] to apply for the layout.
 * @param measurePolicy Measure policy which provides ability to subcompose during the measuring.
 */
@Composable
@UiComposable
fun SubcomposeLayoutNoScroll(
    modifier: Modifier = Modifier,
    state: SubcomposeLayoutState,
    measurePolicy: SubcomposeMeasureScope.(Constraints) -> MeasureResult
) {
    val compositeKeyHash = currentCompositeKeyHash
    val compositionContext = rememberCompositionContext()
    val materialized = currentComposer.materialize(modifier)
    val hasShadow = shouldWrapShadowView(materialized)
    val localMap = currentComposer.currentCompositionLocalMap

    ReusableComposeNode<KNode<*>, KuiklyApplier>(
        factory = ShadowLayoutConstructor.invoke(hasShadow),
        update = {
            set(state, state.setRoot)
            set(compositionContext, state.setCompositionContext)
            set(measurePolicy, state.setMeasurePolicy)
            set(localMap, SetResolvedCompositionLocals)
            set(materialized, SetModifier)
            @OptIn(ExperimentalComposeUiApi::class)
            set(compositeKeyHash, SetCompositeKeyHash)
        }
    )
    if (!currentComposer.skipping) {
        SideEffect {
            state.forceRecomposeChildren()
        }
    }
}

/**
 * Analogue of [Layout] which allows to subcompose the actual content during the measuring stage
 * for example to use the values calculated during the measurement as params for the composition
 * of the children.
 *
 * Possible use cases:
 * * You need to know the constraints passed by the parent during the composition and can't solve
 * your use case with just custom [Layout] or [LayoutModifier].
 * See [BoxWithConstraints].
 * * You want to use the size of one child during the composition of the second child.
 * * You want to compose your items lazily based on the available size. For example you have a
 * list of 100 items and instead of composing all of them you only compose the ones which are
 * currently visible(say 5 of them) and compose next items when the component is scrolled.
 *
 *
 * @param state the state object to be used by the layout.
 * @param modifier [Modifier] to apply for the layout.
 * @param measurePolicy Measure policy which provides ability to subcompose during the measuring.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
@UiComposable
fun SubcomposeLayout(
    state: SubcomposeLayoutState,
    modifier: Modifier = Modifier,
    measurePolicy: SubcomposeMeasureScope.(Constraints) -> MeasureResult,
    scrollableState: ScrollableState? = null,
    orientation: Orientation = Orientation.Vertical,
) {
    if (scrollableState == null) return

    val compositeKeyHash = currentCompositeKeyHash
    val compositionContext = rememberCompositionContext()
    val localMap = currentComposer.currentCompositionLocalMap
    var scrollViewRef: ScrollerView<ScrollerAttr, ScrollerEvent>? by remember { mutableStateOf(null) }
    val isVertical = orientation == Orientation.Vertical
    var scrollViewSize by remember { mutableStateOf(Size.Zero) } // dp值
    val materialized = currentComposer.materialize(modifier)
    val isIOS = LocalConfiguration.current.isIOS
    val density = LocalDensity.current
    scrollableState.kuiklyInfo.orientation = orientation
    scrollableState.kuiklyInfo.pageData = LocalConfiguration.current.pageData

    LaunchedEffect(scrollViewSize) {
        scrollableState.kuiklyInfo.updateContentSizeToRender()
    }

    // 更新kuiklyInfo和scrollview
    LaunchedEffect(scrollViewRef, scrollableState) {
        if (scrollViewRef == null) {
            return@LaunchedEffect
        }

        scrollableState.kuiklyInfo.scrollView = scrollViewRef
        scrollableState.kuiklyInfo.orientation = orientation
        scrollViewRef?.extProps?.set(KuiklyInfoKey, scrollableState.kuiklyInfo as Any)
        val kuiklyInfo = scrollableState.kuiklyInfo

        scrollViewRef?.getViewEvent()?.run {
            layoutFrameDidChange {
                scrollViewSize = Size(it.width, it.height)
            }

            if (scrollableState is PagerState) {
                willDragEndBySync(isSync = isIOS, handler = {
                    val viewportSize = kuiklyInfo.viewportSize
                    val scaleParams = it.scaleWithDensity(density.density)
                    // 实现分页滑动
                    val offset = if (isVertical) scaleParams.offsetY.toInt() else scaleParams.offsetX.toInt()
                    if ((offset < 0 && scrollableState.isAtTop()) || offset > (kuiklyInfo.currentContentSize - viewportSize)) {
                        return@willDragEndBySync
                    }
                    scrollableState.kuiklyWillDragEnd(scaleParams, orientation)
                })
            }
            scrollEnd {
                val scaleParams = it.scaleWithDensity(density.density)
                val offset = if (isVertical) scaleParams.offsetY.toInt() else scaleParams.offsetX.toInt()
                kuiklyInfo.contentOffset = offset

                // 仅触摸滑动结束会回调，api调用和bounce回弹都不会触发
                // / back是回滑,forward是前滑
                scrollableState.kuiklyOnScrollEnd(scaleParams)
            }
            scroll {
                val scaleParams = it.scaleWithDensity(density.density)
                val offset = if (isVertical) scaleParams.offsetY.toInt() else scaleParams.offsetX.toInt()

                kuiklyInfo.contentOffset = offset
                if (kuiklyInfo.ignoreScrollOffset != null) {
                    val ignoreOffset = kuiklyInfo.ignoreScrollOffset!!
                    val epsilon = 0.5 * density.density  // 使用 0.5dp 作为误差值
                    if (abs(ignoreOffset.x.minus(scaleParams.offsetX)) <= epsilon
                        && abs(ignoreOffset.y.minus(scaleParams.offsetY)) <= epsilon) {
                        kuiklyInfo.ignoreScrollOffset = null
                    }
                    return@scroll
                }

                // 忽略较小的滑动
                val delta = offset - kuiklyInfo.composeOffset
                if (delta.toInt() == 0) {
                    return@scroll
                }

                // 更新当前的contentSize大小
                val oldContentSize = kuiklyInfo.currentContentSize
                val newContentSize = scrollableState.calculateContentSize()

                // 如果contentSize变小了，需要确保composeOffset不会超出边界
                if (newContentSize < oldContentSize) {
                    val newMaxScrollOffset = maxOf(0, newContentSize - kuiklyInfo.viewportSize)
                    if (kuiklyInfo.composeOffset > newMaxScrollOffset) {
                        // 如果composeOffset超出新的边界，增加contentSize来保持composeOffset不变
                        val requiredContentSize = kuiklyInfo.composeOffset.toInt() + kuiklyInfo.viewportSize
                        kuiklyInfo.currentContentSize = maxOf(newContentSize, requiredContentSize)
                    } else {
                        kuiklyInfo.currentContentSize = newContentSize
                    }
                } else {
                    kuiklyInfo.currentContentSize = newContentSize
                }
                kuiklyInfo.updateContentSizeToRender()

                val toButtomDelta = if (kuiklyInfo.realContentSize == null) {
                    null
                } else {
                    kuiklyInfo.realContentSize!! - kuiklyInfo.viewportSize - kuiklyInfo.composeOffset
                }
                // 判断是否滑出边界
                if (offset < 0 && scrollableState.isAtTop()) {
                    return@scroll
                } else if (toButtomDelta != null && delta > toButtomDelta) {
                    if (toButtomDelta.toInt() <= 0) {
                        scrollableState.tryExpandStartSize(offset, true)
                        return@scroll
                    }
                    kuiklyInfo.composeOffset += min(delta, toButtomDelta)
                } else {
                    kuiklyInfo.composeOffset = max(0f, kuiklyInfo.composeOffset + delta)
                }

                // 触发compose滑动，并重新布局
                val comsumedDelta = scrollableState.kuiklyOnScroll(delta)

                // 尝试扩容
                scrollableState.tryExpandStartSize(offset, true)
            }
        }

        scrollViewRef?.listenScrollEvent()
    }

    ComposeNode<KNode<*>, KuiklyApplier>(
        factory = {
            val scrollView = ScrollerView<ScrollerAttr, ScrollerEvent>()
            scrollViewRef = scrollView

            KNode(scrollView) {
                attr {
                    if (orientation == Orientation.Vertical) {
                        flexDirectionColumn()
                    } else {
                        flexDirectionRow()
                    }
                    showScrollerIndicator(false)
                }
                event {
                    layoutFrameDidChange {
                        scrollViewSize = Size(it.width, it.height)
                    }
                }
                // 初始化contentview
                initScrollerContentComponentIfNeed()
            }
        },
        update = {
            set(state, state.setRoot)
            set(compositionContext, state.setCompositionContext)
            set(measurePolicy, state.setMeasurePolicy)
            set(localMap, SetResolvedCompositionLocals)
            set(materialized, SetModifier)
            @OptIn(ExperimentalComposeUiApi::class)
            set(compositeKeyHash, SetCompositeKeyHash)
            set(scrollableState) {
                scrollViewRef = this.view as? ScrollerView<ScrollerAttr, ScrollerEvent>
                scrollableState.kuiklyInfo.scrollView = scrollViewRef
                scrollableState.kuiklyInfo.orientation = orientation
                scrollViewRef?.extProps?.set(KuiklyInfoKey, scrollableState.kuiklyInfo as Any)
                scrollViewSize =
                    Size(
                        width = scrollViewRef?.renderView?.currentFrame?.width ?: 0f,
                        height = scrollViewRef?.renderView?.currentFrame?.width ?: 0f,
                    )
            }
        },
    )
    if (!currentComposer.skipping) {
        SideEffect {
            state.forceRecomposeChildren()
        }
    }
}

/**
 * The receiver scope of a [SubcomposeLayout]'s measure lambda which adds ability to dynamically
 * subcompose a content during the measuring on top of the features provided by [MeasureScope].
 */
interface SubcomposeMeasureScope : MeasureScope {
    /**
     * Performs subcomposition of the provided [content] with given [slotId].
     *
     * @param slotId unique id which represents the slot we are composing into. If you have fixed
     * amount or slots you can use enums as slot ids, or if you have a list of items maybe an
     * index in the list or some other unique key can work. To be able to correctly match the
     * content between remeasures you should provide the object which is equals to the one you
     * used during the previous measuring.
     * @param content the composable content which defines the slot. It could emit multiple
     * layouts, in this case the returned list of [Measurable]s will have multiple elements.
     * **Note:** When a [SubcomposeLayout] is in a [LookaheadScope], the subcomposition only
     * happens during the lookahead pass. In the post-lookahead/main pass, [subcompose] will
     * return the list of [Measurable]s that were subcomposed during the lookahead pass. If the
     * structure of the subtree emitted from [content] is dependent on incoming constraints,
     * consider using constraints received from the lookahead pass for both passes.
     */
    fun subcompose(
        slotId: Any?,
        content: @Composable () -> Unit,
    ): List<Measurable>
}

/**
 * State used by [SubcomposeLayout].
 *
 * [slotReusePolicy] the policy defining what slots should be retained to be reused later.
 */
class SubcomposeLayoutState(
    private val slotReusePolicy: SubcomposeSlotReusePolicy,
) {
    /**
     * State used by [SubcomposeLayout].
     */
    constructor() : this(NoOpSubcomposeSlotReusePolicy)

    /**
     * State used by [SubcomposeLayout].
     *
     * @param maxSlotsToRetainForReuse when non-zero the layout will keep active up to this count
     * slots which we were used but not used anymore instead of disposing them. Later when you try to
     * compose a new slot instead of creating a completely new slot the layout would reuse the
     * previous slot which allows to do less work especially if the slot contents are similar.
     */
    @Deprecated(
        "This constructor is deprecated",
        ReplaceWith(
            "SubcomposeLayoutState(SubcomposeSlotReusePolicy(maxSlotsToRetainForReuse))",
            "androidx.compose.ui.layout.SubcomposeSlotReusePolicy",
        ),
    )
    constructor(maxSlotsToRetainForReuse: Int) : this(
        SubcomposeSlotReusePolicy(maxSlotsToRetainForReuse),
    )

    private var _state: LayoutNodeSubcompositionsState? = null
    private val state: LayoutNodeSubcompositionsState
        get() =
            requireNotNull(_state) {
                "SubcomposeLayoutState is not attached to SubcomposeLayout"
            }

    // Pre-allocated lambdas to update LayoutNode
    internal val setRoot: KNode<*>.(SubcomposeLayoutState) -> Unit = {
        _state =
            subcompositionsState ?: LayoutNodeSubcompositionsState(this, slotReusePolicy).also {
                subcompositionsState = it
            }
        state.makeSureStateIsConsistent()
        state.slotReusePolicy = slotReusePolicy
    }
    internal val setCompositionContext:
        LayoutNode.(CompositionContext) -> Unit =
        { state.compositionContext = it }
    internal val setMeasurePolicy:
        LayoutNode.((SubcomposeMeasureScope.(Constraints) -> MeasureResult)) -> Unit =
        { measurePolicy = state.createMeasurePolicy(it) }

    /**
     * Composes the content for the given [slotId]. This makes the next scope.subcompose(slotId)
     * call during the measure pass faster as the content is already composed.
     *
     * If the [slotId] was precomposed already but after the future calculations ended up to not be
     * needed anymore (meaning this slotId is not going to be used during the measure pass
     * anytime soon) you can use [PrecomposedSlotHandle.dispose] on a returned object to dispose the
     * content.
     *
     * @param slotId unique id which represents the slot we are composing into.
     * @param content the composable content which defines the slot.
     * @return [PrecomposedSlotHandle] instance which allows you to dispose the content.
     */
    fun precompose(
        slotId: Any?,
        content: @Composable () -> Unit,
    ): PrecomposedSlotHandle = state.precompose(slotId, content)

    internal fun forceRecomposeChildren() = state.forceRecomposeChildren()

    /**
     * Instance of this interface is returned by [precompose] function.
     */
    interface PrecomposedSlotHandle {
        /**
         * This function allows to dispose the content for the slot which was precomposed
         * previously via [precompose].
         *
         * If this slot was already used during the regular measure pass via
         * [SubcomposeMeasureScope.subcompose] this function will do nothing.
         *
         * This could be useful if after the future calculations this item is not anymore expected to
         * be used during the measure pass anytime soon.
         */
        fun dispose()

        /**
         * The amount of placeables composed into this slot.
         */
        val placeablesCount: Int get() = 0

        /**
         * Performs synchronous measure of the placeable at the given [index].
         *
         * @param index the placeable index. Should be smaller than [placeablesCount].
         * @param constraints Constraints to measure this placeable with.
         */
        fun premeasure(
            index: Int,
            constraints: Constraints,
        ) {}

        /**
         * Conditionally executes [block] for each [Modifier.Node] of this Composition that is a
         * [TraversableNode] with a matching [key].
         *
         * See [androidx.compose.ui.node.traverseDescendants] for the complete semantics of this
         * function.
         */
        fun traverseDescendants(
            key: Any?,
            block: (TraversableNode) -> TraversableNode.Companion.TraverseDescendantsAction,
        ) {}
    }
}

/**
 * This policy allows [SubcomposeLayout] to retain some of slots which we were used but not
 * used anymore instead of disposing them. Next time when you try to compose a new slot instead of
 * creating a completely new slot the layout would reuse the kept slot. This allows to do less
 * work especially if the slot contents are similar.
 */
interface SubcomposeSlotReusePolicy {
    /**
     * This function will be called with [slotIds] set populated with the slot ids available to
     * reuse. In the implementation you can remove slots you don't want to retain.
     */
    fun getSlotsToRetain(slotIds: SlotIdsSet)

    /**
     * Returns true if the content previously composed with [reusableSlotId] is compatible with
     * the content which is going to be composed for [slotId].
     * Slots could be considered incompatible if they display completely different types of the UI.
     */
    fun areCompatible(
        slotId: Any?,
        reusableSlotId: Any?,
    ): Boolean

    /**
     * Set containing slot ids currently available to reuse. Used by [getSlotsToRetain].
     *
     * This class works exactly as [MutableSet], but doesn't allow to add new items in it.
     */
    class SlotIdsSet internal constructor(
        private val set: MutableSet<Any?> = mutableSetOf(),
    ) : Collection<Any?> by set {
        internal fun add(slotId: Any?) = set.add(slotId)

        override fun iterator(): MutableIterator<Any?> = set.iterator()

        /**
         * Removes a [slotId] from this set, if it is present.
         *
         * @return `true` if the slot id was removed, `false` if the set was not modified.
         */
        fun remove(slotId: Any?): Boolean = set.remove(slotId)

        /**
         * Removes all slot ids from [slotIds] that are also contained in this set.
         *
         * @return `true` if any slot id was removed, `false` if the set was not modified.
         */
        fun removeAll(slotIds: Collection<Any?>): Boolean = set.remove(slotIds)

        /**
         * Removes all slot ids that match the given [predicate].
         *
         * @return `true` if any slot id was removed, `false` if the set was not modified.
         */
        fun removeAll(predicate: (Any?) -> Boolean): Boolean = set.removeAll(predicate)

        /**
         * Retains only the slot ids that are contained in [slotIds].
         *
         * @return `true` if any slot id was removed, `false` if the set was not modified.
         */
        fun retainAll(slotIds: Collection<Any?>): Boolean = set.retainAll(slotIds)

        /**
         * Retains only slotIds that match the given [predicate].
         *
         * @return `true` if any slot id was removed, `false` if the set was not modified.
         */
        fun retainAll(predicate: (Any?) -> Boolean): Boolean = set.retainAll(predicate)

        /**
         * Removes all slot ids from this set.
         */
        fun clear() = set.clear()
    }
}

/**
 * Creates [SubcomposeSlotReusePolicy] which retains the fixed amount of slots.
 *
 * @param maxSlotsToRetainForReuse the [SubcomposeLayout] will retain up to this amount of slots.
 */
fun SubcomposeSlotReusePolicy(maxSlotsToRetainForReuse: Int): SubcomposeSlotReusePolicy =
    FixedCountSubcomposeSlotReusePolicy(maxSlotsToRetainForReuse)

/**
 * The inner state containing all the information about active slots and their compositions.
 * It is stored inside LayoutNode object as in fact we need to keep 1-1 mapping between this state
 * and the node: when we compose a slot we first create a virtual LayoutNode child to this node
 * and then save the extra information inside this state.
 * Keeping this state inside LayoutNode also helps us to retain the pool of reusable slots even
 * when a new SubcomposeLayoutState is applied to SubcomposeLayout and even when the
 * SubcomposeLayout's LayoutNode is reused via the ReusableComposeNode mechanism.
 */
internal class LayoutNodeSubcompositionsState(
    private val root: KNode<*>,
    slotReusePolicy: SubcomposeSlotReusePolicy,
) : ComposeNodeLifecycleCallback {
    var compositionContext: CompositionContext? = null

    var slotReusePolicy: SubcomposeSlotReusePolicy = slotReusePolicy
        set(value) {
            if (field !== value) {
                field = value
                // the new policy will be applied after measure
                markActiveNodesAsReused(deactivate = false)
                root.requestRemeasure()
            }
        }

    private var currentIndex = 0
    private var currentPostLookaheadIndex = 0
    private val nodeToNodeState = hashMapOf<LayoutNode, NodeState>()

    // this map contains active slotIds (without precomposed or reusable nodes)
    internal val slotIdToNode = hashMapOf<Any?, LayoutNode>()
    private val scope = Scope()
    private val postLookaheadMeasureScope = PostLookaheadMeasureScopeImpl()

    private val precomposeMap = hashMapOf<Any?, LayoutNode>()
    private val reusableSlotIdsSet = SubcomposeSlotReusePolicy.SlotIdsSet()

    // SlotHandles precomposed in the post-lookahead pass.
    private val postLookaheadPrecomposeSlotHandleMap = mutableMapOf<Any?, SubcomposeLayoutState.PrecomposedSlotHandle>()

    // Slot ids _composed_ in post-lookahead. The valid slot ids are stored between 0 and
    // currentPostLookaheadIndex - 1, beyond index currentPostLookaheadIndex are obsolete ids.
    private val postLookaheadComposedSlotIds = mutableVectorOf<Any?>()

    /**
     * `root.foldedChildren` list consist of:
     * 1) all the active children (used during the last measure pass)
     * 2) `reusableCount` nodes in the middle of the list which were active and stopped being
     * used. now we keep them (up to `maxCountOfSlotsToReuse`) in order to reuse next time we
     * will need to compose a new item
     * 4) `precomposedCount` nodes in the end of the list which were precomposed and
     * are waiting to be used during the next measure passes.
     */
    private var reusableCount = 0
    private var precomposedCount = 0

    override fun onReuse() {
        markActiveNodesAsReused(deactivate = false)
    }

    override fun onDeactivate() {
        markActiveNodesAsReused(deactivate = true)
    }

    override fun onRelease() {
        disposeCurrentNodes()
    }

    fun subcompose(
        slotId: Any?,
        content: @Composable () -> Unit,
    ): List<Measurable> {
        makeSureStateIsConsistent()
        val layoutState = root.layoutState
        checkPrecondition(
            layoutState == LayoutNode.LayoutState.Measuring ||
                layoutState == LayoutNode.LayoutState.LayingOut ||
                layoutState == LayoutNode.LayoutState.LookaheadMeasuring ||
                layoutState == LayoutNode.LayoutState.LookaheadLayingOut,
        ) {
            "subcompose can only be used inside the measure or layout blocks"
        }

        val node =
            slotIdToNode.getOrPut(slotId) {
                val precomposed = precomposeMap.remove(slotId)
                if (precomposed != null) {
                    @Suppress("ExceptionMessage")
                    checkPrecondition(precomposedCount > 0)
                    precomposedCount--
                    precomposed
                } else {
                    takeNodeFromReusables(slotId)
                        ?: createNodeAt(currentIndex)
                }
            }

        // Set the lazyItemKey for KNode to establish mapping with LazyList item
        if (node is KNode<*>) {
            node.lazyItemKey = slotId
        }

        if (root.foldedChildren.getOrNull(currentIndex) !== node) {
            // the node has a new index in the list
            val itemIndex = root.foldedChildren.indexOf(node)
            require(itemIndex >= currentIndex) {
                "Key \"$slotId\" was already used. If you are using LazyColumn/Row please make " +
                    "sure you provide a unique key for each item."
            }
            if (currentIndex != itemIndex) {
                move(itemIndex, currentIndex)
            }
        }
        currentIndex++

        subcompose(node, slotId, content)

        return if (layoutState == LayoutNode.LayoutState.Measuring || layoutState == LayoutNode.LayoutState.LayingOut) {
            node.childMeasurables
        } else {
            node.childLookaheadMeasurables
        }
    }

    private fun subcompose(
        node: LayoutNode,
        slotId: Any?,
        content: @Composable () -> Unit,
    ) {
        val nodeState =
            nodeToNodeState.getOrPut(node) {
                NodeState(slotId, {})
            }
        val hasPendingChanges = nodeState.composition?.hasInvalidations ?: true
        if (nodeState.content !== content || hasPendingChanges || nodeState.forceRecompose) {
            nodeState.content = content
            subcompose(node, nodeState)
            nodeState.forceRecompose = false
        }
    }

    private fun subcompose(
        node: LayoutNode,
        nodeState: NodeState,
    ) {
        Snapshot.withoutReadObservation {
            ignoreRemeasureRequests {
                val content = nodeState.content
                nodeState.composition =
                    subcomposeInto(
                        existing = nodeState.composition,
                        container = node,
                        parent = compositionContext ?: error("parent composition reference not set"),
                        reuseContent = nodeState.forceReuse,
                        composable = {
                            ReusableContentHost(nodeState.active, content)
                        },
                    )
                nodeState.forceReuse = false
            }
        }
    }

    private fun subcomposeInto(
        existing: ReusableComposition?,
        container: LayoutNode,
        reuseContent: Boolean,
        parent: CompositionContext,
        composable: @Composable () -> Unit,
    ): ReusableComposition =
        if (existing == null || existing.isDisposed) {
            createSubcomposition(container as KNode<DeclarativeBaseView<*, *>>, parent)
        } else {
            existing
        }.apply {
            // #IF_KOTLIN_1.9
            if (!reuseContent) {
                // #END_IF
                setContent(composable)
                // #IF_KOTLIN_1.9
            } else {
                setContentWithReuse(composable)
            }
            // #END_IF
        }

    private fun getSlotIdAtIndex(index: Int): Any? {
        val node = root.foldedChildren[index]
        return nodeToNodeState[node]!!.slotId
    }

    fun disposeOrReuseStartingFromIndex(startIndex: Int) {
        reusableCount = 0
        val lastReusableIndex = root.foldedChildren.size - precomposedCount - 1

        if (startIndex <= lastReusableIndex) {
            // construct the set of available slot ids
            reusableSlotIdsSet.clear()
            for (i in startIndex..lastReusableIndex) {
                val slotId = getSlotIdAtIndex(i)
                reusableSlotIdsSet.add(slotId)
            }
            slotReusePolicy.getSlotsToRetain(reusableSlotIdsSet)

            // iterating backwards so it is easier to remove items
            var i = lastReusableIndex
            while (i >= startIndex) {
                val node = root.foldedChildren[i]
                val nodeState = nodeToNodeState[node]!!
                val slotId = nodeState.slotId
                if (reusableSlotIdsSet.contains(slotId)) {
                    reusableCount++
                } else {
                    nodeToNodeState.remove(node)
                    nodeState.composition?.dispose()
                    root.removeAt(i, 1)
                }
                slotIdToNode.remove(slotId)
                i--
            }
        }
    }

    private fun markActiveNodesAsReused(deactivate: Boolean) {
        // #IF_KOTLIN_1.9
        precomposedCount = 0
        precomposeMap.clear()

        val childCount = root.foldedChildren.size
        if (reusableCount != childCount) {
            reusableCount = childCount
            Snapshot.withoutReadObservation {
                for (i in 0 until childCount) {
                    val node = root.foldedChildren[i]
                    val nodeState = nodeToNodeState[node]
                    if (nodeState != null && nodeState.active) {
                        node.resetLayoutState()
                        if (deactivate) {
                            nodeState.composition?.deactivate()
                            nodeState.activeState = mutableStateOf(false)
                        } else {
                            nodeState.active = false
                        }
                        // create a new instance to avoid change notifications
                        nodeState.slotId = ReusedSlotId
                    }
                }
            }
            slotIdToNode.clear()
        }

        makeSureStateIsConsistent()
        // #END_IF
    }

    private fun disposeCurrentNodes() {
        root.ignoreRemeasureRequests {
            nodeToNodeState.values.forEach {
                it.composition?.dispose()
            }
            root.removeAll()
        }

        nodeToNodeState.clear()
        slotIdToNode.clear()
        precomposedCount = 0
        reusableCount = 0
        precomposeMap.clear()

        makeSureStateIsConsistent()
    }

    fun makeSureStateIsConsistent() {
        val childrenCount = root.foldedChildren.size
        require(nodeToNodeState.size == childrenCount) {
            "Inconsistency between the count of nodes tracked by the state " +
                "(${nodeToNodeState.size}) and the children count on the SubcomposeLayout" +
                " ($childrenCount). Are you trying to use the state of the" +
                " disposed SubcomposeLayout?"
        }
        require(childrenCount - reusableCount - precomposedCount >= 0) {
            "Incorrect state. Total children $childrenCount. Reusable children " +
                "$reusableCount. Precomposed children $precomposedCount"
        }
        require(precomposeMap.size == precomposedCount) {
            "Incorrect state. Precomposed children $precomposedCount. Map size " +
                "${precomposeMap.size}"
        }
    }

    private fun LayoutNode.resetLayoutState() {
        measurePassDelegate.measuredByParent = LayoutNode.UsageByParent.NotUsed
        lookaheadPassDelegate?.let {
            it.measuredByParent = LayoutNode.UsageByParent.NotUsed
        }
    }

    private fun takeNodeFromReusables(slotId: Any?): LayoutNode? {
        if (reusableCount == 0) {
            return null
        }
        val reusableNodesSectionEnd = root.foldedChildren.size - precomposedCount
        val reusableNodesSectionStart = reusableNodesSectionEnd - reusableCount
        var index = reusableNodesSectionEnd - 1
        var chosenIndex = -1
        // first try to find a node with exactly the same slotId
        while (index >= reusableNodesSectionStart) {
            if (getSlotIdAtIndex(index) == slotId) {
                // we have a node with the same slotId
                chosenIndex = index
                break
            } else {
                index--
            }
        }
        if (chosenIndex == -1) {
            // try to find a first compatible slotId from the end of the section
            index = reusableNodesSectionEnd - 1
            while (index >= reusableNodesSectionStart) {
                val node = root.foldedChildren[index]
                val nodeState = nodeToNodeState[node]!!
                if (
                    nodeState.slotId === ReusedSlotId ||
                    slotReusePolicy.areCompatible(slotId, nodeState.slotId)
                ) {
                    nodeState.slotId = slotId
                    chosenIndex = index
                    break
                }
                index--
            }
        }
        return if (chosenIndex == -1) {
            // no compatible nodes found
            null
        } else {
            if (index != reusableNodesSectionStart) {
                // we need to rearrange the items
                move(index, reusableNodesSectionStart, 1)
            }
            reusableCount--
            val node = root.foldedChildren[reusableNodesSectionStart]
            val nodeState = nodeToNodeState[node]!!
            // create a new instance to avoid change notifications
            nodeState.activeState = mutableStateOf(true)
            nodeState.forceReuse = true
            nodeState.forceRecompose = true
            node
        }
    }

    fun createMeasurePolicy(block: SubcomposeMeasureScope.(Constraints) -> MeasureResult): MeasurePolicy {
        return object : LayoutNode.NoIntrinsicsMeasurePolicy(error = NoIntrinsicsMessage) {
            override fun MeasureScope.measure(
                measurables: List<Measurable>,
                constraints: Constraints,
            ): MeasureResult {
                scope.layoutDirection = layoutDirection
                scope.density = density
                scope.fontScale = fontScale
                if (!isLookingAhead && root.lookaheadRoot != null) {
                    currentPostLookaheadIndex = 0
                    val result = postLookaheadMeasureScope.block(constraints)
                    val indexAfterMeasure = currentPostLookaheadIndex
                    return createMeasureResult(result) {
                        currentPostLookaheadIndex = indexAfterMeasure
                        result.placeChildren()
                        // dispose
                        disposeUnusedSlotsInPostLookahead()
                    }
                } else {
                    currentIndex = 0
                    val result = scope.block(constraints)
                    val indexAfterMeasure = currentIndex
                    return createMeasureResult(result) {
                        currentIndex = indexAfterMeasure
                        result.placeChildren()
                        checkOffScreenNode(result)
                        disposeOrReuseStartingFromIndex(currentIndex)
                    }
                }
            }
        }
    }

    private fun disposeUnusedSlotsInPostLookahead() {
        postLookaheadPrecomposeSlotHandleMap.entries.removeAll { (slotId, handle) ->
            val id = postLookaheadComposedSlotIds.indexOf(slotId)
            if (id < 0 || id >= currentPostLookaheadIndex) {
                // Slot was not used in the latest pass of post-lookahead.
                handle.dispose()
                true
            } else {
                false
            }
        }
    }

    private inline fun createMeasureResult(
        result: MeasureResult,
        crossinline placeChildrenBlock: () -> Unit,
    ) = object : MeasureResult by result {
        override fun placeChildren() {
            placeChildrenBlock()
        }
    }

    private val NoIntrinsicsMessage =
        "Asking for intrinsic measurements of SubcomposeLayout " +
            "layouts is not supported. This includes components that are built on top of " +
            "SubcomposeLayout, such as lazy lists, BoxWithConstraints, TabRow, etc. To mitigate " +
            "this:\n" +
            "- if intrinsic measurements are used to achieve 'match parent' sizing, consider " +
            "replacing the parent of the component with a custom layout which controls the order in " +
            "which children are measured, making intrinsic measurement not needed\n" +
            "- adding a size modifier to the component, in order to fast return the queried " +
            "intrinsic measurement."

    fun precompose(
        slotId: Any?,
        content: @Composable () -> Unit,
    ): SubcomposeLayoutState.PrecomposedSlotHandle {
        if (!root.isAttached) {
            return object : SubcomposeLayoutState.PrecomposedSlotHandle {
                override fun dispose() {}
            }
        }
        makeSureStateIsConsistent()
        if (!slotIdToNode.containsKey(slotId)) {
            // Yield ownership of PrecomposedHandle from postLookahead to the caller of precompose
            postLookaheadPrecomposeSlotHandleMap.remove(slotId)
            val node =
                precomposeMap.getOrPut(slotId) {
                    val reusedNode = takeNodeFromReusables(slotId)
                    if (reusedNode != null) {
                        // now move this node to the end where we keep precomposed items
                        val nodeIndex = root.foldedChildren.indexOf(reusedNode)
                        move(nodeIndex, root.foldedChildren.size, 1)
                        precomposedCount++
                        reusedNode
                    } else {
                        createNodeAt(root.foldedChildren.size).also {
                            precomposedCount++
                        }
                    }
                }
            subcompose(node, slotId, content)
        }
        return object : SubcomposeLayoutState.PrecomposedSlotHandle {
            override fun dispose() {
                makeSureStateIsConsistent()
                val node = precomposeMap.remove(slotId)
                if (node != null) {
                    check(precomposedCount > 0) { "No pre-composed items to dispose" }
                    val itemIndex = root.foldedChildren.indexOf(node)
                    check(itemIndex >= root.foldedChildren.size - precomposedCount) {
                        "Item is not in pre-composed item range"
                    }
                    // move this item into the reusable section
                    reusableCount++
                    precomposedCount--
                    val reusableStart = root.foldedChildren.size - precomposedCount - reusableCount
                    move(itemIndex, reusableStart, 1)
                    disposeOrReuseStartingFromIndex(reusableStart)
                }
            }

            override val placeablesCount: Int
                get() = precomposeMap[slotId]?.children?.size ?: 0

            override fun premeasure(
                index: Int,
                constraints: Constraints,
            ) {
                val node = precomposeMap[slotId]
                if (node != null && node.isAttached) {
                    val size = node.children.size
                    if (index < 0 || index >= size) {
                        throw IndexOutOfBoundsException(
                            "Index ($index) is out of bound of [0, $size)",
                        )
                    }
                    require(!node.isPlaced) { "Pre-measure called on node that is not placed" }
                    root.ignoreRemeasureRequests {
                        node.requireOwner().measureAndLayout(node.children[index], constraints)
                    }
                }
            }

            override fun traverseDescendants(
                key: Any?,
                block: (TraversableNode) -> TraversableNode.Companion.TraverseDescendantsAction,
            ) {
                precomposeMap[slotId]?.nodes?.head?.traverseDescendants(key, block)
            }
        }
    }

    fun forceRecomposeChildren() {
        val childCount = root.foldedChildren.size
        if (reusableCount != childCount) {
            // only invalidate children if there are any non-reused ones
            // in other cases, all of them are going to be invalidated later anyways
            nodeToNodeState.forEach { (_, nodeState) ->
                nodeState.forceRecompose = true
            }

            if (!root.measurePending) {
                root.requestRemeasure()
            }
        }
    }

    private fun createNodeAt(index: Int) =
        KNode(VirtualNodeView(), isVirtual = true) { }.also { node ->
            ignoreRemeasureRequests {
                // 构建view树
                root.insertTopDown(index, node as KNode<DeclarativeBaseView<*, *>>)
                // 构建node树
                root.insertAt(index, node)
            }
        }

    private fun move(
        from: Int,
        to: Int,
        count: Int = 1,
    ) {
        ignoreRemeasureRequests {
            root.move(from, to, count)
        }
    }

    private inline fun ignoreRemeasureRequests(block: () -> Unit) = root.ignoreRemeasureRequests(block)

    private class NodeState(
        var slotId: Any?,
        var content: @Composable () -> Unit,
        var composition: ReusableComposition? = null,
    ) {
        var forceRecompose = false
        var forceReuse = false
        var activeState = mutableStateOf(true)
        var active: Boolean
            get() = activeState.value
            set(value) {
                activeState.value = value
            }
    }

    private inner class Scope : SubcomposeMeasureScope {
        // MeasureScope delegation
        override var layoutDirection: LayoutDirection = LayoutDirection.Rtl
        override var density: Float = 0f
        override var fontScale: Float = 0f
        override val isLookingAhead: Boolean
            get() =
                root.layoutState == LayoutNode.LayoutState.LookaheadLayingOut ||
                    root.layoutState == LayoutNode.LayoutState.LookaheadMeasuring

        override fun subcompose(
            slotId: Any?,
            content: @Composable () -> Unit,
        ) = this@LayoutNodeSubcompositionsState.subcompose(slotId, content)

        override fun layout(
            width: Int,
            height: Int,
            alignmentLines: Map<AlignmentLine, Int>,
//            rulers: (RulerScope.() -> Unit)?,
            placementBlock: Placeable.PlacementScope.() -> Unit,
        ): MeasureResult {
            checkMeasuredSize(width, height)
            return object : MeasureResult {
                override val width: Int
                    get() = width
                override val height: Int
                    get() = height
                override val alignmentLines: Map<AlignmentLine, Int>
                    get() = alignmentLines
//                override val rulers: (RulerScope.() -> Unit)?
//                    get() = rulers

                override fun placeChildren() {
                    if (isLookingAhead) {
                        val delegate = root.innerCoordinator.lookaheadDelegate
                        if (delegate != null) {
                            delegate.placementScope.placementBlock()
                            return
                        }
                    }
                    root.innerCoordinator.placementScope.placementBlock()
                }
            }
        }
    }

    private inner class PostLookaheadMeasureScopeImpl :
        SubcomposeMeasureScope,
        MeasureScope by scope {
        /**
         * This function retrieves [Measurable]s created for [slotId] based on
         * the subcomposition that happened in the lookahead pass. If [slotId] was not subcomposed
         * in the lookahead pass, [subcompose] will return an [emptyList].
         */
        override fun subcompose(
            slotId: Any?,
            content: @Composable () -> Unit,
        ): List<Measurable> {
            val measurables = slotIdToNode[slotId]?.childMeasurables
            if (measurables != null) {
                return measurables
            }
            return postLookaheadSubcompose(slotId, content)
        }
    }

    private fun postLookaheadSubcompose(
        slotId: Any?,
        content: @Composable () -> Unit,
    ): List<Measurable> {
        require(postLookaheadComposedSlotIds.size >= currentPostLookaheadIndex) {
            "Error: currentPostLookaheadIndex cannot be greater than the size of the" +
                "postLookaheadComposedSlotIds list."
        }
        if (postLookaheadComposedSlotIds.size == currentPostLookaheadIndex) {
            postLookaheadComposedSlotIds.add(slotId)
        } else {
            postLookaheadComposedSlotIds[currentPostLookaheadIndex] = slotId
        }
        currentPostLookaheadIndex++
        if (!precomposeMap.contains(slotId)) {
            // Not composed yet
            precompose(slotId, content).also {
                postLookaheadPrecomposeSlotHandleMap[slotId] = it
            }
            if (root.layoutState == LayoutNode.LayoutState.LayingOut) {
                root.requestLookaheadRelayout(true)
            } else {
                root.requestLookaheadRemeasure(true)
            }
        }

        return precomposeMap[slotId]?.run {
            measurePassDelegate.childDelegates.also {
                it.fastForEach { delegate -> delegate.markDetachedFromParentLookaheadPass() }
            }
        } ?: emptyList()
    }
}

private val ReusedSlotId =
    object {
        override fun toString(): String = "ReusedSlotId"
    }

private class FixedCountSubcomposeSlotReusePolicy(
    private val maxSlotsToRetainForReuse: Int,
) : SubcomposeSlotReusePolicy {
    override fun getSlotsToRetain(slotIds: SubcomposeSlotReusePolicy.SlotIdsSet) {
        if (slotIds.size > maxSlotsToRetainForReuse) {
            var count = 0
            with(slotIds.iterator()) {
                // keep first maxSlotsToRetainForReuse items
                while (hasNext()) {
                    next()
                    count++
                    if (count > maxSlotsToRetainForReuse) {
                        remove()
                    }
                }
            }
        }
    }

    override fun areCompatible(
        slotId: Any?,
        reusableSlotId: Any?,
    ): Boolean = true
}

private object NoOpSubcomposeSlotReusePolicy : SubcomposeSlotReusePolicy {
    override fun getSlotsToRetain(slotIds: SubcomposeSlotReusePolicy.SlotIdsSet) {
        slotIds.clear()
    }

    override fun areCompatible(
        slotId: Any?,
        reusableSlotId: Any?,
    ) = false
}
