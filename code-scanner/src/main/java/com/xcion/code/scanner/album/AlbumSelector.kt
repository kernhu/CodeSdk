package com.xcion.code.scanner.album

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.Fragment


/**
 * @author: KernHu
 * @date: 2024/1/31
 * @Description: 相册选择器，一次只能选择一张
 */
class AlbumSelector {

    companion object {

        public const val ACTIVITY_REQUEST_CODE = 1024
        public const val WRITE_EXTERNAL_STORAGE_REQUEST_CODE = ACTIVITY_REQUEST_CODE + 1

        private var albumSelector: AlbumSelector? = null

        @JvmStatic
        fun with(): AlbumSelector? {
            if (albumSelector == null) {
                albumSelector = AlbumSelector()
            }
            return albumSelector
        }
    }

//    public fun pickOne(activity: Activity) {
//        if (PermissionUtils.checkPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//            intentAlbum(activity)
//        } else {
//            PermissionUtils.requestPermission(
//                activity,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                WRITE_EXTERNAL_STORAGE_REQUEST_CODE
//            )
//        }
//        intentAlbum(activity)
//    }

    public fun pickOne(activity: Activity) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.setType("image/*")
        activity.startActivityForResult(intent, ACTIVITY_REQUEST_CODE)
    }

    public fun pickOne(fragment: Fragment) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.setType("image/*")
        fragment.startActivityForResult(intent, ACTIVITY_REQUEST_CODE)
    }
}