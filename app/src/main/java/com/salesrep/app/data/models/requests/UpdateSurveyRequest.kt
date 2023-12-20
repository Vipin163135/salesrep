package com.salesrep.app.data.models.requests

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.salesrep.app.data.models.response.Section

data class UpdateSurveyRequest(
    val surveys: ArrayList<UpdateSurvey>
)

@Entity(tableName = "updateSurvey")
data class UpdateSurvey(
    @PrimaryKey val id: Int?= null,
    val Survey: SurveyUpdateData,
    val SurveyAnswers: SurveyAnswers,
    var activity_integration_id : String?= null,
    var activity_id : String?= null
    )

data class SurveyUpdateData(
    val account_id: String,
    val id: Int,
    val tmplsurvey_id: Int,
    val lov_survey_status: String,
)

data class SurveyAnswers(
    val Sections: ArrayList<Section>
)