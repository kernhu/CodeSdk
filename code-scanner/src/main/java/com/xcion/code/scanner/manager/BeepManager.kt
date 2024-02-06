package com.xcion.code.scanner.manager

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.xcion.code.scanner.R
import com.xcion.code.scanner.utils.LogUtils
import java.io.Closeable


/**
 * @date: 2024/1/23
 * @Description: 震动和滴声管理类
 */
class BeepManager(private val context: Context?) : MediaPlayer.OnErrorListener, Closeable {


    private val VIBRATE_DURATION = 200L

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var playBeep = false
    private var vibrate = false

    init {
        mediaPlayer = null
        updatePrefs()
    }

    fun setVibrate(vibrate: Boolean) {
        this.vibrate = vibrate
    }

    fun setPlayBeep(playBeep: Boolean) {
        this.playBeep = playBeep
    }

    @Synchronized
    private fun updatePrefs() {
        if (mediaPlayer == null) {
            mediaPlayer = buildMediaPlayer(context)
        }
        try {
            if (vibrator == null) {
                vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

                    (context?.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator

                } else ({
                    context?.getSystemService(Context.VIBRATOR_SERVICE)
                }) as Vibrator?
            }
        } catch (e: Exception) {

        }
    }

    @Synchronized
    fun playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer!!.start()
        }
        try {
            if (vibrate && vibrator?.hasVibrator()!!) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        VibrationEffect.createOneShot(
                            VIBRATE_DURATION,
                            VibrationEffect.DEFAULT_AMPLITUDE
                        )
                    )
                } else {
                    vibrator?.vibrate(VIBRATE_DURATION)
                }
            }
        } catch (e: Exception) {

        }
    }

    private fun buildMediaPlayer(context: Context?): MediaPlayer? {
        val mediaPlayer = MediaPlayer()
        return try {
            val file: AssetFileDescriptor =
                context?.resources?.openRawResourceFd(R.raw.code_scanner_beep)!!
            mediaPlayer.setDataSource(file.fileDescriptor, file.startOffset, file.length)
            mediaPlayer.setOnErrorListener(this)
            mediaPlayer.isLooping = false
            mediaPlayer.prepare()
            mediaPlayer
        } catch (e: Exception) {
            LogUtils.w(e)
            mediaPlayer.release()
            null
        }
    }

    @Synchronized
    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        close()
        updatePrefs()
        return true
    }

    @Synchronized
    override fun close() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer!!.release()
                mediaPlayer = null
            }
        } catch (e: Exception) {
            LogUtils.e(e)
        }
    }


}