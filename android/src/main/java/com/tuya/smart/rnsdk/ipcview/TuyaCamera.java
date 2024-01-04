package com.tuya.smart.rnsdk.ipcview;

import static com.tuya.smart.rnsdk.ipcview.utils.CameraConstants.ARG1_OPERATE_FAIL;
import static com.tuya.smart.rnsdk.ipcview.utils.CameraConstants.ARG1_OPERATE_SUCCESS;
import static com.tuya.smart.rnsdk.ipcview.utils.CameraConstants.MSG_CONNECT;
import static com.tuya.smart.rnsdk.ipcview.utils.CameraConstants.MSG_MUTE;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import com.alibaba.fastjson.JSONObject;
import com.tuya.smart.android.camera.sdk.TuyaIPCSdk;
import com.tuya.smart.android.camera.sdk.api.ITuyaIPCCore;
import com.tuya.smart.android.common.utils.L;
import com.tuya.smart.camera.camerasdk.typlayer.callback.AbsP2pCameraListener;
import com.tuya.smart.camera.camerasdk.typlayer.callback.OnRenderDirectionCallback;
import com.tuya.smart.camera.camerasdk.typlayer.callback.OperationDelegateCallBack;
import com.tuya.smart.camera.ipccamerasdk.p2p.ICameraP2P;
import com.tuya.smart.camera.middleware.p2p.ITuyaSmartCameraP2P;
import com.tuya.smart.camera.middleware.widget.AbsVideoViewCallback;
import com.tuya.smart.camera.middleware.widget.TuyaCameraView;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.rnsdk.R;
import com.tuya.smart.rnsdk.ipcview.utils.CameraConstants;
import com.tuya.smart.rnsdk.ipcview.utils.MessageUtil;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.api.ITuyaDevice;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.utils.ToastUtil;
import java.nio.ByteBuffer;
import java.util.Map;


public class TuyaCamera {
    private static final String TAG = TuyaCamera.class.getSimpleName();
    private final Context context;
    private boolean isPlay = false;
    private int p2pType;
    private String devId;
    private ITuyaSmartCameraP2P mCameraP2P;
    private TuyaCameraView mVideoView;
    private int previewMute = ICameraP2P.MUTE;
    private int videoClarity = ICameraP2P.STANDEND;
    private ImageView muteImg;
    public boolean activeAction = false;

    public TuyaCamera(Context con, String devId) {
        this.devId = devId;
        this.context = con;
    }

    public void resumeCamera () {
        mVideoView.onResume();
        //must register again,or can't callback
        if (null != mCameraP2P) {
            mCameraP2P.registerP2PCameraListener(p2pCameraListener);
            mCameraP2P.generateCameraView(mVideoView.createdView());
            if (mCameraP2P.isConnecting()) {
                preview();
            }else {
                activeAction = true;
                mCameraP2P.connect(devId, new OperationDelegateCallBack() {
                    @Override
                    public void onSuccess(int i, int i1, String s) {
                        Log.e(TAG, String.valueOf(MSG_CONNECT + ARG1_OPERATE_SUCCESS));
                        mHandler.sendMessage(MessageUtil.getMessage(MSG_CONNECT, ARG1_OPERATE_SUCCESS));
                    }

                    @Override
                    public void onFailure(int i, int i1, int i2) {
                        mHandler.sendMessage(MessageUtil.getMessage(MSG_CONNECT, ARG1_OPERATE_FAIL));
                    }
                });
            }
        }
    }

    public void destroyCamera(){
        if (null != mCameraP2P) {
            mCameraP2P.removeOnP2PCameraListener();
            mCameraP2P.destroyP2P();
        }
    }

    public void stopCamera() {
        mVideoView.onPause();
        if (null != mCameraP2P) {
            if (isPlay) {
                mCameraP2P.stopPreview(new OperationDelegateCallBack() {
                    @Override
                    public void onSuccess(int sessionId, int requestId, String data) {

                    }

                    @Override
                    public void onFailure(int sessionId, int requestId, int errCode) {

                    }
                });
                isPlay = false;
            }
            mCameraP2P.removeOnP2PCameraListener();
            mCameraP2P.disconnect(new OperationDelegateCallBack() {
                @Override
                public void onSuccess(int i, int i1, String s) {

                }

                @Override
                public void onFailure(int i, int i1, int i2) {

                }
            });
        }
    }

    // On create data function used to initialize tuya camera
    public void initData(TuyaCameraView cameraView, ImageView muteImage) {
        this.mVideoView = cameraView;
        this.muteImg = muteImage;
        ITuyaIPCCore cameraInstance = TuyaIPCSdk.getCameraInstance();
        if (cameraInstance != null) {
            mCameraP2P = cameraInstance.createCameraP2P(devId);
            p2pType = cameraInstance.getP2PType(devId);
        }
        mVideoView.setViewCallback(new AbsVideoViewCallback() {
            @Override
            public void onCreated(Object o) {
                super.onCreated(o);
                if (null != mCameraP2P) {
                    mCameraP2P.generateCameraView(o);
                }
            }

        });
        mVideoView.createVideoView(p2pType);
        if ( mCameraP2P == null) {
            stopCamera();
            destroyCamera();
            mVideoView.setOnClickListener(null);
            muteImage.setOnClickListener(null);
            mVideoView.setVisibility(View.GONE);
            muteImg.setVisibility(View.GONE);
        }else{
            mCameraP2P.registerP2PCameraListener(p2pCameraListener);
            initDirectionControls();
        }
    }

