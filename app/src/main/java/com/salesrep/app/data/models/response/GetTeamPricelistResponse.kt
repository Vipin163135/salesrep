package com.salesrep.app.data.models.response

import com.salesrep.app.data.models.AccountTemplate
import com.salesrep.app.data.models.AddressTemplate
import com.salesrep.app.data.models.PricelistTemplate

class GetTeamPricelistResponse(
    val Pricelist: PricelistTemplate? = null,
    val Products: List<ProductAssortment>? = null,

    )
