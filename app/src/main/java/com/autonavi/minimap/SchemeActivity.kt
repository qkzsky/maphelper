package com.autonavi.minimap

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.time.Instant
import java.time.LocalDateTime

@SuppressLint("CustomSplashScreen")
class SchemeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.scheme)

        val intent = intent ?: return

        if(!isTaskRoot) {
            val flags = intent.flags and Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT

            if (isLauncher() && flags == Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) {
                finish()
                return
            }
        }

        // 解析scheme
        val uri = intent.data
        val dName = uri?.getQueryParameter("dname").toString()
        val dLon = uri?.getQueryParameter("dlon")?.toDouble()
        val dLat = uri?.getQueryParameter("dlat")?.toDouble()
        val packageName = Properties.getProperties(this, "packageName", ThirdNavigationUtil.TYPE_AMAP_AUTO)
        log(uri)

        if (dLon == null || dLat == null) {
            return
        }

        try {
            ThirdNavigationUtil.openThirdAppNavigation(this, ThirdNavigationUtil.TYPE_AMAP_AUTO, dName, dLon, dLat, packageName)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            Toast.makeText(this, "App not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isLauncher(): Boolean {
        val action = intent.action
        if (TextUtils.isEmpty(action) || !action.equals(Intent.ACTION_MAIN)) {
            return false
        }
        val categories = intent.categories
        if (categories.isNotEmpty()) {
            for (cate in categories) {
                if (cate.equals(Intent.CATEGORY_LAUNCHER)) {
                    return true
                }
            }
        }
        return false
    }

    private fun log(logValue: Any?) {
        if(logValue == null) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            findViewById<TextView>(R.id.log).append("["+ LocalDateTime.now().toString() +"]" + logValue.toString() + "\n")
        }
    }
}