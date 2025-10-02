#!/bin/bash
# resize_images.sh - 裁剪和调整图片尺寸脚本

echo "🖼️  Image Resizing Script"
echo "======================="

# 检查是否安装了ImageMagick
if ! command -v convert &> /dev/null; then
    echo "❌ ImageMagick not found. Please install it first:"
    echo "   macOS: brew install imagemagick"
    echo "   Ubuntu: sudo apt-get install imagemagick"
    exit 1
fi

# 进入demo-videos目录
cd demo-videos

echo "📏 Getting image dimensions..."

# 获取每张图片的尺寸
echo "Original dimensions:"
identify screen1.png | awk '{print "screen1.png: " $3}'
identify screen2.png | awk '{print "screen2.png: " $3}'
identify screen3.png | awk '{print "screen3.png: " $3}'

echo ""
echo "🎯 Resizing images to uniform dimensions..."

# 方案1: 统一裁剪为正方形 (推荐用于截图)
TARGET_SIZE="800x600"

echo "Resizing to ${TARGET_SIZE}..."

# 备份原始文件
cp screen1.png screen1_original.png
cp screen2.png screen2_original.png
cp screen3.png screen3_original.png

# 裁剪并调整大小 (保持比例，居中裁剪)
convert screen1.png -resize "${TARGET_SIZE}^" -gravity center -extent "${TARGET_SIZE}" screen1.png
convert screen2.png -resize "${TARGET_SIZE}^" -gravity center -extent "${TARGET_SIZE}" screen2.png
convert screen3.png -resize "${TARGET_SIZE}^" -gravity center -extent "${TARGET_SIZE}" screen3.png

echo ""
echo "✅ Resizing complete!"
echo "New dimensions:"
identify screen1.png | awk '{print "screen1.png: " $3}'
identify screen2.png | awk '{print "screen2.png: " $3}'
identify screen3.png | awk '{print "screen3.png: " $3}'

echo ""
echo "📁 Original files backed up as:"
echo "   - screen1_original.png"
echo "   - screen2_original.png" 
echo "   - screen3_original.png"

echo ""
echo "🎉 Done! All images now have uniform dimensions."
