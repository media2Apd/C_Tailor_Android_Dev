package com.cuso.tailor.view.home.pdfgenerator

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import com.cuso.tailor.R
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

/**
 * PDF generator for Inventory Payments Made (Payment Receipts).
 * Renders an exact replica of the official receipt layout using WebView to PDF pipeline.
 */
class PaymentReceiptPdfGenerator(private val context: Context) {

    private var activeWebView: WebView? = null

    // Standard A4 dimensions at 72 DPI
    private val pageWidthPt = 595
    private val pageHeightPt = 842

    data class AppliedBillItem(
        val billNumber: String,
        val billDate: String,
        val billAmount: Double,
        val paidAmount: Double,
        val balanceDue: Double
    )

    data class PaymentReceiptData(
        val receiptNumber: String,
        val receiptDate: String,
        val status: String = "PAID",
        val vendorName: String,
        val vendorEmail: String = "",
        val vendorPhone: String = "",
        val vendorAddress: String = "",
        val totalAmountPaid: Double,
        val paidTo: String,
        val referenceNumber: String = "-",
        val paymentMode: String = "Bank Account",
        val amountInWords: String = "",
        val currencySymbol: String = "₹",
        val bills: List<AppliedBillItem> = emptyList(),
        val preparedBy: String = "-",
        val showSignature: Boolean = true,
        val primaryColorHex: String = "#5B45E0"
    )

