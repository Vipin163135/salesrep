package com.salesrep.app.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.salesrep.app.data.models.*
import com.salesrep.app.data.models.inventory.*
import com.salesrep.app.data.models.requests.*
import com.salesrep.app.data.models.response.*

class ObjectConverters {
    companion object {
        val gson = Gson()

        //
        @TypeConverter
        @JvmStatic
        fun stringToUserObject(data: String?): UserTrackTemplate? {
            return gson.fromJson(data, UserTrackTemplate::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun userObjectToString(data: UserTrackTemplate): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToRouteActivityObject(data: String?): UpdateRouteActivity? {
            return gson.fromJson(data, UpdateRouteActivity::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun routeActivityObjectToString(data: UpdateRouteActivity): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToUpdateRouteObject(data: String?): UpdateRoute? {
            return gson.fromJson(data, UpdateRoute::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun updateRouteObjectToString(data: UpdateRoute): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToRouteDataObject(data: String?): RouteData? {
            return gson.fromJson(data, RouteData::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun routeDataObjectToString(data: RouteData): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToRouteActivityDataObject(data: String?): RouteActivityData? {
            return gson.fromJson(data, RouteActivityData::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun routeActivityDataObjectToString(data: RouteActivityData): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToRouteObject(data: String?): GetHomeRoutesResponse? {
            return gson.fromJson(data, GetHomeRoutesResponse::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun routeObjectToString(data: GetHomeRoutesResponse?): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToTrackObject(data: String?): GetRouteAccountResponse? {
            return gson.fromJson(data, GetRouteAccountResponse::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun trackObjectToString(data: GetRouteAccountResponse?): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToOrderListObject(data: String?): OrderListObject? {
            return gson.fromJson(data, OrderListObject::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun OrderListObjectToString(data: OrderListObject?): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToTaskObject(data: String?): TaskData? {
            return gson.fromJson(data, TaskData::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun taskObjectToString(data: TaskData?): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToAccountObject(data: String?): AccountTemplate? {
            return gson.fromJson(data, AccountTemplate::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun accountObjectToString(data: AccountTemplate?): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToAddressObject(data: String?): AddressTemplate? {
            return gson.fromJson(data, AddressTemplate::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun addressObjectToString(data: AddressTemplate?): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToActivityObject(data: String?): ActivityTemplate? {
            return gson.fromJson(data, ActivityTemplate::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun activityObjectToString(data: ActivityTemplate?): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToVisitObject(data: String?): VisitData? {
            return gson.fromJson(data, VisitData::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun activityVisitToString(data: VisitData?): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToProductAssortmentObject(data: String?): ProductAssortment? {
            return gson.fromJson(data, ProductAssortment::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun productAssortmentToString(data: ProductAssortment?): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToProductTemplateObject(data: String?): ProductTemplate? {
            return gson.fromJson(data, ProductTemplate::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun productTemplateToString(data: ProductTemplate?): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToAccountProductTemplateObject(data: String?): AccountProductTemplate? {
            return gson.fromJson(data, AccountProductTemplate::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun accountProductTemplateToString(data: AccountProductTemplate?): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToUpdateProductObject(data: String?): UpdateProductData? {
            return gson.fromJson(data, UpdateProductData::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun updateProductToString(data: UpdateProductData?): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToSurveyDataObject(data: String?): SurveyData? {
            return gson.fromJson(data, SurveyData::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun surveyDataToString(data: SurveyData?): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToSurveyObject(data: String?): Survey? {
            return gson.fromJson(data, Survey::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun surveyToString(data: Survey?): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToSurveyTemplateObject(data: String?): SurveyTemplate? {
            return gson.fromJson(data, SurveyTemplate::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun surveyTemplateToString(data: SurveyTemplate?): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToSectionObject(data: String?): Section? {
            return gson.fromJson(data, Section::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun sectionToString(data: Section?): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToQuestionObject(data: String?): Question? {
            return gson.fromJson(data, Question::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun questionToString(data: Question?): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToAnswerObject(data: String?): Answer? {
            return gson.fromJson(data, Answer::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun answerToString(data: Answer?): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToUpdateSurveyObject(data: String?): UpdateSurvey? {
            return gson.fromJson(data, UpdateSurvey::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun updateSurveyToString(data: UpdateSurvey?): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToUpdateSurveyDataObject(data: String?): SurveyUpdateData? {
            return gson.fromJson(data, SurveyUpdateData::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun updateSurveyDataToString(data: SurveyUpdateData?): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToUpdateSurveyAnswersObject(data: String?): SurveyAnswers? {
            return gson.fromJson(data, SurveyAnswers::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun updateSurveyAnswersToString(data: SurveyAnswers?): String? {
            return gson.toJson(data)
        }
        @TypeConverter
        @JvmStatic
        fun stringToPricelistObject(data: String?): PricelistTemplate? {
            return gson.fromJson(data, PricelistTemplate::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun pricelistObjectToString(data: PricelistTemplate?): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToPromotionDataObject(data: String?): PromotionData? {
            return gson.fromJson(data, PromotionData::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun promotionDataObjectToString(data: PromotionData?): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToTmplpromoObject(data: String?): Tmplpromo? {
            return gson.fromJson(data, Tmplpromo::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun tmplpromoObjectToString(data: Tmplpromo?): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToPromoRuleObject(data: String?): PromoRule? {
            return gson.fromJson(data, PromoRule::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun promoRuleObjectToString(data: PromoRule?): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToCreateOrderObject(data: String?): CreateOrderTemplate? {
            return gson.fromJson(data, CreateOrderTemplate::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun createOrderObjectToString(data: CreateOrderTemplate?): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToCreateOrderDataObject(data: String?): CreateOrderData? {
            return gson.fromJson(data, CreateOrderData::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun createOrderDataObjectToString(data: CreateOrderData?): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToCreateOrderProductObject(data: String?): CreateOrderProduct? {
            return gson.fromJson(data, CreateOrderProduct::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun createOrderProductObjectToString(data: CreateOrderProduct?): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToPaymentProfileObject(data: String?): Paymentprofiles? {
            return gson.fromJson(data, Paymentprofiles::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun paymentProfileToString(data: Paymentprofiles?): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToPaymentProfileTemplate(data: String?): PaymentProfileTemplate? {
            return gson.fromJson(data, PaymentProfileTemplate::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun paymentProfileTemplateToString(data: PaymentProfileTemplate?): String? {
            return gson.toJson(data)
        }
        @TypeConverter
        @JvmStatic
        fun stringToCreatePaymentTemplate(data: String?): CreatePaymentTemplate? {
            return gson.fromJson(data, CreatePaymentTemplate::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun paymentCreateTemplateToString(data: CreatePaymentTemplate?): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToCreatePaymentRequest(data: String?): CreatePaymentRequest? {
            return gson.fromJson(data, CreatePaymentRequest::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun paymentCreateRequestToString(data: CreatePaymentRequest?): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToOrdersProduct(data: String?): OrdersProduct? {
            return gson.fromJson(data, OrdersProduct::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun ordersProductToString(data: OrdersProduct?): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToOrderTemplate(data: String?): OrderTemplate? {
            return gson.fromJson(data, OrderTemplate::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun orderTemplateToString(data: OrderTemplate?): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToPromotionListObject(data: String?): PromotionListObject? {
            return gson.fromJson(data, PromotionListObject::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun promotionListObjectToString(data: PromotionListObject?): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToRedeemDiscount(data: String?): RedeemDiscount? {
            return gson.fromJson(data, RedeemDiscount::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun redeemDiscountToString(data: RedeemDiscount?): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToOrderProductsDiscount(data: String?): OrderProductsDiscount? {
            return gson.fromJson(data, OrderProductsDiscount::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun orderProductsDiscountToString(data: OrderProductsDiscount?): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToPendingPaymentTemplate(data: String?): PendingPaymentTemplate? {
            return gson.fromJson(data, PendingPaymentTemplate::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun pendingPaymentTemplateToString(data: PendingPaymentTemplate?): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToCartProductTemplate(data: String?): CartProduct? {
            return gson.fromJson(data, CartProduct::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun cartProductTemplateToString(data: CartProduct?): String? {
            return gson.toJson(data)
        }
        @TypeConverter
        @JvmStatic
        fun stringToOrder(data: String?): RepriceOrderResponse? {
            return gson.fromJson(data, RepriceOrderResponse::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun orderTemplateToString(data: RepriceOrderResponse?): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToInventory(data: String?): GetTeamInventoryResponse? {
            return gson.fromJson(data, GetTeamInventoryResponse::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun inventoryToString(data: GetTeamInventoryResponse?): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToInventoryTemplate(data: String?): InventoryTemplate? {
            return gson.fromJson(data, InventoryTemplate::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun inventoryTemplateToString(data: InventoryTemplate?): String? {
            return gson.toJson(data)
        }
        @TypeConverter
        @JvmStatic
        fun stringToInvLocData(data: String?): InvLocData? {
            return gson.fromJson(data, InvLocData::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun invLocDataToString(data: InvLocData?): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToInvBinTemplate(data: String?): InvBinTemplate? {
            return gson.fromJson(data, InvBinTemplate::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun invBinTemplateToString(data: InvBinTemplate?): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToInvBinData(data: String?): InvBinData? {
            return gson.fromJson(data, InvBinData::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun invBinDataToString(data: InvBinData?): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToInventoryDataObject(data: String?): InventoryDataObject? {
            return gson.fromJson(data, InventoryDataObject::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun inventoryDataObjectToString(data: InventoryDataObject?): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToInvBinProductsData(data: String?): InvBinProductsData? {
            return gson.fromJson(data, InvBinProductsData::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun invBinProductsDataToString(data: InvBinProductsData?): String? {
            return gson.toJson(data)
        }


        @TypeConverter
        @JvmStatic
        fun stringToInvMovementData(data: String?): InvMovementData? {
            return gson.fromJson(data, InvMovementData::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun invMovementDataToString(data: InvMovementData?): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToInvMovementProductData(data: String?): InvMovementProductData? {
            return gson.fromJson(data, InvMovementProductData::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun invMovementProductDataToString(data: InvMovementProductData?): String? {
            return gson.toJson(data)
        }
//
//        @TypeConverter
//        @JvmStatic
//        fun stringToCreateMovement(data: String?): CreateMovementRequest? {
//            return gson.fromJson(data, CreateMovementRequest::class.java)
//        }
//
//        @TypeConverter
//        @JvmStatic
//        fun createMovementRequestToString(data: CreateMovementRequest?): String? {
//            return gson.toJson(data)
//        }

        @TypeConverter
        @JvmStatic
        fun stringToCreateMovementProduct(data: String?): MovementProduct? {
            return gson.fromJson(data, MovementProduct::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun createMovementProductToString(data: MovementProduct?): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToMovementListData(data: String?): MovementListData? {
            return gson.fromJson(data, MovementListData::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun movementListDataToString(data: MovementListData?): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToMovementCancelData(data: String?): MovementCancelData? {
            return gson.fromJson(data, MovementCancelData::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun movementCancelDataToString(data: MovementCancelData?): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToManufacturerTemplate(data: String?): ManufacturerTemplate? {
            return gson.fromJson(data, ManufacturerTemplate::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun manufacturerTemplateToString(data: ManufacturerTemplate?): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToAssetTemplate(data: String?): AssetTemplate? {
            return gson.fromJson(data, AssetTemplate::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun assetTemplateToString(data: AssetTemplate?): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToAssetData(data: String?): AssetData? {
            return gson.fromJson(data, AssetData::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun assetDataToString(data: AssetData?): String? {
            return gson.toJson(data)
        }

        @TypeConverter
        @JvmStatic
        fun stringToOrderPaymentTemplate(data: String?): OrderPaymentTemplate? {
            return gson.fromJson(data, OrderPaymentTemplate::class.java)
        }

        @TypeConverter
        @JvmStatic
        fun orderPaymentToString(data: OrderPaymentTemplate?): String? {
            return gson.toJson(data)
        }

    }
}