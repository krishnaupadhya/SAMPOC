package com.prism.poc.onboard.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.prism.poc.*


class LoginActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        val username = findViewById<EditText>(R.id.username)
        val password = findViewById<EditText>(R.id.password)
        val login = findViewById<Button>(R.id.login)


        login.setOnClickListener {
            when {
                TextUtils.isEmpty(username.text) -> {
                    showLoginFailed(R.string.invalid_username)
                }
                TextUtils.isEmpty(password.text) -> {
                    showLoginFailed(R.string.invalid_password)
                }
                else -> {
                    GenericUtil.putInSharedPreferences(AppConstants.USER_NAME, username.text.toString())
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                }
            }
        }
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }
}

