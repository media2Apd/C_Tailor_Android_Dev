package com.cuso.mobile.view.home.sales.sales_order.pdfgenerator

import android.content.Context
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import androidx.core.graphics.createBitmap
import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore


@Suppress("unused_parameter")

class QuotationPdfGenerator(private val context: Context) {

    // Strong reference so WebView isn't garbage-collected mid-render
    private var activeWebView: WebView? = null
    private var activePrintWebView: WebView? = null

    data class QuotationData(
        val quotationNumber: String,
        val logoUrl: String? = null,
        val quotationDate: String,
        val customerName: String,
        val customerAddress: String,
        val customerVat: String = "",
        val customerEmail: String = "",
        val customerPhone: String = "",
        val items: List<QuotationItem>,
        val subtotal: Double,
        val discountPercent: Double = 0.0,
        val discountAmount: Double = 0.0,
        val total: Double,
        val termsAndConditions: List<String> = emptyList(),
        val thankYouMessage: String = "Thank you for your business!",
        val poweredBy: String = "This is a computer-generated quotation and does not require a signature."
    )

    data class SavedPdf(
        val uri: Uri?,
        val displayName: String,
        val file: File? = null,
        val sizeBytes: Long = 0L   // populated explicitly when we know it (MediaStore path)
    ) {
        fun exists(): Boolean = file?.exists() ?: (uri != null)
        fun length(): Long = file?.length() ?: sizeBytes
    }

    data class QuotationItem(
        val description: String,
        val quantity: Int,
        val rate: Double,
        val amount: Double
    )

    // A4 size in points (72 dpi) — matches Canvas PDF page size convention
    private val pageWidthPt = 595
    private val pageHeightPt = 842

    // A WebView not attached to any window/view hierarchy can silently fail
    // to fire onPageFinished on some OEM builds (Samsung, MIUI, etc).
    // Attaching it invisibly to the Activity's decor view fixes this.

    private fun Context.findActivity(): android.app.Activity? {
        var ctx = this
        while (ctx is android.content.ContextWrapper) {
            if (ctx is android.app.Activity) return ctx
            ctx = ctx.baseContext
        }
        return null
    }

