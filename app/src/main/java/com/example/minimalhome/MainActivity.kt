package com.example.minimalhome

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.concurrent.thread
import kotlin.math.sin
import kotlin.random.Random

/** Original nature scenes drawn in code: no image files, tiny install, no licensing. */
object Scenes {
    const val COUNT = 4

    fun make(index: Int, w: Int = 960, h: Int = 540): Bitmap {
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        when (((index % COUNT) + COUNT) % COUNT) {
            0 -> dawn(c, w, h)
            1 -> dusk(c, w, h)
            2 -> night(c, w, h)
            else -> dunes(c, w, h)
        }
        return bmp
    }

    private fun col(v: Long) = v.toInt()

    private fun sky(c: Canvas, w: Int, h: Int, top: Int, bottom: Int) {
        val p = Paint()
        p.shader = LinearGradient(0f, 0f, 0f, h * 0.8f, top, bottom, Shader.TileMode.CLAMP)
        c.drawRect(0f, 0f, w.toFloat(), h.toFloat(), p)
    }

    private fun glow(c: Canvas, x: Float, y: Float, r: Float, color: Int) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.shader = RadialGradient(x, y, r, color, Color.TRANSPARENT, Shader.TileMode.CLAMP)
        c.drawCircle(x, y, r, p)
    }

    private fun ridge(c: Canvas, w: Int, h: Int, base: Float, amp: Float, seed: Int, color: Int) {
        val path = Path()
        path.moveTo(0f, h.toFloat())
        var x = 0
        while (x <= w) {
            val t = x / w.toDouble()
            val n = sin(t * 6.0 + seed) * 0.5 + sin(t * 13.0 + seed * 2.1) * 0.3 + sin(t * 29.0 + seed * 3.7) * 0.2
            path.lineTo(x.toFloat(), base + amp * n.toFloat())
            x += 4
        }
        path.lineTo(w.toFloat(), h.toFloat())
        path.close()
        c.drawPath(path, Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color })
    }

    private fun dawn(c: Canvas, w: Int, h: Int) {
        sky(c, w, h, col(0xFF27345F), col(0xFFF3B27A))
        glow(c, w * 0.7f, h * 0.55f, h * 0.7f, col(0xAAFFE2B0))
        ridge(c, w, h, h * 0.55f, h * 0.12f, 1, col(0xFF8E8FB5))
        ridge(c, w, h, h * 0.65f, h * 0.12f, 2, col(0xFF63658F))
        ridge(c, w, h, h * 0.76f, h * 0.10f, 3, col(0xFF3F4268))
        ridge(c, w, h, h * 0.88f, h * 0.08f, 4, col(0xFF222542))
    }

    private fun dusk(c: Canvas, w: Int, h: Int) {
        sky(c, w, h, col(0xFF3A2C5E), col(0xFFFF9A62))
        val sx = w * 0.5f
        val sy = h * 0.52f
        glow(c, sx, sy, h * 0.55f, col(0xBBFFD08A))
        c.drawCircle(sx, sy, h * 0.07f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = col(0xFFFFE8B8) })
        ridge(c, w, h, h * 0.60f, h * 0.10f, 5, col(0xFF4A3A63))
        val lakeTop = h * 0.66f
        val lake = Paint()
        lake.shader = LinearGradient(0f, lakeTop, 0f, h.toFloat(), col(0xFFE58A5B), col(0xFF2E2547), Shader.TileMode.CLAMP)
        c.drawRect(0f, lakeTop, w.toFloat(), h.toFloat(), lake)
        val refl = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = col(0x88FFE0A8) }
        for (i in 0 until 9) {
            val y = lakeTop + 10 + i * (h * 0.032f)
            val half = h * 0.07f * (1f - i * 0.08f)
            c.drawRoundRect(sx - half, y, sx + half, y + 4f, 3f, 3f, refl)
        }
    }

    private fun night(c: Canvas, w: Int, h: Int) {
        sky(c, w, h, col(0xFF050A1C), col(0xFF1B3A4B))
        val rnd = Random(7)
        val star = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        for (i in 0 until 140) {
            star.alpha = rnd.nextInt(90, 255)
            c.drawCircle(rnd.nextFloat() * w, rnd.nextFloat() * h * 0.6f, rnd.nextFloat() * 1.4f + 0.4f, star)
        }
        glow(c, w * 0.78f, h * 0.2f, h * 0.25f, col(0x66DDE8FF))
        c.drawCircle(w * 0.78f, h * 0.2f, h * 0.045f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = col(0xFFF4F1E6) })
        ridge(c, w, h, h * 0.68f, h * 0.08f, 6, col(0xFF10222E))
        val pine = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = col(0xFF08141B) }
        var x = -10f
        while (x < w + 10) {
            val ph = h * (0.12f + rnd.nextFloat() * 0.14f)
            val base = h * 0.92f + rnd.nextFloat() * h * 0.05f
            val pw = ph * 0.38f
            val p = Path()
            p.moveTo(x, base); p.lineTo(x + pw / 2, base - ph); p.lineTo(x + pw, base); p.close()
            c.drawPath(p, pine)
            x += pw * 0.55f
        }
        c.drawRect(0f, h * 0.93f, w.toFloat(), h.toFloat(), pine)
    }

    private fun dunes(c: Canvas, w: Int, h: Int) {
        sky(c, w, h, col(0xFF5B8DB8), col(0xFFF7DDB0))
        glow(c, w * 0.25f, h * 0.35f, h * 0.5f, col(0x99FFF6D8))
        ridge(c, w, h, h * 0.62f, h * 0.07f, 8, col(0xFFE0B27A))
        ridge(c, w, h, h * 0.72f, h * 0.09f, 9, col(0xFFC98F55))
        ridge(c, w, h, h * 0.84f, h * 0.08f, 10, col(0xFF9E6A3A))
        ridge(c, w, h, h * 0.95f, h * 0.05f, 11, col(0xFF6E4527))
    }
}

