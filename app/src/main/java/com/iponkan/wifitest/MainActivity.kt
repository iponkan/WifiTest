package com.iponkan.wifitest

import android.Manifest
import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import ru.alexbykov.nopermission.PermissionHelper


class MainActivity : AppCompatActivity() {

    private lateinit var permissionHelper: PermissionHelper
    // WIFI状态监听器
    private lateinit var wifiStateChangeListener: WifiStateReceiver.WifiStateChangeListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        permissionHelper = PermissionHelper(this)
        permissionHelper.check(Manifest.permission.ACCESS_FINE_LOCATION)
                .onSuccess {
                    init()
                }
                .onDenied {
                    ToastUtil.showToastShort(this, "权限被拒绝，9.0及以上系统无法获取WIFI正确信息")
                    init()
                }
                .onNeverAskAgain {
                    ToastUtil.showToastShort(this, "权限被拒绝，9.0及以上系统无法获取WIFI正确信息,下次不会在询问了")
                    init()
                }
                .run()
    }

    // WIFI广播接收器
    private lateinit var wifiStateReceiver: WifiStateReceiver

    private fun init() {
        wifiStateChangeListener = object : WifiStateReceiver.WifiStateChangeListenerImpl() {
            // WIFI断开
            override fun onDisconnect() {
                // 清除WIFI信息
                main_now_wifi_connect_status.text = this@MainActivity.getString(R.string.no)
                main_now_wifi_name.text = ""
                main_now_wifi_bssid.text = ""
                main_now_wifi_ip.text = ""
            }

            // WIFI连接成功
            override fun onConnected() {
                ToastUtil.showToastShort(this@MainActivity, "refresh")
                val wifiInfo = (this@MainActivity.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager).connectionInfo
                // 设置WIFI信息
                main_now_wifi_connect_status.text = this@MainActivity.getString(R.string.yes)
                main_now_wifi_name.text = wifiInfo.ssid.replace("\"", "")
                main_now_wifi_bssid.text = wifiInfo.bssid
                main_now_wifi_ip.text = wifiInfo.ipAddress.toString()
            }
        }
        // 初始化WIFI广播接收器
        wifiStateReceiver = WifiStateReceiver(wifiStateChangeListener)
        registerWifiStateReceiver()
    }

    /**
     * @Title: registerWifiStateReceiver
     * @Class: MainActivity
     * @Description: 注册WIFI广播接收器
     * @author XueLong xuelongqy@foxmail.com
     * @date 2018/7/5 10:58
     * @update_author
     * @update_time
     * @version V1.0
     * @param
     * @return
     * @throws
     */
    private fun registerWifiStateReceiver() {
        // 判断WIFI广播接收器是否注册过
        val mFilter = IntentFilter()
        mFilter.addAction(WifiManager.RSSI_CHANGED_ACTION) //信号强度变化
        mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION) //网络状态变化
        mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION) //wifi状态，是否连上，密码
        mFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION) //是不是正在获得IP地址
        mFilter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION)
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        this.registerReceiver(wifiStateReceiver, mFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unRegisterWifiStateReceiver()
    }

    /**
     * @Title: unRegisterWifiStateReceiver方法
     * @Class: MainActivity
     * @Description: 注销WIFI广播接收器
     * @author XueLong xuelongqy@foxmail.com
     * @date 2018/7/5 10:59
     * @update_author
     * @update_time
     * @version V1.0
     * @param
     * @return
     * @throws
     */
    private fun unRegisterWifiStateReceiver() {
        this.unregisterReceiver(wifiStateReceiver)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
