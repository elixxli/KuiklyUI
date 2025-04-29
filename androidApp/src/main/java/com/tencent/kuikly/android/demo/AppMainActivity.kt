/*
 * Tencent is pleased to support the open source community by making KuiklyUI
 * available.
 * Copyright (C) 2025 THL A29 Limited, a Tencent company. All rights reserved.
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

package com.tencent.kuikly.android.demo

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.TextView

class AppMainActivity: Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_main)
        val btn = findViewById<View>(R.id.open_kuikly_page_btn)
        val performanceBtn = findViewById<View>(R.id.get_performance_data_btn)
        btn.setOnClickListener {
            val pageData = org.json.JSONObject()
            KuiklyRenderActivity.start(MainActivity@this, "AppTabPage", pageData)
            MainActivity@this.overridePendingTransition(R.anim.slide_from_bottom, 0)
        }
        performanceBtn.setOnClickListener {
            val performanceData = MainActivity@this.getSharedPreferences("performance", MODE_PRIVATE).getString("PerformanceData", "")
            findViewById<TextView>(R.id.get_performance_data_tv).text = performanceData

        }
    }

}
