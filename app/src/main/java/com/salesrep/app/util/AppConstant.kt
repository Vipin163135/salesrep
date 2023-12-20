package com.salesrep.app.util

import java.util.*

const val PLAY_STORE = "https://play.google.com/store/apps/details?id="

const val SPLASH_TIME_MILLIS: Long = 1000
const val CHAT_MAX_TIME = 900000f
const val DEVICE_TYPE = "ANDROID"
const val SIGN_UP_METHOD = "EMAIL"
const val PER_PAGE = 30
const val DEFAULT_LANGUAGE = "en"

const val BASIC_AUTH_HOST_USERNAME = "mobileApp"
const val BASIC_AUTH_HOST_PASSWORD = "bwz@n3kS-5A=zTDe"
//const val BASIC_AUTH_USERNAME = "mobileApp"
//const val BASIC_AUTH_PASSWORD = "bwz@n3kS-5A=zTDe"
const val BASIC_AUTH_USERNAME = "api"
const val BASIC_AUTH_PASSWORD = "4UFj+5Xzv=LNrc-&"
const val RNC_KEY = "jWnZr4t7w!z%C*F-JaNdRgUkXp2s5v8x/A?D(G+KbPeShVmYq3t6w9z\$B&E)H@Mc"

object DataTransferKeys {

    const val KEY_ACCOUNT= "KEY_ACCOUNT"
    const val KEY_NOTIFICATION_DATA= "KEY_NOTIFICATION_DATA"
    const val KEY_ORDER_ID= "KEY_ORDER_ID"
    const val KEY_EMAIL = "KEY_EMAIL"
    const val KEY_NAME= "KEY_NAME"
    const val KEY_TYPE= "type"
    const val KEY_SUB_TYPE= "SUB_TYPE"
    const val PAGE_TO_OPEN = "PAGE_TO_OPEN"
    const val LOGIN_PAGE = "LOGIN_PAGE"
    const val SIGNUP_PAGE = "SIGNUP_PAGE"
    const val SELECT_TEAM_PAGE = "SELECT_TEAM_PAGE"
    const val SET_PASS_PAGE = "SET_PASS_PAGE"
    const val RESET_PASS_PAGE = "RESET_PASS_PAGE"
    const val KEY_ID= "id"
    const val KEY_OFFER_ID= "offer_id"
    const val KEY_TEAMS= "KEY_TEAMS"
    const val KEY_ROUTES= "KEY_ROUTES"
    const val KEY_CURRENT_ROUTE= "KEY_CURRENT_ROUTE"
    const val KEY_ROUTE_ID= "KEY_ROUTE_ID"
    const val KEY_ADDRESS= "KEY_ADDRESS"
    const val KEY_TASKS= "KEY_TASKS"
    const val KEY_TASK_DATA= "KEY_TASK_DATA"
    const val KEY_PRODUCTS= "KEY_PRODUCTS"
    const val KEY_MANUFACTURER= "KEY_MANUFACTURER"
    const val KEY_ACCOUNT_DETAIL= "KEY_ACCOUNT_DETAIL"
    const val KEY_PAYMENT_LIST= "KEY_PAYMENT_LIST"
    const val KEY_ORDER_LIST= "KEY_ORDER_LIST"
    const val KEY_ATTACHMENTS= "KEY_ATTACHMENTS"
    const val KEY_FROM= "KEY_FROM"
    const val KEY_STATUS= "KEY_STATUS"
    const val KEY_ROUTE_STATUS= "KEY_ROUTE_STATUS"
    const val KEY_CANCEL_STATUS= "KEY_CANCEL_STATUS"
    const val KEY_REASON= "KEY_REASON"
    const val KEY_CUSTOMER_DETAIL= "KEY_CUSTOMER_DETAIL"
    const val KEY_CUSTOMER_NAV= "KEY_CUSTOMER_NAV"
    const val KEY_MOVEMENT_DATA= "KEY_MOVEMENT_DATA"
    const val KEY_IS_NEW= "KEY_IS_NEW"
    const val KEY_IS_ORDER= "KEY_IS_ORDER"
    const val KEY_IS_COMPLETED= "KEY_IS_COMPLETED"
    const val ADD_NEW_TASK_RESULT: String = "ADD_NEW_TASK_RESULT"
    const val KEY_DOCUMENT_DETAIL: String = "KEY_DOCUMENT_DETAIL"
    const val SELECT_MOVEMENT_TYPE: String = "SELECT_MOVEMENT_TYPE"
    const val SELECT_MOVEMENT_BIN: String = "SELECT_MOVEMENT_BIN"
    const val KEY_SELECTED_BIN_PRODUCTS: String = "SELECTED_BIN_PRODUCTS"


}

