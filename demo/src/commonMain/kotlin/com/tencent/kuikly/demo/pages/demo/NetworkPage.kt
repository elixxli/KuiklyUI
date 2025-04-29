package com.tencent.kuikly.demo.pages.demo

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.directives.vfor
import com.tencent.kuikly.core.module.NetworkModule
import com.tencent.kuikly.core.nvi.serialization.json.JSONArray
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.tencent.kuikly.core.reactive.handler.observableList
import com.tencent.kuikly.core.views.Image
import com.tencent.kuikly.core.views.List
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.tencent.kuikly.demo.pages.base.BasePager

@Page("NetworkPage")
internal class NetworkPage : BasePager() {

    var profileItemList by observableList<ProfileItem>()

    override fun created() {
        super.created()
        acquireModule<NetworkModule>(NetworkModule.MODULE_NAME).httpRequest("https://11006487-ff05-41ed-98bd-200377b92859.mock.pstmn.io/NetworkModule", false, param = JSONObject().apply {
            put("id", 1)
        }, responseCallback = {data, success, errorMsg ->
            val dataList = data.optJSONArray("dataList") ?: JSONArray()
            val size = dataList.length()
            for (i in 0 until size) {
                profileItemList.add(ProfileItem().decode(dataList.optJSONObject(i) ?: JSONObject()))
            }
        })
    }


    override fun body(): ViewBuilder {
        val ctx = this
        return {
            List {
                attr {
                    marginTop(30f)
                    flex(1f)
                }

                vfor({ ctx.profileItemList }) { item ->
                    View {
                        attr {
                            height(70f)
                            flexDirectionRow()
                            alignItemsCenter()
                            marginLeft(8f)
                            if (ctx.profileItemList.indexOf(item) % 2 == 0) {
                                backgroundColor(Color.GRAY)
                            }
                        }

                        Image {
                            attr {
                                size(30f, 30f)
                                src(item.avatar)
                            }
                        }
                        Text {
                            attr {
                                marginLeft(8f)
                                fontSize(16f)
                                color(Color.BLACK)
                                text(item.name)
                            }
                        }
                    }
                }
            }
        }
    }
}

internal class ProfileItem {
    var name = ""
    var avatar = ""

    fun decode(itemJSONObject: JSONObject): ProfileItem {
        name = itemJSONObject.optString("name")
        avatar = itemJSONObject.optString("avatar")
        return this
    }
}
