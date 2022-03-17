package com.codepath.apps.restclienttemplate

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import com.codepath.apps.restclienttemplate.models.Tweet
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import okhttp3.Headers

class ComposeActivity : AppCompatActivity() {
    lateinit var etCompose: EditText
    lateinit var btnTweet: Button
    lateinit var client: TwitterClient
    lateinit var tvCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compose)

        tvCount = findViewById(R.id.tvCount)
        tvCount.text = "Total Characters Left: 280/280"

        etCompose = findViewById(R.id.etTweetCompose)

        btnTweet = findViewById(R.id.btnTweet)

        client = TwitterApplication.getRestClient(this)

        // Handling user writing text in the compose EditText box
        etCompose.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // Fires right before text is changing
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // Fires right as the text is being changed (even supplies the range of text)
            }

            override fun afterTextChanged(s: Editable) {
                // Fires right after the text has changed
                var remainingCount = 280 - s.length

                tvCount.text = "Total Characters Left: $remainingCount/280"
                if (remainingCount < 0) {
                    btnTweet.isEnabled = false
                    btnTweet.setTextColor(ResourcesCompat.getColor(getResources(), R.color.white, null))
                    btnTweet.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.gray_101, null))
                } else {
                    btnTweet.isEnabled = true
                    btnTweet.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.blue, null))
                }
            }
        })

        // Handling the user's click on the tweet button
        btnTweet.setOnClickListener {
            // Grab the content of edit text (etCompose)
            val tweetContent = etCompose.text.toString()
            // 1. Make suer the tweet isn't empty
            if (tweetContent.isEmpty()) {
                Toast.makeText(this, "Empty tweets not allowed!", Toast.LENGTH_SHORT).show()
            }
            // 2. Make sure the tweet is under character count
            if (tweetContent.length > 280) {
                Toast.makeText(this, "Tweet is too long! Limit is 280 characters", Toast.LENGTH_SHORT).show()
            } else {
                // Make an api call to Twitter to publish tweet
                client.publishTweet(tweetContent, object: JsonHttpResponseHandler() {
                    override fun onSuccess(statusCode: Int, headers: Headers?, json: JSON) {
                        Log.i(TAG, "Successfully published tweet!")
                        // Send the tweet back to TimelineActivity
                        val tweet = Tweet.fromJson(json.jsonObject)

                        val intent = Intent()
                        intent.putExtra("tweet", tweet)
                        setResult(RESULT_OK, intent)
                        finish()
                    }

                    override fun onFailure(
                        statusCode: Int,
                        headers: Headers?,
                        response: String?,
                        throwable: Throwable?,
                    ) {
                        Log.e(TAG, "Failed to publish tweet", throwable)
                    }
                })
            }
        }
    }

    companion object {
        const val TAG = "ComposeActivity"
    }
}