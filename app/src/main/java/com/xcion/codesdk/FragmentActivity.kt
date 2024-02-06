package com.xcion.codesdk

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.xcion.codesdk.databinding.ActivityFragmentBinding
import com.xcion.codesdk.sample.CustomScannerFragment
import com.xcion.codesdk.sample.ScannerFragment

class FragmentActivity : AppCompatActivity() {

    companion object {

        const val KEY_TYPE = "key_type"

        const val TYPE_SCANNER_FRAGMENT = 0
        const val TYPE_CUSTOM_SCANNER_FRAGMENT = 1

    }

    private var binding: ActivityFragmentBinding? = null
    private var type = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_fragment)
        type = intent.getIntExtra(KEY_TYPE, 0)
        if (type == TYPE_SCANNER_FRAGMENT) {
            supportFragmentManager.beginTransaction()
                ?.add(R.id.frame_layout, ScannerFragment(), ScannerFragment::class.java.toString())
                ?.commit()
        } else if (type == TYPE_CUSTOM_SCANNER_FRAGMENT) {
            supportFragmentManager.beginTransaction()
                ?.add(R.id.frame_layout, CustomScannerFragment(), CustomScannerFragment::class.java.toString())
                ?.commit()
        }
    }
}