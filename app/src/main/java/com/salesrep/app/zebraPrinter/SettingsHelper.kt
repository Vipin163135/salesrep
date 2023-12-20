package com.salesrep.app.zebraPrinter

import android.content.Context

class SettingsHelper {

    companion object {
        val PREFS_NAME = "OurSavedAddress"
        val bluetoothAddressKey = "ZEBRA_DEMO_BLUETOOTH_ADDRESS"
        val tcpAddressKey = "ZEBRA_DEMO_TCP_ADDRESS"
        val tcpPortKey = "ZEBRA_DEMO_TCP_PORT"

        fun getIp(context: Context): String? {
            val settings = context.getSharedPreferences(PREFS_NAME, 0)
            return settings.getString(tcpAddressKey, "")
        }

        fun getPort(context: Context): String? {
            val settings = context.getSharedPreferences(PREFS_NAME, 0)
            return settings.getString(tcpPortKey, "")
        }

        fun getBluetoothAddress(context: Context): String? {
            val settings = context.getSharedPreferences(PREFS_NAME, 0)
            return settings.getString(bluetoothAddressKey, "")
        }

        fun saveIp(context: Context, ip: String?) {
            val settings = context.getSharedPreferences(PREFS_NAME, 0)
            val editor = settings.edit()
            editor.putString(tcpAddressKey, ip)
            editor.commit()
        }

        fun savePort(context: Context, port: String?) {
            val settings = context.getSharedPreferences(PREFS_NAME, 0)
            val editor = settings.edit()
            editor.putString(tcpPortKey, port)
            editor.commit()
        }

        fun saveBluetoothAddress(context: Context, address: String?) {
            val settings = context.getSharedPreferences(PREFS_NAME, 0)
            val editor = settings.edit()
            editor.putString(bluetoothAddressKey, address)
            editor.commit()
        }
    }
}