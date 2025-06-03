package com.alphacorp.instaloader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.alphacorp.instaloader.databinding.FragmentMainBinding

class MainFragment : Fragment() {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.downloadTypeSwitch.setOnCheckedChangeListener { _, isChecked ->
            binding.inputLayout.hint = if (isChecked) {
                "Enter Instagram Username"
            } else {
                "Enter Instagram Post Link"
            }
        }

        binding.button.setOnClickListener {
            val input = binding.inputBox.text.toString()
            if (input.isNotEmpty()) {
                binding.button.isEnabled = false
                binding.StatusText.text = "Downloading..."
                (activity as? MainActivity)?.startDownload(input, binding.downloadTypeSwitch.isChecked)
            } else {
                (activity as? MainActivity)?.showSnackbar("Please enter a username or post link")
            }
        }
    }

    fun clearStatusText() {
        binding.StatusText.text = ""
        binding.button.isEnabled = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 