package com.xcion.codesdk.sample

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.xcion.code.scanner.CodeScannerView
import com.xcion.codesdk.R
import com.xcion.codesdk.databinding.FragmentCustomScannerBinding

/**
 * @author: huming
 * @date: 2024/2/4
 * @Description: custom scanner fragment
 */
class CustomScannerFragment : Fragment(), CodeScannerView.Callback {

    private var binding: FragmentCustomScannerBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_custom_scanner, container, false)
        bindData()
        return binding?.root
    }

    private fun bindData() {
        /***
         * 1. you must bind the lifecycle of the activity;
         * and register the callback to listen the result value
         * */
        binding?.codeScannerView?.addLifecycleObserver(this)?.addCallback(this)?.build(this)

        binding?.codeScannerView?.getHeaderLayout()?.findViewById<ImageView>(R.id.header_close)
            ?.setOnClickListener {
                requireActivity().finish()
            }
        binding?.codeScannerView?.getHeaderLayout()?.findViewById<ImageView>(R.id.header_more)
            ?.setOnClickListener {
                Toast.makeText(requireContext().applicationContext, "you click the more button", Toast.LENGTH_SHORT)
                    .show()
            }
        binding?.codeScannerView?.getFooterLayout()?.findViewById<ImageView>(R.id.footer_qrcode)
            ?.setOnClickListener {
                Toast.makeText(
                    requireContext().applicationContext,
                    "you click the my qrcode button",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        /***
         * you must bind the activity result
         * */
        binding?.codeScannerView?.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        /***
         * you must bind the request permission result
         * */
        binding?.codeScannerView?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onScanResult(content: String) {
        /***
         *  here,you can listen the result of scanner
         * */
        Toast.makeText(requireContext().applicationContext, content, Toast.LENGTH_SHORT).show()
        requireActivity().finish()
    }


}