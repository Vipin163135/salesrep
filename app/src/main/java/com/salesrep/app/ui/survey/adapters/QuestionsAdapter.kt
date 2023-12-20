package com.salesrep.app.ui.survey.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.salesrep.app.R
import com.salesrep.app.data.models.response.Question
import com.salesrep.app.databinding.*
import com.salesrep.app.ui.survey.AnswerClickListener

class QuestionsAdapter (
    val context: Context,
    val callbackAns: AnswerClickListener
    ) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var questionList = ArrayList<Question>()
    var isClickable: Boolean =true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
         when(viewType){
            0-> {
                val binding: ItemQuestionDigitBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_question_digit, parent, false
                )
                return DigitViewHolder(binding)
            }
            1-> {
                val binding: ItemQuestionMultiselectBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_question_multiselect, parent, false
                )
                return MultiselectViewHolder(binding)
            }
            2-> {
                val binding: ItemQuestionMultiselectBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_question_multiselect, parent, false
                )
                return RadioViewHolder(binding)
            }
            3-> {
                val binding: ItemQuestionTrueFalseBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_question_true_false, parent, false
                )
                return TrueFalseViewHolder(binding)
            }
            else-> {
                val binding: ItemQuestionDescriptionBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_question_description, parent, false
                )
                return TextViewHolder(binding)
            }

        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder){
            is MultiselectViewHolder -> holder.bind(questionList[position],callbackAns)
            is RadioViewHolder -> holder.bind(questionList[position],callbackAns)
            is TextViewHolder -> holder.bind(questionList[position],callbackAns)
            is DigitViewHolder -> holder.bind(questionList[position],callbackAns)
            is TrueFalseViewHolder -> holder.bind(questionList[position],callbackAns)
            else->{

            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(questionList[position].type){
            "digit"->0
            "list"->1
            "radio"->2
            "check"->3
            "textarea"->4
            else->6
        }
    }
    override fun getItemCount(): Int {
        return questionList.size
    }

    fun notifyData(list: ArrayList<Question>) {
        this.questionList= arrayListOf()
        list.let {
            this.questionList= it
            notifyDataSetChanged()
        }
    }

    fun notifyClickable(isClickable: Boolean) {
        this.isClickable= isClickable
        notifyDataSetChanged()
    }

    fun notifyQuestion(question: Question, position: Int) {
        questionList[position]= question
        notifyItemChanged(position)
    }

   inner class MultiselectViewHolder(val binding: ItemQuestionMultiselectBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(question: Question, callbackAns: AnswerClickListener) {
            binding.tvQuestion.text= "${adapterPosition+1}. ${question.name}"
            val adapter = AnswerMultiselectAdapter(callbackAns, question, isClickable)
            binding.rvAnswers.adapter = adapter
            adapter.notifyData(question.answers)

        }

    }
   inner class DigitViewHolder(val binding: ItemQuestionDigitBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(question: Question, callbackAns: AnswerClickListener) {
            binding.tvQuestion.text= "${adapterPosition+1}. ${question.name}"
            if (!question.answers.isNullOrEmpty())
                binding.answer= question.answers[0]
            if (isClickable==true){
                binding.etAnswer.isClickable=true
                binding.etAnswer.isFocusable=true
            }else{
                binding.etAnswer.isClickable=false
                binding.etAnswer.isFocusable=false
            }
        }
    }

    inner class TextViewHolder(val binding: ItemQuestionDescriptionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(question: Question, callbackAns: AnswerClickListener) {
            binding.tvQuestion.text= "${adapterPosition+1}. ${question.name}"
            if (!question.answers.isNullOrEmpty())
                binding.answer= question.answers[0]
            if (isClickable==true){
                binding.etAnswer.isClickable=true
                binding.etAnswer.isFocusable=true
            }else{
                binding.etAnswer.isClickable=false
                binding.etAnswer.isFocusable=false
            }
        }
    }

    inner class RadioViewHolder(val binding: ItemQuestionMultiselectBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(question: Question, callbackAns: AnswerClickListener) {
            binding.tvQuestion.text= "${adapterPosition+1}. ${question.name}"
            val adapter = AnswerRadioAdapter(callbackAns, question, isClickable)
            binding.rvAnswers.adapter = adapter

            adapter.notifyData(question.answers)
        }

    }
    inner class TrueFalseViewHolder(val binding: ItemQuestionTrueFalseBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(question: Question, callbackAns: AnswerClickListener) {
            binding.tvQuestion.text= "${adapterPosition+1}. ${question.name}"
            if (!question.answers.isNullOrEmpty()) {
                if (isClickable==true) {
                    binding.rlAnswer.setOnClickListener {
                        callbackAns.onAnswerClick(0, question.answers[0], question)
                    }
                }
                binding.tvAnswer.text= question.answers[0].name
                if (question.answers[0].selected_flg){
                    binding.rdAns.setImageResource(R.drawable.ic_check_green)
                }else{
                    binding.rdAns.setImageResource(R.drawable.ic_cross_red)
                }
            }
        }
    }
}
