package com.xcion.code.scanner.utils

import android.util.Log
import java.util.Locale

/**
 * @date: 2024/1/30
 * @Description: java类作用描述
 */
class LogUtils {


    companion object {
        val TAG = "CameraScan"

        /**
         * 是否显示日志
         */
        private var isShowLog = true

        /**
         * 日志优先级别
         */
        private var priority = 1

        /**
         * Priority constant for the println method;use System.out.println
         */
        val PRINTLN = 1

        /**
         * Priority constant for the println method; use Log.v.
         */
        val VERBOSE = 2

        /**
         * Priority constant for the println method; use Log.d.
         */
        val DEBUG = 3

        /**
         * Priority constant for the println method; use Log.i.
         */
        val INFO = 4

        /**
         * Priority constant for the println method; use Log.w.
         */
        val WARN = 5

        /**
         * Priority constant for the println method; use Log.e.
         */
        val ERROR = 6

        /**
         * Priority constant for the println method.use Log.wtf.
         */
        val ASSERT = 7

        val STACK_TRACE_FORMAT = "%s.%s(%s:%d)"

        private val MIN_STACK_OFFSET = 5
        private val LOG_STACK_OFFSET = 6

        /**
         * Drawing toolbox
         */
        private val TOP_LEFT_CORNER = '┌'
        private val BOTTOM_LEFT_CORNER = '└'
        private val MIDDLE_CORNER = '├'
        private val HORIZONTAL_LINE = '│'
        private val DOUBLE_DIVIDER = "────────────────────────────────────────────────────────"
        private val SINGLE_DIVIDER = "┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄"
        private val TOP_BORDER = TOP_LEFT_CORNER.toString() + DOUBLE_DIVIDER + DOUBLE_DIVIDER
        private val BOTTOM_BORDER = BOTTOM_LEFT_CORNER.toString() + DOUBLE_DIVIDER + DOUBLE_DIVIDER
        private val MIDDLE_BORDER = MIDDLE_CORNER.toString() + SINGLE_DIVIDER + SINGLE_DIVIDER
        private val LINE_FEED = "\n"


        /**
         * 设置是否显示日志；此设置可全局控制是否打印日志
         *
         *
         * 如果你只想根据日志级别来控制日志的打印；可以使用[LogUtils.setPriority]；
         *
         * @param isShowLog [LogUtils.isShowLog]
         */
        @JvmStatic
        fun setShowLog(isShowLog: Boolean) {
            this.isShowLog = isShowLog
        }

        /**
         * 是否显示日志
         *
         * @return [LogUtils.isShowLog]
         */
        @JvmStatic
        fun isShowLog(): Boolean {
            return isShowLog
        }

        /**
         * 获取日志优先级别
         *
         * @return [LogUtils.priority]
         */
        @JvmStatic
        fun getPriority(): Int {
            return priority
        }

        /**
         * 设置日志优先级别；设置优先级之后，低于此优先级别的日志将不会打印；
         *
         *
         * 你也可以通过[LogUtils.setShowLog]来全局控制是否打印日志
         *
         * @param priority [LogUtils.priority]
         */
        @JvmStatic
        fun setPriority(priority: Int) {
            this.priority = priority
        }

        /**
         * 获取堆栈信息 className.methodName(fileName:lineNumber)
         *
         * <pre>
         * ┌──────────────────────────
         * Method stack info
         * ├┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄
         * Log message
         * └──────────────────────────
        </pre> *
         *
         * @return
         */
        @JvmStatic
        private fun getStackTraceMessage(msg: Any, stackOffset: Int): String? {
            val caller = getStackTraceElement(stackOffset)
            var callerClazzName = caller.className
            callerClazzName = callerClazzName.substring(callerClazzName.lastIndexOf(".") + 1)
            val methodStack = String.format(
                Locale.getDefault(),
                STACK_TRACE_FORMAT,
                callerClazzName,
                caller.methodName,
                caller.fileName,
                caller.lineNumber
            )
            return StringBuilder().append(TOP_BORDER)
                .append(LINE_FEED)
                .append(methodStack)
                .append(LINE_FEED)
                .append(MIDDLE_BORDER)
                .append(LINE_FEED)
                .append(msg)
                .append(LINE_FEED)
                .append(BOTTOM_BORDER)
                .toString()
        }

        /**
         * 获取堆栈
         *
         * @param n n=0		VMStack
         * n=1		Thread
         * n=3		CurrentStack
         * n=4		CallerStack
         * ...
         * @return
         */
        @JvmStatic
        private fun getStackTraceElement(n: Int): StackTraceElement {
            return Thread.currentThread().stackTrace[n]
        }

        /**
         * 根据异常获取堆栈信息
         *
         * @param t 异常
         * @return
         */
        @JvmStatic
        private fun getStackTraceString(t: Throwable): String {
            return Log.getStackTraceString(t)
        }

        // -----------------------------------Log.v

        // -----------------------------------Log.v
        /**
         * 打印日志；日志级别：[LogUtils.VERBOSE]
         *
         * @param msg 日志信息
         */
        @JvmStatic
        fun v(msg: String) {
            if (isShowLog && priority <= VERBOSE) log(Log.VERBOSE, msg)
        }

        /**
         * 打印日志；日志级别：[LogUtils.VERBOSE]
         *
         * @param t 异常
         */
        @JvmStatic
        fun v(t: Throwable) {
            if (isShowLog && priority <= VERBOSE) log(Log.VERBOSE, t)
        }

        /**
         * 打印日志；日志级别：[LogUtils.VERBOSE]
         *
         * @param msg 日志信息
         * @param t   异常
         */
        @JvmStatic
        fun v(msg: String, t: Throwable) {
            if (isShowLog && priority <= VERBOSE) log(Log.VERBOSE, msg, t)
        }

        // -----------------------------------Log.d

        // -----------------------------------Log.d
        /**
         * 打印日志；日志级别：[LogUtils.DEBUG]
         *
         * @param msg 日志信息
         */
        @JvmStatic
        fun d(msg: String) {
            if (isShowLog && priority <= DEBUG) log(Log.DEBUG, msg)
        }

        /**
         * 打印日志；日志级别：[LogUtils.DEBUG]
         *
         * @param t 异常
         */
        @JvmStatic
        fun d(t: Throwable) {
            if (isShowLog && priority <= DEBUG) log(Log.DEBUG, t)
        }

        /**
         * 打印日志；日志级别：[LogUtils.DEBUG]
         *
         * @param msg 日志信息
         * @param t   异常
         */
        @JvmStatic
        fun d(msg: String, t: Throwable) {
            if (isShowLog && priority <= DEBUG) log(Log.DEBUG, msg, t)
        }

        // -----------------------------------Log.i

        // -----------------------------------Log.i
        /**
         * 打印日志；日志级别：[LogUtils.INFO]
         *
         * @param msg 日志信息
         */
        @JvmStatic
        fun i(msg: String) {
            if (isShowLog && priority <= INFO) log(Log.INFO, msg)
        }

        /**
         * 打印日志；日志级别：[LogUtils.INFO]
         *
         * @param t 异常
         */
        @JvmStatic
        fun i(t: Throwable) {
            if (isShowLog && priority <= INFO) log(Log.INFO, t)
        }

        /**
         * 打印日志；日志级别：[LogUtils.INFO]
         *
         * @param msg 日志信息
         * @param t   异常
         */
        @JvmStatic
        fun i(msg: String, t: Throwable) {
            if (isShowLog && priority <= INFO) log(Log.INFO, msg, t)
        }

        // -----------------------------------Log.w

        // -----------------------------------Log.w
        /**
         * 打印日志；日志级别：[LogUtils.WARN]
         *
         * @param msg 日志信息
         */
        @JvmStatic
        fun w(msg: String) {
            if (isShowLog && priority <= WARN) log(Log.WARN, msg)
        }

        /**
         * 打印日志；日志级别：[LogUtils.WARN]
         *
         * @param t 异常
         */
        @JvmStatic
        fun w(t: Throwable) {
            if (isShowLog && priority <= WARN) log(Log.WARN, t)
        }

        /**
         * 打印日志；日志级别：[LogUtils.WARN]
         *
         * @param msg 日志信息
         * @param t   异常
         */
        @JvmStatic
        fun w(msg: String, t: Throwable) {
            if (isShowLog && priority <= WARN) log(Log.WARN, msg, t)
        }

        // -----------------------------------Log.e

        // -----------------------------------Log.e
        /**
         * 打印日志；日志级别：[LogUtils.ERROR]
         *
         * @param msg 日志信息
         */
        @JvmStatic
        fun e(msg: String) {
            if (isShowLog && priority <= ERROR) log(Log.ERROR, msg)
        }

        /**
         * 打印日志；日志级别：[LogUtils.ERROR]
         *
         * @param t 异常
         */
        @JvmStatic
        fun e(t: Throwable) {
            if (isShowLog && priority <= ERROR) log(Log.ERROR, t)
        }

        /**
         * 打印日志；日志级别：[LogUtils.ERROR]
         *
         * @param msg 日志信息
         * @param t   异常
         */
        @JvmStatic
        fun e(msg: String, t: Throwable) {
            if (isShowLog && priority <= ERROR) log(Log.ERROR, msg, t)
        }

        // -----------------------------------Log.wtf

        // -----------------------------------Log.wtf
        /**
         * 打印日志；日志级别：[LogUtils.ASSERT]
         *
         * @param msg 日志信息
         */
        @JvmStatic
        fun wtf(msg: String) {
            if (isShowLog && priority <= ASSERT) log(Log.ASSERT, msg)
        }

        /**
         * 打印日志；日志级别：[LogUtils.ASSERT]
         *
         * @param t 异常
         */
        @JvmStatic
        fun wtf(t: Throwable) {
            if (isShowLog && priority <= ASSERT) log(Log.ASSERT, t)
        }

        /**
         * 打印日志；日志级别：[LogUtils.ASSERT]
         *
         * @param msg 日志信息
         * @param t   异常
         */
        @JvmStatic
        fun wtf(msg: String, t: Throwable) {
            if (isShowLog && priority <= ASSERT) log(Log.ASSERT, msg, t)
        }

        /**
         * 打印日志；日志级别：ASSERT
         *
         * @param priority 日志优先级别
         * @param msg      日志信息
         */
        @JvmStatic
        private fun log(priority: Int, msg: String) {
            Log.println(priority, TAG, getStackTraceMessage(msg, LOG_STACK_OFFSET)!!)
        }

        /**
         * 打印日志
         *
         * @param priority 日志优先级别
         * @param t        异常
         */
        @JvmStatic
        private fun log(priority: Int, t: Throwable) {
            Log.println(
                priority, TAG,
                getStackTraceMessage(getStackTraceString(t), LOG_STACK_OFFSET)!!
            )
        }

        /**
         * 打印日志
         *
         * @param priority 日志优先级别
         * @param msg      日志信息
         * @param t        异常
         */
        @JvmStatic
        private fun log(priority: Int, msg: String, t: Throwable) {
            Log.println(
                priority, TAG,
                getStackTraceMessage(
                    """
                $msg
                ${getStackTraceString(t)}
                """.trimIndent(), LOG_STACK_OFFSET
                )!!
            )
        }

        // -----------------------------------System.out.println

        // -----------------------------------System.out.println
        /**
         * System.out.println
         *
         * @param msg
         */
        @JvmStatic
        fun println(msg: String) {
            if (isShowLog && priority <= PRINTLN) println(
                getStackTraceMessage(
                    msg,
                    MIN_STACK_OFFSET
                )
            )
        }

        @JvmStatic
        fun println(obj: Any) {
            if (isShowLog && priority <= PRINTLN) println(
                getStackTraceMessage(
                    obj,
                    MIN_STACK_OFFSET
                )
            )
        }
    }
}