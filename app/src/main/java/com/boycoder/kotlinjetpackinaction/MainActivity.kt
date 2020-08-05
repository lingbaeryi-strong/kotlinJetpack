package com.boycoder.kotlinjetpackinaction

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.boycoder.kotlinjetpackinaction.chapter.c04.WebActivity
import com.boycoder.kotlinjetpackinaction.chapter.c06.*
import com.boycoder.kotlinjetpackinaction.entity.User
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private val TAG = "Main"
    private val requestQueue: RequestQueue by lazy(LazyThreadSafetyMode.NONE) {
        Volley.newRequestQueue(this)
    }

    private val preference: SharedPreferences by lazy(LazyThreadSafetyMode.NONE) {
        getSharedPreferences(SP_NAME, MODE_PRIVATE)
    }

    private val spResponse: String? by lazy(LazyThreadSafetyMode.NONE) {
        preference.getString(SP_KEY_RESPONSE, "")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    private fun init() {
        display(spResponse?:"")
        requestOnlineInfo()
    }

    private fun requestOnlineInfo() {
        val url = "https://api.github.com/users/JakeWharton"
        val stringRequest = StringRequest(Request.Method.GET,
                url,
                { response ->
                    display(response)
                },
                { error ->
                    Toast.makeText(this@MainActivity, error?.message, Toast.LENGTH_SHORT).show()
                })
        stringRequest.tag = TAG
        requestQueue.add(stringRequest)
    }

    private fun display(response: String?) {
        if (response.isNullOrBlank()) {
            return
        }

        preference.edit { putString(SP_KEY_RESPONSE, response) }

        val gson = Gson()
        val user = gson.fromJson(response, User::class.java)
        user?.apply {
            Glide.with(this@MainActivity).load("file:///android_asset/bless.gif").into(gif)
            Glide.with(this@MainActivity).load(user.avatar_url).apply(RequestOptions.circleCropTransform()).into(image)
            image.setOnClickListener { gotoImagePreviewActivity(this) }
            username.setOnClickListener { gotoWebActivity() }

            username.text = ktxSpan {
                name!!.bold().italic().size(1.3F).background(Color.YELLOW)
                        .append("\n")
                        .append("\n")
                        .append("Google".strike().italic().size(0.8F).color(Color.GRAY))
                        .append("\n")
                        .append(company!!.color(Color.BLUE).underline())
                        .append("\n")
                        .append("\n")
                        .append(url(blog!!, blog))
            }

            username.movementMethod = LinkMovementMethod.getInstance()
        }

    }

    private fun gotoImagePreviewActivity(user: User) {
        val intent = Intent(this, ImagePreviewActivity::class.java)
        intent.putExtra(EXTRA_PHOTO, user.avatar_url)
        startActivity(intent)
    }

    private fun gotoWebActivity() {
        val intent = Intent(this, WebActivity::class.java)
        startActivity(intent)
    }

    override fun onStop() {
        super.onStop()
        requestQueue.cancelAll(TAG)
    }
}