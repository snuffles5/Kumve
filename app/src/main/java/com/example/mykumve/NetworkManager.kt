package com.example.mykumve

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mykumve.databinding.TravelManagerViewBinding
import com.example.mykumve.databinding.TravelNetworkBinding

class NetworkManager : Fragment() {

    private var _binding : TravelNetworkBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = TravelNetworkBinding.inflate(inflater, container, false)

        binding.msNetworkBtn.setOnClickListener{
            findNavController().navigate(R.id.action_networkManager_to_mainScreenManager)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}