package com.monica.mydiary

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager

import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.monica.mydiary.database.Diary
import com.monica.mydiary.databinding.FragmentComposeBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Date

class ComposeFragment : Fragment() {

    companion object {
        val FILE_PREFIX = "diary_image_"
        val FILE_SUFFIX = ".jpg"
    }

    private val viewModel: DiariesViewModel by activityViewModels { DiariesViewModel.Factory }
    private var _binding: FragmentComposeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var actionMode: ActionMode? = null
    private val args: ComposeFragmentArgs by navArgs()
    private val inUpdateMode get() = args.diaryId != -1
    private var isSaving = false
    private var imageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentComposeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showSoftKeyboard(binding.input)
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
        binding.addPhoto.setOnClickListener {
            selectPhoto()
        }
        viewModel.photoUri.observe(viewLifecycleOwner) {_uri ->
            binding.photo.visibility = View.VISIBLE
            imageUri = _uri
            binding.photo.setImageURI(imageUri)
        }

        if (inUpdateMode) {
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
        clearImage()
        _binding = null
    }

    private fun showSoftKeyboard(view: View) {
        if (view.requestFocus()) {
            val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE)
                    as InputMethodManager
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun selectPhoto() {
        (requireActivity() as MainActivity).pickMedia.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
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
                        if (inUpdateMode) {
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
                if (shouldConfirmToSaveDraft()) {
                    confirmToSaveDraft()
                } else if (inUpdateMode) {
                    findNavController().navigate(R.id.action_ComposeFragment_OverviewFragment)
                }
                actionMode = null
            }
        }
        actionMode = (requireActivity() as AppCompatActivity)
            .startSupportActionMode(callback)
    }

    private fun shouldConfirmToSaveDraft(): Boolean {
        return !inUpdateMode && !isSaving && !binding.input.text.isNullOrEmpty()
    }

    private fun clearImage() {
        viewModel.removeSelectedPhotoUri()
        imageUri = null
    }

    private fun updateDiary() {
        val text = binding.input.text.toString()
        val title = binding.title.text.toString()
        val diary = Diary(args.diaryId, title, Date(), text, imageUri)
        binding.loading.visibility = View.VISIBLE
        viewModel.updateDiary(diary).observe(viewLifecycleOwner) {
            actionMode?.finish()
        }
    }

    private fun saveDiary() {
        isSaving = true
        actionMode?.finish()
        val text = binding.input.text.toString()
        val title = binding.title.text.toString()
        if (text.isNotEmpty() or title.isNotEmpty()) {
            binding.loading.visibility = View.VISIBLE
            //viewModel.saveDiary(title, imageUri, text).observe(viewLifecycleOwner) {
            flow {
                emit(saveDiaryWithImage(title, text))
            }.asLiveData().observe(viewLifecycleOwner) {
                onSaveSucceed()
                isSaving = false
            }

        }
    }

    private suspend fun saveDiaryWithImage(title: String, content:String) {
        var imageFilename:String? = null
        if (imageUri != null) {
            imageFilename = saveImageToFile()
        }
        viewModel.saveDiary(title, imageUri, content)
    }

    fun saveImageToFile(): String {
        val bitmap = (binding.photo.drawable as BitmapDrawable).bitmap
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 0, byteArrayOutputStream)
        val data = byteArrayOutputStream.toByteArray()

        val filename = FILE_PREFIX + System.currentTimeMillis().toString() + FILE_SUFFIX
        requireContext().openFileOutput(filename, Context.MODE_PRIVATE).use {
            it.write(data)
        }
        return filename
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
}