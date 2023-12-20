package com.salesrep.app.data.network.responseUtil

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Response
import java.io.File
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

object ApiUtils {
    private fun getErrorMessage(errorJson: String?): String {
        if (errorJson.isNullOrBlank()) {
            return ""
        }

        return try {
            val errorJsonObject = JSONObject(errorJson)
            errorJsonObject.getString("message")

        } catch (exception: Exception) {
            ""
        }
    }

    fun getError(statusCode: Int, errorJson: String?): AppError {
        val message = getErrorMessage(errorJson)
        return when (statusCode) {
            401 -> {
                AppError.ApiUnauthorized(message)
            }
            -1 -> {
                AppError.ApiError(statusCode, message)
            }
            402 -> {
                AppError.ApiAccountBlock(message)
            }
            403 -> {
                AppError.ApiAccountRuleChanged(message)
            }
            else -> {
                AppError.ApiError(statusCode, message)
            }
        }
    }

    fun failure(throwable: Throwable): AppError {
        return AppError.ApiFailure(throwable.localizedMessage ?: "")
    }

    fun imageToRequestBody(imageFile: File, contentType: String): RequestBody =
        imageFile.asRequestBody(contentType.toMediaTypeOrNull())

    fun keyToRequestBody(key: String, content: String): RequestBody =
        content.toRequestBody(key.toMediaTypeOrNull())

    fun imageToRequestBodyKey(parameterName: String, fileName: String): String =
        "$parameterName\"; filename=\"$fileName"

    fun imageToMultipart(file: File, mediaType: String): MultipartBody.Part {
        return MultipartBody.Part.createFormData(
            "file",
            file.name,
            imageToRequestBody(file, mediaType)
        )
    }
}

fun <T> Response<T>.getAppError(): AppError {
    return ApiUtils.getError(code(), errorBody()?.string())
}