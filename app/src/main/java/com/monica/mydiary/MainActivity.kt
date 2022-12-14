package com.monica.mydiary

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.widget.Toolbar
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import com.google.android.material.appbar.MaterialToolbar
import com.monica.mydiary.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val viewModel: DiariesViewModel by viewModels { DiariesViewModel.Factory }
    private lateinit var binding: ActivityMainBinding

    private lateinit var _pickMedia: ActivityResultLauncher<PickVisualMediaRequest>
    val pickMedia get() = _pickMedia

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Registers a photo picker activity launcher in single-select mode.
        _pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
                uri ->
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            if (uri != null) {
                viewModel.setSelectedPhotoUri(uri)
            }
        }
    }
}