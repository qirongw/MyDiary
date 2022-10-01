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
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
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
        viewModel.getDiary(args.diaryId).observe(viewLifecycleOwner) {
            _diary ->
            if (_diary != null) {
                diary = _diary
                binding.loading.visibility = View.GONE
                binding.textviewSecond.text = diary.title
                setupMenu()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_detail, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when(menuItem.itemId) {
                    R.id.delete -> {
                        deleteDiary()
                        true
                    }
                    else -> false
                }
            }

        }, viewLifecycleOwner)
    }

    private fun deleteDiary() {
        binding.loading.visibility = View.VISIBLE
        viewModel.deleteDiary(diary).observe(viewLifecycleOwner) {
            findNavController().navigate(R.id.action_DetailFragment_to_OverviewFragment)
        }
    }
}