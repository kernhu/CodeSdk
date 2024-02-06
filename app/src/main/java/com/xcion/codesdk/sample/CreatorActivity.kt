package com.xcion.codesdk.sample

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.xcion.code.creator.CodeCreator
import com.xcion.code.creator.enums.CodeFormat
import com.xcion.code.creator.enums.QRCodeParticle
import com.xcion.codesdk.R
import com.xcion.codesdk.databinding.ActivityCreatorBinding
import com.xcion.codesdk.utils.StringUtils

/**
 * @date: 2024/1/22
 * @Description: the simple of barcode and qrcode
 */
class CreatorActivity : AppCompatActivity() {

    private var binding: ActivityCreatorBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_creator)

        binding?.creatorBarcode?.setOnClickListener {
            var content = binding?.creatorInput?.text.toString().trim()
            if (StringUtils.contains(content)) {
                Toast.makeText(
                    CreatorActivity@ this,
                    getString(R.string.warn_content_lawless), Toast.LENGTH_SHORT
                ).show()
            } else {
                generateBarcode(content)
            }
        }

        binding?.creatorQrcodePixel?.setOnClickListener {
            var content = binding?.creatorInput?.text.toString().trim()
            if (TextUtils.isEmpty(content)) {
                Toast.makeText(
                    CreatorActivity@ this,
                    getString(R.string.warn_content_empty), Toast.LENGTH_SHORT
                ).show()
            } else {
                generateQrcodePixel(content)
            }

        }

        binding?.creatorLogoQrcodePixel?.setOnClickListener {
            var content = binding?.creatorInput?.text.toString().trim()
            if (TextUtils.isEmpty(content)) {
                Toast.makeText(
                    CreatorActivity@ this,
                    getString(R.string.warn_content_empty),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                generateQrcodePixelWithLogo(content)
            }
        }

        binding?.creatorQrcodeDot?.setOnClickListener {
            var content = binding?.creatorInput?.text.toString().trim()
            if (TextUtils.isEmpty(content)) {
                Toast.makeText(
                    CreatorActivity@ this,
                    getString(R.string.warn_content_empty),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                generateQrcodeDot(content)
            }
        }

        binding?.creatorLogoQrcodeDot?.setOnClickListener {
            var content = binding?.creatorInput?.text.toString().trim()
            if (TextUtils.isEmpty(content)) {
                Toast.makeText(
                    CreatorActivity@ this,
                    getString(R.string.warn_content_empty),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                generateQrcodeDotWithLogo(content)
            }
        }
    }

    private fun generateBarcode(content: String) = CodeCreator.with()
        .setWidth(800)
        .setHeight(350)
        .setContent(content)
        .setColor(Color.BLACK)
        .setCodeFormat(CodeFormat.BAR_CODE)
        .setCallback(object : CodeCreator.Callback {
            override fun onSuccess(bitmap: Bitmap?) {
                binding?.creatorImage?.setImageBitmap(bitmap)
            }

            override fun onFailure(e: Exception?) {
                Log.e("sos", "generateBarcode  onFailure=" + e?.message)
            }
        }).generate()

    private fun generateQrcodePixel(content: String) {
        CodeCreator.with()
            .setWidth(600)
            .setHeight(600)
            .setContent(content)
            .setColor(Color.BLACK)
            .setCodeFormat(CodeFormat.QR_CODE)
            .setQRCodeParticle(QRCodeParticle.PIXEL)
            .setCallback(object : CodeCreator.Callback {
                override fun onSuccess(bitmap: Bitmap?) {
                    binding?.creatorImage?.setImageBitmap(bitmap)
                }

                override fun onFailure(e: Exception?) {
                    Log.e("sos", "generateQrcode  onFailure=" + e?.message)
                }
            }).generate()
    }

    private fun generateQrcodePixelWithLogo(content: String) {
        var bitmap = BitmapFactory.decodeResource(resources, R.mipmap.icon_logo)
        CodeCreator.with()
            .setWidth(600)
            .setHeight(600)
            .setColor(getColor(R.color.purple_200))
            .setLogoSize(100)
            .setLogo(bitmap)
            .setContent(content)
            .setCodeFormat(CodeFormat.QR_CODE)
            .setQRCodeParticle(QRCodeParticle.PIXEL)
            .setCallback(object : CodeCreator.Callback {
                override fun onSuccess(bitmap: Bitmap?) {
                    binding?.creatorImage?.setImageBitmap(bitmap)
                }

                override fun onFailure(e: Exception?) {
                    Log.e("sos", "generateQrcodeWithLogo  onFailure=" + e?.message)
                }
            }).generate()
    }

    private fun generateQrcodeDot(content: String) {
        CodeCreator.with()
            .setWidth(600)
            .setHeight(600)
            .setColor(getColor(R.color.purple_200))
            .setContent(content)
            .setCodeFormat(CodeFormat.QR_CODE)
            .setQRCodeParticle(QRCodeParticle.DOT)
            .setCallback(object : CodeCreator.Callback {
                override fun onSuccess(bitmap: Bitmap?) {
                    binding?.creatorImage?.setImageBitmap(bitmap)
                }

                override fun onFailure(e: Exception?) {
                    Log.e("sos", "generateQrcodeWithLogo  onFailure=" + e?.message)
                }
            }).generate()
    }

    private fun generateQrcodeDotWithLogo(content: String) {
        var bitmap = BitmapFactory.decodeResource(resources, R.mipmap.icon_logo)
        CodeCreator.with()
            .setWidth(600)
            .setHeight(600)
            .setColor(getColor(R.color.purple_200))
            .setLogoSize(100)
            .setLogo(bitmap)
            .setContent(content)
            .setCodeFormat(CodeFormat.QR_CODE)
            .setQRCodeParticle(QRCodeParticle.DOT)
            .setCallback(object : CodeCreator.Callback {
                override fun onSuccess(bitmap: Bitmap?) {
                    binding?.creatorImage?.setImageBitmap(bitmap)
                }

                override fun onFailure(e: Exception?) {
                    Log.e("sos", "generateQrcodeWithLogo  onFailure=" + e?.message)
                }
            }).generate()
    }
}