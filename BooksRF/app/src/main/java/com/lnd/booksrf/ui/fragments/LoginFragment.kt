package com.lnd.booksrf.ui.fragments

import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.lnd.booksrf.R
import com.lnd.booksrf.databinding.FragmentLoginBinding
import com.lnd.booksrf.utils.message

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth
    private var email = ""
    private var password = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()

        // Si un usuario ya estaba autenticado, mandarlo a la pantalla principal
        firebaseAuth.currentUser?.let { user ->
            if (user.isEmailVerified) {
                actionLoginSuccessful()
            } else {
                firebaseAuth.signOut()
            }
        }

        binding.btnLogin.setOnClickListener {
            if (!validateFields()) return@setOnClickListener
            binding.progressBar.visibility = View.VISIBLE
            authenticateUser(email,password)
        }

        binding.tvSignUp.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            findNavController().navigate(R.id.action_loginFragment_to_SignUpFragment)
        }

        binding.tvForgotPassword.setOnClickListener {
            resetPassword()
        }

        binding.tvCorreoNoVerificado.visibility = View.GONE
        binding.btnReenviarVerificacion.visibility = View.GONE
    }

    private fun validateFields(): Boolean {
        email = binding.tietEmail.text.toString().trim()
        password = binding.tietPassword.text.toString().trim()

        if (email.isEmpty()) {
            binding.tietEmail.error = getString(R.string.email_required)
            binding.tietEmail.requestFocus()
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tietEmail.error = getString(R.string.invalid_email_format)
            binding.tietEmail.requestFocus()
            return false
        }

        if (password.isEmpty()) {
            binding.tietPassword.error = getString(R.string.password_required)
            binding.tietPassword.requestFocus()
            return false
        } else if (password.length < 6) {
            binding.tietPassword.error = getString(R.string.invalid_long_password)
            binding.tietPassword.requestFocus()
            return false
        }

        return true
    }

    private fun handleErrors(task: Task<AuthResult>) {
        var errorCode = ""

        try {
            errorCode = (task.exception as FirebaseAuthException).errorCode
        } catch (e: Exception) {
            e.printStackTrace()
        }

        when (errorCode) {
            "ERROR_INVALID_EMAIL" -> {
                requireActivity().message(getString(R.string.error_invalid_email))
                binding.tietEmail.error = getString(R.string.error_invalid_email)
                binding.tietEmail.requestFocus()
            }
            "ERROR_WRONG_PASSWORD" -> {
                requireActivity().message(getString(R.string.error_wrong_password))
                binding.tietPassword.error = getString(R.string.error_wrong_password)
                binding.tietPassword.requestFocus()
                binding.tietPassword.setText("")
            }
            "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" -> {
                requireActivity().message(getString(R.string.error_account_exists_with_different_credential))
            }
            "ERROR_EMAIL_ALREADY_IN_USE" -> {
                requireActivity().message(getString(R.string.error_email_already_in_use))
                binding.tietEmail.error = getString(R.string.error_email_already_in_use)
                binding.tietEmail.requestFocus()
            }
            "ERROR_USER_TOKEN_EXPIRED" -> {
                requireActivity().message(getString(R.string.error_user_token_expired))
            }
            "ERROR_USER_NOT_FOUND" -> {
                requireActivity().message(getString(R.string.error_user_not_found))
            }
            "ERROR_WEAK_PASSWORD" -> {
                requireActivity().message(getString(R.string.error_weak_password))
                binding.tietPassword.error = getString(R.string.error_password_length)
                binding.tietPassword.requestFocus()
            }
            "NO_NETWORK" -> {
                requireActivity().message(getString(R.string.error_no_network))
            }
            else -> {
                requireActivity().message(getString(R.string.error_authentication_failed))
            }
        }
    }

    private fun authenticateUser(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { authResult ->
            if (authResult.isSuccessful) {
                val user = firebaseAuth.currentUser
                if (user != null && user.isEmailVerified) {
                    actionLoginSuccessful()
                } else {
                    firebaseAuth.signOut()
                    binding.progressBar.visibility = View.GONE

                    binding.tvCorreoNoVerificado.visibility = View.VISIBLE
                    binding.btnReenviarVerificacion.visibility = View.VISIBLE

                    binding.btnReenviarVerificacion.setOnClickListener {
                        user?.sendEmailVerification()?.addOnSuccessListener {
                            requireActivity().message(getString(R.string.verification_email_sent))
                        }?.addOnFailureListener {
                            requireActivity().message(getString(R.string.verification_email_failed))
                        }
                    }

                    requireActivity().message(getString(R.string.email_not_verified))
                }
            } else {
                binding.progressBar.visibility = View.GONE
                handleErrors(authResult)
            }
        }
    }


    private fun actionLoginSuccessful() {
        requireActivity().message(getString(R.string.login_successful))

        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.loginFragment, true)
            .build()

        findNavController().navigate(
            R.id.action_loginFragment_to_booksListFragment,
            null,
            navOptions
        )
    }

    private fun resetPassword(){
        // Edit text programático
        val resetMail = EditText(requireContext())
        resetMail.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.reset_password)
            .setMessage(getString(R.string.write_email))
            .setView(resetMail)
            .setPositiveButton(getString(R.string.send)) { _, _ ->
                val mail = resetMail.text.toString()
                if(mail.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(mail).matches()){
                    // Enviar el enlace de restablecimiento
                    firebaseAuth.sendPasswordResetEmail(mail).addOnCompleteListener {
                        requireActivity().message(getString(R.string.link_sent))
                    }.addOnFailureListener {
                        requireActivity().message(getString(R.string.link_not_sent))
                    }
                }else{
                    requireActivity().message(getString(R.string.invalid_email_format))
                }
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}