object AppRequestCode {
    const val DEFAULT_REQUEST_CODE: Int = 0
    const val AUTOCOMPLETE_REQUEST_CODE: Int = 1001
    const val CANCEL_ROUTE_REQUEST: String = "CANCEL_ROUTE_REQUEST"
    const val ORDER_RETURN_REASON: String = "ORDER_RETURN_REASON"
    const val CANCEL_ORDER_REQUEST: String = "CANCEL_ORDER_REQUEST"
    const val REMOVE_PAYMENT_OPTION_REQUEST: String = "REMOVE_PAYMENT_OPTION_REQUEST"
    const val SELECT_TIME_REQUEST: String = "SELECT_TIME_REQUEST"
    const val SELECT_PRODUCT_REQUEST: String = "SELECT_PRODUCT_REQUEST"
    const val SELECT_PRODUCT_LIST_REQUEST: String = "SELECT_PRODUCT_LIST_REQUEST"
    const val SELECT_CUSTOMER_NAV_REQUEST: String = "SELECT_CUSTOMER_NAV_REQUEST"
    const val CURRENT_ROUTE_STATUS_CHANGED: String = "CURRENT_ROUTE_STATUS_CHANGED"
    const val REFRESH_HOME_REQUEST: String = "REFRESH_HOME_REQUEST"
    const val CURRENT_ROUTE_ACTIVITY_STATUS_CHANGED: String = "CURRENT_ROUTE_ACTIVITY_STATUS_CHANGED"
    const val CURRENT_ROUTE_TRACK_STATUS_CHANGED: String = "CURRENT_ROUTE_TRACK_STATUS_CHANGED"
    const val CURRENT_ACTIVITY_STATUS_CHANGED: String = "CURRENT_ACTIVITY_STATUS_CHANGED"
    const val CURRENT_TASK_STATUS_CHANGED: String = "CURRENT_TASK_STATUS_CHANGED"
    const val CURRENT_TASK_SURVEY_STATUS_COMPLETED: String = "CURRENT_TASK_SURVEY_STATUS_COMPLETED"
    const val CURRENT_TASK_SURVEY_STATUS_CHANGED: String = "CURRENT_TASK_SURVEY_STATUS_CHANGED"
    const val CURRENT_TASK_FACECHECKING_STATUS_CHANGED: String = "CURRENT_TASK_FACECHECKING_STATUS_CHANGED"
    const val CURRENT_TASK_ORDER_STATUS_CHANGED: String = "CURRENT_TASK_ORDER_STATUS_CHANGED"
    const val ADD_NEW_TASK_REQUEST: String = "ADD_NEW_TASK_REQUEST"
    const val SELECT_MOVEMENT_TYPE_REQUEST: String = "SELECT_MOVEMENT_TYPE_REQUEST"
    const val SELECT_MOVEMENT_BIN_REQUEST: String = "SELECT_MOVEMENT_BIN_REQUEST"
    const val UPDATE_MOVEMENT_LIST_REQUEST: String = "UPDATE_MOVEMENt_LIST_REQUEST"
}

