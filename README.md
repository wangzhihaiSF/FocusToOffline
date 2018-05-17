### android:scaleType

 1. android:scaleType="center"
保持原图的大小，显示在ImageView的中心。当原图的size大于ImageView的size，
超过部分裁剪处理。

2. android:scaleType="centerCrop"
以填满整个ImageView为目的，将原图的中心对准ImageView的中心，等比例放大
原图，直到填满ImageView为止（指的是ImageView的宽和高都要填满），原图超
过ImageView的部分作裁剪处理。

3. android:scaleType="centerInside"
以原图完全显示为目的，将图片的内容完整居中显示，通过按比例缩小原图的
size宽(高)等于或小于ImageView的宽(高)。如果原图的size本身就小于
ImageView的size，则原图的size不作任何处理，居中显示在ImageView。

4. android:scaleType="matrix"
不改变原图的大小，从ImageView的左上角开始绘制原图，原图超过ImageView
的部分作裁剪处理。

5. android:scaleType="fitCenter"
把原图按比例扩大或缩小到ImageView的ImageView的高度，居中显示

6. android:scaleType="fitEnd"
把原图按比例扩大(缩小)到ImageView的高度，显示起始于ImageView的右下方

7. android:scaleType="fitStart"
把原图按比例扩大(缩小)到ImageView的高度，显示起始于ImageView的左上方

8. android:scaleType="fitXY"
把原图按照View指定的大小在View中显示，拉伸显示图片，不保持原比例，填满
ImageView.