package com.salesrep.app.data.network.responseUtil
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class AppService(

	@SerializedName("type")
	@Expose
	val type: String,
	@SerializedName("message")
	@Expose
	val message: String

)