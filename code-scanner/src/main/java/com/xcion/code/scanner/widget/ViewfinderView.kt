package com.xcion.code.scanner.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import androidx.annotation.*
import com.xcion.code.scanner.R


/**
 * @date: 2024/1/22
 * @Description: Camera 取景器
 */
class ViewfinderView(
    context: Context?,
    @Nullable attrs: AttributeSet?,
    defStyleAttr: Int
) : View(context, attrs, defStyleAttr) {

    /**
     * 默认范围比例，之所以默认为 1.2 是因为内切圆半径和外切圆半径之和的二分之一（即：（1 + √2) / 2 ≈ 1.2）
     */
    private val DEFAULT_RANGE_RATIO = 1.2f

    /**
     * 最大缩放比例
     */
    private val MAX_ZOOM_RATIO = 1.2f

    /**
     * 动画间隔
     */
    private val POINT_ANIMATION_INTERVAL = 3000

    /**
     * 画笔
     */
    private var paint: Paint? = null

    /**
     * 文本画笔
     */
    private var textPaint: TextPaint? = null

    /**
     * 扫描框外面遮罩颜色
     */
    private var maskColor = 0

    /**
     * 扫描区域边框颜色
     */
    private var frameColor = 0

    /**
     * 扫描线颜色
     */
    private var laserColor = 0

    /**
     * 扫描框四角颜色
     */
    private var frameCornerColor = 0

    /**
     * 提示文本与扫描框的边距
     */
    private var labelTextPadding = 0f

    /**
     * 提示文本的宽度
     */
    private var labelTextWidth = 0

    /**
     * 提示文本的位置
     */
    private var labelTextLocation: TextLocation? = null

    /**
     * 扫描区域提示文本
     */
    private var labelText: String? = null

    /**
     * 扫描区域提示文本颜色
     */
    private var labelTextColor = 0

    /**
     * 提示文本字体大小
     */
    private var labelTextSize = 0f

    /**
     * 扫描线开始位置
     */
    private var scannerStart = 0

    /**
     * 扫描线结束位置
     */
    private var scannerEnd = 0

    /**
     * 扫描框宽
     */
    private var frameWidth = 0

    /**
     * 扫描框高
     */
    private var frameHeight = 0

    /**
     * 激光扫描风格
     */
    private var laserStyle: LaserStyle? = null

    /**
     * 网格列数
     */
    private var laserGridColumn = 0

    /**
     * 网格高度
     */
    private var laserGridHeight = 0

    /**
     * 扫描框
     */
    private var frame: Rect? = null

    /**
     * 扫描区边角的宽
     */
    private var frameCornerStrokeWidth = 0

    /**
     * 扫描区边角的高
     */
    private var frameCornerSize = 0

    /**
     * 扫描线每次移动距离
     */
    private var laserMovementSpeed = 0

    /**
     * 扫描线高度
     */
    private var laserLineHeight = 0

    /**
     * 扫描动画延迟间隔时间 默认20毫秒
     */
    private var laserAnimationInterval = 0L

    /**
     * 边框线宽度
     */
    private var frameLineStrokeWidth = 0

    /**
     * 扫描框占比
     */
    private var frameRatio = 0f

    /**
     * 扫描框内间距
     */
    private var framePaddingLeft = 0f
    private var framePaddingTop = 0f
    private var framePaddingRight = 0f
    private var framePaddingBottom = 0f

    /**
     * 扫描框对齐方式
     */
    private var frameGravity: FrameGravity? = null

    private var frameBitmap: Bitmap? = null

    /**
     * 结果点颜色
     */
    private var pointColor = 0

    /**
     * 结果点描边颜色
     */
    private var pointStrokeColor = 0
    private var pointBitmap: Bitmap? = null
    private var isPointAnimation = true

    /**
     * 结果点动画间隔时间
     */
    private var pointAnimationInterval = 0L

    /**
     * 结果点半径
     */
    private var pointRadius = 0f

    /**
     * 结果点外圈描边的半径与结果点半径的比例
     */
    private var pointStrokeRatio = 0f

    /**
     * 设置结果点外圈描边的半径
     */
    private var pointStrokeRadius = 0f

    /**
     * 当前缩放比例
     */
    private var currentZoomRatio = 1.0f

    /**
     * 最后一次缩放比例（即上一次缩放比例）
     */
    private var lastZoomRatio = 0f

    /**
     * 缩放速度
     */
    private var zoomSpeed = 0.02f

    private var zoomCount = 0

    /**
     * 结果点有效点击范围半径
     */
    private var pointRangeRadius = 0f

    private var laserBitmap: Bitmap? = null

    private var laserBitmapRatio = 0f

    private var laserBitmapWidth = 0f

    private var viewfinderStyle = ViewfinderStyle.CLASSIC

    private var pointList: List<Point>? = null

    private var isShowPoints = false

    private var minDimension = 0

    private var onItemClickListener: OnItemClickListener? = null

    private var gestureDetector: GestureDetector? = null


    constructor(context: Context?, @Nullable attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context?) : this(context, null)

    init {
        context?.let { init(it, attrs) }
    }


    /**
     * 取景框样式
     */
    @IntDef(ViewfinderStyle.CLASSIC, ViewfinderStyle.POPULAR)
    @Retention(AnnotationRetention.SOURCE)
    annotation class ViewfinderStyle {
        companion object {
            /**
             * 经典样式：经典的扫描风格（带扫描框）
             */
            const val CLASSIC = 0

            /**
             * 流行样式：类似于新版的微信全屏扫描（不带扫描框）
             */
            const val POPULAR = 1
        }
    }

    /**
     * 扫描线样式
     */
    enum class LaserStyle(val mValue: Int) {
        /**
         * 无
         */
        NONE(0),

        /**
         * 线条样式
         */
        LINE(1),

        /**
         * 网格样式
         */
        GRID(2),

        /**
         * 图片样式
         */
        IMAGE(3);

        companion object {
            fun getFromInt(value: Int): LaserStyle {
                for (style in values()) {
                    if (style.mValue == value) {
                        return style
                    }
                }
                return LINE
            }
        }
    }

    /**
     * 文字位置
     */
    enum class TextLocation(private val mValue: Int) {
        TOP(0), BOTTOM(1);

        companion object {
            fun getFromInt(value: Int): TextLocation {
                for (location in values()) {
                    if (location.mValue == value) {
                        return location
                    }
                }
                return TOP
            }
        }
    }

    /**
     * 扫描框对齐方式
     */
    enum class FrameGravity(val mValue: Int) {
        CENTER(0), LEFT(1), TOP(2), RIGHT(3), BOTTOM(4);

        companion object {
            fun getFromInt(value: Int): FrameGravity {
                for (gravity in values()) {
                    if (gravity.mValue == value) {
                        return gravity
                    }
                }
                return CENTER
            }
        }
    }

    /**
     * 初始化
     *
     * @param context
     * @param attrs
     */
    private fun init(context: Context, attrs: AttributeSet?) {
        // 初始化自定义属性信息
        val array: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.ViewfinderView)
        val displayMetrics = resources.displayMetrics
        viewfinderStyle =
            array.getInt(R.styleable.ViewfinderView_vfvViewfinderStyle, ViewfinderStyle.CLASSIC)
        maskColor = array.getColor(
            R.styleable.ViewfinderView_vfvMaskColor,
            getColor(context, R.color.code_scanner_viewfinder_mask)
        )
        frameColor = array.getColor(
            R.styleable.ViewfinderView_vfvFrameColor,
            getColor(context, R.color.code_scanner_viewfinder_frame)
        )
        frameWidth = array.getDimensionPixelSize(R.styleable.ViewfinderView_vfvFrameWidth, 0)
        frameHeight = array.getDimensionPixelSize(R.styleable.ViewfinderView_vfvFrameHeight, 0)
        frameRatio = array.getFloat(R.styleable.ViewfinderView_vfvFrameRatio, 0.625f)
        frameLineStrokeWidth = array.getDimension(
            R.styleable.ViewfinderView_vfvFrameLineStrokeWidth,
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, displayMetrics)
        ).toInt()
        framePaddingLeft = array.getDimension(R.styleable.ViewfinderView_vfvFramePaddingLeft, 0f)
        framePaddingTop = array.getDimension(R.styleable.ViewfinderView_vfvFramePaddingTop, 0f)
        framePaddingRight = array.getDimension(R.styleable.ViewfinderView_vfvFramePaddingRight, 0f)
        framePaddingBottom = array.getDimension(R.styleable.ViewfinderView_vfvFramePaddingBottom, 0f)
        frameGravity = FrameGravity.getFromInt(
            array.getInt(
                R.styleable.ViewfinderView_vfvFrameGravity,
                FrameGravity.CENTER.mValue
            )
        )
        frameCornerColor = array.getColor(
            R.styleable.ViewfinderView_vfvFrameCornerColor,
            getColor(context, R.color.code_scanner_viewfinder_corner)
        )
        frameCornerSize = array.getDimension(
            R.styleable.ViewfinderView_vfvFrameCornerSize,
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, displayMetrics)
        ).toInt()
        frameCornerStrokeWidth = array.getDimension(
            R.styleable.ViewfinderView_vfvFrameCornerStrokeWidth,
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, displayMetrics)
        ).toInt()
        val frameDrawable = array.getDrawable(R.styleable.ViewfinderView_vfvFrameDrawable)
        laserLineHeight = array.getDimension(
            R.styleable.ViewfinderView_vfvLaserLineHeight,
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5f, displayMetrics)
        ).toInt()
        laserMovementSpeed = array.getDimension(
            R.styleable.ViewfinderView_vfvLaserMovementSpeed,
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, displayMetrics)
        ).toInt()
        laserAnimationInterval =
            array.getInteger(R.styleable.ViewfinderView_vfvLaserAnimationInterval, 20).toLong()
        laserGridColumn = array.getInt(R.styleable.ViewfinderView_vfvLaserGridColumn, 20)
        laserGridHeight = array.getDimension(
            R.styleable.ViewfinderView_vfvLaserGridHeight,
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40f, displayMetrics)
        ).toInt()
        laserColor = array.getColor(
            R.styleable.ViewfinderView_vfvLaserColor,
            getColor(context, R.color.code_scanner_viewfinder_laser)
        )
        laserStyle = LaserStyle.getFromInt(
            array.getInt(
                R.styleable.ViewfinderView_vfvLaserStyle,
                LaserStyle.LINE.mValue
            )
        )
        laserBitmapRatio = array.getFloat(R.styleable.ViewfinderView_vfvLaserDrawableRatio, 0.625f)
        val laserDrawable = array.getDrawable(R.styleable.ViewfinderView_vfvLaserDrawable)
        labelText = array.getString(R.styleable.ViewfinderView_vfvLabelText)
        labelTextColor = array.getColor(
            R.styleable.ViewfinderView_vfvLabelTextColor,
            getColor(context, R.color.code_scanner_viewfinder_label_text)
        )
        labelTextSize = array.getDimension(
            R.styleable.ViewfinderView_vfvLabelTextSize,
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14f, displayMetrics)
        )
        labelTextPadding = array.getDimension(
            R.styleable.ViewfinderView_vfvLabelTextPadding,
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f, displayMetrics)
        )
        labelTextWidth = array.getDimensionPixelSize(R.styleable.ViewfinderView_vfvLabelTextWidth, 0)
        labelTextLocation =
            TextLocation.getFromInt(array.getInt(R.styleable.ViewfinderView_vfvLabelTextLocation, 0))
        pointColor = array.getColor(
            R.styleable.ViewfinderView_vfvPointColor,
            getColor(context, R.color.code_scanner_viewfinder_point)
        )
        pointStrokeColor = array.getColor(
            R.styleable.ViewfinderView_vfvPointStrokeColor,
            getColor(context, R.color.code_scanner_viewfinder_point_stroke)
        )
        pointRadius = array.getDimension(
            R.styleable.ViewfinderView_vfvPointRadius,
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15f, displayMetrics)
        )
        pointStrokeRatio =
            array.getFloat(R.styleable.ViewfinderView_vfvPointStrokeRatio, DEFAULT_RANGE_RATIO)
        val pointDrawable = array.getDrawable(R.styleable.ViewfinderView_vfvPointDrawable)
        isPointAnimation = array.getBoolean(R.styleable.ViewfinderView_vfvPointAnimation, true)
        pointAnimationInterval = array.getInt(
            R.styleable.ViewfinderView_vfvPointAnimationInterval,
            POINT_ANIMATION_INTERVAL
        ).toLong()
        array.recycle()
        if (frameDrawable != null) {
            frameBitmap = getBitmapFormDrawable(frameDrawable)
        }
        if (laserDrawable != null) {
            laserBitmap = getBitmapFormDrawable(laserDrawable)
        }
        if (pointDrawable != null) {
            pointBitmap = getBitmapFormDrawable(pointDrawable)
            pointRangeRadius =
                (pointBitmap!!.width + pointBitmap!!.height) / 4 * DEFAULT_RANGE_RATIO
        } else {
            pointStrokeRadius = pointRadius * pointStrokeRatio
            pointRangeRadius = pointStrokeRadius * DEFAULT_RANGE_RATIO
        }
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint?.isAntiAlias = true
        textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        gestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                return if (isShowPoints && checkSingleTap(e.x, e.y)) {
                    true
                } else super.onSingleTapUp(e)
            }
        })


    }

    /**
     * 获取颜色
     *
     * @param context
     * @param id
     * @return
     */
    private fun getColor(context: Context, @ColorRes id: Int): Int {
        return if (Build.VERSION.SDK_INT >= 23) {
            context.getColor(id)
        } else {
            context.resources.getColor(id, null)
        }
    }

    /**
     * 根据 drawable 获取对应的 bitmap
     *
     * @param drawable
     * @return
     */
    private fun getBitmapFormDrawable(drawable: Drawable): Bitmap? {
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            if (drawable.opacity != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, bitmap.width, bitmap.height)
        drawable.draw(canvas)
        return bitmap
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        initFrame(width, height)
    }

    /**
     * 缩放扫描线位图
     */
    private fun scaleLaserBitmap() {
        if (laserBitmap != null && laserBitmapWidth > 0) {
            val ratio = laserBitmapWidth / laserBitmap!!.width
            val matrix = Matrix()
            matrix.postScale(ratio, ratio)
            val w = laserBitmap!!.width
            val h = laserBitmap!!.height
            laserBitmap = Bitmap.createBitmap(laserBitmap!!, 0, 0, w, h, matrix, true)
        }
    }

    /**
     * 初始化扫描框
     *
     * @param width
     * @param height
     */
    private fun initFrame(width: Int, height: Int) {
        minDimension = Math.min(width, height)
        val size = (minDimension * frameRatio).toInt()
        if (laserBitmapWidth <= 0) {
            laserBitmapWidth = minDimension * laserBitmapRatio
            scaleLaserBitmap()
        }
        if (frameWidth <= 0 || frameWidth > width) {
            frameWidth = size
        }
        if (frameHeight <= 0 || frameHeight > height) {
            frameHeight = size
        }
        if (labelTextWidth <= 0) {
            labelTextWidth = width - paddingLeft - paddingRight
        }
        var leftOffsets = (width - frameWidth) / 2 + framePaddingLeft - framePaddingRight
        var topOffsets = (height - frameHeight) / 2 + framePaddingTop - framePaddingBottom
        if (frameGravity == FrameGravity.LEFT) leftOffsets = framePaddingLeft
        else if (frameGravity == FrameGravity.TOP) topOffsets = framePaddingTop
        else if (frameGravity == FrameGravity.RIGHT) leftOffsets =
            width - frameWidth + framePaddingRight
        else if (frameGravity == FrameGravity.BOTTOM) topOffsets =
            height - frameHeight + framePaddingBottom
        frame = Rect(
            leftOffsets.toInt(),
            topOffsets.toInt(),
            leftOffsets.toInt() + frameWidth,
            topOffsets.toInt() + frameHeight
        )
    }

    override fun onDraw(canvas: Canvas) {
        if (isShowPoints) {
            // 显示结果点
            drawMask(canvas, width, height)
            drawResultPoints(canvas, pointList)
            if (isPointAnimation) {
                // 显示动画并且结果点标记的图片为空时，支持缩放动画
                calcPointZoomAnimation()
            }
            return
        }
        if (frame == null) {
            return
        }
        if (scannerStart == 0 || scannerEnd == 0) {
            scannerStart = frame?.top!!
            scannerEnd = frame?.bottom!! - laserLineHeight
        }

        // CLASSIC样式：经典样式（带扫描框）
        if (viewfinderStyle == ViewfinderStyle.CLASSIC) {
            // 绘制模糊区域
            drawExterior(canvas, frame!!, width, height)
            // 绘制扫描动画
            drawLaserScanner(canvas, frame!!)
            // 绘制取景区域框
            drawFrame(canvas, frame!!)
            // 绘制提示信息
            drawTextInfo(canvas, frame!!)
            // 间隔更新取景区域
            postInvalidateDelayed(
                laserAnimationInterval.toLong(),
                frame?.left!!,
                frame?.top!!,
                frame?.right!!,
                frame?.bottom!!
            )
        } else if (viewfinderStyle == ViewfinderStyle.POPULAR) {
            // POPULAR样式：类似于新版的微信全屏扫描（不带扫描框）
            // 绘制扫描动画
            drawLaserScanner(canvas, frame!!)
            // 绘制提示信息
            drawTextInfo(canvas, frame!!)
            postInvalidateDelayed(laserAnimationInterval.toLong())
        }
    }

    /**
     * 绘制文本
     *
     * @param canvas
     * @param frame
     */
    private fun drawTextInfo(canvas: Canvas, frame: Rect) {
        if (!TextUtils.isEmpty(labelText)) {
            textPaint!!.color = labelTextColor
            textPaint!!.textSize = labelTextSize
            textPaint!!.textAlign = Paint.Align.CENTER
            val staticLayout = StaticLayout(
                labelText,
                textPaint,
                labelTextWidth,
                Layout.Alignment.ALIGN_NORMAL,
                1.2f,
                0.0f,
                true
            )
            if (labelTextLocation == TextLocation.BOTTOM) {
                canvas.translate(
                    (frame.left + frame.width() / 2).toFloat(),
                    frame.bottom + labelTextPadding
                )
            } else {
                canvas.translate(
                    (frame.left + frame.width() / 2).toFloat(),
                    frame.top - labelTextPadding - staticLayout.height
                )
            }
            staticLayout.draw(canvas)
        }
    }

    /**
     * 绘制边角
     *
     * @param canvas
     * @param frame
     */
    private fun drawCorner(canvas: Canvas, frame: Rect) {
        paint?.color = frameCornerColor
        // 左上
        paint?.let {
            canvas.drawRect(
                frame.left.toFloat(),
                frame.top.toFloat(),
                (frame.left + frameCornerStrokeWidth).toFloat(),
                (frame.top + frameCornerSize).toFloat(),
                it
            )
        }
        paint?.let {
            canvas.drawRect(
                frame.left.toFloat(),
                frame.top.toFloat(),
                (frame.left + frameCornerSize).toFloat(),
                (frame.top + frameCornerStrokeWidth).toFloat(),
                it
            )
        }
        // 右上
        paint?.let {
            canvas.drawRect(
                (frame.right - frameCornerStrokeWidth).toFloat(),
                frame.top.toFloat(),
                frame.right.toFloat(),
                (frame.top + frameCornerSize).toFloat(),
                it
            )
        }
        paint?.let {
            canvas.drawRect(
                (frame.right - frameCornerSize).toFloat(),
                frame.top.toFloat(),
                frame.right.toFloat(),
                (frame.top + frameCornerStrokeWidth).toFloat(),
                it
            )
        }
        // 左下
        paint?.let {
            canvas.drawRect(
                frame.left.toFloat(),
                (frame.bottom - frameCornerStrokeWidth).toFloat(),
                (frame.left + frameCornerSize).toFloat(),
                frame.bottom.toFloat(),
                it
            )
        }
        paint?.let {
            canvas.drawRect(
                frame.left.toFloat(),
                (frame.bottom - frameCornerSize).toFloat(),
                (frame.left + frameCornerStrokeWidth).toFloat(),
                frame.bottom.toFloat(),
                it
            )
        }
        // 右下
        paint?.let {
            canvas.drawRect(
                (frame.right - frameCornerStrokeWidth).toFloat(),
                (frame.bottom - frameCornerSize).toFloat(),
                frame.right.toFloat(),
                frame.bottom.toFloat(),
                it
            )
        }
        paint?.let {
            canvas.drawRect(
                (frame.right - frameCornerSize).toFloat(),
                (frame.bottom - frameCornerStrokeWidth).toFloat(),
                frame.right.toFloat(),
                frame.bottom.toFloat(),
                it
            )
        }
    }

    /**
     * 绘制扫描动画
     *
     * @param canvas
     * @param frame
     */
    private fun drawImageScanner(canvas: Canvas, frame: Rect) {
        if (laserBitmap != null) {
            canvas.drawBitmap(
                laserBitmap!!,
                ((width - laserBitmap!!.width) / 2).toFloat(),
                scannerStart.toFloat(),
                paint
            )
            if (scannerStart < scannerEnd) {
                scannerStart += laserMovementSpeed
            } else {
                scannerStart = frame.top
            }
        } else {
            drawLineScanner(canvas, frame)
        }
    }

    /**
     * 绘制激光扫描线
     *
     * @param canvas
     * @param frame
     */
    private fun drawLaserScanner(canvas: Canvas, frame: Rect) {
        if (laserStyle != null) {
            paint?.color = laserColor
            if (laserStyle == LaserStyle.LINE) drawLineScanner(canvas, frame)
            else if (laserStyle == LaserStyle.GRID) drawGridScanner(canvas, frame)
            else if (laserStyle == LaserStyle.IMAGE) drawImageScanner(canvas, frame)
            paint?.shader = null
        }
    }

    /**
     * 绘制线性式扫描
     *
     * @param canvas
     * @param frame
     */
    private fun drawLineScanner(canvas: Canvas, frame: Rect) {
        // 线性渐变
        val linearGradient: LinearGradient = LinearGradient(
            frame.centerX().toFloat(), scannerStart.toFloat(),
            frame.centerX().toFloat(), (scannerStart + laserLineHeight).toFloat(),
            shadeColor(laserColor),
            laserColor,
            Shader.TileMode.MIRROR
        )
        paint?.shader = linearGradient
        if (scannerStart < scannerEnd) {
            // 椭圆
            val rectF = RectF(
                (frame?.left?.plus(frameCornerSize))!!.toFloat(),
                scannerStart.toFloat(),
                (frame?.right?.minus(frameCornerSize))!!.toFloat(),
                (scannerStart + laserLineHeight).toFloat()
            )
            paint?.let { canvas.drawOval(rectF, it) }
            scannerStart += laserMovementSpeed
        } else {
            scannerStart = frame.top
        }
    }

    /**
     * 绘制网格式扫描
     *
     * @param canvas
     * @param frame
     */
    private fun drawGridScanner(canvas: Canvas, frame: Rect) {
        val stroke = 2
        paint?.strokeWidth = stroke.toFloat()
        // 计算Y轴开始位置
        val startY =
            if (laserGridHeight > 0 && scannerStart - frame.top > laserGridHeight) scannerStart - laserGridHeight else frame.top
        val linearGradient: LinearGradient = LinearGradient(
            frame.centerX().toFloat(),
            startY.toFloat(),
            frame.centerX().toFloat(),
            scannerStart.toFloat(),
            intArrayOf(shadeColor(laserColor), laserColor),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        // 给画笔设置着色器
        paint?.shader = linearGradient
        val wUnit: Float = frame.width() * 1.0f / laserGridColumn
        // 遍历绘制网格纵线
        for (i in 1 until laserGridColumn) {
            paint?.let {
                canvas.drawLine(
                    frame.left + i * wUnit,
                    startY.toFloat(),
                    frame.left + i * wUnit,
                    scannerStart.toFloat(),
                    it
                )
            }
        }
        val height =
            if (laserGridHeight > 0 && scannerStart - frame.top > laserGridHeight) laserGridHeight else scannerStart - frame.top

        // 遍历绘制网格横线
        var i = 0
        while (i <= height / wUnit) {
            paint?.let {
                canvas.drawLine(
                    (frame.left + frameLineStrokeWidth).toFloat(),
                    scannerStart - i * wUnit, (frame.right - frameLineStrokeWidth).toFloat(),
                    scannerStart - i * wUnit, it
                )
            }
            i++
        }
        if (scannerStart < scannerEnd) {
            scannerStart += laserMovementSpeed
        } else {
            scannerStart = frame.top
        }
    }

    /**
     * 处理颜色模糊
     *
     * @param color
     * @return
     */
    private fun shadeColor(color: Int): Int {
        val hax = Integer.toHexString(color)
        val result = "01" + hax.substring(2)
        return Integer.valueOf(result, 16)
    }

    /**
     * 绘制扫描区边框
     *
     * @param canvas
     * @param frame
     */
    private fun drawFrame(canvas: Canvas, frame: Rect) {
        paint?.color = frameColor
        if (frameBitmap != null) {
            canvas.drawBitmap(frameBitmap!!, null, frame, paint)
        } else {
            paint?.let {
                canvas.drawRect(
                    frame.left.toFloat(),
                    frame.top.toFloat(),
                    frame.right.toFloat(),
                    (frame.top + frameLineStrokeWidth).toFloat(),
                    it
                )
            }
            paint?.let {
                canvas.drawRect(
                    frame.left.toFloat(),
                    frame.top.toFloat(),
                    (frame.left + frameLineStrokeWidth).toFloat(),
                    frame.bottom.toFloat(),
                    it
                )
            }
            paint?.let {
                canvas.drawRect(
                    (frame.right - frameLineStrokeWidth).toFloat(),
                    frame.top.toFloat(),
                    frame.right.toFloat(),
                    frame.bottom.toFloat(),
                    it
                )
            }
            paint?.let {
                canvas.drawRect(
                    frame.left.toFloat(),
                    (frame.bottom - frameLineStrokeWidth).toFloat(),
                    frame.right.toFloat(),
                    frame.bottom.toFloat(),
                    it
                )
            }
            // 绘制取景区域边角
            drawCorner(canvas, frame)
        }
    }

    /**
     * 绘制模糊区域
     *
     * @param canvas
     * @param frame
     * @param width
     * @param height
     */
    private fun drawExterior(canvas: Canvas, frame: Rect, width: Int, height: Int) {
        if (maskColor != 0) {
            paint?.color = maskColor
            paint?.let { canvas.drawRect(0F, 0F, width.toFloat(), frame.top.toFloat(), it) }
            paint?.let {
                canvas.drawRect(
                    0F, frame.top.toFloat(),
                    frame.left.toFloat(), frame.bottom.toFloat(), it
                )
            }
            paint?.let {
                canvas.drawRect(
                    frame.right.toFloat(), frame.top.toFloat(), width.toFloat(),
                    frame.bottom.toFloat(), it
                )
            }
            paint?.let {
                canvas.drawRect(
                    0F, frame.bottom.toFloat(), width.toFloat(), height.toFloat(),
                    it
                )
            }
        }
    }

    /**
     * 绘制遮罩层
     *
     * @param canvas
     * @param width
     * @param height
     */
    private fun drawMask(canvas: Canvas, width: Int, height: Int) {
        if (maskColor != 0) {
            paint?.color = maskColor
            paint?.let { canvas.drawRect(0F, 0F, width.toFloat(), height.toFloat(), it) }
        }
    }

    /**
     * 根据结果点集合绘制结果点
     *
     * @param canvas
     * @param points
     */
    private fun drawResultPoints(canvas: Canvas, points: List<Point>?) {
        paint?.color = Color.WHITE
        if (points != null) {
            for (point in points) {
                drawResultPoint(canvas, point, currentZoomRatio)
            }
        }
    }

    /**
     * 计算点的缩放动画
     */
    private fun calcPointZoomAnimation() {
        if (currentZoomRatio <= 1f) {
            lastZoomRatio = currentZoomRatio
            currentZoomRatio += zoomSpeed
            if (zoomCount < 2) {
                // 记住缩放回合次数
                zoomCount++
            } else {
                zoomCount = 0
            }
        } else if (currentZoomRatio >= MAX_ZOOM_RATIO) {
            lastZoomRatio = currentZoomRatio
            currentZoomRatio -= zoomSpeed
        } else {
            if (lastZoomRatio > currentZoomRatio) {
                lastZoomRatio = currentZoomRatio
                currentZoomRatio -= zoomSpeed
            } else {
                lastZoomRatio = currentZoomRatio
                currentZoomRatio += zoomSpeed
            }
        }

        // 每间隔3秒触发一套缩放动画，一套动画缩放三个回合(即：每次zoomCount累加到2后重置为0时)
        postInvalidateDelayed((if (zoomCount == 0 && lastZoomRatio == 1f) pointAnimationInterval else laserAnimationInterval * 2))
    }

    /**
     * 绘制结果点
     *
     * @param canvas
     * @param point
     */
    private fun drawResultPoint(canvas: Canvas, point: Point, currentZoomRatio: Float) {
        if (pointBitmap != null) {
            val left: Float = point.x - pointBitmap!!.width / 2.0f
            val top: Float = point.y - pointBitmap!!.height / 2.0f
            if (isPointAnimation) {
                val dstW = Math.round(pointBitmap!!.width * currentZoomRatio)
                val dstH = Math.round(pointBitmap!!.height * currentZoomRatio)
                val dstLeft: Int = point.x - Math.round(dstW / 2.0f)
                val dstTop: Int = point.y - Math.round(dstH / 2.0f)
                val dstRect = Rect(dstLeft, dstTop, dstLeft + dstW, dstTop + dstH)
                canvas.drawBitmap(pointBitmap!!, null, dstRect, paint)
            } else {
                canvas.drawBitmap(pointBitmap!!, left, top, paint)
            }
        } else {
            paint?.color = pointStrokeColor
            paint?.let {
                canvas.drawCircle(
                    point.x.toFloat(), point.y.toFloat(), pointStrokeRadius * currentZoomRatio,
                    it
                )
            }
            paint?.color = pointColor
            paint?.let {
                canvas.drawCircle(
                    point.x.toFloat(), point.y.toFloat(), pointRadius * currentZoomRatio,
                    it
                )
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (isShowPoints) {
            gestureDetector!!.onTouchEvent(event!!)
        }
        return isShowPoints || super.onTouchEvent(event)
    }

    private fun checkSingleTap(x: Float, y: Float): Boolean {
        if (pointList != null) {
            for (i in pointList!!.indices) {
                val point: Point = pointList!![i]
                val distance = getDistance(x, y, point.x, point.y)
                if (distance <= pointRangeRadius) {
                    if (onItemClickListener != null) {
                        onItemClickListener!!.onItemClick(i)
                    }
                    return true
                }
            }
        }
        return true
    }

    /**
     * 获取两点之间的距离
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return
     */
    private fun getDistance(x1: Float, y1: Float, x2: Int, y2: Int): Float {
        return Math.sqrt(Math.pow((x1 - x2).toDouble(), 2.0) + Math.pow((y1 - y2).toDouble(), 2.0))
            .toFloat()
    }

    /**
     * 是否显示结果点
     *
     * @return
     */
    fun isShowPoints(): Boolean {
        return isShowPoints
    }

    /**
     * 显示扫描动画
     */
    fun showScanner() {
        isShowPoints = false
        invalidate()
    }

    /**
     * 显示结果点
     *
     * @param points
     */
    fun showResultPoints(points: List<Point>?) {
        pointList = points
        isShowPoints = true
        zoomCount = 0
        lastZoomRatio = 0f
        currentZoomRatio = 1f
        invalidate()
    }

    /**
     * 设置 扫描区外遮罩的颜色
     *
     * @param maskColor
     */
    fun setMaskColor(maskColor: Int) {
        this.maskColor = maskColor
    }

    /**
     * 设置 扫描区边框的颜色
     *
     * @param frameColor
     */
    fun setFrameColor(frameColor: Int) {
        this.frameColor = frameColor
    }

    /**
     * 设置扫描区激光线的颜色
     *
     * @param laserColor
     */
    fun setLaserColor(laserColor: Int) {
        this.laserColor = laserColor
    }

    /**
     * 设置扫描区边角的颜色
     *
     * @param frameCornerColor
     */
    fun setFrameCornerColor(frameCornerColor: Int) {
        this.frameCornerColor = frameCornerColor
    }

    /**
     * 设置提示文本距离扫描区的间距
     *
     * @param labelTextPadding
     */
    fun setLabelTextPadding(labelTextPadding: Float) {
        this.labelTextPadding = labelTextPadding
    }

    /**
     * 设置提示文本的宽度，默认为View的宽度
     *
     * @param labelTextWidth
     */
    fun setLabelTextWidth(labelTextWidth: Int) {
        this.labelTextWidth = labelTextWidth
    }

    /**
     * 设置提示文本显示位置
     *
     * @param labelTextLocation
     */
    fun setLabelTextLocation(labelTextLocation: TextLocation?) {
        this.labelTextLocation = labelTextLocation
    }

    /**
     * 设置提示文本信息
     *
     * @param labelText
     */
    fun setLabelText(labelText: String?) {
        this.labelText = labelText
    }

    /**
     * 设置提示文本字体颜色
     *
     * @param color
     */
    fun setLabelTextColor(@ColorInt color: Int) {
        labelTextColor = color
    }

    /**
     * 设置提示文本字体颜色
     *
     * @param id
     */
    fun setLabelTextColorResource(@ColorRes id: Int) {
        labelTextColor = getColor(context, id)
    }

    /**
     * 设置提示文本字体大小
     *
     * @param textSize
     */
    fun setLabelTextSize(textSize: Float) {
        labelTextSize = textSize
    }

    /**
     * 设置激光样式
     *
     * @param laserStyle
     */
    fun setLaserStyle(laserStyle: LaserStyle?) {
        this.laserStyle = laserStyle
    }

    /**
     * 设置网格激光扫描列数
     *
     * @param laserGridColumn
     */
    fun setLaserGridColumn(laserGridColumn: Int) {
        this.laserGridColumn = laserGridColumn
    }

    /**
     * 设置网格激光扫描高度，为0时，表示动态铺满
     *
     * @param laserGridHeight
     */
    fun setLaserGridHeight(laserGridHeight: Int) {
        this.laserGridHeight = laserGridHeight
    }

    /**
     * 设置扫描区边角的宽
     *
     * @param frameCornerStrokeWidth
     */
    fun setFrameCornerStrokeWidth(frameCornerStrokeWidth: Int) {
        this.frameCornerStrokeWidth = frameCornerStrokeWidth
    }

    /**
     * 设置扫描区边角的高
     *
     * @param frameCornerSize
     */
    fun setFrameCornerSize(frameCornerSize: Int) {
        this.frameCornerSize = frameCornerSize
    }

    /**
     * 设置激光扫描的速度：即：每次移动的距离
     *
     * @param laserMovementSpeed
     */
    fun setLaserMovementSpeed(laserMovementSpeed: Int) {
        this.laserMovementSpeed = laserMovementSpeed
    }

    /**
     * 设置扫描线高度
     *
     * @param laserLineHeight
     */
    fun setLaserLineHeight(laserLineHeight: Int) {
        this.laserLineHeight = laserLineHeight
    }

    /**
     * 设置边框线宽度
     *
     * @param frameLineStrokeWidth
     */
    fun setFrameLineStrokeWidth(frameLineStrokeWidth: Int) {
        this.frameLineStrokeWidth = frameLineStrokeWidth
    }

    /**
     * 设置扫描框图片
     *
     * @param drawableResId
     */
    fun setFrameDrawable(@DrawableRes drawableResId: Int) {
        var drawable = context.getDrawable(drawableResId)
        setFrameBitmap(drawable?.let { drawableToBitmap(it) })
    }

    /**
     * 设置扫描框图片
     *
     * @param frameBitmap
     */
    fun setFrameBitmap(frameBitmap: Bitmap?) {
        this.frameBitmap = frameBitmap
        invalidate()
    }

    /**
     * 设置扫描动画延迟间隔时间，单位：毫秒
     *
     * @param laserAnimationInterval
     */
    fun setLaserAnimationInterval(laserAnimationInterval: Long) {
        this.laserAnimationInterval = laserAnimationInterval
    }

    /**
     * 设置结果点的颜色
     *
     * @param pointColor
     */
    fun setPointColor(pointColor: Int) {
        this.pointColor = pointColor
    }

    /**
     * 设置结果点描边的颜色
     *
     * @param pointStrokeColor
     */
    fun setPointStrokeColor(pointStrokeColor: Int) {
        this.pointStrokeColor = pointStrokeColor
    }

    /**
     * 设置结果点的半径
     *
     * @param pointRadius
     */
    fun setPointRadius(pointRadius: Float) {
        this.pointRadius = pointRadius
    }

    /**
     * 设置激光扫描自定义图片
     *
     * @param drawableResId
     */
    fun setLaserDrawable(@DrawableRes drawableResId: Int) {
        var drawable = context.getDrawable(drawableResId)
        setLaserBitmap(drawable?.let { drawableToBitmap(it) })
    }

    /**
     * 设置激光扫描自定义图片
     *
     * @param laserBitmap
     */
    fun setLaserBitmap(laserBitmap: Bitmap?) {
        this.laserBitmap = laserBitmap
        scaleLaserBitmap()
    }

    /**
     * 设置结果点图片
     *
     * @param drawableResId
     */
    fun setPointDrawable(@DrawableRes drawableResId: Int) {
        setPointBitmap(BitmapFactory.decodeResource(resources, drawableResId))
    }

    /**
     * 设置结果点图片
     *
     * @param bitmap
     */
    fun setPointBitmap(bitmap: Bitmap?) {
        pointBitmap = bitmap
        pointRangeRadius = (pointBitmap!!.width + pointBitmap!!.height) / 4 * DEFAULT_RANGE_RATIO
    }

    /**
     * 设置点的动画间隔时长；单位：毫秒
     *
     * @param pointAnimationInterval
     */
    fun setPointAnimationInterval(pointAnimationInterval: Long) {
        this.pointAnimationInterval = pointAnimationInterval
    }

    /**
     * 设置取景框样式；支持：classic：经典样式（带扫描框那种）、popular：流行样式（不带扫描框）
     *
     * @param viewfinderStyle
     */
    fun setViewfinderStyle(viewfinderStyle: Int) {
        this.viewfinderStyle = viewfinderStyle
    }

    /**
     * 设置扫描框的宽度
     *
     * @param frameWidth
     */
    fun setFrameWidth(frameWidth: Int) {
        this.frameWidth = frameWidth
    }

    /**
     * 设置扫描框的高度
     *
     * @param frameHeight
     */
    fun setFrameHeight(frameHeight: Int) {
        this.frameHeight = frameHeight
    }

    /**
     * 设置扫描框的与视图宽的占比；默认：0.625
     *
     * @param frameRatio
     */
    fun setFrameRatio(frameRatio: Float) {
        this.frameRatio = frameRatio
    }

    /**
     * 设置扫描框左边的间距
     *
     * @param framePaddingLeft
     */
    fun setFramePaddingLeft(framePaddingLeft: Float) {
        this.framePaddingLeft = framePaddingLeft
    }

    /**
     * 设置扫描框顶部的间距
     *
     * @param framePaddingTop
     */
    fun setFramePaddingTop(framePaddingTop: Float) {
        this.framePaddingTop = framePaddingTop
    }

    /**
     * 设置扫描框右边的间距
     *
     * @param framePaddingRight
     */
    fun setFramePaddingRight(framePaddingRight: Float) {
        this.framePaddingRight = framePaddingRight
    }

    /**
     * 设置扫描框的间距
     *
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    fun setFramePadding(left: Float, top: Float, right: Float, bottom: Float) {
        framePaddingLeft = left
        framePaddingTop = top
        framePaddingRight = right
        framePaddingBottom = bottom
    }

    /**
     * 设置扫描框底部的间距
     *
     * @param framePaddingBottom
     */
    fun setFramePaddingBottom(framePaddingBottom: Float) {
        this.framePaddingBottom = framePaddingBottom
    }

    /**
     * 设置扫描框的对齐方式；默认居中对齐；即：[FrameGravity.CENTER]
     *
     * @param frameGravity
     */
    fun setFrameGravity(frameGravity: FrameGravity?) {
        this.frameGravity = frameGravity
    }

    /**
     * 设置是否显示结果点缩放动画；默认为：true
     *
     * @param pointAnimation
     */
    fun setPointAnimation(pointAnimation: Boolean) {
        isPointAnimation = pointAnimation
    }

    /**
     * 设置结果点外圈描边的半径；默认为：[.pointRadius] 的 [.pointStrokeRatio] 倍
     *
     * @param pointStrokeRadius
     */
    fun setPointStrokeRadius(pointStrokeRadius: Float) {
        this.pointStrokeRadius = pointStrokeRadius
    }

    /**
     * 设置显示结果点动画的缩放速度；默认为：0.02 / [.laserAnimationInterval]
     *
     * @param zoomSpeed
     */
    fun setZoomSpeed(zoomSpeed: Float) {
        this.zoomSpeed = zoomSpeed
    }

    /**
     * 设置结果点有效点击范围半径；默认为：[.pointStrokeRadius] 的 [.DEFAULT_RANGE_RATIO] 倍；
     * 需要注意的是，因为有效点击范围是建立在结果点的基础之上才有意义的；其主要目的是为了支持一定的容错范围；所以如果在此方法之后；
     * 有直接或间接有调用[.setPointBitmap]方法的话，那么 [.pointRangeRadius]的值将会被覆盖。
     *
     * @param pointRangeRadius
     */
    fun setPointRangeRadius(pointRangeRadius: Float) {
        this.pointRangeRadius = pointRangeRadius
    }

    /**
     * 设置扫描线位图的宽度比例；默认为：0.625；此方法会改变[.laserBitmapWidth]
     *
     * @param laserBitmapRatio
     */
    fun setLaserBitmapRatio(laserBitmapRatio: Float) {
        this.laserBitmapRatio = laserBitmapRatio
        if (minDimension > 0) {
            laserBitmapWidth = minDimension * laserBitmapRatio
            scaleLaserBitmap()
        }
    }

    /**
     * 设置扫描线位图的宽度
     *
     * @param laserBitmapWidth
     */
    fun setLaserBitmapWidth(laserBitmapWidth: Float) {
        this.laserBitmapWidth = laserBitmapWidth
        scaleLaserBitmap()
    }

    /**
     * 设置点击Item监听
     *
     * @param listener
     */
    fun setOnItemClickListener(listener: OnItemClickListener?) {
        onItemClickListener = listener
    }

    /**
     * Item点击监听
     */
    open interface OnItemClickListener {
        /**
         * Item点击事件
         *
         * @param position
         */
        fun onItemClick(position: Int)
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap? {
        var bitmap: Bitmap? = null
        val width = drawable.intrinsicWidth
        val height = drawable.intrinsicHeight
        val config =
            if (drawable.opacity != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
        bitmap = Bitmap.createBitmap(width, height, config)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)
        return bitmap
    }

}