object RouteStatus {
    const val STATUS_IN_PROGRESS= "In Progress"
    const val STATUS_COMPLETED= "Completed"
    const val STATUS_NOT_STARTED= "Not Started"
    const val STATUS_SKIPPED= "Skipped"
    const val STATUS_PAUSED= "Paused"
    const val STATUS_CANCELLED= "Cancelled"
    const val STATUS_PENDING= "Pending"
    const val STATUS_OFFROUTE= "STATUS_OFFROUTE"
}

object TrackConstants{

    const val RUNNING_DATABASE_NAME = "running_db"

    const val REQUEST_CODE_LOCATION_PERMISSION = 0

    const val ACTION_START_OR_RESUME_SERVICE = "ACTION_START_OR_RESUME_SERVICE"
    const val ACTION_PAUSE_SERVICE = "ACTION_PAUSE_SERVICE"
    const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
    const val ACTION_SHOW_TRACKING_FRAGMENT = "ACTION_SHOW_TRACKING_FRAGMENT"

    const val LOCATION_UPDATE_INTERVAL = 5000L
    const val FASTEST_LOCATION_INTERVAL = 2000L

    const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Tracking"
    const val NOTIFICATION_ID = 1
}


object DateFormat {
    const val DATE_TIME_FORMAT = "dd-MMM-yyyy · hh:mm:ss a"
    const val INPUT_DATE_TIME_FORMAT = "dd-MM-yyyy HH:mm:ss"
    const val DELIVERY_DATE_FORMAT = "dd MMM yyyy"
    const val TIME_FORMAT = "HH:mm"
    const val DATE_MON_YEAR_FORMAT = "dd-MM-yy"
    const val YEAR_MON_DATE_FORMAT = "yyyy-MM-dd"
    const val MON_DAY_YEAR = "MM/dd/yyyy"
    const val MON_DATE = "MMM dd"
    const val DATE_FORMAT_SLASH = "dd/MMM/yy"
    const val DATE_FORMAT_SLASH_YEAR = "dd/MM/yyyy"

    const val DATE_FORMAT_RENEW = "yyyy-MM-dd hh:mm:ss"
    const val DATE_FORMAT_YMDHMS = "yyyyMMddhhmmss"
    const val DELIVERY_ORDER_DATE_FORMAT = "$DATE_FORMAT_SLASH_YEAR HH:mm"
    const val DATE_FORMAT_DAY_ONLY = "EEE"
}

object AppUrls {
//    var PRIVACY_POLICY_URL = "${Config.BASE_URL}driver/v1/privacyPolicy/en"
//    var TERMS_AND_CONDITIONS_URL = "${Config.BASE_URL}driver/v1/tAndC/en"

}

object FileType {
    const val IMAGE = "IMAGE"
    const val DOCUMENTS = "doc"
    const val PHOTOS = "Photos"
    const val VIDEO = "VIDEO"
    const val AUDIO = "AUDIO"
    const val SCAN = "Scan"
    const val LINK = "Links"
    const val CUSTOM = "Custom"
    const val HTML = "HTML"
}

object ChatType {
    const val MESSAGE_TYPE_TEXT = "TEXT"
    const val MESSAGE_DELIVERED = "D"
    const val MESSAGE_SEEN = "S"
}

object MessageType {
    const val MSG_TYPE_TEXT = "TEXT"
    const val MSG_TYPE_IMAGE = "IMAGE"
    const val MSG_TYPE_VIDEO = "VIDEO"
    const val MSG_TYPE_AUDIO = "AUDIO"
    const val PRODUCTS = "PRODUCTS"
}

object MediaContentType {
    const val CONTENT_TYPE_TEXT = "text/plain"
    const val CONTENT_TYPE_IMAGE = "image/*"
    const val CONTENT_TYPE_VIDEO = "video/*"
}
object OrderType {
    const val TYPE_RETURN = "Return"
}


object TimerConstants {
    const val DELAY_MS: Long = 2000//delay in milliseconds before task is to be executed
    const val PERIOD_MS: Long = 3000
}

object AppLocales {
    var brazilLocale = Locale("pt", "BR")
    var usLocale = Locale("en", "US")
}