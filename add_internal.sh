#!/bin/bash

process_add_internal() {
  echo "Processing $1"
  # 读取文件内容
  CONTENT=$(cat "$1")
  # 使用正则表达式查找类定义，并在没有 internal、private 或 protected 的类、枚举、单例、抽象类和接口前添加 internal，同时避免在已经有 ::class 的地方添加，并保留文件末尾的换行符
  NEW_CONTENT=$(echo "$CONTENT" | perl -pe 's/^(?:(?!(internal|private|protected|::))\b\s)*(sealed\s+)?(data\s+)?(enum\s+)?(abstract\s+)?(object\s+)?(class|interface)\s+(\w+)/internal $1$2$3$4$5$6$7 $8/g;
s/^(?:(?!(internal|private|protected|::))\b\s)*fun\s+/internal fun /g;
s/^(?:(?!(internal|private|protected|::))\b\s)*val\s+/internal val /g;
s/^(?:(?!(internal|private|protected|::))\b\s)*object\s+/internal object /g;

s/^(?:(?!(internal|private|protected|::))\b\s)*public fun\s+/internal fun /g;
s/^(?:(?!(internal|private|protected|::))\b\s)*public data class\s+/internal data class /g;

s/^(?:(?!(internal|private|protected|::))\b\s)*typealias\s+/internal typealias /g;


s/^(?:(?!(internal|private|protected|::))\b\s)*abstract open class\s+/internal abstract open class /g;

s/^(?:(?!(internal|private|protected|::))\b\s)*open class\s+/internal open class /g;
s/^(?:(?!(internal|private|protected|::))\b\s)*const val\s+/internal const val /g;
s/^(.*?)\bpublic\s+class\b(.*)$/$1internal class$2/g;
s/^(.*?)\bpublic\s+object\b(.*)$/$1internal object$2/g;

s/^(?:(?!(internal|private|protected|::))\b\s)*public interface\s+/internal interface /g;

s/^(.*?)\bpublic\s+public companion object\b(.*)$/$1internal companion object$2/g;
')
  NEW_CONTENT=$(echo "$NEW_CONTENT" | perl -pe 's/\n?$/\n/;')
  # 将修改后的内容写回文件
  echo "$NEW_CONTENT" > "$FILE"
}
process_add_dir_internal() {
  # 查找所有的 Kotlin 文件
  KOTLIN_FILES=$(find "$1" -type f -name "*.kt")
  for FILE in $KOTLIN_FILES; do
    process_add_internal "$FILE"
  done
}

# 修改此变量以指向你的模块目录
MODULE_DIR="./demo/src/commonMain"
process_add_dir_internal "$MODULE_DIR"

echo "All done!"