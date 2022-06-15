package com.autonavi.minimap

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri


class ThirdNavigationUtil {
    companion object{

        const val TYPE_AMAP_MOBILE = "amap_mobile"
        const val TYPE_AMAP_AUTO = "amap_auto"

        const val AMAP_MOBILE_PKG = "com.autonavi.minimap"
        const val AMAP_AUTO_PKG = "com.autonavi.amapauto"

        // 导航方式(0 速度快; 1 费用少; 2 路程短; 3 不走高速；4 躲避拥堵；5 不走高速且避免收费；6 不走高速且躲避拥堵；7 躲避收费和拥堵；8不走高速躲避收费和拥堵
        const val NAVI_STYLE = 0

        fun openThirdAppNavigation(c: Context, mType: String, dName : String, dLon :Double, dLat: Double, pkgName : String?) {
            if (pkgName == "") {
                println("pkgName is empty")
                return
            }

            if (mType == TYPE_AMAP_MOBILE) {
                val uriStr = if (Properties.getProperties(c, "customPlan", "true").toBoolean()) {
                    "amapuri://route/plan/?did=BGVIS2&dname=${Uri.encode(dName)}&dlon=${dLon}&dlat=${dLat}&dev=0&t=0"
                } else {
                    "androidamap://navi?sourceApplication=${c.getString(R.string.app_name)}&poiname=${Uri.encode(dName)}&lon=${dLon}&lat=${dLat}&dev=0&style={$NAVI_STYLE}"
                }

                // 高德地图手机版
                val packageName = pkgName ?: AMAP_MOBILE_PKG
                val launchIntent = Intent(Intent.ACTION_VIEW)
                launchIntent.setPackage(packageName)
                launchIntent.data = Uri.parse(uriStr)
                startActivity(c, launchIntent)

            } else if (mType == TYPE_AMAP_AUTO) {
                // 高德地图车机版
                val packageName = pkgName ?: AMAP_AUTO_PKG
                val launchIntent = Intent(Intent.ACTION_VIEW)

                launchIntent.component = ComponentName.createRelative(packageName,
                    "com.autonavi.auto.remote.fill.UsbFillActivity")
                startActivity(c, launchIntent)
                sendBroadcast(c, packageName, dName, dLon, dLat)
            }
        }

        @SuppressLint("QueryPermissionsNeeded")
        private fun startActivity(c: Context, mapIntent: Intent) {
            val isValid = c.packageManager.queryIntentActivities(mapIntent, 0).isNotEmpty()
            if (isValid) {
                return
            }

            //如果已经启动apk，则直接将apk从后台调到前台运行（类似home键之后再点击apk图标启动），如果未启动apk，则重新启动
            mapIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or Intent.FLAG_ACTIVITY_NEW_TASK)
            c.startActivity(mapIntent)
        }

        private fun sendBroadcast(c: Context, packageName : String, dName: String, dLon: Double, dLat: Double) {
            Thread{
                try {
                    Thread.sleep(2000)
                } catch (e:InterruptedException) {
                    e.printStackTrace()
                }

                val intent = Intent(Intent.ACTION_VIEW)
                // intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                intent.setComponent(ComponentName.createRelative(packageName,
                    "com.autonavi.amapauto.adapter.internal.AmapAutoBroadcastReceiver"))
                intent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV")

                if (Properties.getProperties(c, "customPlan", "true").toBoolean()) {
                    // https://lbs.amap.com/api/amap-auto/guide/android/navi
                    // 10007 规划导航
                    intent.putExtra("KEY_TYPE", 10007)
                    intent.putExtra("EXTRA_DNAM", dName)
                    intent.putExtra("ENTRY_LON", dLon)
                    intent.putExtra("ENTRY_LAT", dLat)
                    intent.putExtra("EXTRA_DEV", 0)
                    intent.putExtra("EXTRA_M", -1)
                }else{
                    // 10038 直接发起导航，不用规划
                    intent.flags = Intent.FLAG_INCLUDE_STOPPED_PACKAGES
                    intent.putExtra("KEY_TYPE", 10038)
                    intent.putExtra("POINAME", dName)
                    intent.putExtra("LON", dLon)
                    intent.putExtra("LAT", dLat)
                    intent.putExtra("DEV", 0)
                    intent.putExtra("STYLE", NAVI_STYLE)
                    intent.putExtra("SOURCE_APP", c.getString(R.string.app_name))
                }

                c.sendBroadcast(intent)
            }.run()
        }
    }
}