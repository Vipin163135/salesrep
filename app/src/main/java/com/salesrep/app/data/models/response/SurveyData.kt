package com.salesrep.app.data.models.response

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SurveyData(
 val Survey: Survey,
 val SurveyTemplate: SurveyTemplate
):Parcelable

@Parcelize
data class Survey(
    val account_id: Int,
    val description: String,
    val id: Int,
    var lov_survey_status: String,
    val name: String,
    val score: Double?=0.0,
    val tmplsurvey_id: Int,
    val type: String
):Parcelable

@Parcelize
data class SurveyTemplate(
    val account_id: Int,
    val Sections: List<Section>,
    val answered_questions: Int,
    val current_question_idx: Int,
    val current_section_idx: Int,
    val id: Int,
    val max_score: Double?=0.0,
    val progress_percentage: Double?=0.0,
    val score: Double?=0.0,
    val total_questions: Int
):Parcelable

@Parcelize
data class Section(
    val answered_flg: Boolean,
    val kind: String,
    val max_score: Double?=0.0,
    val name: String,
    var questions: List<Question>,
    val weight: Int
):Parcelable

@Parcelize
data class Question(
    val answered_flg: Boolean,
    var answers: ArrayList<Answer>,
    val correct_answer_text: String,
    val description: String,
    val explanation_text: String,
    val explanation_type: String,
    val hint_text: String,
    val incorrect_answer_text: String,
    val kind: String,
    val max_score: Int,
    val message_flg: Boolean,
    val multiple_flg: Boolean,
    val name: String,
    val required_flg: Boolean,
    val score: Int,
    val type: String,
    var section_name: String?=null,
    val weight: Int
):Parcelable

@Parcelize
data class Answer(
    var `data`: String?=null,
    val kind: String,
    val name: String,
    val score: Int,
    var selected_flg: Boolean= false
):Parcelable

