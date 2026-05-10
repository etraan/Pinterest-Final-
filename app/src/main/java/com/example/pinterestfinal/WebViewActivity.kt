// Team Members: Ellie Traan, Kymmani Allen, Jazmyne Graham
package com.example.pinterestfinal

import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.TextView
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * WebViewActivity — displays a Pinterest search page for the selected post topic
 * inside an in-app WebView, keeping the user inside the app rather than
 * opening an external browser. This mirrors the StockPageActivity pattern
 * from the class StockMarketTracker example, and the WebView slides from Class 13.
 */
class WebViewActivity : AppCompatActivity() {

    companion object {
        // Key for the post title passed in via intent extras
        const val EXTRA_POST_TITLE = "extra_post_title"
    }

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvLoadingMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_web_view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set up toolbar with back navigation
        val toolbar: Toolbar = findViewById(R.id.toolbarWebView)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Retrieve the post title passed from PostDetailActivity
        val postTitle = intent.getStringExtra(EXTRA_POST_TITLE) ?: "photography"
        supportActionBar?.title = postTitle

        webView          = findViewById(R.id.webView)
        progressBar      = findViewById(R.id.progressBarWeb)
        tvLoadingMessage = findViewById(R.id.tvLoadingMessage)

        setupWebView(postTitle)
    }

    /**
     * Configures the WebView settings and loads the Pinterest search URL
     * for the given post title. Uses a custom WebViewClient to keep all
     * navigation inside the app rather than opening the system browser —
     * this is the exact solution shown in the Class 13 WebView slides.
     */
    private fun setupWebView(postTitle: String) {
        // Enable JavaScript (required for Pinterest to render correctly)
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.builtInZoomControls = true
        webSettings.displayZoomControls = false
        webSettings.useWideViewPort = true
        webSettings.loadWithOverviewMode = true

        // Override URL loading so links open inside this WebView,
        // not in the external Chrome browser — matches the solution from Class 13
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView, request: WebResourceRequest
            ): Boolean {
                view.loadUrl(request.url.toString())
                return true
            }

            // Show the WebView and hide the loading indicator once the page loads
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility      = View.GONE
                tvLoadingMessage.visibility = View.GONE
                webView.visibility          = View.VISIBLE
            }
        }

        // Build a Pinterest search URL using the post title as the search query
        // URL-encode spaces as + signs for a valid query string
        val query = postTitle.trim().replace(" ", "+")
        val url   = "https://www.pinterest.com/search/pins/?q=$query"

        // Load the URL — same pattern as wv.loadUrl() from the class slides
        webView.loadUrl(url)
    }

    // Handle the toolbar back arrow — returns to PostDetailActivity
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
