package com.salesrep.app.zebraPrinter

import android.printservice.PrinterDiscoverySession
import android.print.PrinterId
import android.print.PrinterCapabilitiesInfo
import android.print.PrintAttributes
import android.print.PrintAttributes.Resolution
import android.print.PrinterInfo
import android.printservice.PrintJob
import android.printservice.PrintService
import android.util.Log
import java.io.FileInputStream
import java.io.IOException
import java.lang.StringBuilder
import java.util.ArrayList

class MyPrintService : PrintService() {

    override fun onCreatePrinterDiscoverySession(): PrinterDiscoverySession {
        Log.d("myprinter", "MyPrintService#onCreatePrinterDiscoverySession() called")
        return object : PrinterDiscoverySession() {
            override fun onStartPrinterDiscovery(priorityList: List<PrinterId>) {
                Log.d(
                    "myprinter",
                    "PrinterDiscoverySession#onStartPrinterDiscovery(priorityList: $priorityList) called"
                )
                if (!priorityList.isEmpty()) {
                    return
                }
                val printers: MutableList<PrinterInfo> = ArrayList()
                val printerId = generatePrinterId("aaa")
                val builder =
                    PrinterInfo.Builder(printerId, "dummy printer", PrinterInfo.STATUS_IDLE)
                val capBuilder = PrinterCapabilitiesInfo.Builder(printerId)
                capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_A4, true)
                capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_A3, false)
                capBuilder.addResolution(
                    Resolution("resolutionId", "default resolution", 600, 600),
                    true
                )
                capBuilder.setColorModes(
                    PrintAttributes.COLOR_MODE_COLOR or PrintAttributes.COLOR_MODE_MONOCHROME,
                    PrintAttributes.COLOR_MODE_COLOR
                )
                builder.setCapabilities(capBuilder.build())
                printers.add(builder.build())
                addPrinters(printers)
            }

            override fun onStopPrinterDiscovery() {
                Log.d("myprinter", "MyPrintService#onStopPrinterDiscovery() called")
            }

            override fun onValidatePrinters(printerIds: List<PrinterId>) {
                Log.d(
                    "myprinter",
                    "MyPrintService#onValidatePrinters(printerIds: $printerIds) called"
                )
            }

            override fun onStartPrinterStateTracking(printerId: PrinterId) {
                Log.d(
                    "myprinter",
                    "MyPrintService#onStartPrinterStateTracking(printerId: $printerId) called"
                )
            }

            override fun onStopPrinterStateTracking(printerId: PrinterId) {
                Log.d(
                    "myprinter",
                    "MyPrintService#onStopPrinterStateTracking(printerId: $printerId) called"
                )
            }

            override fun onDestroy() {
                Log.d("myprinter", "MyPrintService#onDestroy() called")
            }
        }
    }

    override fun onPrintJobQueued(printJob: PrintJob) {
        Log.d("myprinter", "queued: " + printJob.id.toString())
        printJob.start()
        val document = printJob.document
        val `in` = FileInputStream(document.data!!.fileDescriptor)
        try {
            val buffer = ByteArray(4)
            val read = `in`.read(buffer)
            Log.d("myprinter", "first " + buffer.size + "bytes of content: " + toString(buffer))
        } catch (e: IOException) {
            Log.d("myprinter", "", e)
        } finally {
            try {
                `in`.close()
            } catch (e: IOException) {
                assert(true)
            }
        }
        printJob.complete()
    }

    override fun onRequestCancelPrintJob(printJob: PrintJob) {
        Log.d("myprinter", "canceled: " + printJob.id.toString())
        printJob.cancel()
    }

    companion object {
        private fun toString(bytes: ByteArray): String {
            val sb = StringBuilder()
            for (b in bytes) {
                sb.append(java.lang.Byte.toString(b)).append(',')
            }
            if (sb.length != 0) {
                sb.setLength(sb.length - 1)
            }
            return sb.toString()
        }
    }
}