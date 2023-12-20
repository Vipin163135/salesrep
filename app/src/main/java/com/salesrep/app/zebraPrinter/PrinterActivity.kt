package com.salesrep.app.zebraPrinter

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.salesrep.app.R
import com.zebra.sdk.comm.BluetoothConnection
import com.zebra.sdk.comm.Connection
import com.zebra.sdk.comm.ConnectionException
import com.zebra.sdk.comm.TcpConnection
import com.zebra.sdk.printer.*
import com.zebra.sdk.util.internal.Sleeper.sleep
import java.lang.Thread.sleep

class PrinterActivity: AppCompatActivity() {

    var connection: Connection? = null

    var btRadioButton: RadioButton? = null
    var macAddressEditText: EditText? = null
    var ipAddressEditText: EditText? = null
    var portNumberEditText: EditText? = null
    val bluetoothAddressKey = "ZEBRA_DEMO_BLUETOOTH_ADDRESS"
    val tcpAddressKey = "ZEBRA_DEMO_TCP_ADDRESS"
    val tcpPortKey = "ZEBRA_DEMO_TCP_PORT"
    val PREFS_NAME = "OurSavedAddress"

    var testButton: Button? = null
    var printer: ZebraPrinter? = null

    var statusField: TextView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_print)
        val settings: SharedPreferences = getSharedPreferences(PREFS_NAME, 0)
        ipAddressEditText = this.findViewById(R.id.ipAddressInput)
        val ip = settings.getString(tcpAddressKey, "")
        ipAddressEditText!!.setText(ip)
        portNumberEditText = this.findViewById(R.id.portInput)
        val port = settings.getString(tcpPortKey, "")
        portNumberEditText!!.setText(port)
        macAddressEditText = this.findViewById(R.id.macInput)
        val mac = settings.getString(bluetoothAddressKey, "")
        macAddressEditText!!.setText(mac)

        statusField = this.findViewById(R.id.statusText)
        btRadioButton = this.findViewById(R.id.bluetoothRadio)
        val radioGroup = this.findViewById(R.id.radioGroup) as RadioGroup
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.bluetoothRadio) {
                toggleEditField(macAddressEditText!!, true)
                toggleEditField(portNumberEditText!!, false)
                toggleEditField(ipAddressEditText!!, false)
            } else {
                toggleEditField(portNumberEditText!!, true)
                toggleEditField(ipAddressEditText!!, true)
                toggleEditField(macAddressEditText!!, false)
            }
        }
        testButton = this.findViewById(R.id.testButton)
        testButton!!.setOnClickListener {
            Thread {
                enableTestButton(false)
                doConnectionTest()
            }.start()
        }
    }


