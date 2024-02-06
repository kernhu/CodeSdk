package com.xcion.codesdk.sample

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.xcion.code.creator.CodeCreator
import com.xcion.code.creator.enums.CodeFormat
import com.xcion.code.creator.enums.QRCodeParticle
import com.xcion.codesdk.R
import com.xcion.codesdk.databinding.ActivityCodeListBinding

/**
 * @author: huming
 * @date: 2024/2/5
 * @Description: java类作用描述
 */
class CodeListActivity : AppCompatActivity() {

    private var binding: ActivityCodeListBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_code_list)


        initView()
    }

    private fun initView() {
        CodeCreator.with()
            .setWidth(500)
            .setHeight(500)
            .setContent("Hello World")
            .setColor(Color.BLACK)
            .setCodeFormat(CodeFormat.QR_CODE)
            .setQRCodeParticle(QRCodeParticle.PIXEL)
            .setCallback(object : CodeCreator.Callback {
                override fun onSuccess(bitmap: Bitmap?) {
                    binding?.image1?.setImageBitmap(bitmap)
                }

                override fun onFailure(e: Exception?) {
                    Log.e("sos", "generateQrcode  onFailure=" + e?.message)
                }
            }).generate()

        CodeCreator.with()
            .setWidth(500)
            .setHeight(500)
            .setContent("你好世界")
            .setColor(Color.BLACK)
            .setCodeFormat(CodeFormat.QR_CODE)
            .setQRCodeParticle(QRCodeParticle.DOT)
            .setCallback(object : CodeCreator.Callback {
                override fun onSuccess(bitmap: Bitmap?) {
                    binding?.image2?.setImageBitmap(bitmap)
                }

                override fun onFailure(e: Exception?) {
                    Log.e("sos", "generateQrcode  onFailure=" + e?.message)
                }
            }).generate()

        CodeCreator.with()
            .setWidth(900)
            .setHeight(350)
            .setContent("10243396258")
            .setColor(Color.BLACK)
            .setCodeFormat(CodeFormat.BAR_CODE)
            .setCallback(object : CodeCreator.Callback {
                override fun onSuccess(bitmap: Bitmap?) {
                    binding?.image3?.setImageBitmap(bitmap)
                }

                override fun onFailure(e: Exception?) {
                    Log.e("sos", "generateQrcode  onFailure=" + e?.message)
                }
            }).generate()


        var bitmap = BitmapFactory.decodeResource(resources, R.mipmap.icon_logo)
        CodeCreator.with()
            .setWidth(800)
            .setHeight(800)
            .setColor(getColor(R.color.purple_200))
            .setLogoSize(100)
            .setLogo(bitmap)
            .setContent("https://github.com/")
            .setCodeFormat(CodeFormat.QR_CODE)
            .setQRCodeParticle(QRCodeParticle.PIXEL)
            .setCallback(object : CodeCreator.Callback {
                override fun onSuccess(bitmap: Bitmap?) {
                    binding?.image4?.setImageBitmap(bitmap)
                }

                override fun onFailure(e: Exception?) {
                    Log.e("sos", "generateQrcodeWithLogo  onFailure=" + e?.message)
                }
            }).generate()
    }
}