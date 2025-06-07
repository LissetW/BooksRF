package com.lnd.booksrf.ui.fragments

import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.lnd.booksrf.utils.message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.lnd.booksrf.R
import com.lnd.booksrf.databinding.FragmentSignUpBinding


class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        firebaseAuth = FirebaseAuth.getInstance()

        binding.btnSignUp.setOnClickListener {
            if (validateFields()) {
                binding.progressBar.visibility = View.VISIBLE
                createUser(binding.tietEmail.text.toString(), binding.tietPassword.text.toString())
            }
        }
    }

    private fun validateFields(): Boolean {
        val email = binding.tietEmail.text.toString().trim()
        val password = binding.tietPassword.text.toString().trim()
        var isValid = true

        if (email.isEmpty()) {
            binding.tilEmail.error = getString(R.string.email_required)
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = getString(R.string.invalid_email_format)
            isValid = false
        } else {
            binding.tilEmail.error = null
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = getString(R.string.password_required)
            isValid = false
        } else if (password.length < 6) {
            binding.tilPassword.error = getString(R.string.invalid_long_password)
            isValid = false
        } else {
            binding.tilPassword.error = null
        }

        return isValid
    }

    private fun createUser(usr: String, psw: String) {
        firebaseAuth.createUserWithEmailAndPassword(usr, psw).addOnCompleteListener { authResult ->
            binding.progressBar.visibility = View.GONE
            if (authResult.isSuccessful) {
                firebaseAuth.currentUser?.sendEmailVerification()
                    ?.addOnSuccessListener {
                        requireActivity().message(getString(R.string.verification_email_sent))
                    }
                    ?.addOnFailureListener {
                        requireActivity().message(getString(R.string.verification_email_not_sent))
                    }

                requireActivity().message(getString(R.string.user_created))
                actionLoginSuccessful()
            } else {
                handleErrors(authResult)
            }
        }
    }

    private fun actionLoginSuccessful() {
        // Navegar al home o a login
        findNavController().navigate(R.id.action_SignUpFragment_to_loginFragment)
    }

    private fun handleErrors(task: Task<AuthResult>) {
        val message = when (val exception = task.exception) {
            is FirebaseAuthWeakPasswordException -> getString(R.string.invalid_long_password)
            is FirebaseAuthInvalidCredentialsException -> getString(R.string.invalid_email_format)
            is FirebaseAuthUserCollisionException -> getString(R.string.email_already_registered)
            else -> exception?.localizedMessage ?: getString(R.string.unknown_error)
        }
        requireActivity().message(message)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