//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu)
//        return true
//    }


    fun connect(): ZebraPrinter? {
        setStatus("Connecting...", Color.YELLOW)
        connection = null
        if (isBluetoothSelected()) {
            connection = BluetoothConnection(getMacAddressFieldText())
            SettingsHelper.saveBluetoothAddress(this, getMacAddressFieldText())
        } else {
            try {
                val port: Int = getTcpPortNumber()!!.toInt()
                connection = TcpConnection(getTcpAddress(), port)
                SettingsHelper.saveIp(this, getTcpAddress())
                SettingsHelper.savePort(this, getTcpPortNumber())
            } catch (e: NumberFormatException) {
                setStatus("Port Number Is Invalid", Color.RED)
                return null
            }
        }
        try {
            connection!!.open()
            setStatus("Connected", Color.GREEN)
        } catch (e: ConnectionException) {
            setStatus("Comm Error! Disconnecting", Color.RED)
            DemoSleeper.sleep(1000)
            disconnect()
        }
        var printer: ZebraPrinter? = null
        if (connection!!.isConnected()) {
            try {
                printer = ZebraPrinterFactory.getInstance(connection)
                setStatus("Determining Printer Language", Color.YELLOW)
                val pl: String = SGD.GET("device.languages", connection)
                setStatus("Printer Language $pl", Color.BLUE)
            } catch (e: ConnectionException) {
                setStatus("Unknown Printer Language", Color.RED)
                printer = null
                DemoSleeper.sleep(1000)
                disconnect()
            } catch (e: ZebraPrinterLanguageUnknownException) {
                setStatus("Unknown Printer Language", Color.RED)
                printer = null
                DemoSleeper.sleep(1000)
                disconnect()
            }
        }
        return printer
    }

    fun disconnect() {
        try {
            setStatus("Disconnecting", Color.RED)
            if (connection != null) {
                connection!!.close()
            }
            setStatus("Not Connected", Color.RED)
        } catch (e: ConnectionException) {
            setStatus("COMM Error! Disconnected", Color.RED)
        } finally {
            enableTestButton(true)
        }
    }

    fun setStatus(statusMessage: String, color: Int) {
        runOnUiThread(Runnable {
            statusField!!.setBackgroundColor(color)
            statusField!!.text = statusMessage
        })
        DemoSleeper.sleep(1000)
    }


    fun sendTestLabel() {
        try {
            val linkOsPrinter: ZebraPrinterLinkOs = ZebraPrinterFactory.createLinkOsPrinter(printer)
            val printerStatus: PrinterStatus =
                if (linkOsPrinter != null) linkOsPrinter.getCurrentStatus() else printer!!.getCurrentStatus()
            if (printerStatus.isReadyToPrint) {
                val configLabel: ByteArray = getConfigLabel()?: ByteArray(0)
                connection!!.write(configLabel)
                setStatus("Sending Data", Color.BLUE)
            } else if (printerStatus.isHeadOpen) {
                setStatus("Printer Head Open", Color.RED)
            } else if (printerStatus.isPaused) {
                setStatus("Printer is Paused", Color.RED)
            } else if (printerStatus.isPaperOut) {
                setStatus("Printer Media Out", Color.RED)
            }
            DemoSleeper.sleep(1500)
            if (connection is BluetoothConnection) {
                val friendlyName: String = (connection as BluetoothConnection?)!!.getFriendlyName()
                setStatus(friendlyName, Color.MAGENTA)
                DemoSleeper.sleep(500)
            }
        } catch (e: ConnectionException) {
            setStatus(e.message!!, Color.RED)
        } finally {
            disconnect()
        }
    }

    fun enableTestButton(enabled: Boolean) {
        runOnUiThread(Runnable { testButton!!.isEnabled = enabled })
    }

    /*
   * Returns the command for a test label depending on the printer control language
   * The test label is a box with the word "TEST" inside of it
   *
   * _________________________
   * |                       |
   * |                       |
   * |        TEST           |
   * |                       |
   * |                       |
   * |_______________________|
   *
   *
   */
    fun getConfigLabel(): ByteArray? {
        var configLabel: ByteArray? = null
        try {
            val printerLanguage: PrinterLanguage = printer!!.getPrinterControlLanguage()
            SGD.SET("device.languages", "zpl", connection)
            if (printerLanguage === PrinterLanguage.ZPL) {
                configLabel =
                    "^XA^FO17,16^GB379,371,8^FS^FT65,255^A0N,135,134^FDTEST^FS^XZ".toByteArray()
            } else if (printerLanguage === PrinterLanguage.CPCL) {
                val cpclConfigLabel = """
                ! 0 200 200 406 1
                ON-FEED IGNORE
                BOX 20 20 380 380 8
                T 0 6 137 177 TEST
                PRINT
                
                """.trimIndent()
                configLabel = cpclConfigLabel.toByteArray()
            }
        } catch (e: ConnectionException) {
            Toast.makeText(this,"Printer Connection Exception",Toast.LENGTH_SHORT).show()
        }
        return configLabel
    }


    fun doConnectionTest() {
        printer = connect()
        if (printer != null) {
            sendTestLabel()
        } else {
            disconnect()
        }
    }

    fun toggleEditField(editText: EditText, set: Boolean) {
        /*
         * Note: Disabled EditText fields may still get focus by some other means, and allow text input.
         *       See http://code.google.com/p/android/issues/detail?id=2771
         */
        editText.isEnabled = set
        editText.isFocusable = set
        editText.isFocusableInTouchMode = set
    }

    fun isBluetoothSelected(): Boolean {
        return btRadioButton!!.isChecked
    }

    fun getMacAddressFieldText(): String? {
        return macAddressEditText!!.text.toString()
    }

    fun getTcpAddress(): String? {
        return ipAddressEditText!!.text.toString()
    }

    fun getTcpPortNumber(): String? {
        return portNumberEditText!!.text.toString()
    }


}
