package com.cuso.tailor.view.home.pdfgenerator

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.print.PrintAttributes
import android.print.PrintManager
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.graphics.createBitmap
import com.cuso.tailor.model.finance.CustomerPaymentDetailData
import com.cuso.tailor.utils.currencySymbol
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class FinancePaymentReceiptPdfGenerator(private val context: Context) {

    private var activeWebView: WebView? = null
    private val pageWidthPt = 595
    private val pageHeightPt = 842

    data class SavedPdf(
        val uri: Uri?,
        val displayName: String,
        val file: File? = null,
        val sizeBytes: Long = 0L
    )

    private fun Context.findActivity(): Activity? {
        var ctx = this
        while (ctx is ContextWrapper) {
            if (ctx is Activity) return ctx
            ctx = ctx.baseContext
        }
        return null
    }

    private fun attachToWindow(webView: WebView) {
        val activity = context.findActivity() ?: return
        val decorView = activity.window?.decorView as? ViewGroup ?: return
        webView.visibility = View.INVISIBLE
        webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        try {
            decorView.addView(webView, 0)
        } catch (e: Exception) {
            Log.e("PaymentReceiptPdf", "attachToWindow failed", e)
        }
    }

    private fun detachFromWindow(webView: WebView) {
        try {
            (webView.parent as? ViewGroup)?.removeView(webView)
        } catch (e: Exception) {
            Log.e("PaymentReceiptPdf", "detachFromWindow failed", e)
        }
    }

    /**
     * Launch system Print Dialog for the receipt
     */
    @SuppressLint("SetJavaScriptEnabled")
    fun printReceipt(data: CustomerPaymentDetailData) {
        val activity = context.findActivity() ?: return
        val webView = WebView(context)
        activeWebView = webView

        webView.settings.javaScriptEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = activity.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                val jobName = "${data.paymentNumber.ifBlank { "Receipt" }}_Print"
                val printAdapter = webView.createPrintDocumentAdapter(jobName)
                val printAttributes = PrintAttributes.Builder()
                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                    .setResolution(PrintAttributes.Resolution("res1", "default", 300, 300))
                    .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                    .build()

                printManager?.print(jobName, printAdapter, printAttributes)
            }
        }
        webView.loadDataWithBaseURL(null, buildReceiptHtml(data), "text/html", "UTF-8", null)
    }

    /**
     * Generate & Download PDF to the Downloads folder
     */
    @SuppressLint("SetJavaScriptEnabled")
    fun downloadReceiptPdf(
        data: CustomerPaymentDetailData,
        onComplete: ((SavedPdf?) -> Unit)? = null
    ) {
        val fileName = "${data.paymentNumber.ifBlank { "Receipt" }}_${System.currentTimeMillis()}.pdf"
        Toast.makeText(context, "Generating PDF...", Toast.LENGTH_SHORT).show()

        val density = context.resources.displayMetrics.density
        val renderWidthPx = (pageWidthPt * density).toInt().coerceAtLeast(800)

        val webView = WebView(context)
        activeWebView = webView

        webView.layoutParams = ViewGroup.LayoutParams(
            renderWidthPx,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        webView.settings.javaScriptEnabled = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true

        var finished = false
        val mainHandler = Handler(Looper.getMainLooper())

        fun finish(result: SavedPdf?) {
            if (finished) return
            finished = true
            detachFromWindow(webView)
            activeWebView = null
            if (result != null) {
                Toast.makeText(context, "PDF saved to Downloads: $fileName", Toast.LENGTH_LONG).show()
                onComplete?.invoke(result)
            } else {
                Toast.makeText(context, "Failed to generate PDF", Toast.LENGTH_SHORT).show()
                onComplete?.invoke(null)
            }
        }

        mainHandler.postDelayed({
            if (!finished) {
                try {
                    val result = renderWebViewToPdf(webView, renderWidthPx, density, fileName)
                    finish(result)
                } catch (_: Exception) {
                    finish(null)
                }
            }
        }, 8000)

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                view?.postDelayed({
                    if (finished) return@postDelayed
                    try {
                        val result = renderWebViewToPdf(webView, renderWidthPx, density, fileName)
                        finish(result)
                    } catch (_: Exception) {
                        finish(null)
                    }
                }, 350)
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                finish(null)
            }
        }

        attachToWindow(webView)
        webView.loadDataWithBaseURL(null, buildReceiptHtml(data), "text/html", "UTF-8", null)
    }

    private fun renderWebViewToPdf(
        webView: WebView,
        renderWidthPx: Int,
        density: Float,
        fileName: String
    ): SavedPdf? {
        webView.measure(
            View.MeasureSpec.makeMeasureSpec(renderWidthPx, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        val contentHeightPx = (webView.contentHeight * density).toInt().coerceAtLeast(webView.measuredHeight)

        if (contentHeightPx <= 0) return null

        webView.layout(0, 0, renderWidthPx, contentHeightPx)

        val fullBitmap = createBitmap(renderWidthPx, contentHeightPx)
        val canvas = Canvas(fullBitmap)
        canvas.drawColor(Color.WHITE)
        webView.draw(canvas)

        val pageHeightPx = (renderWidthPx.toFloat() * pageHeightPt / pageWidthPt).toInt()
        val pdfDocument = PdfDocument()

        var yOffset = 0
        var pageNumber = 1
        while (yOffset < contentHeightPx) {
            val sliceHeight = minOf(pageHeightPx, contentHeightPx - yOffset)
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidthPt, pageHeightPt, pageNumber).create()
            val page = pdfDocument.startPage(pageInfo)

            val srcRect = Rect(0, yOffset, renderWidthPx, yOffset + sliceHeight)
            val dstRect = Rect(
                0, 0, pageWidthPt,
                (pageHeightPt.toFloat() * sliceHeight / pageHeightPx).toInt()
            )
            page.canvas.drawBitmap(fullBitmap, srcRect, dstRect, null)

            pdfDocument.finishPage(page)
            yOffset += pageHeightPx
            pageNumber++
        }

        val outputStream = ByteArrayOutputStream()
        pdfDocument.writeTo(outputStream)
        pdfDocument.close()
        fullBitmap.recycle()

        val bytes = outputStream.toByteArray()

        return writeBytesToDownloads(bytes, fileName)
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
            Log.e("PaymentReceiptPdf", "writeBytesToDownloads failed for '$fileName'", e)
            null
        }
    }

    /**
     * Builds HTML matching the attached PDF design exactly
     */
    private fun buildReceiptHtml(data: CustomerPaymentDetailData): String {
        val formattedDate = try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val date = parser.parse(data.paymentDate)
            if (date != null) {
                SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
            } else data.paymentDate.take(10)
        } catch (_: Exception) {
            data.paymentDate.take(10)
        }

        val isVoid = data.status.equals("Void", ignoreCase = true)
        val statusText = if (isVoid) "VOID" else "PAID"
        val statusBg = if (isVoid) "#FEE2E2" else "#E0F2FE"
        val statusColor = if (isVoid) "#DC2626" else "#0284C7"

        val formattedAmount = "${currencySymbol()}${String.format(Locale.getDefault(), "%,.0f", data.amount)}"
        val invoice = data.invoice
        val billNumber = invoice?.invoiceNumber ?: (data.salesOrder?.orderCode ?: data.paymentNumber)
        val billAmount = if (invoice != null) "${currencySymbol()}${String.format(Locale.getDefault(), "%,.0f", invoice.grandTotal)}" else formattedAmount

        return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <title>${data.paymentNumber}</title>
            <style>
                * { margin: 0; padding: 0; box-sizing: border-box; }
                body {
                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
                    background: #ffffff;
                    padding: 40px 48px;
                    color: #1e293b;
                }
                .header-table {
                    width: 100%;
                    margin-bottom: 24px;
                }
                .company-name {
                    font-size: 18px;
                    font-weight: 700;
                    color: #0f172a;
                    display: flex;
                    align-items: center;
                    gap: 8px;
                }
                .company-meta {
                    font-size: 11.5px;
                    color: #64748b;
                    line-height: 1.5;
                    margin-top: 4px;
                }
                .receipt-title-box {
                    text-align: right;
                }
                .status-badge {
                    display: inline-block;
                    background: $statusBg;
                    color: $statusColor;
                    font-size: 11px;
                    font-weight: 700;
                    padding: 3px 12px;
                    border-radius: 999px;
                    letter-spacing: 0.5px;
                    margin-bottom: 8px;
                }
                .receipt-title {
                    font-size: 22px;
                    font-weight: 900;
                    color: #0f172a;
                    letter-spacing: 0.5px;
                    margin-bottom: 6px;
                }
                .receipt-meta {
                    font-size: 11.5px;
                    color: #334155;
                    line-height: 1.5;
                }
                .amount-hero-card {
                    background: #f8faff;
                    border: 1px solid #eef2ff;
                    border-radius: 12px;
                    text-align: center;
                    padding: 24px 16px;
                    margin: 28px 0 32px 0;
                }
                .amount-hero-label {
                    font-size: 11px;
                    font-weight: 700;
                    color: #94a3b8;
                    letter-spacing: 1.2px;
                    margin-bottom: 6px;
                }
                .amount-hero-val {
                    font-size: 38px;
                    font-weight: 800;
                    color: #2563eb;
                }
                .info-grid {
                    width: 100%;
                    margin-bottom: 32px;
                }
                .info-grid td {
                    width: 50%;
                    vertical-align: top;
                    padding-bottom: 18px;
                }
                .info-label {
                    font-size: 10.5px;
                    font-weight: 700;
                    color: #94a3b8;
                    letter-spacing: 0.8px;
                    text-transform: uppercase;
                    margin-bottom: 4px;
                }
                .info-value {
                    font-size: 13.5px;
                    font-weight: 600;
                    color: #0f172a;
                }
                .info-sub {
                    font-size: 12px;
                    color: #64748b;
                }
                .section-header {
                    font-size: 10.5px;
                    font-weight: 700;
                    color: #94a3b8;
                    letter-spacing: 0.8px;
                    text-transform: uppercase;
                    margin-bottom: 12px;
                }
                .bills-table {
                    width: 100%;
                    border-collapse: collapse;
                    margin-bottom: 24px;
                }
                .bills-table th {
                    font-size: 10px;
                    font-weight: 700;
                    color: #64748b;
                    letter-spacing: 0.6px;
                    text-transform: uppercase;
                    padding: 8px 0;
                    border-bottom: 1px solid #f1f5f9;
                }
                .bills-table td {
                    font-size: 13px;
                    padding: 14px 0;
                    border-bottom: 1px solid #f8fafc;
                }
                .col-left { text-align: left; }
                .col-center { text-align: center; }
                .col-right { text-align: right; }
                .total-applied-row td {
                    padding-top: 18px;
                    border-bottom: none;
                    font-size: 13px;
                    font-weight: 700;
                    color: #0f172a;
                }
            </style>
        </head>
        <body>
            <!-- TOP HEADER -->
            <table class="header-table">
                <tr>
                    <td style="vertical-align: top;">
                        <div class="company-name">
                            <span style="display:inline-block; width:14px; height:10px; border:2px solid #2563eb; border-radius:2px;"></span>
                            Nexus Boutique
                        </div>
                        <div class="company-meta">
                            vignesh1p21mc057@gmail.com<br>
                            +919962376763<br>
                            Udhayam Colony, Anna Nagar, Chennai, Tamil Nadu
                        </div>
                    </td>
                    <td class="receipt-title-box" style="vertical-align: top;">
                        <div class="status-badge">$statusText</div>
                        <div class="receipt-title">PAYMENT RECEIPT</div>
                        <div class="receipt-meta">
                            Receipt #: ${data.paymentNumber}<br>
                            Date: $formattedDate
                        </div>
                    </td>
                </tr>
            </table>

            <!-- HERO TOTAL PAID CARD -->
            <div class="amount-hero-card">
                <div class="amount-hero-label">TOTAL AMOUNT PAID</div>
                <div class="amount-hero-val">$formattedAmount</div>
            </div>

            <!-- CUSTOMER & TRANSACTION DETAILS GRID -->
            <table class="info-grid">
                <tr>
                    <td>
                        <div class="info-label">PAID BY (CUSTOMER)</div>
                        <div class="info-value">${data.customer?.fullName ?: "Customer"}</div>
                        <div class="info-sub">${data.customer?.mobileNumber ?: ""}</div>
                    </td>
                    <td>
                        <div class="info-label">REFERENCE / UTR</div>
                        <div class="info-value">${data.referenceNumber?.ifBlank { "—" } ?: "—"}</div>
                    </td>
                </tr>
                <tr>
                    <td>
                        <div class="info-label">MODE OF PAYMENT</div>
                        <div class="info-value">${data.paymentMode.ifBlank { "Cash" }}</div>
                    </td>
                    <td>
                        <div class="info-label">DEPOSIT LEDGER</div>
                        <div class="info-value">${data.depositToAccount?.accountName ?: "Cash on Hand"}</div>
                    </td>
                </tr>
            </table>

            <!-- APPLIED BILLS & ORDERS -->
            <div class="section-header">APPLIED BILLS & ORDERS</div>
            <table class="bills-table">
                <thead>
                    <tr>
                        <th class="col-left" style="width: 35%;">BILL / REF NUMBER</th>
                        <th class="col-center" style="width: 25%;">BILL DATE</th>
                        <th class="col-right" style="width: 20%;">BILL AMOUNT</th>
                        <th class="col-right" style="width: 20%;">PAID AMOUNT</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td class="col-left" style="font-weight: 700;">$billNumber</td>
                        <td class="col-center" style="color: #64748b;">—</td>
                        <td class="col-right" style="color: #475569;">$billAmount</td>
                        <td class="col-right" style="font-weight: 700; color: #16a34a;">$formattedAmount</td>
                    </tr>
                    <tr class="total-applied-row">
                        <td colspan="2" class="col-left">TOTAL APPLIED AMOUNT:</td>
                        <td colspan="2" class="col-right">$formattedAmount</td>
                    </tr>
                </tbody>
            </table>
        </body>
        </html>
        """.trimIndent()
    }
}