    private fun attachToWindow(webView: WebView) {
        val activity = context.findActivity() ?: return
        val decorView = activity.window?.decorView as? android.view.ViewGroup ?: return
        webView.visibility = android.view.View.INVISIBLE
        webView.setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)
        try {
            decorView.addView(webView, 0) // index only — preserves existing LayoutParams
        } catch (e: Exception) {
            android.util.Log.e("QuotationPdfGenerator", "attachToWindow failed", e)
        }
    }

    private fun detachFromWindow(webView: WebView) {
        try {
            (webView.parent as? android.view.ViewGroup)?.removeView(webView)
        } catch (e: Exception) {
            android.util.Log.e("QuotationPdfGenerator", "detachFromWindow failed", e)
        }
    }

    // ────────────────────────────────────────────────────────────
    // MAIN ENTRY: Renders the SAME HTML used in preview into a WebView,
    // captures it as bitmap(s), and writes those bitmaps into a
    // PdfDocument — no package-private classes involved.
    // ────────────────────────────────────────────────────────────
    fun generatePdfFromHtml(
        data: QuotationData,
        fileName: String = "quotation_${data.quotationNumber}.pdf",
        saveToDownloads: Boolean = false,
        onComplete: (SavedPdf?) -> Unit
    ) {
        val density = context.resources.displayMetrics.density
        val renderWidthPx = (pageWidthPt * density).toInt().coerceAtLeast(800)

        val webView = WebView(context)
        activeWebView = webView

        webView.layoutParams = android.view.ViewGroup.LayoutParams(
            renderWidthPx,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
        webView.settings.javaScriptEnabled = false
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true

        // Guard so onComplete fires exactly once even if both the
        // WebViewClient callback and the timeout trigger
        var finished = false
        val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())

        fun finish(result: SavedPdf?) {
            if (finished) return
            finished = true
            detachFromWindow(webView)
            activeWebView = null
            onComplete(result)
        }

        // Safety net: some devices never fire onPageFinished for certain content.
        // Force-resolve after 8s so the UI never hangs forever.
        mainHandler.postDelayed({
            if (!finished) {
                try {
                    android.util.Log.d("QuotationPdfGenerator", "onPageFinished never fired — using 8s timeout fallback")
                    val result = renderWebViewToPdf(webView, renderWidthPx, density, fileName, saveToDownloads)
                    finish(result)
                } catch (e: Exception) {
                    android.util.Log.e("QuotationPdfGenerator", "Render failed (timeout path)", e)
                    finish(null)
                }
            }
        }, 8000)

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                view?.postDelayed({
                    if (finished) return@postDelayed
                    try {
                        val result = renderWebViewToPdf(webView, renderWidthPx, density, fileName, saveToDownloads)
                        finish(result)
                    } catch (e: Exception) {
                        android.util.Log.e("QuotationPdfGenerator", "Render failed (onPageFinished path)", e)
                        finish(null)
                    }
                }, 350)
            }

            override fun onReceivedError(
                view: WebView?,
                request: android.webkit.WebResourceRequest?,
                error: android.webkit.WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                finish(null)
            }
        }

        attachToWindow(webView)
        webView.loadDataWithBaseURL(null, buildQuotationHtml(data), "text/html", "UTF-8", null)
    }

    private fun renderWebViewToPdf(
        webView: WebView,
        renderWidthPx: Int,
        density: Float,
        fileName: String,
        saveToDownloads: Boolean
    ): SavedPdf? {
        webView.measure(
            android.view.View.MeasureSpec.makeMeasureSpec(renderWidthPx, android.view.View.MeasureSpec.EXACTLY),
            android.view.View.MeasureSpec.makeMeasureSpec(0, android.view.View.MeasureSpec.UNSPECIFIED)
        )
        val contentHeightPx = (webView.contentHeight * density).toInt().coerceAtLeast(webView.measuredHeight)

        if (contentHeightPx <= 0) {
            android.util.Log.e(
                "QuotationPdfGenerator",
                "contentHeightPx=$contentHeightPx (contentHeight=${webView.contentHeight}, measuredHeight=${webView.measuredHeight}) — aborting, WebView likely not attached/laid out"
            )
            return null
        }

        android.util.Log.d("QuotationPdfGenerator", "Rendering PDF: widthPx=$renderWidthPx heightPx=$contentHeightPx")
        webView.layout(0, 0, renderWidthPx, contentHeightPx)

        val fullBitmap = createBitmap(renderWidthPx, contentHeightPx)
        val canvas = Canvas(fullBitmap)
        canvas.drawColor(android.graphics.Color.WHITE)
        webView.draw(canvas)

        val pageHeightPx = (renderWidthPx.toFloat() * pageHeightPt / pageWidthPt).toInt()
        val pdfDocument = PdfDocument()

        var yOffset = 0
        var pageNumber = 1
        while (yOffset < contentHeightPx) {
            val sliceHeight = minOf(pageHeightPx, contentHeightPx - yOffset)
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidthPt, pageHeightPt, pageNumber).create()
            val page = pdfDocument.startPage(pageInfo)

            val srcRect = android.graphics.Rect(0, yOffset, renderWidthPx, yOffset + sliceHeight)
            val dstRect = android.graphics.Rect(
                0, 0, pageWidthPt,
                (pageHeightPt.toFloat() * sliceHeight / pageHeightPx).toInt()
            )
            page.canvas.drawBitmap(fullBitmap, srcRect, dstRect, null)

            pdfDocument.finishPage(page)
            yOffset += pageHeightPx
            pageNumber++
        }

        val outputStream = java.io.ByteArrayOutputStream()
        pdfDocument.writeTo(outputStream)
        pdfDocument.close()
        fullBitmap.recycle()

        val bytes = outputStream.toByteArray()

        return if (saveToDownloads) {
            writeBytesToDownloads(bytes, fileName)
        } else {
            val file = File(context.getExternalFilesDir(null), fileName)
            FileOutputStream(file).use { it.write(bytes) }
            SavedPdf(uri = Uri.fromFile(file), displayName = fileName, file = file)
        }
    }

    private fun writeBytesToDownloads(bytes: ByteArray, fileName: String): SavedPdf? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    ?: return null

                resolver.openOutputStream(uri)?.use { out -> out.write(bytes) }

                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)

                SavedPdf(uri = uri, displayName = fileName, sizeBytes = bytes.size.toLong())
            } else {
                @Suppress("DEPRECATION")
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val file = File(downloadsDir, fileName)
                FileOutputStream(file).use { it.write(bytes) }
                SavedPdf(uri = Uri.fromFile(file), displayName = fileName, file = file)
            }
        } catch (e: Exception) {
            android.util.Log.e("QuotationPdfGenerator", "writeBytesToDownloads failed for '$fileName'", e)
            null
        }
    }

    // ── Download PDF (public Downloads folder, exact preview match) ──
    fun downloadQuotationPdf(data: QuotationData, onComplete: (SavedPdf?) -> Unit) {
        val fileName = "quotation_${data.quotationNumber}_${System.currentTimeMillis()}.pdf"
        generatePdfFromHtml(
            data = data,
            fileName = fileName,
            saveToDownloads = true,
            onComplete = { saved ->
                if (saved != null && saved.exists() && saved.length() > 0) {
                    onComplete(saved)
                } else {
                    onComplete(null)
                }
            }
        )
    }

    // ── Print (still uses WebView's built-in adapter — this one IS allowed,
    //     since we're not subclassing the callback classes ourselves) ──
    fun printQuotationPdf(data: QuotationData) {
        val webView = WebView(context)
        activePrintWebView = webView

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                val printAdapter = webView.createPrintDocumentAdapter("Quotation_${data.quotationNumber}")
                val attributes = PrintAttributes.Builder()
                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                    .setResolution(PrintAttributes.Resolution("pdf", "pdf", 300, 300))
                    .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                    .build()
                printManager.print("Quotation", printAdapter, attributes)
                activePrintWebView = null
            }
        }
        webView.loadDataWithBaseURL(null, buildQuotationHtml(data), "text/html", "UTF-8", null)
    }

    // ── HTML for WebView (SAME html used for preview, download, print) ──
    fun buildQuotationHtml(data: QuotationData): String {
        val itemsHtml = data.items.joinToString("") { item ->
            """
        <tr>
            <td class="desc">${item.description}</td>
            <td class="num">${item.quantity}</td>
            <td class="num">₹${String.format(Locale.US, "%.0f", item.rate)}</td>
            <td class="num">₹${String.format(Locale.US, "%.0f", item.amount)}</td>
        </tr>
        """.trimIndent()
        }

        val termsHtml = data.termsAndConditions.joinToString("") { term ->
            "<li>$term</li>"
        }

        return """
    <!DOCTYPE html>
    <html lang="en">
    <head>
    <meta charset="UTF-8">
    <title>Quotation</title>
    <style>
      * {
        margin: 0;
        padding: 0;
        box-sizing: border-box;
      }

      body {
        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
        background: #f3f4f6;
        padding: 24px;
      }

      .container {
        max-width: 710px;
        margin: 0 auto;
        background: #ffffff;
        border-radius: 8px;
        overflow: hidden;
      }

      .topbar {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 20px 24px;
        border-bottom: 1px solid #eef0f3;
      }

      .topbar h1 {
        font-size: 18px;
        font-weight: 700;
        color: black;
      }

      .topbar .icons {
        display: flex;
        gap: 16px;
      }

      .topbar .icons span {
        width: 20px;
        height: 20px;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        color: #4338ca;
        cursor: pointer;
      }

      .quote-header {
        background: #f7f8fa;
        padding: 32px 24px;
        display: flex;
        justify-content: space-between;
      }

      .avatar-box {
        width: 110px;
        height: 110px;
        border-radius: 4px;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 6px;
        background: #fff;
        margin-bottom: 16px;
        overflow: hidden;
      }

      .avatar-box img {
        max-width: 100%;
        max-height: 100%;
        object-fit: contain;
      }

      .avatar-box .dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        background: #cbd5e1;
      }

      .avatar-box .dot.active {
        width: 22px;
        height: 22px;
        background: #7c3aed;
      }

      .recipient-label {
        font-size: 12px;
        font-weight: 700;
        letter-spacing: 0.03em;
        color: #111827;
        margin-bottom: 6px;
      }

      .recipient-info {
        font-size: 13px;
        color: #4b5563;
        line-height: 1.5;
      }

      .quote-meta {
        text-align: right;
      }

      .quote-meta h2 {
        font-size: 24px;
        color: #1e293b;
        margin-bottom: 20px;
      }

      .quote-meta .label {
        font-size: 12px;
        font-weight: 700;
        color: #111827;
        margin-bottom: 2px;
      }

      .quote-meta .value {
        font-size: 13px;
        color: #000000;
        margin-bottom: 14px;
      }

      .items-section {
        padding: 24px 24px 8px;
      }

      table {
        width: 100%;
        border-collapse: collapse;
      }

      thead th {
        font-size: 11px;
        letter-spacing: 0.04em;
        color: #9ca3af;
        text-transform: uppercase;
        font-weight: 600;
        text-align: left;
        padding-bottom: 12px;
        border-bottom: 1px solid #eef0f3;
      }

      thead th.num {
        text-align: right;
      }

      tbody td {
        padding: 14px 0;
        font-size: 14px;
        color: #1f2937;
        border-bottom: 1px solid #f1f2f4;
      }

      tbody td.num {
        text-align: right;
        color: #374151;
      }

      tbody td.desc {
        color: #4338ca;
        font-weight: 500;
      }

      .totals {
        padding: 20px 24px 0;
        display: flex;
        flex-direction: column;
        align-items: flex-end;
        gap: 8px;
      }

      .totals .row {
        display: flex;
        justify-content: space-between;
        width: 260px;
        font-size: 13px;
        color: #6b7280;
      }

      .totals .row.total {
        font-size: 15px;
        font-weight: 700;
        color: #4338ca;
        padding-top: 8px;
      }

      .totals .row .amt {
        color: #1f2937;
        font-weight: 500;
      }

      .totals .row.total .amt {
        color: #4338ca;
        font-weight: 700;
      }

      .terms {
        padding: 40px 24px 20px;
      }

      .terms h3 {
        font-size: 13px;
        color: #1f2937;
        margin-bottom: 10px;
      }

      .terms ul {
        list-style: none;
      }

      .terms li {
        font-size: 12.5px;
        color: #000000;
        line-height: 1.9;
        position: relative;
        padding-left: 14px;
      }

      .terms li::before {
        content: "•";
        position: absolute;
        left: 0;
        color: #000000;
      }

      .footer {
        text-align: center;
        padding: 20px 24px 36px;
      }

      .footer p {
        font-size: 12.5px;
        color: #6b7280;
        margin-bottom: 4px;
      }

      .footer p.small {
        font-size: 11.5px;
        color: #9ca3af;
      }
    </style>
    </head>
    <body>

      <div class="container">

        <div class="quote-header">
          <div>
            <div class="avatar-box">
              ${if (!data.logoUrl.isNullOrEmpty()) {
            """<img src="${data.logoUrl}" onerror="this.style.display='none'"/>"""
        } else {
            """<span class="dot"></span><span class="dot active"></span><span class="dot"></span><span class="dot"></span>"""
        }}
            </div>
            <div class="recipient-label">RECIPIENT</div>
            <div class="recipient-info">
              ${data.customerName}<br>
              ${data.customerAddress.replace("\n", "<br>")}
              ${if (data.customerEmail.isNotEmpty()) "<br>${data.customerEmail}" else ""}
              ${if (data.customerPhone.isNotEmpty()) "<br>${data.customerPhone}" else ""}
            </div>
          </div>

          <div class="quote-meta">
            <h2>Quotation</h2>
            <div class="label">QUOTATION NO</div>
            <div class="value">${data.quotationNumber}</div>
            <div class="label">QUOTATION DATE</div>
            <div class="value">${data.quotationDate}</div>
          </div>
        </div>

        <div class="items-section">
          <table>
            <thead>
              <tr>
                <th>TASK DESCRIPTION</th>
                <th class="num">Qty</th>
                <th class="num">RATE</th>
                <th class="num">AMOUNT</th>
              </tr>
            </thead>
            <tbody>
              $itemsHtml
            </tbody>
          </table>
        </div>

        <div class="totals">
          <div class="row">
            <span>SUBTOTAL</span>
            <span class="amt">₹${String.format(Locale.US, "%.2f", data.subtotal)}</span>
          </div>
          ${if (data.discountPercent > 0) """
          <div class="row">
            <span>DISCOUNT ${String.format(Locale.US, "%.0f", data.discountPercent)}%</span>
            <span class="amt">-₹${String.format(Locale.US, "%.2f", data.discountAmount)}</span>
          </div>
          """ else ""}
          <div class="row total">
            <span>TOTAL</span>
            <span class="amt">₹${String.format(Locale.US, "%.2f", data.total)}</span>
          </div>
        </div>

        ${if (data.termsAndConditions.isNotEmpty()) """
        <div class="terms">
          <h3>Terms &amp; Conditions:</h3>
          <ul>
            $termsHtml
          </ul>
        </div>
        """ else ""}

        <div class="footer">
          <p>${data.thankYouMessage}</p>
          <p class="small">${data.poweredBy}</p>
          <img >
        </div>

      </div>

    </body>
    </html>
    """.trimIndent()
    }
}