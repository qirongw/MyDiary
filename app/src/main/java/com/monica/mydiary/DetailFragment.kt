package com.monica.mydiary

import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.monica.mydiary.database.Diary
import com.monica.mydiary.databinding.FragmentDetailBinding

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class DetailFragment : Fragment() {

    private val viewModel: DiariesViewModel by activityViewModels { DiariesViewModel.Factory }
    private val args: DetailFragmentArgs by navArgs()
    private var _binding: FragmentDetailBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var diary: Diary
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.loading.visibility = View.VISIBLE
        //(requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar)
        viewModel.getDiary(args.diaryId).observe(viewLifecycleOwner) {
            _diary ->
            if (_diary != null) {
                diary = _diary
                binding.loading.visibility = View.GONE
                binding.detailContent.text = diary.content
                binding.date.text = OverviewAdapter.dateFormatter.format(diary.date)
                binding.toolbar.inflateMenu(R.menu.menu_detail)
                binding.toolbar.title = diary.title
                val navController = findNavController()
                val appBarConfiguration = AppBarConfiguration(navController.graph)
                binding.toolbar.setupWithNavController(navController, appBarConfiguration)
                binding.toolbar.setOnMenuItemClickListener {
                    when(it.itemId) {
                        R.id.delete -> {
                            deleteDiary()
                            true
                        }
                        R.id.edit -> {
                            editDiary()
                            true
                        }
                        else -> false
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun editDiary() {
        val action = DetailFragmentDirections.actionDetailFragmentToComposeFragment(diary.id)
        findNavController().navigate(action)
    }

    private fun deleteDiary() {
        binding.loading.visibility = View.VISIBLE
        viewModel.deleteDiary(diary).observe(viewLifecycleOwner) {
            findNavController().navigate(R.id.action_DetailFragment_to_OverviewFragment)
        }
    }
}