package com.salesrep.app.ui.survey

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.github.fragivity.navigator
import com.github.fragivity.pop
import com.salesrep.app.R
import com.salesrep.app.base.BaseFragment
import com.salesrep.app.dao.UpdateSurveyDao
import com.salesrep.app.data.models.requests.SurveyAnswers
import com.salesrep.app.data.models.requests.SurveyUpdateData
import com.salesrep.app.data.models.requests.UpdateSurvey
import com.salesrep.app.data.models.response.*
import com.salesrep.app.data.network.responseUtil.ApisRespHandler
import com.salesrep.app.data.network.responseUtil.Status
import com.salesrep.app.data.repos.UserRepository
import com.salesrep.app.databinding.FragmentSurveyBinding
import com.salesrep.app.ui.dialogs.ProgressDialog
import com.salesrep.app.ui.home.HomeViewModel
import com.salesrep.app.ui.survey.adapters.QuestionsAdapter
import com.salesrep.app.util.*
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class RouteSurveyFragment : BaseFragment<FragmentSurveyBinding>(), AnswerClickListener {

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var updateSurveyDao: UpdateSurveyDao

    private lateinit var binding: FragmentSurveyBinding
    override val viewModel by viewModels<HomeViewModel>()
    private lateinit var progressDialog: ProgressDialog
    private lateinit var surveyList: List<SurveyData>

    private lateinit var questionList: ArrayList<Question>
    private var spinnerList = arrayListOf<String>()
    private var surveySelectedPos = -1

    private lateinit var questionAdapter: QuestionsAdapter


    //    private val productLocations by lazy {
//        prefsManager.getObject(
//            PrefsManager.APP_FORM_CATALOG,
//            GetFormCatalogResponse::class.java
//        )?.ProductLocations
//    }

    private val taskActivity by lazy {
        arguments?.getParcelable<GetRouteAccountResponse>(
            DataTransferKeys.KEY_PRODUCTS
        )
    }
    private val task by lazy {
        arguments?.getParcelable<TaskData>(
            DataTransferKeys.KEY_TASK_DATA
        )
    }
    override fun getLayoutResId(): Int {
        return R.layout.fragment_survey
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        instance: Bundle?,
        binding: FragmentSurveyBinding
    ) {
        this.binding = binding

        progressDialog = ProgressDialog(requireActivity())
        initialize()
        listeners()
        bindObservers()
    }

    private fun listeners() {

        binding.tvBack.setOnClickListener {
            navigator.pop()
        }

        binding.llOption.setOnClickListener {
            if (surveyList.isNullOrEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.no_survey), Toast.LENGTH_LONG)
                    .show()
            } else
                binding.spinner.performClick()
        }

        binding.tvTeamName.setOnClickListener {
            if (surveyList.isNullOrEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.no_survey), Toast.LENGTH_LONG)
                    .show()
            } else
                binding.spinner.performClick()
        }


        binding.tvSave.setOnClickListener {
            if (surveySelectedPos > 0) {
                setFragmentResult(
                    AppRequestCode.CURRENT_TASK_SURVEY_STATUS_CHANGED, bundleOf()
                )
                hitUpdateSurveyApi(surveyList[surveySelectedPos - 1].Survey?.lov_survey_status)
            }
        }

        binding.tvComplete.setOnClickListener {
            if (surveySelectedPos > 0) {
                setFragmentResult(
                    AppRequestCode.CURRENT_TASK_SURVEY_STATUS_CHANGED, bundleOf()
                )
                hitUpdateSurveyApi("Completed")
            }
        }

        binding.tvEnd.setOnClickListener {
            if (surveySelectedPos>0)
                hitUpdateSurveyApi("Completed")

            setFragmentResult(
                AppRequestCode.CURRENT_TASK_SURVEY_STATUS_COMPLETED, bundleOf()
            )
            progressDialog.setLoading(false)

            navigator.pop()
        }

    }

    private fun initialize() {
        surveyList= arrayListOf()
        taskActivity?.Surveys?.let {
            surveyList = it
        }
        spinnerList = arrayListOf()
        spinnerList.add("Select Survey")
        surveyList.forEach {
            spinnerList.add(it.Survey.name)
        }
        if (spinnerList.size > 1) {
            setSpinnerData()
            binding.tvCusomerName.text = taskActivity?.Account?.name
            binding.tvCusomerNum.text = taskActivity?.Account?.accountname
            questionAdapter = QuestionsAdapter(requireContext(), this)
            binding.rvQuestions.adapter = questionAdapter
        }


        val isCompleted = arguments?.getBoolean(DataTransferKeys.KEY_IS_COMPLETED, false)
        if (isCompleted == true) {
            binding.tvComplete.gone()
            binding.tvEnd.gone()
            binding.tvSave.gone()
            questionAdapter.notifyClickable(false)
        }

    }

    @SuppressLint("FragmentLiveDataObserve")
    private fun bindObservers() {

        viewModel.updateSurveyApiResponse.setObserver(viewLifecycleOwner, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.LOADING -> {
                    progressDialog.setLoading(true)
                }
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)
                }
                Status.ERROR -> {
                    progressDialog.setLoading(false)
                    ApisRespHandler.handleError(
                        it.error,
                        requireActivity(),
                        prefsManager = prefsManager
                    )
                }
            }
        })
    }


    fun setSpinnerData() {

        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            spinnerList.toList()
        )

        binding.spinner.adapter = spinnerAdapter

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position > 0) {

                    surveySelectedPos = position
                    binding.tvTeamName.text = spinnerList?.get(position)

                    questionList = arrayListOf()
                    surveyList[position - 1].SurveyTemplate.let { survey ->

                        survey.Sections.forEach { section ->
                            section.questions.forEach { question ->
                                val ques = question
//                    if (question.mutiple_flg) {
//                        var cnt = 0
//                        question.answers.forEach { if (it.score > 0) cnt++ }
//                        ques.count = cnt
//                    }
                                ques.section_name = section.name
                                questionList.add(ques)
                            }
                        }
                    }

                    questionAdapter.notifyData(questionList)

                    binding.tvStatus.text = surveyList[position - 1].Survey?.lov_survey_status
                    if (surveyList[position - 1].Survey.lov_survey_status == "Completed") {
                        binding.tvSave.gone()
                        binding.tvComplete.gone()
                        questionAdapter.notifyClickable(false)
                    } else {
                        binding.tvSave.visible()
                        binding.tvComplete.visible()
                        questionAdapter.notifyClickable(true)
                    }

                } else {
                    binding.tvSave.gone()
                    binding.tvComplete.gone()

                }

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    override fun onAnswerClick(position: Int, answer: Answer, question: Question) {

        if (!answer.selected_flg) {
//            if (question.multiple_flg) {
            answer.selected_flg = true
            question.answers[position] = answer

            if (!question.multiple_flg) {
                question.answers.forEachIndexed { index, answer ->
                    if (index != position) answer.selected_flg = false
                }
            }
            val pos =
                questionList.indexOfFirst { it.name == question.name && it.description == it.description }

            questionList[pos] = question
            questionAdapter.notifyQuestion(question, pos)

        } else if (answer.selected_flg) {
            answer.selected_flg = false
            question.answers[position] = answer
            val pos =
                questionList.indexOfFirst { it.name == question.name && it.description == it.description }

            questionList[pos] = question
            questionAdapter.notifyQuestion(question, pos)

        }
    }

    private fun hitUpdateSurveyApi(lovSurveyStatus: String) {
        val sectionList = arrayListOf<Section>()

        surveyList[surveySelectedPos - 1].SurveyTemplate.Sections.forEach { section ->
            val quesList = arrayListOf<Question>()
            questionList.forEach { question ->

                if (section.name == question.section_name) {
                    quesList.add(question)
                }
            }
            section.questions = quesList
            sectionList.add(section)
        }

        surveyList[surveySelectedPos - 1].Survey.lov_survey_status = lovSurveyStatus
        binding.tvStatus.text = surveyList[surveySelectedPos - 1].Survey?.lov_survey_status
        if (surveyList[surveySelectedPos - 1].Survey.lov_survey_status == "Completed") {
            binding.tvSave.gone()
            binding.tvComplete.gone()
            questionAdapter.notifyClickable(false)
        } else {
            binding.tvSave.visible()
            binding.tvComplete.visible()
            questionAdapter.notifyClickable(true)
        }


        val setGameAnswerRequest = UpdateSurvey(
            id = surveyList[surveySelectedPos - 1].Survey.id,
            activity_integration_id= task?.Activity?.integration_id.toString(),
            activity_id= task?.Activity?.id.toString(),
            Survey = SurveyUpdateData(
                id = surveyList[surveySelectedPos - 1].Survey.id,
                tmplsurvey_id = surveyList[surveySelectedPos - 1].Survey.tmplsurvey_id,
                account_id = surveyList[surveySelectedPos - 1].Survey.account_id.toString(),
                lov_survey_status = lovSurveyStatus
            ),
            SurveyAnswers = SurveyAnswers(Sections = sectionList)
        )
        if (isConnectedToInternet(requireContext(), false)) {
            viewModel.updateSurveyApi(requireContext(), arrayListOf(setGameAnswerRequest))
        } else {
            lifecycleScope.launchWhenStarted {
                val result = updateSurveyDao.insert(setGameAnswerRequest)
                Timber.e(result.toString())
            }
        }
    }


}