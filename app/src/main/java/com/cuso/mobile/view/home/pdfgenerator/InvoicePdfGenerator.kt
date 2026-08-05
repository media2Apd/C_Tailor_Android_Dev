package com.cuso.mobile.view.home.pdfgenerator

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import androidx.core.graphics.createBitmap
import android.content.ContentValues
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
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
import androidx.core.content.ContextCompat
import com.cuso.mobile.R
import java.io.ByteArrayOutputStream

@Suppress("unused_parameter")
class InvoicePdfGenerator(private val context: Context) {

    // Strong reference so WebView isn't garbage-collected mid-render
    private var activeWebView: WebView? = null
    private var activePrintWebView: WebView? = null

    data class InvoiceItemData(
        val description: String,
        val hsnSku: String = "-",
        val quantity: Int,
        val unitPrice: Double,
        val discount: String = "-",
        val tax: Double,
        val total: Double
    )

    data class InvoiceData(
        val invoiceNumber: String,
        val invoiceDate: String,
        val dueDate: String,
        val status: String = "unpaid",
        val customerName: String,
        val billToAddress: String = "",
        val billToPhone: String = "",
        val billToEmail: String = "",
        val shipToAddress: String = "",
        val orderReference: String = "",
        val items: List<InvoiceItemData>,
        val subtotal: Double,
        val discountAmount: Double = 0.0,
        val taxAmount: Double,
        val taxPercent: Double = 0.0,
        val shippingAmount: Double = 0.0,
        val totalAmount: Double,
        val paidAmount: Double = 0.0,
        val balanceAmount: Double = 0.0,
        val paymentMethod: String = "Bank Transfer / Card / UPI",
        val bankName: String = "",
        val accountNo: String = "",
        val ifscSwift: String = "",
        val termsAndConditions: String = "Payment due within 30 days of invoice date. Late fees may apply.",
        // dynamic company identity — comes from the API's organization object
        val companyName: String = "",
        val companyAddress: String = "",
        val companyEmail: String = "",
        val companyPhone: String = "",
        val companyGst: String = "",
        val logoUrl: String? = null   // base64 data URI or remote URL; falls back to initial-letter circle if empty
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

    // A4 size in points (72 dpi)
    private val pageWidthPt = 595
    private val pageHeightPt = 842

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
            Log.e("InvoicePdfGenerator", "attachToWindow failed", e)
        }
    }

    private fun detachFromWindow(webView: WebView) {
        try {
            (webView.parent as? ViewGroup)?.removeView(webView)
        } catch (e: Exception) {
            Log.e("InvoicePdfGenerator", "detachFromWindow failed", e)
        }
    }

    // ────────────────────────────────────────────────────────────
    // MAIN ENTRY: renders the SAME HTML used for preview into a WebView,
    // captures bitmap(s), writes into PdfDocument.
    // ────────────────────────────────────────────────────────────
    @SuppressLint("SetJavaScriptEnabled")
    fun generatePdfFromHtml(
        data: InvoiceData,
        fileName: String = "invoice_${data.invoiceNumber}.pdf",
        saveToDownloads: Boolean = false,
        onComplete: (SavedPdf?) -> Unit
    ) {
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

        mainHandler.postDelayed({
            if (!finished) {
                try {
                    Log.d("InvoicePdfGenerator", "onPageFinished never fired — using 8s timeout fallback")
                    val result = renderWebViewToPdf(webView, renderWidthPx, density, fileName, saveToDownloads)
                    finish(result)
                } catch (e: Exception) {
                    Log.e("InvoicePdfGenerator", "Render failed (timeout path)", e)
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
                        Log.e("InvoicePdfGenerator", "Render failed (onPageFinished path)", e)
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
        webView.loadDataWithBaseURL(null, buildInvoiceHtml(data), "text/html", "UTF-8", null)
    }

    private fun renderWebViewToPdf(
        webView: WebView,
        renderWidthPx: Int,
        density: Float,
        fileName: String,
        saveToDownloads: Boolean
    ): SavedPdf? {
        webView.measure(
            View.MeasureSpec.makeMeasureSpec(renderWidthPx, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        val contentHeightPx = (webView.contentHeight * density).toInt().coerceAtLeast(webView.measuredHeight)

        if (contentHeightPx <= 0) {
            Log.e(
                "InvoicePdfGenerator",
                "contentHeightPx=$contentHeightPx (contentHeight=${webView.contentHeight}, measuredHeight=${webView.measuredHeight}) — aborting, WebView likely not attached/laid out"
            )
            return null
        }

        Log.d("InvoicePdfGenerator", "Rendering PDF: widthPx=$renderWidthPx heightPx=$contentHeightPx")
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
            Log.e("InvoicePdfGenerator", "writeBytesToDownloads failed for '$fileName'", e)
            null
        }
    }

    // ── Download PDF (public Downloads folder, exact preview match) ──
    fun downloadInvoicePdf(data: InvoiceData, onComplete: (SavedPdf?) -> Unit) {
        val fileName = "invoice_${data.invoiceNumber}_${System.currentTimeMillis()}.pdf"
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

    // ── Converts a drawable resource (footer/brand logo) into a Base64 data URI ──
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
            Log.e("InvoicePdfGenerator", "drawableToBase64 failed for resId=$resId", e)
            ""
        }
    }

    // ── HTML for WebView (SAME HTML used for preview, download, print) ──
    // Design: exact match of the "RELDA" wide-layout invoice (logo-row, gst-bar,
    // parties-box, QR code, script signature, footer-brand) — every value below
    // comes from `data`, which is populated from the API response.
    fun buildInvoiceHtml(data: InvoiceData): String {

        fun money(v: Double) = "₹" + String.format(Locale.US, "%,.2f", v)

        val companyInitial = data.companyName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "C"
        val logoBlockHtml = if (!data.logoUrl.isNullOrEmpty()) {
            """<img src="${data.logoUrl}" class="logo-icon-img" />"""
        } else {
            """<div class="logo-icon">$companyInitial</div>"""
        }
        val footerLogoBase64 = drawableToBase64(R.drawable.cuso_technologies_logo)   // 🔁 replace R.drawable.logo with your actual drawable name
        val footerLogoTag = if (footerLogoBase64.isNotEmpty()) {
            """<img src="$footerLogoBase64" class="footer-logo"/>"""
        } else ""

        val itemsHtml = data.items.joinToString("") { item ->
            """
        <tr>
            <td class="desc">${item.description}</td>
            <td>${item.hsnSku}</td>
            <td class="center">${item.quantity}</td>
            <td class="num">${money(item.unitPrice)}</td>
            <td class="num">${item.discount}</td>
            <td class="num">${String.format(Locale.US, "%.0f", item.tax)}%</td>
            <td class="num total-cell">${money(item.total)}</td>
        </tr>
        """.trimIndent()
        }

        val taxLabel = if (data.taxPercent > 0) {
            "Tax Breakdown (VAT ${String.format(Locale.US, "%.0f", data.taxPercent)}%):"
        } else {
            "Tax Breakdown:"
        }

        val bankBoxHtml = if (data.bankName.isNotEmpty() || data.accountNo.isNotEmpty() || data.ifscSwift.isNotEmpty()) {
            """
      <div class="bank-box">
        <div class="bank-details-text">
          <div class="bank-title">Bank Details:</div>
          <span class="label">Bank Name:</span> ${data.bankName.ifEmpty { "-" }}<br>
          <span class="label">Account No:</span> ${data.accountNo.ifEmpty { "-" }}<br>
          <span class="label">IFSC/SWIFT:</span> ${data.ifscSwift.ifEmpty { "-" }}
        </div>
        <div class="qr-wrap">
          <div class="qr-code" id="qrCode"></div>
          <div class="qr-caption">UPI QR /<br>Payment QR<br>Code</div>
        </div>
      </div>
      """
        } else ""

        return """
    <!DOCTYPE html>
    <html lang="en">
    <head>
    <meta charset="UTF-8">
    <title>Invoice</title>
    <style>
      * { margin: 0; padding: 0; box-sizing: border-box; }

      body {
        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
        background: #f3f4f6;
        padding: 32px;
      }

      .container {
        max-width: 880px;
        margin: 0 auto;
        background: #ffffff;
        padding: 32px 40px 40px;
      }

      .inv-top {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        gap: 24px;
        margin-bottom: 20px;
      }
      

      .inv-top-col { display: flex; flex-direction: column; }
      .inv-top-col.left { flex: 1; min-width: 0; }
      .inv-top-col.right { align-items: flex-end; text-align: right; flex-shrink: 0; }

      .logo-row { display: flex; align-items: center; gap: 8px; margin-bottom: 10px; }

      .logo-icon {
        width: 90px;
        height: 90px;
        border-radius: 50%;
        background: #4F46E5;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #ffffff;
        font-size: 50px;
        font-weight: 700;
        font-family: Georgia, serif;
        flex-shrink: 0;
      }

      .logo-icon-img {
        width: 100px;
        height: 100px;
        object-fit: contain;
        border-radius: 8px;
        flex-shrink: 0;
      }

     
      .invoice-title { font-size: 34px; font-weight: 800; color: #111827; letter-spacing: 0.5px; margin-bottom: 12px; }

      .company-meta { font-size: 13px; color: #6b7280; line-height: 1.85; }
      .company-meta strong { color: #111827; font-weight: 700; }

      .invoice-meta { text-align: right; font-size: 13px; line-height: 1.85; }
      .invoice-meta .k { color: #111827; font-weight: 700; }
      .invoice-meta .v { color: #374151; }

      .gst-bar {
        background: #F3F4F6;
        width: 100%;
        padding: 12px 16px;
        font-size: 13px;
        color: #374151;
        margin-bottom: 20px;
        white-space: nowrap;
      }

      .parties-box { background: #F8F9FA; display: flex; margin-bottom: 20px; }

      .party-col { flex: 1; padding: 20px 24px; text-align: left; }
      .party-col.divider { border-left: 1px solid #E5E7EB; }

      .party-label { font-size: 13px; font-weight: 700; color: #111827; margin-bottom: 6px; }
      .party-name { font-size: 14px; font-weight: 700; color: #111827; margin-bottom: 4px; }
      .party-detail { font-size: 12.5px; color: #6b7280; line-height: 1.7; }

      .ref-block { margin-top: 10px; font-size: 12.5px; color: #6b7280; }
      .ref-block .ref-label { font-weight: 700; color: #111827; margin-bottom: 2px; }

      table.items { width: 100%; border-collapse: collapse; margin-bottom: 20px; }

      table.items thead th {
        background: #F3F4F6;
        font-size: 12px;
        font-weight: 700;
        color: #374151;
        text-align: left;
        padding: 10px 12px;
      }
      table.items thead th.num { text-align: right; }
      table.items thead th.center { text-align: center; }

      table.items tbody tr:nth-child(even) td { background: #FAFAFA; }
      table.items tbody td { font-size: 13px; color: #1f2937; padding: 12px; }
      table.items tbody td.num { text-align: right; }
      table.items tbody td.center { text-align: center; }
      table.items tbody td.desc { font-weight: 500; }
      table.items tbody td.total-cell { font-weight: 700; }

      .bottom-row {
        display: flex;
        justify-content: space-between;
        gap: 32px;
        margin-bottom: 24px;
      }

      .payment-col { flex: 1.3; }

      .payment-row { font-size: 13px; margin-bottom: 14px; }
      .payment-row .lbl { font-weight: 700; color: #111827; }
      .payment-row .val { color: #374151; margin-left: 4px; }

      .bank-box {
        background: #F8F9FA;
        padding: 16px 20px;
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        gap: 16px;
      }

      .bank-details-text { font-size: 12.5px; color: #374151; line-height: 1.9; }

      .bank-details-text .bank-title {
        font-size: 13px;
        font-weight: 700;
        color: #111827;
        margin-bottom: 6px;
      }

      .bank-details-text .label { font-weight: 700; color: #111827; }

      .qr-wrap { text-align: center; flex-shrink: 0; }

      .qr-code {
        width: 76px;
        height: 76px;
        background: #ffffff;
        display: grid;
        grid-template-columns: repeat(7, 1fr);
        grid-template-rows: repeat(7, 1fr);
        padding: 3px;
        border: 1px solid #E5E7EB;
      }
      .qr-code div { background: #111827; }
      .qr-code div.off { background: transparent; }

      .qr-caption { font-size: 10.5px; color: #9ca3af; margin-top: 6px; line-height: 1.4; }

      .totals-col { flex: 1; }

      .totals-row {
        display: flex;
        justify-content: space-between;
        padding: 6px 0;
        font-size: 13px;
        color: #6b7280;
      }
      .totals-row .amt { color: #111827; font-weight: 500; }

      .grand-total-row {
        display: flex;
        justify-content: space-between;
        align-items: center;
        background: #EEF0FF;
        padding: 14px 16px;
        margin-top: 8px;
      }
      .grand-total-row .label { font-size: 15px; font-weight: 700; color: #111827; }
      .grand-total-row .amt { font-size: 18px; font-weight: 800; color: #111827; }

      .terms { margin-bottom: 32px; }
      .terms .title { font-size: 13px; font-weight: 700; color: #111827; margin-bottom: 6px; }
      .terms .body { font-size: 12.5px; color: #6b7280; line-height: 1.7; max-width: 640px; }

      .signature-block { display: flex; justify-content: flex-end; margin-bottom: 40px; }
      .signature-inner { text-align: center; }

      .signature-script {
        font-family: 'Brush Script MT', 'Segoe Script', cursive;
        font-size: 32px;
        color: #111827;
        line-height: 1;
      }

      .signature-line { width: 160px; border-top: 1px solid #D1D5DB; margin-top: 8px; }
      .signature-caption { font-size: 12px; color: #9ca3af; margin-top: 6px; }

      .footer-brand {
        text-align: center;
        font-size: 14px;
        color: #9ca3af;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 2px;
      }
      
      .footer-logo {
        height: 26px;                 
        width: auto;                  
        object-fit: contain;
      }
      .footer-icon {
        width: 30px;
        height: 30px;
        display: inline-grid;
        grid-template-columns: 1fr 1fr;
        grid-template-rows: 1fr 1fr;
        gap: 1px;
      }
      .footer-icon span:nth-child(1) { background: #EF4444; }
      .footer-icon span:nth-child(2) { background: #22C55E; }
      .footer-icon span:nth-child(3) { background: #3B82F6; }
      .footer-icon span:nth-child(4) { background: #F59E0B; }
    </style>
    </head>
    <body>

    <div class="container">

      <div class="inv-top">
        <div class="inv-top-col left">
          <div class="logo-row">
            $logoBlockHtml
          </div>
          <div class="company-meta">
            ${data.companyAddress}<br>
            ${if (data.companyEmail.isNotEmpty()) "<strong>Email:</strong> ${data.companyEmail}<br>" else ""}
            ${if (data.companyPhone.isNotEmpty()) "<strong>Phone:</strong> ${data.companyPhone}" else ""}
          </div>
        </div>
        <div class="inv-top-col right">
          <div class="invoice-title">INVOICE</div>
          <div class="invoice-meta">
            <div><span class="k">Invoice No:</span> <span class="v">${data.invoiceNumber}</span></div>
            <div><span class="k">Invoice Date:</span> <span class="v">${data.invoiceDate}</span></div>
            <div><span class="k">Due Date:</span> <span class="v">${data.dueDate}</span></div>
          </div>
        </div>
      </div>

      ${if (data.companyGst.isNotEmpty()) """<div class="gst-bar">GST/VAT/ABN/EIN:&nbsp;${data.companyGst}</div>""" else ""}

      <div class="parties-box">
        <div class="party-col">
          <div class="party-label">Bill To:</div>
          <div class="party-name">${data.customerName}</div>
          <div class="party-detail">
            ${data.billToAddress}<br>
            ${if (data.billToPhone.isNotEmpty()) "Phone: ${data.billToPhone}<br>" else ""}
            ${if (data.billToEmail.isNotEmpty()) "Email:&nbsp;${data.billToEmail}" else ""}
          </div>
        </div>
        <div class="party-col divider">
          <div class="party-label">Ship To:</div>
          <div class="party-name">${data.customerName}</div>
          <div class="party-detail">${data.shipToAddress}</div>
          <div class="ref-block">
            <div class="ref-label">Reference:</div>
            Order ID: ${data.orderReference.ifEmpty { "N/A" }}
          </div>
        </div>
      </div>

      <table class="items">
        <thead>
          <tr>
            <th>Item/Description</th>
            <th>HSN/SKU</th>
            <th class="center">Quantity</th>
            <th class="num">Unit Price</th>
            <th class="num">Discount</th>
            <th class="num">Tax %</th>
            <th class="num">Total</th>
          </tr>
        </thead>
        <tbody>
          $itemsHtml
        </tbody>
      </table>

      <div class="bottom-row">
        <div class="payment-col">
          <div class="payment-row">
            <span class="lbl">Payment Method:</span><span class="val">${data.paymentMethod}</span>
          </div>
          $bankBoxHtml
        </div>

        <div class="totals-col">
          <div class="totals-row"><span>Subtotal:</span><span class="amt">${money(data.subtotal)}</span></div>
          <div class="totals-row"><span>Discount:</span><span class="amt">-${money(data.discountAmount)}</span></div>
          <div class="totals-row"><span>$taxLabel</span><span class="amt">${money(data.taxAmount)}</span></div>
          <div class="totals-row"><span>Shipping/Handling:</span><span class="amt">${money(data.shippingAmount)}</span></div>
          <div class="grand-total-row">
            <span class="label">Grand Total:</span>
            <span class="amt">${money(data.totalAmount)}</span>
          </div>
        </div>
      </div>

      <div class="terms">
        <div class="title">Terms & Conditions:</div>
        <div class="body">${data.termsAndConditions}</div>
      </div>

      <div class="signature-block">
        <div class="signature-inner">
          <div class="signature-script">Signature</div>
          <div class="signature-line"></div>
          <div class="signature-caption">Authorized Signature</div>
        </div>
      </div>

      <div class="footer-brand">
        Created with cuso invoice $footerLogoTag
      </div>

    </div>

    <script>
      const qr = document.getElementById('qrCode');
      if (qr) {
        const pattern = [1,1,1,0,1,0,1, 1,0,1,1,0,1,1, 1,1,0,1,1,1,0, 0,1,1,0,1,0,1, 1,0,1,1,1,0,1, 1,1,0,0,1,1,0, 0,1,1,1,0,1,1];
        pattern.forEach(bit => {
          const cell = document.createElement('div');
          if (!bit) cell.className = 'off';
          qr.appendChild(cell);
        });
      }
    </script>

    </body>
    </html>
    """.trimIndent()
    }
}