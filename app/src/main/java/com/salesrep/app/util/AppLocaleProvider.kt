package com.salesrep.app.util

import dev.b3nedikt.app_locale.AppLocale
import dev.b3nedikt.restring.LocaleProvider

object AppLocaleProvider : LocaleProvider {

        override val isInitial
            get() = AppLocale.isInitial

        override var currentLocale
            get() = AppLocale.desiredLocale
            set(value) {
                AppLocale.desiredLocale = value
            }
    }