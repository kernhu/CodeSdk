package com.xcion.codesdk

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.xcion.codesdk.databinding.ActivityMainBinding
import com.xcion.codesdk.sample.CodeListActivity
import com.xcion.codesdk.sample.CreatorActivity
import com.xcion.codesdk.sample.CustomScannerActivity
import com.xcion.codesdk.sample.ScannerActivity

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        bindEvents()
    }


    private fun bindEvents() {

        binding?.scannerAll1?.setOnClickListener {
            var intent = Intent(this, ScannerActivity::class.java)
            startActivity(intent)
        }

        binding?.scannerAll2?.setOnClickListener {
            var intent = Intent(this, FragmentActivity::class.java)
            intent.putExtra(FragmentActivity.KEY_TYPE, FragmentActivity.TYPE_SCANNER_FRAGMENT)
            startActivity(intent)
        }

        binding?.scannerAll3?.setOnClickListener {
            var intent = Intent(this, CustomScannerActivity::class.java)
            startActivity(intent)
        }

        binding?.scannerAll4?.setOnClickListener {
            var intent = Intent(this, FragmentActivity::class.java)
            intent.putExtra(FragmentActivity.KEY_TYPE, FragmentActivity.TYPE_CUSTOM_SCANNER_FRAGMENT)
            startActivity(intent)
        }

        binding?.creatorAll?.setOnClickListener {
            var intent = Intent(this, CreatorActivity::class.java)
            startActivity(intent)
        }

        binding?.creatorList?.setOnClickListener {
            var intent = Intent(this, CodeListActivity::class.java)
            startActivity(intent)
        }

    }


}