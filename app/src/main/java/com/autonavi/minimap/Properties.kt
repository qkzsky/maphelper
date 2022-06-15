package com.autonavi.minimap

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity

class Properties {
    companion object{
        private val configName = "global.properties"

        fun getProperties(c: Context, key: String, defaultValue : String?): String {
            val sp = c.getSharedPreferences(configName, AppCompatActivity.MODE_PRIVATE)

            var result = sp.getString(key, null).toString()
            if (result == "null" && defaultValue != null) {
                result = defaultValue
                setProperties(c, key, result)
            }
            return result
        }

        @SuppressLint("CommitPrefEdits")
        fun setProperties(c: Context, key: String, value: String) {
            val sp = c.getSharedPreferences(configName, AppCompatActivity.MODE_PRIVATE)
            val editor = sp.edit()
            editor.putString(key, value)
            editor.apply()
        }
    }
}
