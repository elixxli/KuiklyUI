# 1.记录原始url
ORIGIN_DISTRIBUTION_URL=$(grep "distributionUrl" gradle/wrapper/gradle-wrapper.properties | cut -d "=" -f 2)
echo "origin gradle url: $ORIGIN_DISTRIBUTION_URL"
# 2.切换gradle版本
NEW_DISTRIBUTION_URL="https\:\/\/services.gradle.org\/distributions\/gradle-7.5.1-bin.zip"
sed -i.bak "s/distributionUrl=.*$/distributionUrl=$NEW_DISTRIBUTION_URL/" gradle/wrapper/gradle-wrapper.properties

# 3. 修改 gradle.properties，关闭 androidx
sed -i.bak -e "s/kotlin.mpp.enableGranularSourceSetsMetadata=true//g" -e "s/kotlin.native.enableDependencyPropagation=false//g" -e '$ a\
#\
#kotlin.mpp.applyDefaultHierarchyTemplate=false' gradle.properties

# 4. 解决语法问题
ios_main_dir="core/src/iosMain/kotlin/com/tencent/kuikly"

ios_platform_impl="$ios_main_dir/core/module/PlatformImp.kt"
sed -i.bak '1i\
@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
' "$ios_platform_impl"

ios_exception_tracker="$ios_main_dir/core/exception/ExceptionTracker.kt"
sed -i.bak -e '1i\
@file:OptIn(kotlin.experimental.ExperimentalNativeApi::class)
' -e "s/import kotlin.native.concurrent.AtomicReference/import kotlin.concurrent.AtomicReference/g" "$ios_exception_tracker"

# 5. 开始发布
KUIKLY_AGP_VERSION="7.4.2" KUIKLY_KOTLIN_VERSION="1.9.22" ./gradlew -c settings.1.9.22.gradle.kts :core-annotations:publishToMavenLocal --stacktrace
KUIKLY_AGP_VERSION="7.4.2" KUIKLY_KOTLIN_VERSION="1.9.22" ./gradlew -c settings.1.9.22.gradle.kts :core-ksp:publishToMavenLocal --stacktrace
KUIKLY_AGP_VERSION="7.4.2" KUIKLY_KOTLIN_VERSION="1.9.22" ./gradlew -c settings.1.9.22.gradle.kts :core:publishToMavenLocal --stacktrace
KUIKLY_AGP_VERSION="7.4.2" KUIKLY_KOTLIN_VERSION="1.9.22" ./gradlew -c settings.1.9.22.gradle.kts :core-render-android:publishToMavenLocal --stacktrace

#  6. 还原其他文件
mv gradle/wrapper/gradle-wrapper.properties.bak gradle/wrapper/gradle-wrapper.properties
mv gradle.properties.bak gradle.properties
mv "$ios_platform_impl.bak" "$ios_platform_impl"
mv "$ios_exception_tracker.bak" "$ios_exception_tracker"