    data class SavedPdf(
        val uri: Uri?,
        val displayName: String,
        val file: File? = null,
        val sizeBytes: Long = 0L
    ) {
        fun exists(): Boolean = file?.exists() ?: (uri != null)
        fun length(): Long = file?.length() ?: sizeBytes
    }

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
            Log.e("PaymentReceiptPdf", "Failed to attach WebView to window", e)
        }
    }

    private fun detachFromWindow(webView: WebView) {
        try {
            (webView.parent as? ViewGroup)?.removeView(webView)
        } catch (e: Exception) {
            Log.e("PaymentReceiptPdf", "Failed to detach WebView from window", e)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun downloadReceiptPdf(
        data: PaymentReceiptData,
        onComplete: (SavedPdf?) -> Unit
    ) {
        val cleanNumber = data.receiptNumber.replace("/", "_").replace(" ", "_")
        val fileName = "Receipt_${cleanNumber}_${System.currentTimeMillis()}.pdf"

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
            onComplete(result)
        }

        // Safety fallback timer to prevent hanging
        mainHandler.postDelayed({
            if (!finished) {
                try {
                    val result = renderWebViewToPdf(webView, renderWidthPx, density, fileName)
                    finish(result)
                } catch (e: Exception) {
                    Log.e("PaymentReceiptPdf", "Render failed on timeout fallback", e)
                    finish(null)
                }
            }
        }, 7000)

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                view?.postDelayed({
                    if (finished) return@postDelayed
                    try {
                        val result = renderWebViewToPdf(webView, renderWidthPx, density, fileName)
                        finish(result)
                    } catch (e: Exception) {
                        Log.e("PaymentReceiptPdf", "Render failed onPageFinished", e)
                        finish(null)
                    }
                }, 300)
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
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

        if (contentHeightPx <= 0) {
            Log.e("PaymentReceiptPdf", "Render aborted: content height is zero")
            return null
        }

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
            Log.e("PaymentReceiptPdf", "Failed to write PDF to Downloads directory", e)
            null
        }
    }

    private fun drawableToBase64(resId: Int): String {
        return try {
            val drawable = ContextCompat.getDrawable(context, resId) ?: return ""
            val bitmap = if (drawable is BitmapDrawable) {
                drawable.bitmap
            } else {
                val width = drawable.intrinsicWidth.coerceAtLeast(1)
                val height = drawable.intrinsicHeight.coerceAtLeast(1)
                val bmp = createBitmap(width, height)
                val canvas = Canvas(bmp)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bmp
            }
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            val bytes = outputStream.toByteArray()
            "data:image/png;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Builds the exact HTML structure matching the payment receipt design.
     */
    fun buildReceiptHtml(data: PaymentReceiptData): String {
        fun formatMoney(v: Double) = "${data.currencySymbol}${String.format(Locale.US, "%,.0f", v)}"

        val shirtLogoBase64 = drawableToBase64(R.drawable.ic_shirts)
        val brandIconHtml = if (shirtLogoBase64.isNotEmpty()) {
            """<img src="$shirtLogoBase64" class="shirt-logo" alt="icon"/>"""
        } else {
            """
            <svg class="shirt-logo-svg" viewBox="0 0 24 24" fill="none" stroke="${data.primaryColorHex}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20.38 3.46L16 2a4 4 0 01-8 0L3.62 3.46a2 2 0 00-1.34 2.23l.58 3.47a1 1 0 00.99.84H6v10a2 2 0 002 2h8a2 2 0 002-2V10h2.15a1 1 0 00.99-.84l.58-3.47a2 2 0 00-1.34-2.23z"/>
            </svg>
            """.trimIndent()
        }

        val rowsHtml = if (data.bills.isNotEmpty()) {
            data.bills.joinToString("\n") { bill ->
                """
                <tr>
                    <td class="bold dark-text">${bill.billNumber}</td>
                    <td>${bill.billDate}</td>
                    <td>${formatMoney(bill.billAmount)}</td>
                    <td class="bold dark-text">${formatMoney(bill.paidAmount)}</td>
                    <td>${formatMoney(bill.balanceDue)}</td>
                </tr>
                """.trimIndent()
            }
        } else {
            """
            <tr>
                <td colspan="5" style="text-align: center; color: #94A3B8; padding: 16px;">No applied bills</td>
            </tr>
            """.trimIndent()
        }
        // Load cuso_technologies_logo as base64 string
        val footerLogoBase64 = drawableToBase64(R.drawable.cuso_technologies_logo)
        val footerLogoTag = if (footerLogoBase64.isNotEmpty()) {
            """<img src="$footerLogoBase64" class="footer-logo" alt="cuso"/>"""
        } else {
            """<span class="footer-dot-icon"></span>"""
        }

        val totalAppliedAmount = if (data.bills.isNotEmpty()) {
            data.bills.sumOf { it.paidAmount }
        } else {
            data.totalAmountPaid
        }

        return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Payment Receipt</title>
            <style>
                * {
                    margin: 0;
                    padding: 0;
                    box-sizing: border-box;
                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
                }

                body {
                    background: #FFFFFF;
                    color: #1E293B;
                    padding: 40px 48px;
                    width: 100%;
                }

                /* Header Layout */
                .header-table {
                    width: 100%;
                    border-collapse: collapse;
                    margin-bottom: 24px;
                }

                .header-left {
                    vertical-align: top;
                    width: 60%;
                }

                .header-right {
                    vertical-align: top;
                    text-align: right;
                    width: 40%;
                }

                .vendor-title-row {
                    display: flex;
                    align-items: center;
                    gap: 8px;
                    margin-bottom: 4px;
                }

                .shirt-logo, .shirt-logo-svg {
                    width: 18px;
                    height: 18px;
                    display: inline-block;
                    vertical-align: middle;
                }

                .vendor-name {
                    font-size: 13px;
                    font-weight: 700;
                    color: #0F172A;
                }

                .vendor-meta {
                    font-size: 9.5px;
                    color: #64748B;
                    line-height: 1.45;
                }

                .status-badge {
                    display: inline-block;
                    font-size: 10px;
                    font-weight: 700;
                    color: #16A34A;
                    letter-spacing: 0.5px;
                    margin-bottom: 12px;
                }

                .receipt-title {
                    font-size: 11px;
                    font-weight: 700;
                    letter-spacing: 0.5px;
                    color: #0F172A;
                    margin-bottom: 4px;
                }

                .receipt-meta-row {
                    font-size: 9px;
                    color: #64748B;
                    line-height: 1.5;
                }

                .receipt-meta-row span {
                    color: #1E293B;
                    font-weight: 500;
                }

                /* Total Amount Paid Banner */
                .banner-box {
                    background-color: #F6F4FE;
                    border: 1px solid #EBE4FD;
                    border-radius: 6px;
                    padding: 18px 12px;
                    text-align: center;
                    margin-bottom: 24px;
                }

                .banner-label {
                    font-size: 9.5px;
                    font-weight: 700;
                    color: ${data.primaryColorHex};
                    letter-spacing: 0.8px;
                    margin-bottom: 6px;
                }

                .banner-amount {
                    font-size: 24px;
                    font-weight: 800;
                    color: #0F172A;
                    letter-spacing: -0.5px;
                }

                /* Key-Value Details Grid */
                .details-table {
                    width: 100%;
                    border-collapse: collapse;
                    margin-bottom: 24px;
                }

                .details-table td {
                    vertical-align: top;
                    padding: 6px 0;
                    width: 50%;
                }

                .field-label {
                    font-size: 8px;
                    font-weight: 700;
                    color: #94A3B8;
                    letter-spacing: 0.5px;
                    text-transform: uppercase;
                    margin-bottom: 2px;
                }

                .field-value {
                    font-size: 9.5px;
                    font-weight: 500;
                    color: #0F172A;
                }

                .field-value.bold {
                    font-weight: 700;
                }

                .text-right {
                    text-align: right;
                }

                /* Applied Bills Table */
                .section-header {
                    font-size: 8.5px;
                    font-weight: 700;
                    color: #64748B;
                    letter-spacing: 0.5px;
                    margin-bottom: 8px;
                    text-transform: uppercase;
                }

                .bills-table {
                    width: 100%;
                    border-collapse: collapse;
                    margin-bottom: 12px;
                }

                .bills-table thead th {
                    background-color: #F8FAFC;
                    font-size: 8px;
                    font-weight: 700;
                    color: #64748B;
                    letter-spacing: 0.5px;
                    text-transform: uppercase;
                    padding: 8px 10px;
                    text-align: left;
                    border-top: 1px solid #F1F5F9;
                    border-bottom: 1px solid #F1F5F9;
                }

                .bills-table tbody td {
                    font-size: 9px;
                    color: #475569;
                    padding: 10px;
                    border-bottom: 1px solid #F8FAFC;
                }

                .bills-table tbody td.bold {
                    font-weight: 600;
                }

                .bills-table tbody td.dark-text {
                    color: #0F172A;
                }

                /* Summary Row */
                .summary-table {
                    width: 100%;
                    border-collapse: collapse;
                    margin-bottom: 40px;
                }

                .summary-table td {
                    padding: 6px 10px;
                    font-size: 9.5px;
                }

                .summary-label {
                    text-align: right;
                    font-weight: 700;
                    color: #0F172A;
                    width: 80%;
                }

                .summary-value {
                    text-align: right;
                    font-weight: 700;
                    color: #0F172A;
                    width: 20%;
                }

                /* Signature Block */
                .signature-section {
                    display: flex;
                    justify-content: space-between;
                    margin-top: 50px;
                    margin-bottom: 40px;
                    padding: 0 40px;
                }

                .signature-col {
                    text-align: center;
                    width: 160px;
                }

                .signature-line {
                    border-top: 1px solid #E2E8F0;
                    margin-bottom: 6px;
                }

                .signature-label {
                    font-size: 7.5px;
                    font-weight: 700;
                    color: #94A3B8;
                    letter-spacing: 0.5px;
                    text-transform: uppercase;
                }

                /* Disclaimer Footer */
                .disclaimer-text {
                    font-size: 7.5px;
                    color: #94A3B8;
                    text-align: center;
                    margin-bottom: 16px;
                }

                .footer-brand {
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    gap: 4px;
                    font-size: 8px;
                    color: #64748B;
                }
                
                .footer-logo {
                    height: 11px;
                    width: auto;
                    object-fit: contain;
                    vertical-align: middle;
                }

                .footer-dot-icon {
                    width: 7px;
                    height: 7px;
                    background: #F59E0B;
                    display: inline-block;
                    border-radius: 1px;
                }
            </style>
        </head>
        <body>

            <!-- TOP HEADER -->
            <table class="header-table">
                <tr>
                    <td class="header-left">
                        <div class="vendor-title-row">
                            $brandIconHtml
                            <span class="vendor-name">${data.vendorName}</span>
                        </div>
                        <div class="vendor-meta">
                            ${if (data.vendorEmail.isNotEmpty()) "${data.vendorEmail}<br>" else ""}
                            ${if (data.vendorPhone.isNotEmpty()) "${data.vendorPhone}<br>" else ""}
                            ${data.vendorAddress}
                        </div>
                    </td>
                    <td class="header-right">
                        <div class="status-badge">${data.status.uppercase()}</div>
                        <div class="receipt-title">PAYMENT RECEIPT</div>
                        <div class="receipt-meta-row">Receipt #: <span>${data.receiptNumber}</span></div>
                        <div class="receipt-meta-row">Date: <span>${data.receiptDate}</span></div>
                    </td>
                </tr>
            </table>

            <!-- TOTAL AMOUNT PAID BANNER -->
            <div class="banner-box">
                <div class="banner-label">TOTAL AMOUNT PAID</div>
                <div class="banner-amount">${formatMoney(data.totalAmountPaid)}</div>
            </div>

            <!-- KEY-VALUE PAYMENT METRICS -->
            <table class="details-table">
                <tr>
                    <td>
                        <div class="field-label">PAID TO</div>
                        <div class="field-value">${data.paidTo}</div>
                    </td>
                    <td class="text-right">
                        <div class="field-label">REFERENCE</div>
                        <div class="field-value bold">${data.referenceNumber}</div>
                    </td>
                </tr>
                <tr>
                    <td style="padding-top: 14px;">
                        <div class="field-label">MODE OF PAYMENT</div>
                        <div class="field-value">${data.paymentMode}</div>
                    </td>
                    <td class="text-right" style="padding-top: 14px;">
                        <div class="field-label">AMOUNT IN WORDS</div>
                        <div class="field-value">${data.amountInWords}</div>
                    </td>
                </tr>
            </table>

            <!-- APPLIED BILLS SECTION -->
            <div class="section-header">APPLIED BILLS</div>
            <table class="bills-table">
                <thead>
                    <tr>
                        <th>BILL NUMBER</th>
                        <th>BILL DATE</th>
                        <th>BILL AMOUNT</th>
                        <th>PAID AMOUNT</th>
                        <th>BALANCE DUE</th>
                    </tr>
                </thead>
                <tbody>
                    $rowsHtml
                </tbody>
            </table>

            <!-- TOTAL APPLIED AMOUNT -->
            <table class="bills-table">
                <tr>
                    <td class="summary-label">Total Applied Amount</td>
                    <td class="summary-value">${formatMoney(totalAppliedAmount)}</td>
                </tr>
            </table>

            <!-- SIGNATURE AREA -->
            <div class="signature-section">
                <div class="signature-col">
                    <div style="height: 18px;"></div>
                    <div class="signature-line"></div>
                    <div class="signature-label">PREPARED BY</div>
                </div>
                ${if (data.showSignature) """
                <div class="signature-col">
                    <div style="height: 18px;"></div>
                    <div class="signature-line"></div>
                    <div class="signature-label">AUTHORIZED SIGNATURE</div>
                </div>
                """ else ""}
            </div>

            <!-- COMPUTER-GENERATED DISCLAIMER -->
            <div class="disclaimer-text">
                This is a computer-generated receipt and does not require a physical signature.
            </div>

          <!-- FOOTER BRANDING -->
            <div class="footer-brand">
                <span>Created with cuso Receipt</span>
                 $footerLogoTag
            </div>

        </body>
        </html>
        """.trimIndent()
    }
}