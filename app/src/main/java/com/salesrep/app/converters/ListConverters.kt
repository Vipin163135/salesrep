package com.salesrep.app.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.salesrep.app.data.models.*
import com.salesrep.app.data.models.inventory.*
import com.salesrep.app.data.models.requests.*
import com.salesrep.app.data.models.response.*
import java.lang.reflect.Type

class ListConverters {

  companion object {
    val gson = Gson()



    @TypeConverter
    @JvmStatic
    fun stringToRouteArrayList(data: String?): ArrayList<GetHomeRoutesResponse>? {
      val listType: Type = object : TypeToken<ArrayList<GetHomeRoutesResponse?>?>() {}.type
      return Gson().fromJson(data, listType)
    }
    @TypeConverter
    @JvmStatic
    fun routeArrayListToString(list: ArrayList<GetHomeRoutesResponse>?): String? {
      return gson.toJson(list)
    }
    @TypeConverter
    @JvmStatic
    fun stringToRouteList(data: String?): List<GetHomeRoutesResponse>? {
      val listType: Type = object : TypeToken<List<GetHomeRoutesResponse?>?>() {}.type
      return Gson().fromJson(data, listType)
    }
    @TypeConverter
    @JvmStatic
    fun routeListToString(list: List<GetHomeRoutesResponse>?): String? {
      return gson.toJson(list)
    }

    @TypeConverter
    @JvmStatic
    fun stringToTrackList(data: String?): List<TrackTemplate>? {
      val listType: Type = object : TypeToken<List<TrackTemplate?>?>() {}.type
      return Gson().fromJson(data, listType)
    }
    @TypeConverter
    @JvmStatic
    fun trackListToString(list: List<TrackTemplate>?): String? {
      return gson.toJson(list)
    }

    @TypeConverter
    @JvmStatic
    fun stringToRouteTemplateList(data: String?): ArrayList<Route>? {
      val listType: Type = object : TypeToken<ArrayList<Route>?>() {}.type
      return Gson().fromJson(data, listType)
    }
    @TypeConverter
    @JvmStatic
    fun routeTemplateListToString(list: ArrayList<Route>?): String? {
      return gson.toJson(list)
    }

    @TypeConverter
    @JvmStatic
    fun stringToCurrentTrackList(data: String?): List<GetRouteAccountResponse>? {
      val listType: Type = object : TypeToken<List<GetRouteAccountResponse>?>() {}.type
      return Gson().fromJson(data, listType)
    }
    @TypeConverter
    @JvmStatic
    fun currentTrackListToString(list: List<GetRouteAccountResponse>?): String? {
      return gson.toJson(list)
    }

    @TypeConverter
    @JvmStatic
    fun stringToCurrentOrderList(data: String?): List<OrderListObject>? {
      val listType: Type = object : TypeToken<List<OrderListObject>?>() {}.type
      return Gson().fromJson(data, listType)
    }
    @TypeConverter
    @JvmStatic
    fun currentOrderListToString(list: List<OrderListObject>?): String? {
      return gson.toJson(list)
    }

    @TypeConverter
    @JvmStatic
    fun stringToTaskList(data: String?): List<TaskData>? {
      val listType: Type = object : TypeToken<List<TaskData>?>() {}.type
      return Gson().fromJson(data, listType)
    }
    @TypeConverter
    @JvmStatic
    fun taskListToString(list: List<TaskData>?): String? {
      return gson.toJson(list)
    }

    @TypeConverter
    @JvmStatic
    fun stringToUpdateActivityList(data: String?): List<UpdateRouteActivity>? {
      val listType: Type = object : TypeToken<List<UpdateRouteActivity>?>() {}.type
      return Gson().fromJson(data, listType)
    }
    @TypeConverter
    @JvmStatic
    fun updateActivityListToString(list: List<UpdateRouteActivity>?): String? {
      return gson.toJson(list)
    }

    @TypeConverter
    @JvmStatic
    fun stringToUpdateRouteList(data: String?): List<UpdateRoute>? {
      val listType: Type = object : TypeToken<List<UpdateRoute>?>() {}.type
      return Gson().fromJson(data, listType)
    }

    @TypeConverter
    @JvmStatic
    fun updateRouteListToString(list: List<UpdateRoute>?): String? {
      return gson.toJson(list)
    }

    @TypeConverter
    @JvmStatic
    fun stringToProductAssortList(data: String?): List<ProductAssortment>? {
      val listType: Type = object : TypeToken<List<ProductAssortment>?>() {}.type
      return Gson().fromJson(data, listType)
    }

    @TypeConverter
    @JvmStatic
    fun productAssortListToString(list: List<ProductAssortment>?): String? {
      return gson.toJson(list)
    }

    @TypeConverter
    @JvmStatic
    fun stringToUpdateProductList(data: String?): List<UpdateProductData>? {
      val listType: Type = object : TypeToken<List<UpdateProductData>?>() {}.type
      return Gson().fromJson(data, listType)
    }

    @TypeConverter
    @JvmStatic
    fun updateProductListToString(list: List<UpdateProductData>?): String? {
      return gson.toJson(list)
    }

    @TypeConverter
    @JvmStatic
    fun surveyDataList(data: String?): List<SurveyData>? {
      val listType: Type = object : TypeToken<List<SurveyData>?>() {}.type
      return Gson().fromJson(data, listType)
    }

    @TypeConverter
    @JvmStatic
    fun surveyDataListToString(list: List<SurveyData>?): String? {
      return gson.toJson(list)
    }

    @TypeConverter
    @JvmStatic
    fun sectionList(data: String?): List<Section>? {
      val listType: Type = object : TypeToken<List<Section>?>() {}.type
      return Gson().fromJson(data, listType)
    }

    @TypeConverter
    @JvmStatic
    fun sectionListToString(list: List<Section>?): String? {
      return gson.toJson(list)
    }

    @TypeConverter
    @JvmStatic
    fun questionList(data: String?): List<Question>? {
      val listType: Type = object : TypeToken<List<Question>?>() {}.type
      return Gson().fromJson(data, listType)
    }

    @TypeConverter
    @JvmStatic
    fun questionListToString(list: List<Question>?): String? {
      return gson.toJson(list)
    }

    @TypeConverter
    @JvmStatic
    fun answerList(data: String?): ArrayList<Answer>? {
      val listType: Type = object : TypeToken<ArrayList<Answer>?>() {}.type
      return Gson().fromJson(data, listType)
    }

    @TypeConverter
    @JvmStatic
    fun answerListToString(list: ArrayList<Answer>?): String? {
      return gson.toJson(list)
    }

    @TypeConverter
    @JvmStatic
    fun stringToUpdateSurveyList(data: String?): List<UpdateSurvey>? {
      val listType: Type = object : TypeToken<List<UpdateSurvey>?>() {}.type
      return Gson().fromJson(data, listType)
    }

    @TypeConverter
    @JvmStatic
    fun updateSurveyToString(list: ArrayList<Survey>?): String? {
      return gson.toJson(list)
    }
    @TypeConverter
    @JvmStatic
    fun stringToPriceList(data: String?): List<PricelistTemplate>? {
      val listType: Type = object : TypeToken<List<PricelistTemplate>?>() {}.type
      return Gson().fromJson(data, listType)
    }

    @TypeConverter
    @JvmStatic
    fun pricelistToString(list: ArrayList<PricelistTemplate>?): String? {
      return gson.toJson(list)
    }

    @TypeConverter
    @JvmStatic
    fun stringToPromoDataList(data: String?): List<PromotionData>? {
      val listType: Type = object : TypeToken<List<PromotionData>?>() {}.type
      return Gson().fromJson(data, listType)
    }

    @TypeConverter
    @JvmStatic
    fun promoDatalistToString(list: List<PromotionData>?): String? {
      return gson.toJson(list)
    }

    @TypeConverter
    @JvmStatic
    fun stringToPromoRuleList(data: String?): List<PromoRule>? {
      val listType: Type = object : TypeToken<List<PromoRule>?>() {}.type
      return Gson().fromJson(data, listType)
    }

    @TypeConverter
    @JvmStatic
    fun promoRulelistToString(list: ArrayList<PromoRule>?): String? {
      return gson.toJson(list)
    }

    @TypeConverter
    @JvmStatic
    fun stringToCreateOrderTemplateArrayList(data: String?): ArrayList<CreateOrderTemplate>? {
      val listType: Type = object : TypeToken<ArrayList<CreateOrderTemplate>?>() {}.type
      return Gson().fromJson(data, listType)
    }

    @TypeConverter
    @JvmStatic
    fun createOrderTemplatearraylistToString(list: ArrayList<CreateOrderTemplate>?): String? {
      return gson.toJson(list)
    }
    @TypeConverter
    @JvmStatic
    fun stringToCreateOrderTemplateList(data: String?): List<CreateOrderTemplate>? {
      val listType: Type = object : TypeToken<List<CreateOrderTemplate>?>() {}.type
      return Gson().fromJson(data, listType)
    }

    @TypeConverter
    @JvmStatic
    fun createOrderTemplatelistToString(list: List<CreateOrderTemplate>?): String? {
      return gson.toJson(list)
    }

    @TypeConverter
    @JvmStatic
    fun stringToCreateOrderProductList(data: String?): ArrayList<CreateOrderProduct>? {
      val listType: Type = object : TypeToken<ArrayList<CreateOrderProduct>?>() {}.type
      return Gson().fromJson(data, listType)
    }

    @TypeConverter
    @JvmStatic
    fun createOrderProductlistToString(list: ArrayList<CreateOrderProduct>?): String? {
      return gson.toJson(list)
    }

    @TypeConverter
    @JvmStatic
    fun stringToPaymentProfileList(data: String?): ArrayList<Paymentprofiles>? {
      val listType: Type = object : TypeToken<ArrayList<Paymentprofiles>?>() {}.type
      return Gson().fromJson(data, listType)
    }

    @TypeConverter
    @JvmStatic
    fun paymentProfileListToString(list: ArrayList<Paymentprofiles>?): String? {
      return gson.toJson(list)
    }

    @TypeConverter
    @JvmStatic
    fun stringToPaymentCreateList(data: String?): ArrayList<CreatePaymentRequest>? {
      val listType: Type = object : TypeToken<ArrayList<CreatePaymentRequest>?>() {}.type
      return Gson().fromJson(data, listType)
    }

    @TypeConverter
    @JvmStatic
    fun paymentCreateListToString(list: ArrayList<CreatePaymentRequest>?): String? {
      return gson.toJson(list)
    }
    @TypeConverter
    @JvmStatic
    fun stringToPaymentTemplateList(data: String?): ArrayList<CreatePaymentTemplate>? {
      val listType: Type = object : TypeToken<ArrayList<CreatePaymentTemplate>?>() {}.type
      return Gson().fromJson(data, listType)
    }

    @TypeConverter
    @JvmStatic
    fun paymentTemplateListToString(list: ArrayList<CreatePaymentTemplate>?): String? {
      return gson.toJson(list)
    }
    @TypeConverter
    @JvmStatic
    fun stringToPromotionListObjectList(data: String?): ArrayList<PromotionListObject>? {
      val listType: Type = object : TypeToken<ArrayList<PromotionListObject>?>() {}.type
      return Gson().fromJson(data, listType)
    }

    @TypeConverter
    @JvmStatic
    fun promotionListObjectListToString(list: ArrayList<PromotionListObject>?): String? {
      return gson.toJson(list)
    }

    @TypeConverter
    @JvmStatic
    fun stringToRedeemDiscountList(data: String?): ArrayList<RedeemDiscount>? {
      val listType: Type = object : TypeToken<ArrayList<RedeemDiscount>?>() {}.type
      return Gson().fromJson(data, listType)
    }

    @TypeConverter
    @JvmStatic
    fun redeemDiscountListToString(list: ArrayList<RedeemDiscount>?): String? {
      return gson.toJson(list)
    }

    @TypeConverter
    @JvmStatic
    fun stringPendingList(data: String?): ArrayList<PendingPaymentTemplate>? {
      val listType: Type = object : TypeToken<ArrayList<PendingPaymentTemplate>?>() {}.type
      return Gson().fromJson(data, listType)
    }

    @TypeConverter
    @JvmStatic
    fun pendingPaymentListToString(list: ArrayList<PendingPaymentTemplate>?): String? {
      return gson.toJson(list)
    }

    @TypeConverter
    @JvmStatic
    fun stringOrderList(data: String?): List<RepriceOrderResponse>? {
      val listType: Type = object : TypeToken<List<RepriceOrderResponse>?>() {}.type
      return Gson().fromJson(data, listType)
    }

    @TypeConverter
    @JvmStatic
    fun orderListToString(list: List<RepriceOrderResponse>?): String? {
      return gson.toJson(list)
    }
    @TypeConverter
    @JvmStatic
    fun stringCartProductList(data: String?): ArrayList<CartProduct>? {
      val listType: Type = object : TypeToken<ArrayList<CartProduct>?>() {}.type
      return Gson().fromJson(data, listType)
    }

    @TypeConverter
    @JvmStatic
    fun cartProductListToString(list: ArrayList<CartProduct>?): String? {
      return gson.toJson(list)
    }

    @TypeConverter
    @JvmStatic
    fun stringInventoryTemplateList(data: String?): ArrayList<InventoryTemplate>? {
      val listType: Type = object : TypeToken<ArrayList<InventoryTemplate>?>() {}.type
      return Gson().fromJson(data, listType)
    }

    @TypeConverter
    @JvmStatic
    fun inventoryTemplateListToString(list: ArrayList<InventoryTemplate>?): String? {
      return gson.toJson(list)
    }

    @TypeConverter
    @JvmStatic
    fun stringInvMovementTemplateList(data: String?): ArrayList<InvMovementTemplate>? {
      val listType: Type = object : TypeToken<ArrayList<InvMovementTemplate>?>() {}.type
      return Gson().fromJson(data, listType)
    }

    @TypeConverter
    @JvmStatic
    fun invMovementTemplateListToString(list: ArrayList<InvMovementTemplate>?): String? {
      return gson.toJson(list)
    }

    @TypeConverter
    @JvmStatic
    fun stringToInvMovementProductDataList(data: String?): ArrayList<InvMovementProductData>? {
      val listType: Type = object : TypeToken<ArrayList<InvMovementProductData>?>() {}.type
      return Gson().fromJson(data, listType)
    }

    @TypeConverter
    @JvmStatic
    fun invMovementProductDataListToString(list: ArrayList<InvMovementProductData>?): String? {
      return gson.toJson(list)
    }

    @TypeConverter
    @JvmStatic
    fun stringToInvBinTemplateList(data: String?): ArrayList<InvBinTemplate>? {
      val listType: Type = object : TypeToken<ArrayList<InvBinTemplate>?>() {}.type
      return Gson().fromJson(data, listType)
    }

    @TypeConverter
    @JvmStatic
    fun invBinTemplateListToString(list: ArrayList<InvBinTemplate>?): String? {
      return gson.toJson(list)
    }

    @TypeConverter
    @JvmStatic
    fun stringToInventoryDataObjectList(data: String?): ArrayList<InventoryDataObject>? {
      val listType: Type = object : TypeToken<ArrayList<InventoryDataObject>?>() {}.type
      return Gson().fromJson(data, listType)
    }

    @TypeConverter
    @JvmStatic
    fun inventoryDataObjectListToString(list: ArrayList<InventoryDataObject>?): String? {
      return gson.toJson(list)
    }

    @TypeConverter
    @JvmStatic
    fun stringToMovementList(data: String?): ArrayList<MovementListData>? {
      val listType: Type = object : TypeToken<ArrayList<MovementListData>?>() {}.type
      return Gson().fromJson(data, listType)
    }

    @TypeConverter
    @JvmStatic
    fun movementListDataToString(list: ArrayList<MovementListData>?): String? {
      return gson.toJson(list)
    }

    @TypeConverter
    @JvmStatic
    fun stringToMovementProductList(data: String?): ArrayList<MovementProduct>? {
      val listType: Type = object : TypeToken<ArrayList<InventoryDataObject>?>() {}.type
      return Gson().fromJson(data, listType)
    }

    @TypeConverter
    @JvmStatic
    fun movementProductListToString(list: ArrayList<MovementProduct>?): String? {
      return gson.toJson(list)
    }

    @TypeConverter
    @JvmStatic
    fun stringToAssetList(data: String?): ArrayList<AssetTemplate>? {
      val listType: Type = object : TypeToken<ArrayList<AssetTemplate>?>() {}.type
      return Gson().fromJson(data, listType)
    }

    @TypeConverter
    @JvmStatic
    fun assetListToString(list: ArrayList<AssetTemplate>?): String? {
      return gson.toJson(list)
    }

    @TypeConverter
    @JvmStatic
    fun stringToOrderPaymentTemplateList(data: String?): ArrayList<OrderPaymentTemplate>? {
      val listType: Type = object : TypeToken<ArrayList<OrderPaymentTemplate>?>() {}.type
      return Gson().fromJson(data, listType)
    }

    @TypeConverter
    @JvmStatic
    fun orderPaymentListToString(list: ArrayList<OrderPaymentTemplate>?): String? {
      return gson.toJson(list)
    }

  }
}