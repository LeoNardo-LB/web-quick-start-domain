#!/bin/bash
#
# Spec 覆盖率检查脚本
# 用途：验证 openspec/specs/ 下的规范是否有对应的实现记录
#
# 使用方法：
#   ./scripts/check-spec-coverage.sh [--verbose]
#
# 退出码：
#   0 - 所有 spec 都有覆盖
#   1 - 存在未覆盖的 spec
#

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
SPECS_DIR="$PROJECT_ROOT/openspec/specs"
ARCHIVE_DIR="$PROJECT_ROOT/openspec/changes/archive"
VERBOSE=false

# 解析参数
while [[ $# -gt 0 ]]; do
    case $1 in
        --verbose|-v)
            VERBOSE=true
            shift
            ;;
        *)
            echo "Usage: $0 [--verbose]"
            exit 1
            ;;
    esac
done

echo "=========================================="
echo "  Spec 覆盖率检查"
echo "=========================================="
echo ""

# 统计变量
TOTAL_SPECS=0
COVERED_SPECS=0
UNCOVERED_SPECS=()

echo "📋 检查 specs 目录: $SPECS_DIR"
echo ""

# 遍历所有 spec.md 文件
for spec_file in "$SPECS_DIR"/*/spec.md; do
    if [[ ! -f "$spec_file" ]]; then
        continue
    fi
    
    TOTAL_SPECS=$((TOTAL_SPECS + 1))
    spec_name=$(basename "$(dirname "$spec_file")")
    
    # 检查是否有对应的归档变更
    found_coverage=false
    
    # 方法1：检查 archive 中是否有同名目录
    if find "$ARCHIVE_DIR" -type d -name "*$spec_name*" 2>/dev/null | grep -q .; then
        found_coverage=true
    fi
    
    # 方法2：检查 archive 中的 specs 子目录
    if find "$ARCHIVE_DIR" -path "*/specs/$spec_name" -type d 2>/dev/null | grep -q .; then
        found_coverage=true
    fi
    
    # 方法3：检查 archive 中是否有引用此 spec 的文件
    if grep -r "$spec_name" "$ARCHIVE_DIR" --include="*.md" 2>/dev/null | grep -q .; then
        found_coverage=true
    fi
    
    if $found_coverage; then
        COVERED_SPECS=$((COVERED_SPECS + 1))
        if $VERBOSE; then
            echo "  ✅ $spec_name - 已覆盖"
        fi
    else
        UNCOVERED_SPECS+=("$spec_name")
        echo "  ❌ $spec_name - 未覆盖"
    fi
done

echo ""
echo "=========================================="
echo "  检查结果"
echo "=========================================="
echo ""
echo "  总 Spec 数: $TOTAL_SPECS"
echo "  已覆盖:     $COVERED_SPECS"
echo "  未覆盖:     ${#UNCOVERED_SPECS[@]}"
echo ""

# 计算覆盖率
if [[ $TOTAL_SPECS -gt 0 ]]; then
    COVERAGE_PERCENT=$((COVERED_SPECS * 100 / TOTAL_SPECS))
    echo "  覆盖率:     ${COVERAGE_PERCENT}%"
else
    echo "  覆盖率:     N/A (无 spec 文件)"
fi

echo ""

# 输出未覆盖的 spec 列表
if [[ ${#UNCOVERED_SPECS[@]} -gt 0 ]]; then
    echo "⚠️  未覆盖的 Spec 列表:"
    for spec in "${UNCOVERED_SPECS[@]}"; do
        echo "    - $spec"
    done
    echo ""
    echo "💡 建议：使用 /opsx-new 创建对应的变更记录"
    exit 1
else
    echo "✅ 所有 Spec 都有对应的实现记录！"
    exit 0
fi
