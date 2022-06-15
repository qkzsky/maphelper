package com.autonavi.minimap

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.core.view.forEach

class MainActivity : AppCompatActivity() {

    private lateinit var packageNameView : EditText
    private lateinit var runButtonView: Button
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var customPlanView: Switch

    private lateinit var mapTypeGroup:RadioGroup
    private val defaultPkgName = "com.autonavi.amapautoclone"
    private val defaultCustomPlan = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()
        initListener()

        if(checkThirdParty()) {
            startApp(Properties.getProperties(this, "packageName", defaultPkgName))
        }
    }

    @SuppressLint("ResourceType")
    private fun initView() {
        packageNameView = findViewById<EditText>(R.id.packageName)
        runButtonView = findViewById<Button>(R.id.run)
        customPlanView = findViewById<Switch>(R.id.customPlan)
        mapTypeGroup = findViewById<RadioGroup>(R.id.mapTypeGroup)

        val packageName = Properties.getProperties(this, "packageName", defaultPkgName)
        packageNameView.setText(packageName)

        val customPlan = Properties.getProperties(this, "customPlan", defaultCustomPlan.toString())
        if (customPlanView.isChecked != customPlan.toBoolean()) {
            customPlanView.toggle()
        }

        val mapType = Properties.getProperties(this, "mapType", ThirdNavigationUtil.TYPE_AMAP_AUTO)
        mapTypeGroup.findViewWithTag<RadioButton>(mapType).toggle()
    }

    private fun initListener() {
        val context = this
        runButtonView.setOnClickListener {
            runTest()
        }

        customPlanView.setOnCheckedChangeListener { _, isChecked ->
            run {
                Properties.setProperties(context, "customPlan", isChecked.toString())
            }
        }

        mapTypeGroup.setOnCheckedChangeListener { _, checkedId ->
            run {
                val checkedTag = findViewById<RadioButton>(checkedId).tag.toString()
                Properties.setProperties(context, "mapType", checkedTag)
            }
        }

    }

    private fun checkThirdParty() :Boolean {
        val key = intent.getStringExtra("key")
        val packageName = intent.getStringExtra("packageName")
        if(TextUtils.isEmpty(key) || TextUtils.isEmpty(packageName)) {
            if (intent.sourceBounds != null) {
                return false
            }
        }

        return true
    }

    private fun startApp(packageName : String) {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            try {
                startActivity(intent)
                finish()
            }catch (e : ActivityNotFoundException) {
                Toast.makeText(this, "App not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun runTest() {
        val packageName = packageNameView.text.toString()
        Properties.setProperties(this, "packageName", packageName)

        try {
            val dlon = 114.302467
            val dlat = 30.544649
            val dname = "黄鹤楼"

            val mapType = Properties.getProperties(this, "mapType", ThirdNavigationUtil.TYPE_AMAP_AUTO)
            ThirdNavigationUtil.openThirdAppNavigation(this, mapType, dname, dlon, dlat, packageName)

        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            Toast.makeText(this, "App not found", Toast.LENGTH_SHORT).show()
        }
    }
}