class WallpaperView(ctx: Context) : View(ctx) {
    private var cur: Bitmap? = null
    private var nxt: Bitmap? = null
    private var t = 1f
    private val paint = Paint(Paint.FILTER_BITMAP_FLAG)
    private val shade = Paint()

    fun crossfadeTo(b: Bitmap) {
        if (cur == null) { cur = b; invalidate(); return }
        nxt = b
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 2000
            addUpdateListener { t = it.animatedValue as Float; invalidate() }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(a: Animator) {
                    cur = nxt; nxt = null; t = 1f; invalidate()
                }
            })
            start()
        }
    }

    private fun drawCrop(c: Canvas, b: Bitmap, alpha: Int) {
        val vw = width.toFloat(); val vh = height.toFloat()
        val scale = maxOf(vw / b.width, vh / b.height)
        val sw = vw / scale; val sh = vh / scale
        val sx = (b.width - sw) / 2f; val sy = (b.height - sh) / 2f
        paint.alpha = alpha
        c.drawBitmap(b, Rect(sx.toInt(), sy.toInt(), (sx + sw).toInt(), (sy + sh).toInt()),
            RectF(0f, 0f, vw, vh), paint)
    }

    override fun onDraw(canvas: Canvas) {
        cur?.let { drawCrop(canvas, it, 255) }
        nxt?.let { drawCrop(canvas, it, (t * 255).toInt()) }
        shade.shader = LinearGradient(0f, height * 0.6f, 0f, height.toFloat(),
            Color.TRANSPARENT, 0xAA000000.toInt(), Shader.TileMode.CLAMP)
        canvas.drawRect(0f, height * 0.6f, width.toFloat(), height.toFloat(), shade)
    }
}

/**
 * Minimal Apple TV-style home screen.
 * - Dock of favourite apps (edit FAVOURITES below).
 * - Wallpaper rotates through built-in nature scenes.
 * - Uses each installed app's own icon; shows a letter tile if the app isn't installed
 *   and opens its Play Store search instead.
 */
class MainActivity : Activity() {

    private data class Fav(val label: String, val packages: List<String>)

