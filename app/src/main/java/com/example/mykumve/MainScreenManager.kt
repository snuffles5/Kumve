package com.example.mykumve

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mykumve.databinding.MainScreenBinding

class MainScreenManager: Fragment(){

    private var _binding : MainScreenBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = MainScreenBinding.inflate(inflater,container,false)

        /*binding.add_btn.setOnClickListener{
            //findNavController().navigate(R.id.action_allItemsFragment_to_addItemFragment)
        }
        */
    return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //binding.recycler.adapter = ItemAdapter(ItemManager.items)
        //binding.recycler.layoutManager = LinearLayoutManager(requireContext())
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}