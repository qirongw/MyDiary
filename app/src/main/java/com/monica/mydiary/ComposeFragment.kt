package com.monica.mydiary

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.monica.mydiary.database.Diary
import com.monica.mydiary.databinding.FragmentComposeBinding
import java.util.Date

class ComposeFragment : Fragment() {

    private val viewModel: DiariesViewModel by activityViewModels { DiariesViewModel.Factory }
    private var _binding: FragmentComposeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var actionMode: ActionMode? = null
    private val args: ComposeFragmentArgs by navArgs()
    private val isUpdating get() = args.diaryId != -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentComposeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*
        ViewCompat.setOnApplyWindowInsetsListener(binding.appbar) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.updatePadding(top = insets.top)
            windowInsets
        }*/

        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)

        if (isUpdating) {
            viewModel.getDiary(args.diaryId).observe(viewLifecycleOwner) {
                _diary ->
                    if (_diary != null) {
                        binding.input.setText(_diary.content)
                        binding.title.setText(_diary.title)
                        enableEditingActionMode()
                    }
            }
        } else {
            binding.input.setText(viewModel.draft)
            setupContextualMenu()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            ViewCompat.setOnApplyWindowInsetsListener(view) {
                v, insets ->
                v.updatePadding(
                    top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top,
                    bottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom)

                // Return the insets so that they keep going down the view hierarchy
                insets
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupContextualMenu() {
        if (!binding.input.text.isNullOrEmpty()) {
            enableEditingActionMode()
        }
        binding.input.doAfterTextChanged { editable ->
            if (!editable.isNullOrEmpty()) {
                enableEditingActionMode()
            } else {
                actionMode?.finish()
            }
        }
    }

    private fun enableEditingActionMode() {
        if (actionMode != null) {
            return
        }
        val callback = object : ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                mode?.menuInflater?.inflate(R.menu.menu_compose, menu)
                return true
            }

            override fun onPrepareActionMode(p0: ActionMode?, p1: Menu?): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode?, menuItem: MenuItem?): Boolean {
                return when (menuItem?.itemId) {
                    R.id.save -> {
                        if (isUpdating) {
                            updateDiary()
                        } else {
                            saveDiary()
                        }
                        return true
                    }
                    else -> false
                }
            }

            override fun onDestroyActionMode(p0: ActionMode?) {
                if (isUpdating) {
                    findNavController().navigate(R.id.action_ComposeFragment_OverviewFragment)
                } else if (!binding.input.text.isNullOrEmpty()) {
                    confirmToSaveDraft()
                }
                actionMode = null
            }
        }
        actionMode = (requireActivity() as AppCompatActivity)
            .startSupportActionMode(callback)
    }

    private fun updateDiary() {
        val text = binding.input.text.toString()
        val title = binding.title.text.toString()
        val diary = Diary(args.diaryId, title, Date(), text)
        viewModel.updateDiary(diary).observe(viewLifecycleOwner) {
            actionMode?.finish()
        }
    }

    private fun saveDiary() {
        val text = binding.input.text.toString()
        val title = binding.title.text.toString()
        if (text.isNotEmpty()) {
            if (viewModel.saveDiary(title, text)) {
                onSaveSucceed()
            } else {
                onSaveFailed()
            }
        }
    }

    private fun confirmToSaveDraft() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.save_draft)
            .setNegativeButton(resources.getString(R.string.discard)) {
                    _, _ ->
                viewModel.discardDraft()
                onDraftDiscarded()
            }
            .setPositiveButton(resources.getString(R.string.save)) {
                    _, _ ->
                viewModel.saveDraft(binding.input.text.toString())
                onDraftSaved()
            }
            .setCancelable(false)
            .show()
    }

    private fun onDraftSaved() {
        findNavController().navigate(R.id.action_ComposeFragment_OverviewFragment)
        Toast.makeText(requireContext(), "Draft Saved", Toast.LENGTH_SHORT).show()
    }

    private fun onDraftDiscarded() {
        findNavController().navigate(R.id.action_ComposeFragment_OverviewFragment)
    }

    private fun onSaveSucceed() {
        binding.input.text.clear()
        actionMode?.finish()
        findNavController().navigate(R.id.action_ComposeFragment_OverviewFragment)
        Toast.makeText(requireContext(), "Diary Saved Successfully!", Toast.LENGTH_SHORT).show()
    }

    private fun onSaveFailed() {}
}