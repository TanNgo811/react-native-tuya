package com.tuya.smart.rnsdk.ipcview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.tuya.smart.camera.ipccamerasdk.p2p.ICameraP2P
import com.tuya.smart.camera.middleware.widget.TuyaCameraView
import com.tuya.smart.rnsdk.R

class TuyaIpcFragment(private val devId: String?) : Fragment() {
    var tuyaCamera: TuyaCamera? = null
    private var mVideoView: TuyaCameraView? = null
    private var muteImg: ImageView? = null
    private var qualityTv: TextView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v: View = inflater.inflate(R.layout.fragment_tuya_ipc, container, false)
        v.setOnTouchListener{ view: View?, event: MotionEvent? -> true }
        mVideoView = v.findViewById(R.id.camera_video_view)
        muteImg = v.findViewById(R.id.camera_mute)
        qualityTv = v.findViewById(R.id.camera_quality)
        muteImg!!.isSelected = true
        tuyaCamera = TuyaCamera(requireContext(), devId!!)
        tuyaCamera!!.initData(mVideoView, muteImg!!)
        initListener()
        return v
    }

    private fun initListener() {
        muteImg!!.setOnClickListener {
            muteImg!!.isClickable = false
            tuyaCamera!!.muteClick()
            muteImg!!.isClickable = true
        }
        qualityTv?.setOnClickListener {
            tuyaCamera!!.setVideoClarity()
            qualityTv!!.text = if (tuyaCamera!!.videoClarity == ICameraP2P.HD) "HD" else "SD"
        }
    }

    override fun onPause() {
        super.onPause()
        tuyaCamera!!.stopCamera()
        // do any logic that should happen in an `onPause` method
        // e.g.: customView.onPause();
    }

    override fun onResume() {
        super.onResume()
        tuyaCamera!!.resumeCamera()
        // do any logic that should happen in an `onResume` method
        // e.g.: customView.onResume();
    }

    override fun onDestroy() {
        super.onDestroy()
        tuyaCamera!!.destroyCamera()
        // do any logic that should happen in an `onDestroy` method
        // e.g.: customView.onDestroy();
    }

    companion object {
        fun newInstance(devId: String?): TuyaIpcFragment {
            val fragment = TuyaIpcFragment(devId)
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}