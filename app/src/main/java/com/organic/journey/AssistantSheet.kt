package com.organic.journey

import android.R
import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.organic.journey.databinding.SheetAssistantBinding

class AssistantSheet : BottomSheetDialogFragment() {

    enum class Mode { SUMMARY, TRANSLATE, LISTEN }

    private var _binding: SheetAssistantBinding? = null
    private val binding get() = _binding!!
    private val vm: AssistantViewModel by activityViewModels()

    private lateinit var mode: Mode
    private var speech: SpeechController? = null
    private var speaking = false
    private var shownText = ""
    private var shownLang = Lang.ENGLISH

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = SheetAssistantBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mode = Mode.valueOf(requireArguments().getString(ARG_MODE)!!)
        binding.root.layoutParams?.height = (resources.displayMetrics.heightPixels * 0.65f).toInt()

        binding.spLang.adapter = ArrayAdapter(
            requireContext(), R.layout.simple_spinner_dropdown_item, Lang.values()
        )
        binding.spLang.setSelection(
            if (mode == Mode.TRANSLATE) Lang.HINDI.ordinal else Lang.ENGLISH.ordinal
        )

        speech = SpeechController(
            requireContext(),
            onSpeakingChanged = { setSpeaking(it) },
            onMessage = { msg -> context?.let { Toast.makeText(it, msg, Toast.LENGTH_LONG).show() } }
        )

        when (mode) {
            Mode.SUMMARY -> {
                binding.tvSheetTitle.text = "Summarize"
                binding.tvLangLabel.text = "Summary language"
                binding.btAction.text = "Summarize"
                binding.btAction.setOnClickListener { vm.summarize(selectedLang()) }
                vm.summary.observe(viewLifecycleOwner) { render(it) }
            }
            Mode.TRANSLATE -> {
                binding.tvSheetTitle.text = "Translate page ${vm.currentPage}"
                binding.tvLangLabel.text = "Translate to"
                binding.btAction.text = "Translate"
                binding.btAction.setOnClickListener { vm.translateCurrentPage(selectedLang()) }
                vm.translation.observe(viewLifecycleOwner) { render(it) }
            }
            Mode.LISTEN -> {
                binding.tvSheetTitle.text = "Read page ${vm.currentPage} aloud"
                binding.tvLangLabel.text = "Page language"
                binding.btAction.isVisible = false
                shownText = vm.pageText()
                binding.tvResult.text = shownText.ifBlank { "This page has no selectable text." }
                setResultButtons(shownText.isNotBlank())
            }
        }

        binding.btListen.setOnClickListener {
            if (speaking) {
                speech?.stop()
            } else if (shownText.isNotBlank()) {
                val lang = if (mode == Mode.LISTEN) selectedLang() else shownLang
                speech?.speak(shownText, lang.locale)
            }
        }

        binding.btCopy.setOnClickListener {
            val cm = requireContext().getSystemService(ClipboardManager::class.java)
            cm.setPrimaryClip(ClipData.newPlainText("AI result", shownText))
            Toast.makeText(requireContext(), "Copied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun selectedLang() = binding.spLang.selectedItem as Lang

    private fun render(state: UiState) {
        binding.progress.isVisible = state == UiState.Loading
        binding.btAction.isEnabled = state != UiState.Loading
        when (state) {
            UiState.Idle, UiState.Loading -> {
                binding.tvResult.text = ""
                setResultButtons(false)
            }
            is UiState.Success -> {
                shownText = state.text
                shownLang = state.lang
                binding.spLang.setSelection(state.lang.ordinal)
                binding.tvResult.text = "${state.label}\n\n${state.text}"
                setResultButtons(true)
            }
            is UiState.Error -> {
                binding.tvResult.text = state.message
                setResultButtons(false)
            }
        }
    }

    private fun setResultButtons(enabled: Boolean) {
        binding.btListen.isEnabled = enabled
        binding.btCopy.isEnabled = enabled
    }

    private fun setSpeaking(on: Boolean) {
        speaking = on
        _binding?.btListen?.text = if (on) "Stop" else "Listen"
    }

    override fun onStart() {
        super.onStart()
        (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onDestroyView() {
        speech?.shutdown()
        speech = null
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val ARG_MODE = "mode"

        fun show(fm: FragmentManager, mode: Mode) {
            AssistantSheet().apply {
                arguments = bundleOf(ARG_MODE to mode.name)
            }.show(fm, "assistant")
        }
    }
}