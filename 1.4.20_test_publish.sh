# 1.记录原始url
ORIGIN_DISTRIBUTION_URL=$(grep "distributionUrl" gradle/wrapper/gradle-wrapper.properties | cut -d "=" -f 2)
echo "origin gradle url: $ORIGIN_DISTRIBUTION_URL"

# 2.切换gradle版本
NEW_DISTRIBUTION_URL="https\:\/\/services.gradle.org\/distributions\/gradle-7.3.3-bin.zip"
sed -i.bak "s/distributionUrl=.*$/distributionUrl=$NEW_DISTRIBUTION_URL/" gradle/wrapper/gradle-wrapper.properties

# 3. 解决语法问题
ios_main_dir="core/src/iosMain/kotlin/com/tencent/kuikly"

ios_platform_impl="$ios_main_dir/core/module/PlatformImp.kt"
sed -i.bak '/@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)/d' "$ios_platform_impl"

ios_exception_tracker="$ios_main_dir/core/exception/ExceptionTracker.kt"
sed -i.bak \
    -e '/@file:OptIn(kotlin\.experimental\.ExperimentalNativeApi::class)/d' \
    -e 's/import kotlin\.concurrent\.AtomicReference/import kotlin.native.concurrent.AtomicReference/g' \
    "$ios_exception_tracker"

./gradlew --stop
KUIKLY_AGP_VERSION="4.2.1" KUIKLY_KOTLIN_VERSION="1.4.20" ./gradlew -c settings.1.4.20.gradle.kts :core-annotations:publishToMavenLocal --stacktrace
KUIKLY_AGP_VERSION="4.2.1" KUIKLY_KOTLIN_VERSION="1.4.20" ./gradlew -c settings.1.4.20.gradle.kts :core-kapt:publishToMavenLocal --stacktrace
KUIKLY_AGP_VERSION="4.2.1" KUIKLY_KOTLIN_VERSION="1.4.20" ./gradlew -c settings.1.4.20.gradle.kts :core:publishToMavenLocal --stacktrace
KUIKLY_AGP_VERSION="4.2.1" KUIKLY_KOTLIN_VERSION="1.4.20" ./gradlew -c settings.1.4.20.gradle.kts :core-render-android:publishToMavenLocal --stacktrace

# 5. 还原其他文件
mv gradle/wrapper/gradle-wrapper.properties.bak gradle/wrapper/gradle-wrapper.properties
mv "$ios_platform_impl.bak" "$ios_platform_impl"
mv "$ios_exception_tracker.bak" "$ios_exception_tracker"