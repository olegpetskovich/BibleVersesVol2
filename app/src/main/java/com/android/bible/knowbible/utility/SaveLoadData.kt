package com.android.bible.knowbible.utility

import android.content.Context
import android.content.SharedPreferences

class SaveLoadData(context: Context) {
    private val preferences: SharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)

    fun saveInt(key: String, value: Int) {
        // Извлеките редактор, чтобы изменить Общие настройки.

        val prefEditor = preferences.edit()
        prefEditor.putInt(key, value)

        prefEditor.apply()
    }

    fun loadInt(key: String): Int {
        return preferences.getInt(key, -1)
    }

    fun saveLong(key: String, value: Long) {
        // Извлеките редактор, чтобы изменить Общие настройки.

        val prefEditor = preferences.edit()
        prefEditor.putLong(key, value)

        prefEditor.apply()
    }

    fun loadLong(key: String): Long {
        return preferences.getLong(key, -1)
    }

    fun saveString(key: String, value: String) {
        // Извлеките редактор, чтобы изменить Общие настройки.
        val prefEditor = preferences.edit()
        prefEditor.putString(key, value)

        prefEditor.apply()
    }

    fun loadString(key: String): String? {
        return preferences.getString(key, "")
    }

    fun saveBoolean(key: String, state: Boolean) {
        // Извлеките редактор, чтобы изменить Общие настройки.

        val prefEditor = preferences.edit()
        prefEditor.putBoolean(key, state)

        prefEditor.apply()
    }

    fun loadBoolean(key: String): Boolean {
        return preferences.getBoolean(key, false)
    }

    fun isApplicationFirstRun(key: String, value: Boolean): Boolean {
        return preferences.getBoolean(key, value)
    }
}