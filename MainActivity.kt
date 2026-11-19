package com.ashika.livetv

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

class MainActivity : AppCompatActivity() {
    private val channels = mutableListOf<Channel>()
    private val favorites = linkedSetOf<String>()
    private lateinit var channelList: LinearLayout
    private lateinit var status: TextView
    private lateinit var playerView: PlayerView
    private lateinit var search: TextInputEditText
    private var player: ExoPlayer? = null
    private var selectedTab = "All"
    private var query = ""

    private val allUrl = "https://iptv-org.github.io/iptv/index.m3u"
    private val bdUrl = "https://iptv-org.github.io/iptv/countries/bd.m3u"
    private val inUrl = "https://iptv-org.github.io/iptv/countries/in.m3u"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(8, 13, 25)
        window.navigationBarColor = Color.rgb(8, 13, 25)
        buildUi()
        loadPlaylist(allUrl, "Loading channels…")
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    private fun buildUi() {
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(8, 13, 25))
            setPadding(dp(14), dp(10), dp(14), dp(8))
        }
        val header = TextView(this).apply {
            text = "ASHIKA LIVE TV"; textSize = 22f; setTextColor(Color.WHITE)
            gravity = Gravity.CENTER_VERTICAL
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        page.addView(header, LinearLayout.LayoutParams(-1, dp(48)))

        playerView = PlayerView(this).apply {
            useController = true
            setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
            setBackgroundColor(Color.BLACK)
        }
        page.addView(playerView, LinearLayout.LayoutParams(-1, dp(218)))

        status = TextView(this).apply {
            text = "Connecting…"; textSize = 12f; setTextColor(Color.LTGRAY)
            setPadding(dp(2), dp(7), 0, dp(7))
        }
        page.addView(status)

        search = TextInputEditText(this).apply {
            hint = "Search channels"; setSingleLine(true); textSize = 15f
            setTextColor(Color.WHITE); setHintTextColor(Color.GRAY)
            setPadding(dp(12), 0, dp(12), 0)
            setBackgroundColor(Color.rgb(24, 32, 48))
        }
        page.addView(search, LinearLayout.LayoutParams(-1, dp(46)))
        search.addTextChangedListener(SimpleTextWatcher { query = it; renderChannels() })

        val tabs = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        listOf("All", "Bangladesh", "India", "Favorites").forEach { tab ->
            val button = MaterialButton(this).apply {
                text = tab; textSize = 10f; insetTop = 0; insetBottom = 0
                setPadding(dp(3), 0, dp(3), 0)
                setOnClickListener {
                    selectedTab = tab
                    when (tab) {
                        "Bangladesh" -> loadPlaylist(bdUrl, "Loading Bangladesh channels…")
                        "India" -> loadPlaylist(inUrl, "Loading India channels…")
                        "All" -> loadPlaylist(allUrl, "Loading all channels…")
                        else -> renderChannels()
                    }
                }
            }
            tabs.addView(button, LinearLayout.LayoutParams(0, dp(42), 1f))
        }
        page.addView(tabs)

        val scroll = ScrollView(this)
        channelList = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(channelList)
        page.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(page)
    }

    private fun loadPlaylist(url: String, message: String) {
        status.text = message
        lifecycleScope.launch {
            try {
                val body = withContext(Dispatchers.IO) {
                    URL(url).openConnection().apply {
                        connectTimeout = 15000; readTimeout = 25000
                        setRequestProperty("User-Agent", "AshikaLiveTV/1.0")
                    }.getInputStream().bufferedReader().use { it.readText() }
                }
                val parsed = M3uParser.parse(body)
                channels.clear(); channels.addAll(parsed)
                status.text = "${parsed.size} channels loaded"
                renderChannels()
            } catch (error: Exception) {
                status.text = "Playlist load failed. Check internet and retry."
                Toast.makeText(this@MainActivity, error.localizedMessage ?: "Network error", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun renderChannels() {
        channelList.removeAllViews()
        val filtered = channels.filter { channel ->
            val tabMatch = when (selectedTab) {
                "Bangladesh" -> channel.country.equals("Bangladesh", true) || channel.group.contains("Bangladesh", true)
                "India" -> channel.country.equals("India", true) || channel.group.contains("India", true)
                "Favorites" -> favorites.contains(channel.url)
                else -> true
            }
            tabMatch && channel.name.contains(query, true)
        }
        if (filtered.isEmpty()) {
            channelList.addView(TextView(this).apply {
                text = if (selectedTab == "Favorites") "No favorites yet. Tap ☆ beside a channel." else "No channels found."
                setTextColor(Color.LTGRAY); setPadding(dp(8), dp(20), dp(8), dp(20))
            })
        }
        filtered.take(1200).forEach { channel ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(10), dp(5), dp(6), dp(5)); setBackgroundColor(Color.rgb(18, 27, 43))
            }
            val name = TextView(this).apply {
                text = "▶  ${channel.name}\n${channel.group}"; textSize = 14f
                setTextColor(Color.WHITE); setPadding(dp(4), dp(3), dp(4), dp(3))
            }
            row.addView(name, LinearLayout.LayoutParams(0, dp(54), 1f))
            val favorite = TextView(this).apply {
                text = if (favorites.contains(channel.url)) "★" else "☆"
                textSize = 24f; setTextColor(Color.YELLOW); gravity = Gravity.CENTER
                setOnClickListener {
                    if (!favorites.add(channel.url)) favorites.remove(channel.url)
                    text = if (favorites.contains(channel.url)) "★" else "☆"
                    if (selectedTab == "Favorites") renderChannels()
                }
            }
            row.addView(favorite, LinearLayout.LayoutParams(dp(44), dp(54)))
            row.setOnClickListener { play(channel) }
            val params = LinearLayout.LayoutParams(-1, dp(60)).apply { bottomMargin = dp(5) }
            channelList.addView(row, params)
        }
    }

    private fun play(channel: Channel) {
        try {
            if (player == null) {
                player = ExoPlayer.Builder(this).build().also { exo ->
                    playerView.player = exo
                    exo.addListener(object : Player.Listener {
                        override fun onPlayerError(error: PlaybackException) {
                            status.text = "Stream unavailable. Try another channel."
                        }
                        override fun onPlaybackStateChanged(state: Int) {
                            if (state == Player.STATE_READY) status.text = "LIVE • ${channel.name}"
                            if (state == Player.STATE_BUFFERING) status.text = "Buffering… • ${channel.name}"
                        }
                    })
                }
            }
            player?.setMediaItem(MediaItem.fromUri(channel.url))
            player?.prepare(); player?.play()
            status.text = "Starting ${channel.name}…"
        } catch (_: Exception) {
            status.text = "Unable to play this stream."
        }
    }

    override fun onDestroy() {
        playerView.player = null
        player?.release(); player = null
        super.onDestroy()
    }
}
