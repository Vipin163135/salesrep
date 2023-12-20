package com.salesrep.app.ui.survey

import com.salesrep.app.data.models.response.Answer
import com.salesrep.app.data.models.response.Question

interface AnswerClickListener {
    fun onAnswerClick(position: Int, answer: Answer, question: Question)
}