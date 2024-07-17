
package com.example.mykumve.ui.register

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.mykumve.R
import com.example.mykumve.databinding.RegisterBinding
import com.example.mykumve.ui.viewmodel.UserViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class RegisterManager : Fragment(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_PICK = 2
    private lateinit var currentPhotoPath: String
    private var _binding: RegisterBinding? = null
    private val binding get() = _binding!!

    private val userViewModel: UserViewModel by activityViewModels()
    private var imageUri: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = RegisterBinding.inflate(inflater, container, false)
        setupFieldValidation(binding.name, 3, getString(R.string.error_empty_name))
        setupFieldValidation(binding.passwordRegister, 6, getString(R.string.error_invalid_password))
        setupFieldValidation(binding.emailRegister, getString(R.string.error_invalid_email))
        setupFieldValidation(binding.PhoneRegister, getString(R.string.error_invalid_phone))

        binding.imagePersonRegister.setOnClickListener {
            showImagePickerDialog()
        }

        binding.RegisterBtn.setOnClickListener {
            if (validateInput()) {
                launch {
                    registerUser(it)
                }
            }
        }

        return binding.root
    }

    private fun showImagePickerDialog() {
        val items = arrayOf<CharSequence>("Take Photo", "Choose from Library", "Cancel")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Add Photo!")
        builder.setItems(items) { dialog, item ->
            when {
                items[item] == "Take Photo" -> {
                    dispatchTakePictureIntent()
                    dialog.dismiss()
                }
                items[item] == "Choose from Library" -> {
                    dispatchPickImageIntent()
                    dialog.dismiss()
                }
                items[item] == "Cancel" -> {
                    dialog.dismiss()
                }
            }
        }
        builder.show()
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    private fun dispatchPickImageIntent() {
        val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickIntent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            binding.imagePersonRegister.setImageBitmap(imageBitmap)
            // Save the image if needed and get its URI
            imageUri = saveImage(imageBitmap)
        } else if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            val selectedImageUri = data?.data
            selectedImageUri?.let {
                binding.imagePersonRegister.setImageURI(it)
                imageUri = it.toString()
            }
        }
    }

    private fun saveImage(bitmap: Bitmap): String {
        // Example: This method saves the image to storage and returns its URI
        // You can implement saving logic according to your app's requirements
        // Here's a basic example assuming you want to save to external storage:
        val savedImageUri = MediaStore.Images.Media.insertImage(
            requireContext().contentResolver,
            bitmap,
            "image_${System.currentTimeMillis()}",
            "Image captured via camera"
        )
        return savedImageUri.toString()
    }

    private fun registerUser(registerBtn: View?) {
        val fullName = binding.name.text.toString()
        val password = binding.passwordRegister.text.toString()
        val email = binding.emailRegister.text.toString()
        val phone = _normalizePhoneNumber(binding.PhoneRegister.text.toString())
        val photo = imageUri

        val nameParts = fullName.split(" ")
        val firstName = nameParts.firstOrNull() ?: ""
        val surname = if (nameParts.size > 1) nameParts.drop(1).joinToString(" ") else null
        userViewModel.registerUser(
            firstName,
            surname,
            email,
            password,
            photo,
            phone
        ) { result ->
            launch(Dispatchers.Main) {
                if (isAdded) {
                    if (result.success) {
                        Toast.makeText(
                            requireContext(),
                            R.string.registration_successful,
                            Toast.LENGTH_SHORT
                        ).show()
                        findNavController().navigate(R.id.action_registerManager_to_loginManager)
                    } else {
                        Toast.makeText(requireContext(), result.reason, Toast.LENGTH_SHORT).show()
                        // Todo descriptive error
                    }
                }
            }
        }
    }

    private fun validateInput(): Boolean {
        var isValid = true

        val fullName = binding.name.text.toString()
        val password = binding.passwordRegister.text.toString()
        val email = binding.emailRegister.text.toString()
        val phone = binding.PhoneRegister.text.toString()

        if (fullName.isBlank() || fullName.length < 3) {
            binding.name.error = getString(R.string.error_empty_name)
            isValid = false
        } else {
            binding.name.error = null
        }

        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailRegister.error = getString(R.string.error_invalid_email)
            isValid = false
        } else {
            binding.emailRegister.error = null
        }

        if (password.isBlank() || password.length < 6) {
            binding.passwordRegister.error = getString(R.string.error_invalid_password)
            isValid = false
        } else {
            binding.passwordRegister.error = null
        }

        if (phone.isBlank() || !isValidPhoneNumber(phone)) {
            binding.PhoneRegister.error = getString(R.string.error_invalid_phone)
            isValid = false
        } else {
            binding.PhoneRegister.error = null
        }

        return isValid
    }

    private fun setupFieldValidation(editText: EditText, minLength: Int, errorMessage: String) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (count > before) {
                    if ((s?.length ?: 0) >= minLength) {
                        editText.error = null
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        editText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && editText.text.length < minLength) {
                editText.error = errorMessage
            } else {
                editText.error = null
            }
        }
    }

    private fun setupFieldValidation(editText: EditText, errorMessage: String) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {}
        })

        editText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && editText.text.isBlank()) {
                editText.error = errorMessage
            } else {
                editText.error = null
            }
        }
    }

    private fun _normalizePhoneNumber(phoneNumber: String): String {
        // Implement your phone number normalization logic here if needed
        return phoneNumber
    }

    private fun isValidPhoneNumber(phone: String): Boolean {
        // Implement your phone number validation logic here if needed
        return true
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

//
//package com.example.mykumve.ui.register
//
//import android.os.Bundle
//import android.text.Editable
//import android.text.TextWatcher
//import android.util.Patterns
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.EditText
//import android.widget.Toast
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.activityViewModels
//import androidx.navigation.fragment.findNavController
//import com.example.mykumve.R
//import com.example.mykumve.databinding.RegisterBinding
//import com.example.mykumve.ui.viewmodel.UserViewModel
//import com.example.mykumve.util.ImagePickerUtil
//import com.example.mykumve.util.PATTERNS
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlin.coroutines.CoroutineContext
//
//
//
//class RegisterManager : Fragment(), CoroutineScope {
//
//    override val coroutineContext: CoroutineContext
//        get() = Dispatchers.IO
//
//    private val REQUEST_IMAGE_CAPTURE = 1
//    private lateinit var currentPhotoPath: String
//    private var _binding: RegisterBinding? = null
//    private val binding get() = _binding!!
//
//    private val userViewModel: UserViewModel by activityViewModels()
//    private lateinit var imagePickerUtil: ImagePickerUtil
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = RegisterBinding.inflate(inflater, container, false)
//        setupFieldValidation(binding.name, 3, getString(R.string.error_empty_name))
//        setupFieldValidation(binding.passwordRegister, 6, getString(R.string.error_invalid_password))
//        setupFieldValidation(binding.emailRegister, getString(R.string.error_invalid_email))
//        setupFieldValidation(binding.PhoneRegister, getString(R.string.error_invalid_phone))
//
//        imagePickerUtil = ImagePickerUtil(this) { uri ->
//            binding.imagePersonRegister.setImageURI(uri)
//        }
//
//        binding.RegisterBtn.setOnClickListener {
//            if (validateInput()) {
//                launch {
//                    registerUser(it)
//                }
//            }
//        }
//        binding.imagePersonRegister.setOnClickListener {
//            imagePickerUtil.pickImage()
//        }
//
//        return binding.root
//    }
//
//    private fun registerUser(registerBtn: View?) {
//        val fullName = binding.name.text.toString()
//        val password = binding.passwordRegister.text.toString()
//        val email = binding.emailRegister.text.toString()
//        val phone = _normalizePhoneNumber(binding.PhoneRegister.text.toString())
//        val photo = imagePickerUtil.getImageUri()?.toString()
//
//        val nameParts = fullName.split(" ")
//        val firstName = nameParts.firstOrNull() ?: ""
//        val surname = if (nameParts.size > 1) nameParts.drop(1).joinToString(" ") else null
//        userViewModel.registerUser(
//            firstName,
//            surname,
//            email,
//            password,
//            photo,
//            phone
//        ) { result ->
//            launch(Dispatchers.Main) {
//                if (isAdded) {
//                    if (result.success) {
//                        Toast.makeText(
//                            requireContext(),
//                            R.string.registration_successful,
//                            Toast.LENGTH_SHORT
//                        ).show()
//                        findNavController().navigate(R.id.action_registerManager_to_loginManager)
//                    } else {
//                        Toast.makeText(requireContext(), result.reason, Toast.LENGTH_SHORT).show()
//                        // Todo descriptive error
//                    }
//                }
//            }
//        }
//    }
//
//
//    private fun validateInput(): Boolean {
//        var isValid = true
//
//        val fullName = binding.name.text.toString()
//        val password = binding.passwordRegister.text.toString()
//        val email = binding.emailRegister.text.toString()
//        val phone = binding.PhoneRegister.text.toString()
//
//        if (fullName.isBlank() || fullName.length < 3) {
//            binding.name.error = getString(R.string.error_empty_name)
//            isValid = false
//        } else {
//            binding.name.error = null
//        }
//
//        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
//            binding.emailRegister.error = getString(R.string.error_invalid_email)
//            isValid = false
//        } else {
//            binding.emailRegister.error = null
//        }
//
//        if (password.isBlank() || password.length < 6) {
//            binding.passwordRegister.error = getString(R.string.error_invalid_password)
//            isValid = false
//        } else {
//            binding.passwordRegister.error = null
//        }
//
//        if (phone.isBlank() || !isValidPhoneNumber(phone)) {
//            binding.PhoneRegister.error = getString(R.string.error_invalid_phone)
//            isValid = false
//        } else {
//            binding.PhoneRegister.error = null
//        }
//
//        return isValid
//    }
//
//    private fun isValidPhoneNumber(phone: String): Boolean {
//// Regex to match an international phone number with a country code (+1 to +9999) optionally followed by a space,
//        // then either 10 digits starting with 0 or 9 digits not starting with 0.
//        val phoneRegex = Regex(PATTERNS.PHONE)
//        return phoneRegex.matches(phone)
//    }
//
//    private fun setupFieldValidation(editText: EditText, minLength: Int, errorMessage: String) {
//        editText.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                if (count > before) {
//                    if ((s?.length ?: 0) >= minLength) {
//                        editText.error = null
//                    }
//                }
//            }
//
//            override fun afterTextChanged(s: Editable?) {}
//        })
//
//        editText.setOnFocusChangeListener { _, hasFocus ->
//            if (!hasFocus && editText.text.length < minLength) {
//                editText.error = errorMessage
//            } else {
//                editText.error = null
//            }
//        }
//    }
//
//    private fun setupFieldValidation(editText: EditText, errorMessage: String) {
//        editText.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
//
//            override fun afterTextChanged(s: Editable?) {}
//        })
//
//        editText.setOnFocusChangeListener { _, hasFocus ->
//            if (!hasFocus && editText.text.isBlank()) {
//                editText.error = errorMessage
//            } else {
//                editText.error = null
//            }
//        }
//    }
//
//    fun _normalizePhoneNumber(phoneNumber: String): String {
//        val matchResult = Regex(PATTERNS.PHONE).matchEntire(phoneNumber)
//
//        if (matchResult != null) {
//            // Extract the country code and the digits after it
//            val countryCode = matchResult.groupValues[1]
//            var digits = matchResult.groupValues[2]
//
//            // Remove the leading zero if it's a 10-digit number starting with zero
//            if (digits.startsWith('0')) {
//                digits = digits.substring(1)
//            }
//
//            // Return the normalized phone number
//            return "+$countryCode$digits"
//        } else {
//            throw IllegalArgumentException("Invalid format: Use '+[country code] [9-10 digits]'. Example: +1234567890 or +123 04567890.")
//        }
//    }
//    private fun showToast(message: String) {
//        launch(Dispatchers.Main) {
//            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
//        }
//    }
//}
