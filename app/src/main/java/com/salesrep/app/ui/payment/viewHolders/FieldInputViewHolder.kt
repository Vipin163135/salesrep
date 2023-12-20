package com.salesrep.app.ui.payment.viewHolders

import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.salesrep.app.data.models.FieldItem
import com.salesrep.app.util.limitLength
import com.salesrep.app.util.onlyNumbers
import kotlinx.android.synthetic.main.item_field_input_type.view.*

class FieldInputViewHolder (
    itemView: View
//    callBack: OnProductChanges
) : RecyclerView.ViewHolder(itemView) {

    private lateinit var fieldItem: FieldItem
    private var context = itemView.context

    init {
        itemView.etField?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                //do nothing
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //do nothing
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                if (p0?.trim()?.isNotEmpty() == true) {
                fieldItem.setValue = itemView.etField?.text.toString().trim()
//                }
            }
        })

//        itemView.etField.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
////            context?.selectorRelativeLayout(itemView.tvName, itemView.rlText, hasFocus)
//        }



    }

    fun bind(item: FieldItem) = with(itemView) {
        fieldItem = item

        when {
            item.type == "cardnumber" -> {
                etField?.onlyNumbers()
            }
            item.key == "card_exp" -> {
                etField?.inputType= (InputType.TYPE_CLASS_PHONE)
            }
            item.type == "string" -> {
                etField?.inputType = (InputType.TYPE_CLASS_TEXT )
            }
            item.type == "number" -> {
                etField?.onlyNumbers()
            }

            item.type == "username" -> {
                etField?.inputType= InputType.TYPE_CLASS_TEXT +  InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            }
            item.type == "password" -> {
                etField?.inputType= InputType.TYPE_CLASS_TEXT + InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            else -> etField?.inputType = InputType.TYPE_CLASS_TEXT + InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        }

        if (item.max >5) {
            etField?.limitLength(item.max)
        }else{
            etField?.limitLength(50)
        }
        etField?.hint= item.format

        tvFieldName?.text = item.value

//        if (item.name.equals(), true)) {
//            etField.setFilters(arrayOf<InputFilter>(LengthFilter(50)))
//            etField.requestLayout()
//        }

//        if (item?.details_url!=null && item.details_url.isNotEmpty()){
//            ivDetail?.visible()
//        }else{
//            ivDetail?.hide()
//        }

//        if (fieldItem.values?.isNotEmpty() == true)
//            etField?.setText(fieldItem.values?.get(0)?.value ?: "")
//        else {
//            etField?.setText("")
//        }

//        if (item.dependents?.isNotEmpty() == true && item.dependents?.get(0)?.values?.isNotEmpty() == true) {
//            tvDependent?.visible()
//            tvDependent?.text = item.dependents?.get(0)?.values?.get(0)?.value
//        } else
//            tvDependent?.hide()
    }

}