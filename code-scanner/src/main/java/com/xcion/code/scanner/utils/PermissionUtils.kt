package com.xcion.code.scanner.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.IntRange
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment

/**
 * @date: 2024/1/30
 * @Description: Permission 权限工具类
 */
class PermissionUtils {

    companion object {

        /**
         * 检测是否授权
         *
         * @param context
         * @param permission
         * @return 返回{@code true} 表示已授权，`false`表示未授权
         */
        @JvmStatic
        fun checkPermission(context: Context, permission: String): Boolean {
            return ActivityCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }

        /**
         * 请求权限
         *
         * @param activity
         * @param permission
         * @param requestCode
         */
        @JvmStatic
        fun requestPermission(
            activity: Activity,
            permission: String,
            @IntRange(from = 0) requestCode: Int
        ) {
            requestPermissions(activity, arrayOf(permission), requestCode)
        }

        /**
         * 请求权限
         *
         * @param fragment
         * @param permission
         * @param requestCode
         */
        @JvmStatic
        fun requestPermission(
            fragment: Fragment,
            permission: String,
            @IntRange(from = 0) requestCode: Int
        ) {
            requestPermissions(fragment, arrayOf(permission), requestCode)
        }

        /**
         * 请求权限
         *
         * @param activity
         * @param permissions
         * @param requestCode
         */
        @JvmStatic
        fun requestPermissions(
            activity: Activity,
            permissions: Array<String>,
            @IntRange(from = 0) requestCode: Int
        ) {
            LogUtils.d("requestPermissions: $permissions")
            ActivityCompat.requestPermissions(activity, permissions, requestCode)
        }

        /**
         * 请求权限
         *
         * @param fragment
         * @param permissions
         * @param requestCode
         */
        @JvmStatic
        fun requestPermissions(
            fragment: Fragment,
            permissions: Array<String>,
            @IntRange(from = 0) requestCode: Int
        ) {
            LogUtils.d("requestPermissions: $permissions")
            fragment.requestPermissions(permissions, requestCode)
        }

        /**
         * 请求权限结果
         *
         * @param requestPermission 请求的权限
         * @param permissions
         * @param grantResults
         * @return 返回{@code true} 表示已授权，`false`表示未授权
         */
        @JvmStatic
        fun requestPermissionsResult(
            requestPermission: String,
            permissions: Array<String>,
            grantResults: IntArray
        ): Boolean {
            val length = permissions.size
            for (i in 0 until length) {
                if (requestPermission == permissions[i]) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        return true
                    }
                }
            }
            return false
        }

        /**
         * 请求权限结果
         *
         * @param requestPermissions 请求的权限
         * @param permissions
         * @param grantResults
         * @return 返回{@code true} 表示全部已授权，`false`表示未全部授权
         */
        @JvmStatic
        fun requestPermissionsResult(
            requestPermissions: Array<String>,
            permissions: Array<String>,
            grantResults: IntArray
        ): Boolean {
            val length = permissions.size
            for (i in 0 until length) {
                for (j in requestPermissions.indices) {
                    if (requestPermissions[j] == permissions[i]) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            return false
                        }
                    }
                }
            }
            return true
        }

    }

}