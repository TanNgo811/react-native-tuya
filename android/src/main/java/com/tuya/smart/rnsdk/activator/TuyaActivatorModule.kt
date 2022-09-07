package com.tuya.smart.rnsdk.activator


import android.content.Intent
import android.provider.Settings
import com.facebook.react.bridge.*
import com.tuya.smart.android.ble.api.ScanType
import com.tuya.smart.android.common.utils.WiFiUtil
import com.tuya.smart.home.sdk.TuyaHomeSdk
import com.tuya.smart.home.sdk.builder.ActivatorBuilder
import com.tuya.smart.home.sdk.builder.TuyaCameraActivatorBuilder
import com.tuya.smart.home.sdk.builder.TuyaGwSubDevActivatorBuilder
import com.tuya.smart.rnsdk.utils.Constant.DEVID
import com.tuya.smart.rnsdk.utils.Constant.HOMEID
import com.tuya.smart.rnsdk.utils.Constant.PASSWORD
import com.tuya.smart.rnsdk.utils.Constant.SSID
import com.tuya.smart.rnsdk.utils.Constant.TIME
import com.tuya.smart.rnsdk.utils.Constant.TOKEN
import com.tuya.smart.rnsdk.utils.Constant.TYPE
import com.tuya.smart.rnsdk.utils.ReactParamsCheck
import com.tuya.smart.rnsdk.utils.TuyaReactUtils
import com.tuya.smart.sdk.api.*
import com.tuya.smart.sdk.bean.DeviceBean
import com.tuya.smart.sdk.bean.MultiModeActivatorBean
import com.tuya.smart.sdk.enums.ActivatorModelEnum


class TuyaActivatorModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

  var mITuyaActivator: ITuyaActivator? = null
  var mTuyaGWActivator: ITuyaActivator? = null
  var mTuyaCameraActivator: ITuyaCameraDevActivator? = null
  override fun getName(): String {
    return "TuyaActivatorModule"
  }

  @ReactMethod
  fun startBluetoothScan(promise: Promise) {
    TuyaHomeSdk.getBleOperator().startLeScan(60000, ScanType.SINGLE
    ) { bean -> promise.resolve(TuyaReactUtils.parseToWritableMap(bean)) };
  }

  @ReactMethod
  fun stopBluetoothScan() {
    TuyaHomeSdk.getBleOperator().stopLeScan();
  }


  @ReactMethod
  fun initBluetoothDualModeActivator(params: ReadableMap, promise: Promise) {
    if (ReactParamsCheck.checkParams(arrayOf(HOMEID, SSID, PASSWORD), params)) {

      TuyaHomeSdk.getBleOperator().startLeScan(60000, ScanType.SINGLE
      ) { bean ->
        params.getDouble(HOMEID).toLong().let {
          TuyaHomeSdk.getActivatorInstance()
            .getActivatorToken(it, object : ITuyaActivatorGetToken {
              override fun onSuccess(token: String) {
                val multiModeActivatorBean = MultiModeActivatorBean();
                multiModeActivatorBean.ssid = params.getString(SSID);
                multiModeActivatorBean.pwd = params.getString(PASSWORD);

                multiModeActivatorBean.uuid = bean.getUuid();
                multiModeActivatorBean.deviceType = bean.getDeviceType();
                multiModeActivatorBean.mac = bean.getMac();
                multiModeActivatorBean.address = bean.getAddress();


                multiModeActivatorBean.homeId = params.getDouble(HOMEID).toLong();
                multiModeActivatorBean.token = token;
                multiModeActivatorBean.timeout = 180000;
                multiModeActivatorBean.phase1Timeout = 60000;

                TuyaHomeSdk.getActivator().newMultiModeActivator()
                  .startActivator(multiModeActivatorBean, object : IMultiModeActivatorListener {
                    override fun onSuccess(bean: DeviceBean) {
                      promise.resolve(TuyaReactUtils.parseToWritableMap(bean));
                    }

                    override fun onFailure(code: Int, msg: String?, handle: Any?) {
                      promise.reject(code.toString(), msg);
                    }
                  });
              }

              override fun onFailure(s: String, s1: String) {
                promise.reject(s, s1);
              }
            })
        }
      };
    }
  }


  @ReactMethod
  fun getCurrentWifi(params: ReadableMap, successCallback: Callback,
                     errorCallback: Callback) {
    successCallback.invoke(WiFiUtil.getCurrentSSID(reactApplicationContext.applicationContext));
  }

  @ReactMethod
  fun openNetworkSettings(params: ReadableMap) {
    val currentActivity = currentActivity ?: return
    try {
      currentActivity.startActivity(Intent(Settings.ACTION_SETTINGS))
    } catch (e: Exception) {
    }

  }

  @ReactMethod
  fun initActivator(params: ReadableMap, promise: Promise) {
    if (ReactParamsCheck.checkParams(arrayOf(HOMEID, SSID, PASSWORD, TIME, TYPE), params)) {
      TuyaHomeSdk.getActivatorInstance().getActivatorToken(params.getDouble(HOMEID).toLong(), object : ITuyaActivatorGetToken {
        override fun onSuccess(token: String) {
          mITuyaActivator = TuyaHomeSdk.getActivatorInstance().newActivator(ActivatorBuilder()
            .setSsid(params.getString(SSID))
            .setContext(reactApplicationContext.applicationContext)
            .setPassword(params.getString(PASSWORD))
            .setActivatorModel(ActivatorModelEnum.valueOf(params.getString(TYPE) as String))
            .setTimeOut(params.getInt(TIME).toLong())
            .setToken(token).setListener(getITuyaSmartActivatorListener(promise)))
          mITuyaActivator?.start()
        }


        override fun onFailure(s: String, s1: String) {
          promise.reject(s, s1)
        }
      })
    }

  }

  /**
   * Get Activator Token Generator
   */
  @ReactMethod
  fun getInitActivatorToken(params: ReadableMap, promise: Promise) {
    if (ReactParamsCheck.checkParams(arrayOf(HOMEID, SSID), params)) {
      TuyaHomeSdk.getActivatorInstance().getActivatorToken(params.getDouble(HOMEID).toLong(), object : ITuyaActivatorGetToken {
        override fun onSuccess(token: String) {
          println("qrCodeUrl " + token)
          promise.resolve(token)
        }

        override fun onFailure(errorCode: String?, errorMsg: String?) {
          promise.reject(errorCode, errorMsg)
        }
      })
    }
  }

  /**
   * QR Code Activator
   */
  @ReactMethod
  fun initQrCodeActivator(params: ReadableMap, promise: Promise) {
    if (ReactParamsCheck.checkParams(arrayOf(SSID, PASSWORD, TOKEN, TIME), params)) {
      val builder = TuyaCameraActivatorBuilder()
        .setToken(params.getString(TOKEN))
        .setTimeOut(params.getDouble(TIME).toLong())
        .setSsid(params.getString(SSID))
        .setContext(reactApplicationContext.applicationContext)
        .setPassword(params.getString(PASSWORD))
        .setListener(object : ITuyaSmartCameraActivatorListener {
          override fun onQRCodeSuccess(qrcodeUrl: String) {
            try {
              //
            } catch (e: Exception) {
              e.printStackTrace()
            }
          }

          override fun onError(errorCode: String, errorMsg: String) {
            //
            promise.reject(errorCode, errorMsg)
          }

          override fun onActiveSuccess(devResp: DeviceBean) {
            //
            println("qrCodeUrl ${devResp.name}")
            println("qrCodeUrl ${devResp.devId}")
            promise.resolve(TuyaReactUtils.parseToWritableMap(devResp))
          }
        })

      mTuyaCameraActivator = TuyaHomeSdk.getActivatorInstance().newCameraDevActivator(builder)
      mTuyaCameraActivator?.createQRCode()
      mTuyaCameraActivator?.start()
    }
  }


  /**
   * ZigBee子设备配网需要ZigBee网关设备云在线的情况下才能发起,且子设备处于配网状态。
   */
  @ReactMethod
  fun newGwSubDevActivator(params: ReadableMap, promise: Promise) {
    if (ReactParamsCheck.checkParams(arrayOf(DEVID, TIME), params)) {
      val builder = TuyaGwSubDevActivatorBuilder()
        //设置网关ID
        .setDevId(params.getString(DEVID))
        //设置配网超时时间
        .setTimeOut(params.getInt(TIME).toLong())
        .setListener(object : ITuyaSmartActivatorListener {
          override fun onError(var1: String, var2: String) {
            promise.reject(var1, var2)
          }

          /**
           * 设备配网成功,且设备上线（手机可以直接控制），可以通过
           */
          override fun onActiveSuccess(var1: DeviceBean) {
            promise.resolve(TuyaReactUtils.parseToWritableMap(var1))
          }

          /**
           * device_find 发现设备
          device_bind_success 设备绑定成功，但还未上线，此时设备处于离线状态，无法控制设备。
           */
          override fun onStep(var1: String, var2: Any) {
            // promise.reject(var1,"")
          }
        })

      mTuyaGWActivator = TuyaHomeSdk.getActivatorInstance().newGwSubDevActivator(builder)
    }
  }

  @ReactMethod
  fun stopConfig() {
    mITuyaActivator?.stop()
    mTuyaGWActivator?.stop()
    mTuyaCameraActivator?.stop()
  }

  @ReactMethod
  fun onDestory() {
    mITuyaActivator?.onDestroy()
    mTuyaGWActivator?.onDestroy()
    mTuyaCameraActivator?.onDestroy()
  }

  fun getITuyaSmartActivatorListener(promise: Promise): ITuyaSmartActivatorListener {
    return object : ITuyaSmartActivatorListener {
      /**
       * 1001        网络错误
      1002        配网设备激活接口调用失败，接口调用不成功
      1003        配网设备激活失败，设备找不到。
      1004        token 获取失败
      1005        设备没有上线
      1006        配网超时
       */
      override fun onError(var1: String, var2: String) {
        promise.reject(var1, var2)
      }

      /**
       * 设备配网成功,且设备上线（手机可以直接控制），可以通过
       */
      override fun onActiveSuccess(var1: DeviceBean) {
        promise.resolve(TuyaReactUtils.parseToWritableMap(var1))
      }

      /**
       * device_find 发现设备
      device_bind_success 设备绑定成功，但还未上线，此时设备处于离线状态，无法控制设备。
       */
      override fun onStep(var1: String, var2: Any) {
        // IOS 没有onStep保持一致
        //promise.reject(var1,"")
      }
    }
  }
}
