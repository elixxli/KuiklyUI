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

package com.tencent.kuikly.core.base

class Animation {
    private lateinit var timingFuncType : TimingFuncType
    private var duration : Float = 0f
    private var animationType= AnimationType.PLAIN
    private var damping : Float = 0f
    private var velocity : Float = 0f
    private var delay: Float = 0f
    private var repeatForever: Boolean = false

    var key: String = ""

    fun delay(time: Float): Animation {
        delay = time
        return this
    }

    fun repeatForever(forever: Boolean): Animation {
        repeatForever = forever
        return this
    }

    override fun toString(): String {
        return "${animationType.ordinal} ${timingFuncType.ordinal} $duration $damping $velocity $delay ${repeatForever.toInt()} $key"
    }

    companion object {

        /*
         * @param durationS 动画持续时间，单位秒
         * @param key 业务方设置，动画结束回调会回传，用于区分是哪个动画结束
         */
        fun linear(durationS: Float, key: String = ""): Animation {
            return create(AnimationType.PLAIN, TimingFuncType.LINEAR, durationS, key = key)
        }

        /*
         * @param durationS 动画持续时间，单位秒
         * @param key 业务方设置，动画结束回调会回传，用于区分是哪个动画结束
         */
        fun easeIn(durationS: Float, key: String = ""): Animation {
            return create(AnimationType.PLAIN, TimingFuncType.EASEIN, durationS, key = key)
        }

        /*
         * @param durationS 动画持续时间，单位秒
         * @param key 业务方设置，动画结束回调会回传，用于区分是哪个动画结束
         */
        fun easeOut(durationS: Float, key: String = ""): Animation {
            return create(AnimationType.PLAIN, TimingFuncType.EASEOUT, durationS, key = key)
        }

        /*
         * @param durationS 动画持续时间，单位秒
         * @param key 业务方设置，动画结束回调会回传，用于区分是哪个动画结束
         */
        fun easeInOut(durationS: Float, key: String = ""): Animation {
            return create(AnimationType.PLAIN, TimingFuncType.EASEINOUT, durationS, key = key)
        }

        fun springLinear(durationS: Float, damping: Float, velocity: Float, key: String = ""): Animation {
            return createSpring(TimingFuncType.LINEAR, durationS, damping, velocity, key = key)
        }

        fun springEaseIn(durationS: Float, damping: Float, velocity: Float, key: String = ""): Animation {
            return createSpring(TimingFuncType.EASEIN, durationS, damping, velocity, key = key)
        }

        fun springEaseOut(durationS: Float, damping: Float, velocity: Float, key: String = ""): Animation {
            return createSpring(TimingFuncType.EASEOUT, durationS, damping, velocity, key = key)
        }

        fun springEaseInOut(durationS: Float, damping: Float, velocity: Float, key: String = ""): Animation {
            return createSpring(TimingFuncType.EASEINOUT, durationS, damping, velocity, key = key)
        }

        private fun create(animationType: AnimationType, timingFuncType: TimingFuncType, durationS: Float, delay: Float = 0f, repeatForever: Boolean = false, key: String = ""): Animation  {
            val animation = Animation()
            animation.animationType = animationType
            animation.timingFuncType = timingFuncType
            animation.duration = durationS
            animation.delay = delay
            animation.repeatForever = repeatForever
            animation.key = key
            return animation
        }

        private fun createSpring(timingFuncType: TimingFuncType, durationS: Float, damping: Float, velocity: Float, delay: Float = 0f, repeatForever: Boolean = false, key: String = ""): Animation  {
            val animation = Animation()
            animation.animationType = AnimationType.SRPING
            animation.timingFuncType = timingFuncType
            animation.duration = durationS
            animation.damping = damping
            animation.velocity = velocity
            animation.delay = delay
            animation.repeatForever = repeatForever
            animation.key = key
            return animation
        }
    }
}
enum class TimingFuncType(value: Int) {
    LINEAR(0),
    EASEIN(1),
    EASEOUT(2),
    EASEINOUT(3);
    companion object {
        fun fromInt(value: Int): TimingFuncType {
            return values().firstOrNull { it.ordinal == value } ?: LINEAR
        }
    }
}

enum class AnimationType(value: Int) {
    PLAIN(0),
    SRPING(1);
    companion object {
        fun fromInt(value: Int): AnimationType {
            return values().firstOrNull { it.ordinal == value } ?: PLAIN
        }
    }
}