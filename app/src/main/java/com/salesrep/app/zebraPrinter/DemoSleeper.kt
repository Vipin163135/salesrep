package com.salesrep.app.zebraPrinter

class DemoSleeper {
    companion object {
        fun sleep(ms: Int) {
            try {
                Thread.sleep(ms.toLong())
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }
}