    public void initDirectionControls(){
        if (querySupportByDPID(CameraConstants.PTZ_CONTROL)) {
            mVideoView.setOnRenderDirectionCallback(new OnRenderDirectionCallback() {

                @Override
                public void onLeft() {
                    publishDps(CameraConstants.PTZ_CONTROL, CameraConstants.PTZ_RIGHT);
                }

                @Override
                public void onRight() {
                    publishDps(CameraConstants.PTZ_CONTROL, CameraConstants.PTZ_LEFT);

                }

                @Override
                public void onUp() {
                    publishDps(CameraConstants.PTZ_CONTROL, CameraConstants.PTZ_UP);
                }

                @Override
                public void onDown() {
                    publishDps(CameraConstants.PTZ_CONTROL, CameraConstants.PTZ_DOWN);
                }

                @Override
                public void onCancel() {
                    publishDps(CameraConstants.PTZ_STOP, true);
                }
            });
        }
    }

    // Check if camera can rotate
    private boolean querySupportByDPID(String dpId) {
        DeviceBean deviceBean = TuyaHomeSdk.getDataInstance().getDeviceBean(devId);
        if (deviceBean != null) {
            Map<String, Object> dps = deviceBean.getDps();
            return dps != null && dps.get(dpId) != null;
        }
        return false;
    }

    // Send camera action
    private ITuyaDevice iTuyaDevice;
    private void publishDps(String dpId, Object value) {
        if (iTuyaDevice == null ) {
            iTuyaDevice = TuyaHomeSdk.newDeviceInstance(devId);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(dpId, value);
        String dps = jsonObject.toString();
        iTuyaDevice.publishDps(dps, new IResultCallback() {
            @Override
            public void onError(String code, String error) {
                Log.e(TAG, "publishDps err " + dps);
            }

            @Override
            public void onSuccess() {
                Log.i(TAG, "publishDps suc " + dps);
            }
        });
    }

    // Camera event receiver
    private AbsP2pCameraListener p2pCameraListener = new AbsP2pCameraListener() {
        @Override
        public void onReceiveSpeakerEchoData(ByteBuffer pcm, int sampleRate) {
            if (null != mCameraP2P) {
                int length = pcm.capacity();
                L.d(TAG, "receiveSpeakerEchoData pcmlength " + length + " sampleRate " + sampleRate);
                byte[] pcmData = new byte[length];
                pcm.get(pcmData, 0, length);
                mCameraP2P.sendAudioTalkData(pcmData, length);
            }
        }
    };

    // Video streaming from camera
    private void preview() {
        activeAction = true;
        mCameraP2P.startPreview(videoClarity, new OperationDelegateCallBack() {
            @Override
            public void onSuccess(int sessionId, int requestId, String data) {
                Log.e(TAG, "start preview onSuccess");
                isPlay = true;
                activeAction = false;
            }

            @Override
            public void onFailure(int sessionId, int requestId, int errCode) {
                Log.e(TAG, "start preview onFailure, errCode: " + errCode);
                isPlay = false;
                activeAction = false;
            }
        });
    }

    public void muteClick() {
        activeAction = true;
        muteImg.setClickable(false);
        int mute;
        mute = previewMute == ICameraP2P.MUTE ? ICameraP2P.UNMUTE : ICameraP2P.MUTE;
        mCameraP2P.setMute(mute, new OperationDelegateCallBack() {
            @Override
            public void onSuccess(int sessionId, int requestId, String data) {
                try {
                    previewMute = Integer.parseInt(data);
                }catch (Exception e){

                }
                mHandler.sendMessage(MessageUtil.getMessage(MSG_MUTE, ARG1_OPERATE_SUCCESS));
            }

            @Override
            public void onFailure(int sessionId, int requestId, int errCode) {
                mHandler.sendMessage(MessageUtil.getMessage(MSG_MUTE, ARG1_OPERATE_FAIL));
            }
        });
        muteImg.setClickable(true);
    }

    public void setVideoClarity() {
        activeAction = true;
        Log.e("","set clarity to " + videoClarity);
        videoClarity = videoClarity == ICameraP2P.HD ? ICameraP2P.STANDEND : ICameraP2P.HD;
        stopCamera();
        resumeCamera();
    }


    private final Handler mHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CONNECT:
                    handleConnect(msg);
                    activeAction = false;
                    break;
                case MSG_MUTE:
                    handleMute(msg);
                    activeAction = false;
                    break;
            }
            super.handleMessage(msg);
        }
    };


    private void handleMute(Message msg) {
        if (msg.arg1 == ARG1_OPERATE_SUCCESS) {
            muteImg.setSelected(previewMute == ICameraP2P.MUTE);
        } else {
            ToastUtil.shortToast(context, Resources.getSystem().getString(R.string.operation_failed));
        }
    }

    private void handleConnect(Message msg) {
        if (msg.arg1 == ARG1_OPERATE_SUCCESS) {
            preview();
        } else {
            //ToastUtil.shortToast(context, Resources.getSystem().getString(R.string.connect_failed));
        }
    }

    public String getDevId() {
        return devId;
    }
    public void setDevId(String devId) { this.devId = devId; }
    public int getVideoClarity() { return videoClarity; }
}