    // Edit this list to change the dock. First installed package in each list wins.
    private val FAVOURITES = listOf(
        Fav("YouTube", listOf("com.google.android.youtube.tv", "com.google.android.youtube.tvunplugged")),
        Fav("Netflix", listOf("com.netflix.ninja")),
        Fav("Prime Video", listOf("com.amazon.amazonvideo.livingroom", "com.amazon.avod")),
        Fav("Paramount+", listOf("com.cbs.ott", "com.cbs.tve")),
        Fav("NFL+", listOf("com.gotv.nflgamecenter.us.lite", "com.nfl.gamepass")),
        Fav("HBO Max", listOf("com.wbd.stream", "com.hbo.hbonow", "com.hbo.hbomax")),
    )

    private val ui = Handler(Looper.getMainLooper())
    private lateinit var wallpaper: WallpaperView
    private lateinit var clock: TextView
    private lateinit var dock: LinearLayout
    private var sceneIndex = 0
    private var overlay: View? = null

    private val rotate = object : Runnable {
        override fun run() { nextScene(); ui.postDelayed(this, 5 * 60 * 1000L) }
    }
    private val tick = object : Runnable {
        override fun run() {
            clock.text = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
            ui.postDelayed(this, 15_000L)
        }
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.setBackgroundDrawable(ColorDrawable(Color.BLACK))

        val root = FrameLayout(this)
        wallpaper = WallpaperView(this)
        root.addView(wallpaper, FrameLayout.LayoutParams(-1, -1))

        clock = TextView(this).apply {
            setTextColor(Color.WHITE); textSize = 34f
            typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
            setShadowLayer(8f, 0f, 2f, 0x99000000.toInt())
        }
        root.addView(clock, FrameLayout.LayoutParams(-2, -2, Gravity.TOP or Gravity.END).apply {
            setMargins(0, dp(28), dp(48), 0)
        })

        dock = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(22), dp(16), dp(22), dp(16))
            background = GradientDrawable().apply {
                cornerRadius = dp(30).toFloat(); setColor(0x55101018)
                setStroke(dp(1), 0x33FFFFFF)
            }
        }
        root.addView(dock, FrameLayout.LayoutParams(-2, -2, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL).apply {
            bottomMargin = dp(36)
        })
        setContentView(root)
        buildDock()
    }

    private fun launchIntentFor(pkgs: List<String>): Pair<String, Intent>? {
        for (p in pkgs) {
            val i = packageManager.getLeanbackLaunchIntentForPackage(p) ?: packageManager.getLaunchIntentForPackage(p)
            if (i != null) return p to i
        }
        return null
    }

    private fun iconFor(pkg: String): Drawable? = try {
        val pm = packageManager
        val info = pm.getApplicationInfo(pkg, 0)
        pm.getApplicationBanner(pkg) ?: pm.getApplicationIcon(info)
    } catch (e: Exception) { null }

    private fun tile(label: String, icon: Drawable?, onOpen: () -> Unit, size: Int = 84): View {
        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER_HORIZONTAL
            isFocusable = true; isClickable = true
            setPadding(dp(10), dp(8), dp(10), dp(8))
            isFocusableInTouchMode = false
            setOnClickListener { onOpen() }
            setOnFocusChangeListener { v, has ->
                v.animate().scaleX(if (has) 1.18f else 1f).scaleY(if (has) 1.18f else 1f).setDuration(140).start()
                v.background = if (has) GradientDrawable().apply {
                    cornerRadius = dp(22).toFloat(); setColor(0x33FFFFFF)
                } else null
                v.elevation = if (has) dp(8).toFloat() else 0f
            }
        }
        val box = FrameLayout(this)
        if (icon != null) {
            box.addView(ImageView(this).apply {
                setImageDrawable(icon); scaleType = ImageView.ScaleType.FIT_CENTER
                clipToOutline = true
                outlineProvider = android.view.ViewOutlineProvider.BACKGROUND
                background = GradientDrawable().apply { cornerRadius = dp(18).toFloat(); setColor(0x22000000) }
            }, FrameLayout.LayoutParams(dp(size), dp(size)))
        } else {
            box.addView(TextView(this).apply {
                text = label.take(1); setTextColor(Color.WHITE); textSize = 30f
                gravity = Gravity.CENTER; typeface = Typeface.DEFAULT_BOLD
                background = GradientDrawable().apply { cornerRadius = dp(18).toFloat(); setColor(0x66808090) }
            }, FrameLayout.LayoutParams(dp(size), dp(size)))
        }
        col.addView(box, LinearLayout.LayoutParams(dp(size), dp(size)))
        col.addView(TextView(this).apply {
            text = label; setTextColor(Color.WHITE); textSize = 12f; gravity = Gravity.CENTER
            maxLines = 1; setShadowLayer(6f, 0f, 1f, 0x99000000.toInt())
        }, LinearLayout.LayoutParams(dp(size + 16), -2).apply { topMargin = dp(6) })
        return col
    }

    private fun buildDock() {
        dock.removeAllViews()
        for (f in FAVOURITES) {
            val found = launchIntentFor(f.packages)
            val view = if (found != null) {
                tile(f.label, iconFor(found.first), { startActivitySafe(found.second) })
            } else {
                tile(f.label, null, {
                    startActivitySafe(Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=" + Uri.encode(f.label))))
                })
            }
            dock.addView(view, LinearLayout.LayoutParams(-2, -2).apply { setMargins(dp(4), 0, dp(4), 0) })
        }
        dock.addView(tile("All apps", null, { showAllApps() }), LinearLayout.LayoutParams(-2, -2).apply { setMargins(dp(14), 0, dp(4), 0) })
        dock.addView(tile("Settings", null, { startActivitySafe(Intent(Settings.ACTION_SETTINGS)) }), LinearLayout.LayoutParams(-2, -2).apply { setMargins(dp(4), 0, dp(4), 0) })
        dock.getChildAt(0)?.requestFocus()
    }

    private fun startActivitySafe(i: Intent) {
        try { i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(i) } catch (e: Exception) { /* app missing or disabled */ }
    }

    private fun showAllApps() {
        if (overlay != null) return
        val pm = packageManager
        val main = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        val alt = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = (pm.queryIntentActivities(main, 0) + pm.queryIntentActivities(alt, 0))
            .filter { it.activityInfo.packageName != packageName }
            .distinctBy { it.activityInfo.packageName }
            .sortedBy { it.loadLabel(pm).toString().lowercase() }

        val grid = GridLayout(this).apply { columnCount = 6; setPadding(dp(40), dp(40), dp(40), dp(40)) }
        for (ri in apps) {
            val intent = pm.getLeanbackLaunchIntentForPackage(ri.activityInfo.packageName)
                ?: pm.getLaunchIntentForPackage(ri.activityInfo.packageName) ?: continue
            grid.addView(tile(ri.loadLabel(pm).toString(), ri.loadIcon(pm), { startActivitySafe(intent) }, 72),
                GridLayout.LayoutParams().apply { setMargins(dp(8), dp(8), dp(8), dp(8)) })
        }
        val scroll = ScrollView(this).apply {
            setBackgroundColor(0xE6080810.toInt()); addView(grid)
            isFillViewport = true
        }
        (window.decorView as ViewGroup).findViewById<ViewGroup>(android.R.id.content).addView(scroll, FrameLayout.LayoutParams(-1, -1))
        overlay = scroll
        grid.getChildAt(0)?.requestFocus()
    }

    private fun hideAllApps() {
        overlay?.let { (it.parent as? ViewGroup)?.removeView(it) }
        overlay = null
        dock.getChildAt(0)?.requestFocus()
    }

    private fun nextScene() {
        val i = sceneIndex++
        thread { val b = Scenes.make(i); ui.post { wallpaper.crossfadeTo(b) } }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) { if (overlay != null) hideAllApps(); return true }
        return super.onKeyDown(keyCode, event)
    }

    override fun onResume() {
        super.onResume()
        buildDock()
        ui.removeCallbacks(rotate); ui.post(rotate)
        ui.removeCallbacks(tick); ui.post(tick)
    }

    override fun onPause() {
        super.onPause()
        ui.removeCallbacks(rotate); ui.removeCallbacks(tick)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (overlay != null) hideAllApps()
    }
}
