/*
 * Tencent is pleased to support the open source community by making KuiklyUI
 * available.
 * Copyright (C) 2025 Tencent. All rights reserved.
 * Licensed under the License of KuiklyUI;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * https://github.com/Tencent-TDS/KuiklyUI/blob/main/LICENSE
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tencent.kuikly.demo.pages.demo

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewRef
import com.tencent.kuikly.core.directives.vfor
import com.tencent.kuikly.core.log.KLog
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.tencent.kuikly.core.pager.Pager
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.reactive.handler.observableList
import com.tencent.kuikly.core.views.List
import com.tencent.kuikly.core.views.PageList
import com.tencent.kuikly.core.views.PageListView
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.tencent.kuikly.demo.pages.base.BasePager
import com.tencent.kuikly.demo.pages.base.BridgeModule
import com.tencent.kuikly.demo.pages.demo.base.NavBar

/**
 * 与宿主 KuiklyNativeTabBarController 配合：始终同一个 Kuikly 实例；
 * 底栏切换：Native → Kotlin（NativeTabBarSelected）；PageList 横滑 → Kotlin → Native（syncNativeTabBarIndex）。
 */
@Page("NativeTabBarHost")
internal class NativeTabBarHostPage : BasePager() {

    var selectedTabIndex by observable(0)
    var tabBarSafeLeft by observable(0f)
    var tabBarSafeBottom by observable(0f)
    var tabBarSafeRight by observable(0f)

    var listPadLeft by observable(LIST_HORIZONTAL_MARGIN)
    var listPadBottom by observable(LIST_CONTENT_MARGIN)
    var listPadRight by observable(LIST_HORIZONTAL_MARGIN)
    var listItemWidth by observable(0f)
    var pageListAreaHeight by observable(0f)

    private var pageListRef: ViewRef<PageListView<*, *>>? = null

    private val tabTitles = listOf("首页", "发现", "我的")
    private var tabPageIndexes by observableList<Int>()

    private fun navBarHeight(): Float = pageData.statusBarHeight + 44f

    private fun refreshListLayoutMetrics() {
        listPadLeft = LIST_HORIZONTAL_MARGIN + tabBarSafeLeft
        listPadBottom = tabBarSafeBottom + LIST_CONTENT_MARGIN
        listPadRight = 0f
        val contentW = pageData.pageViewWidth - listPadLeft
        listItemWidth = if (contentW > 0f) contentW else pageData.pageViewWidth
        val h = pageData.pageViewHeight - navBarHeight()
        pageListAreaHeight = if (h > 0f) h else pageData.pageViewHeight
        flexNode.markDirty()
    }

    override fun created() {
        super.created()
        if (tabPageIndexes.isEmpty()) {
            tabPageIndexes.addAll(listOf(0, 1, 2))
        }
        val h = pageData.pageViewHeight - navBarHeight()
        if (h > 0f) {
            pageListAreaHeight = h
        }
    }

    override fun onReceivePagerEvent(pagerEvent: String, eventData: JSONObject) {
        super.onReceivePagerEvent(pagerEvent, eventData)
        when (pagerEvent) {
            PAGER_EVENT_TAB_SELECTED -> {
                val index = eventData.optInt("index")
                if (index in tabTitles.indices) {
                    selectedTabIndex = index
                    pageListRef?.view?.scrollToPageIndex(index, false)
                }
            }
            PAGER_EVENT_TAB_BAR_SAFE_AREA -> {
                tabBarSafeLeft = eventData.optDouble("left").toFloat()
                tabBarSafeBottom = eventData.optDouble("bottom").toFloat()
                tabBarSafeRight = eventData.optDouble("right").toFloat()
                refreshListLayoutMetrics()
                KLog.i(
                    "NativeTabBar",
                    "event safe l=$tabBarSafeLeft b=$tabBarSafeBottom r=$tabBarSafeRight " +
                        "page=${pageData.pageViewWidth}x${pageData.pageViewHeight} " +
                        "pad l=$listPadLeft b=$listPadBottom r=$listPadRight",
                )
            }
            Pager.PAGER_EVENT_DID_APPEAR,
            Pager.PAGER_EVENT_ROOT_VIEW_SIZE_CHANGED -> {
                refreshListLayoutMetrics()
                KLog.i(
                    "NativeTabBar",
                    "sizeChanged page=${pageData.pageViewWidth}x${pageData.pageViewHeight} " +
                        "safe l=$tabBarSafeLeft b=$tabBarSafeBottom r=$tabBarSafeRight " +
                        "pad l=$listPadLeft b=$listPadBottom r=$listPadRight",
                )
            }
        }
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr {
                flexDirectionColumn()
                backgroundColor(Color(0xFFF2F2F7))
            }
            NavBar {
                attr {
                    title = "单页 Tab 宿主"
                    backDisable = true
                }
            }
            PageList {
                ref {
                    ctx.pageListRef = it
                }
                event {
                    pageIndexDidChanged { params ->
                        val index = (params as JSONObject).optInt("index")
                        if (index !in ctx.tabTitles.indices || index == ctx.selectedTabIndex) {
                            return@pageIndexDidChanged
                        }
                        ctx.selectedTabIndex = index
                        ctx.acquireModule<BridgeModule>(BridgeModule.MODULE_NAME)
                            .syncNativeTabBarIndex(index)
                    }
                }
                attr {
                    flex(1f)
                    pageDirection(true)
                    pageItemWidth(pagerData.pageViewWidth)
                    pageItemHeight(ctx.pageListAreaHeight)
                    defaultPageIndex(ctx.selectedTabIndex)
                    showScrollerIndicator(false)
                    offscreenPageLimit(1)
                }
                vfor({ ctx.tabPageIndexes }) { tabIndex ->
                    List {
                        attr {
                            flex(1f)
                            padding(
                                top = 12f,
                                left = ctx.listPadLeft,
                                bottom = ctx.listPadBottom,
                                right = 0f,
                            )
                        }
                        for (row in 0 until 36) {
                            val line = row + 1
                            val tabName = ctx.tabTitles[tabIndex]
                            View {
                                attr {
                                    width(ctx.listItemWidth)
                                    height(56f)
                                    marginBottom(10f)
                                    borderRadius(12f)
                                    padding(left = 16f)
                                    justifyContentCenter()
                                    backgroundColor(Color.WHITE)
                                }
                                event {
                                    click {
                                        KLog.i("NativeTabBar", "click $tabName · 第 $line 行")
                                    }
                                }
                                Text {
                                    attr {
                                        text("$tabName · 第 $line 行")
                                        fontSize(16f)
                                        color(Color.BLACK)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val PAGER_EVENT_TAB_SELECTED = "NativeTabBarSelected"
        const val PAGER_EVENT_TAB_BAR_SAFE_AREA = "NativeTabBarSafeAreaInsets"

        private const val LIST_HORIZONTAL_MARGIN = 20f
        private const val LIST_CONTENT_MARGIN = 16f
    }
}
