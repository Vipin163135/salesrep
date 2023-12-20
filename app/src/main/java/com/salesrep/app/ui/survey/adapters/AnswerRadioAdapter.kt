package com.salesrep.app.ui.survey.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.salesrep.app.R
import com.salesrep.app.data.models.response.Answer
import com.salesrep.app.data.models.response.Question
import com.salesrep.app.databinding.ItemAnswerRadioBinding
import com.salesrep.app.ui.survey.AnswerClickListener

class AnswerRadioAdapter(
    val callBack: AnswerClickListener,
    val question: Question,
    val isClickable: Boolean?= true
) :
    RecyclerView.Adapter<AnswerRadioAdapter.ViewHolder>() {

    private var answerList = listOf<Answer>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemAnswerRadioBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_answer_radio, parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        holder.bind(productList[position])
        holder.bind(answerList[position],  question,callBack)
    }

    override fun getItemCount(): Int {
        return answerList.size
    }

    fun notifyData(list: ArrayList<Answer>) {
        this.answerList= listOf()
        list?.let {
            this.answerList= it
            notifyDataSetChanged()
        }
    }

   inner class ViewHolder(val binding: ItemAnswerRadioBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(answer: Answer, question: Question, callBack: AnswerClickListener) {

            binding.rlAnswer.setOnClickListener {
               if (isClickable==true) {
                   callBack.onAnswerClick(adapterPosition, answer, question)
               }
            }

            binding.tvAnswer.text = answer.name

//            if (question.answered_flg) {
//
//                if ((answer.score > 0.0f ) && answer.selected_flg) {
//                    binding.rlAnswer.setBackgroundResource(R.drawable.bg_answer_selected_green)
//                    binding.tvAnswer.setTextColor(binding.root.context.resources.getColor(R.color.answer_green))
//                } else if ((answer.score > 0.0f )) {
//                    binding.rlAnswer.setBackgroundResource(R.drawable.bg_answer_green_outline)
//                    binding.tvAnswer.setTextColor(binding.root.context.resources.getColor(R.color.answer_green))
//                } else if (answer.selected_flg) {
//                    binding.rlAnswer.setBackgroundResource(R.drawable.bg_answer_selected_red)
//                    binding.tvAnswer.setTextColor(binding.root.context.resources.getColor(R.color.answer_red))
//                } else {
//                    binding.rlAnswer.setBackgroundResource(R.drawable.bg_answer_red_outline)
//                    binding.tvAnswer.setTextColor(binding.root.context.resources.getColor(R.color.answer_red))
//                }
//
//            } else
            if (answer.selected_flg) {
                binding.rlAnswer.setBackgroundResource(R.drawable.bg_outline_grey_dark_filled)
                binding.tvAnswer.setTextColor(binding.root.context.resources.getColor(R.color.grey_6))
                binding.rdAns.isChecked=true
            }
            else {
                binding.rlAnswer.setBackgroundResource(R.drawable.bg_outline_grey_dark)
                binding.tvAnswer.setTextColor(binding.root.context.resources.getColor(R.color.grey_6))
                binding.rdAns.isChecked=false
            }

        }

    }
}
