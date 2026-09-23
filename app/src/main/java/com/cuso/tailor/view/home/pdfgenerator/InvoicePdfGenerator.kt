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

@Suppress("unused_parameter")
class InvoicePdfGenerator(private val context: Context) {

    // Strong reference to prevent WebView garbage collection mid-render
    private var activeWebView: WebView? = null

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
        val termsAndConditions: String = "Payment due within 30 days of invoice date. Late fees may apply, Goods remain property of Apex Global Solutions until paid in full.",
        val companyName: String = "",
        val companyAddress: String = "",
        val companyEmail: String = "",
        val companyPhone: String = "",
        val companyGst: String = "",
        val logoUrl: String? = null
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

    // A4 dimensions in points (72 DPI)
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
            Log.e("InvoicePdfGenerator", "contentHeightPx <= 0 — aborting render")
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
            Log.e("InvoicePdfGenerator", "drawableToBase64 failed", e)
            ""
        }
    }

    fun buildInvoiceHtml(data: InvoiceData): String {
        fun money(v: Double) = "₹" + String.format(Locale.US, "%,.2f", v)

        // Strip branch suffix (e.g., "- Main Branch") to show only the main organization name
        val cleanCompanyName = data.companyName
            .substringBefore(" - ")
            .substringBefore(" – ")
            .trim()
            .ifEmpty { "RELDA" }

        val logoBlockHtml = if (!data.logoUrl.isNullOrEmpty()) {
            """<img src="${data.logoUrl}" class="brand-logo" alt="Logo"/>"""
        } else {
            """
            <div class="brand-badge-row">
              <div class="brand-icon">
                <svg viewBox="0 0 24 24" width="16" height="16" fill="none">
                  <circle cx="12" cy="12" r="11" fill="#DC2626"/>
                  <path d="M6 12C9 9 15 9 18 12C15 15 9 15 6 12Z" fill="#FFFFFF"/>
                </svg>
              </div>
              <span class="brand-name">$cleanCompanyName</span>
            </div>
            """.trimIndent()
        }

        val footerLogoBase64 = drawableToBase64(R.drawable.cuso_technologies_logo)
        val footerLogoTag = if (footerLogoBase64.isNotEmpty()) {
            """<img src="$footerLogoBase64" class="footer-logo" alt="cuso"/>"""
        } else {
            """<span class="footer-dot-icon"></span>"""
        }

        val itemsHtml = data.items.mapIndexed { index, item ->
            val isEvenRow = (index % 2 == 1)
            val rowClass = if (isEvenRow) "even-row" else "odd-row"
            """
            <tr class="$rowClass">
                <td class="desc">${item.description.ifEmpty { "-" }}</td>
                <td>${item.hsnSku.ifEmpty { "-" }}</td>
                <td class="center">${item.quantity}</td>
                <td class="num">${money(item.unitPrice)}</td>
                <td class="num">${item.discount.ifEmpty { "-" }}</td>
                <td class="num">${String.format(Locale.US, "%.0f", item.tax)}%</td>
                <td class="num bold">${money(item.total)}</td>
            </tr>
            """.trimIndent()
        }.joinToString("\n")

        val taxLabel = if (data.taxPercent > 0) {
            "Tax Breakdown (VAT ${String.format(Locale.US, "%.0f", data.taxPercent)}%):"
        } else {
            "Tax Breakdown (VAT 10%):"
        }

        val gstBarHtml = if (data.companyGst.isNotEmpty()) {
            """
            <div class="gst-container">
              <span class="gst-badge">GST/VAT/ABN/EIN:${data.companyGst}</span>
            </div>
            """.trimIndent()
        } else ""

        return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=420, initial-scale=1.0">
        <title>Invoice</title>
        <style>
          * { margin: 0; padding: 0; box-sizing: border-box; }

          body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
            background: #ffffff;
            color: #111827;
            padding: 12px;
            display: flex;
            justify-content: center;
          }

          .container {
            width: 100%;
            max-width: 420px;
            background: #ffffff;
            padding: 4px;
          }

          /* ── TOP SECTION ── */
          .inv-top {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            margin-bottom: 6px;
          }

          .inv-top-left {
            flex: 1;
            padding-right: 12px;
          }

          .inv-top-right {
            text-align: right;
            flex-shrink: 0;
          }

          .brand-badge-row {
            display: flex;
            align-items: center;
            gap: 5px;
            margin-bottom: 4px;
          }

          .brand-logo {
            max-height: 26px;
            max-width: 130px;
            object-fit: contain;
            margin-bottom: 4px;
          }

          .brand-icon {
            display: flex;
            align-items: center;
            justify-content: center;
          }

          .brand-name {
            font-size: 13.5px;
            font-weight: 800;
            color: #DC2626;
            letter-spacing: 0.3px;
            font-family: serif;
          }

          .company-meta {
            font-size: 5.23px;
            color: #111827;
            line-height: 1.5;
          }
          .company-meta span.bold {
            font-weight: 700;
          }

          /* Invoice Title: 15.29px bold black */
          .invoice-title {
            font-size: 15.29px;
            font-weight: 800;
            color: #000000;
            letter-spacing: 0.5px;
            line-height: 1.1;
            margin-bottom: 5px;
          }

          /* Invoice meta: 5.23px semi bold */
          .invoice-meta {
            font-size: 5.23px;
            font-weight: 600;
            color: #111827;
            line-height: 1.5;
            text-align: right;
          }
          .invoice-meta .k {
            font-weight: 700;
            color: #000000;
          }

          /* GSTIN badge: background #EFF3F7 hugging text only */
          .gst-container {
            margin-top: 5px;
            margin-bottom: 8px;
            text-align: left;
          }
          .gst-badge {
            display: inline-block;
            background-color: #EFF3F7;
            padding: 3.5px 8px;
            font-size: 5.23px;
            font-weight: 600;
            color: #111827;
            border-radius: 2.5px;
          }

          /* ── BILL TO / SHIP TO BOX ── */
          .parties-box {
            border: 0.75px solid #E5E7EB;
            border-radius: 3px;
            display: flex;
            margin-bottom: 8px;
            background: #ffffff;
          }

          .party-col {
            flex: 1;
            padding: 6px 9px;
            font-size: 5.23px;
            line-height: 1.45;
            color: #111827;
          }

          .party-col.divider {
            border-left: 0.75px solid #E5E7EB;
          }

          .party-title {
            font-size: 5.23px;
            font-weight: 700;
            color: #000000;
            margin-bottom: 2px;
          }

          .party-name {
            font-size: 5.23px;
            font-weight: 600;
            color: #111827;
          }

          .ref-row {
            margin-top: 3px;
            display: flex;
            justify-content: space-between;
            align-items: center;
          }

          /* ── ITEMS TABLE ── */
          table.items {
            width: 100%;
            border-collapse: collapse;
            margin-bottom: 9px;
          }

          /* Table title bar: background #EFF3F7 */
          table.items thead th {
            background-color: #EFF3F7;
            font-size: 5.23px;
            font-weight: 700;
            color: #000000;
            text-align: left;
            padding: 6px 6px;
            border: none;
          }

          table.items thead th.num { text-align: right; }
          table.items thead th.center { text-align: center; }

          /* Table data color: #767676, size: 5.23px with increased vertical padding */
          table.items tbody td {
            font-size: 5.23px;
            color: #767676;
            padding: 6px 6px;
            border: none;
            vertical-align: middle;
          }

          /* Alternating rows background: #EFF3F7 */
          table.items tbody tr.even-row td {
            background-color: #EFF3F7;
          }

          table.items tbody td.num { text-align: right; }
          table.items tbody td.center { text-align: center; }
          table.items tbody td.bold { font-weight: 700; }

          /* ── BOTTOM SECTION: PAYMENT & TOTALS ── */
          .bottom-section {
            display: flex;
            justify-content: space-between;
            gap: 12px;
            margin-bottom: 9px;
          }

          .payment-col {
            flex: 1.15;
          }

          /* Payment details enclosed in a single border box */
          .payment-box {
            border: 0.75px solid #E5E7EB;
            border-radius: 3px;
            padding: 6px 8px;
            background: #ffffff;
          }

          .payment-method-header {
            font-size: 5.23px;
            color: #000000;
            margin-bottom: 6px;
          }
          .payment-method-header .lbl-bold {
            font-weight: 700;
          }

          .bank-content-row {
            display: flex;
            justify-content: space-between;
            align-items: center;
          }

          .bank-details-text {
            font-size: 5.23px;
            line-height: 1.5;
            color: #111827;
          }
          .bank-details-text .bank-title {
            font-size: 5.23px;
            font-weight: 700;
            color: #000000;
            margin-bottom: 2px;
          }
          .lbl-bold { font-weight: 700; }

          .qr-wrap {
            display: flex;
            flex-direction: column;
            align-items: center;
            margin-left: 6px;
          }

          .qr-code {
            width: 34px;
            height: 34px;
            background: #ffffff;
            border: 0.75px solid #000000;
            display: grid;
            grid-template-columns: repeat(6, 1fr);
            grid-template-rows: repeat(6, 1fr);
            padding: 1.5px;
            gap: 0.5px;
          }
          .qr-code div { background: #000000; }
          .qr-code div.off { background: transparent; }

          .qr-caption {
            font-size: 4px;
            color: #111827;
            text-align: center;
            line-height: 1.1;
            margin-top: 2px;
          }

          .totals-col {
            flex: 0.95;
            display: flex;
            flex-direction: column;
            justify-content: flex-start;
          }

          .totals-row {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 3.5px 6px;
            font-size: 5.23px;
            color: #111827;
          }

          /* Tax breakdown row: background #EFF3F7 */
          .totals-row.tax-highlight {
            background-color: #EFF3F7;
            border-radius: 2px;
            padding: 4.5px 6px;
            font-weight: 500;
            margin: 1.5px 0;
          }

          /* Grand total row: background #EFF3F7 */
          .totals-row.grand-total {
            background-color: #EFF3F7;
            border-radius: 2px;
            font-size: 5.8px;
            font-weight: 800;
            color: #000000;
            padding: 5.5px 6px;
            margin-top: 2.5px;
          }

          /* ── TERMS & CONDITIONS ── */
          .terms-block {
            margin-top: 6px;
            margin-bottom: 9px;
          }

          .terms-header {
            font-size: 5.23px;
            font-weight: 700;
            color: #5F5F5F;
            margin-bottom: 2px;
          }

          .terms-body {
            font-size: 5.23px;
            color: #767676;
            line-height: 1.5;
          }

          /* ── SIGNATURE ── */
          .signature-wrapper {
            display: flex;
            justify-content: flex-end;
            margin-top: 6px;
            margin-bottom: 12px;
          }

          .signature-box {
            text-align: center;
            width: 80px;
          }

          .signature-script {
            font-family: 'Brush Script MT', 'Dancing Script', 'Segoe Script', cursive;
            font-size: 16px;
            color: #111827;
            line-height: 1;
            margin-bottom: 2px;
          }

          .signature-line {
            width: 100%;
            border-top: 0.75px solid #D1D5DB;
            margin-bottom: 2px;
          }

          .signature-caption {
            font-size: 4.2px;
            color: #6B7280;
          }

          /* ── FOOTER ── */
          .footer-brand {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 3px;
            font-size: 4.5px;
            color: #6B7280;
            margin-top: 6px;
          }

          .footer-logo {
            height: 9px;
            width: auto;
            object-fit: contain;
          }

          .footer-dot-icon {
            width: 5px;
            height: 5px;
            background: #3B82F6;
            display: inline-block;
            border-radius: 1px;
          }
        </style>
        </head>
        <body>

        <div class="container">

          <!-- TOP IDENTITY & INVOICE META -->
          <div class="inv-top">
            <div class="inv-top-left">
              $logoBlockHtml
              <div class="company-meta">
                ${data.companyAddress.ifEmpty { "-" }}<br>
                ${if (data.companyEmail.isNotEmpty()) """<span class="bold">Email:</span> ${data.companyEmail}<br>""" else ""}
                ${if (data.companyPhone.isNotEmpty()) """<span class="bold">Phone:</span> ${data.companyPhone}""" else ""}
              </div>
            </div>
            <div class="inv-top-right">
              <div class="invoice-title">INVOICE</div>
              <div class="invoice-meta">
                <div><span class="k">Invoice No:</span> ${data.invoiceNumber.ifEmpty { "-" }}</div>
                <div><span class="k">Invoice Date:</span> ${data.invoiceDate.ifEmpty { "-" }}</div>
                <div><span class="k">Due Date:</span> ${data.dueDate.ifEmpty { "-" }}</div>
              </div>
            </div>
          </div>

          <!-- GSTIN / TAX IDENTIFIER BADGE -->
          $gstBarHtml

          <!-- BILL TO / SHIP TO BOX -->
          <div class="parties-box">
            <div class="party-col">
              <div class="party-title">Bill To:</div>
              <div class="party-name">${data.customerName.ifEmpty { "-" }}</div>
              <div>${data.billToAddress.ifEmpty { "-" }}</div>
              ${if (data.billToPhone.isNotEmpty()) "<div>Phone: ${data.billToPhone}</div>" else ""}
              ${if (data.billToEmail.isNotEmpty()) "<div>Email: ${data.billToEmail}</div>" else ""}
            </div>
            <div class="party-col divider">
              <div class="party-title">Ship To:</div>
              <div class="party-name">${data.customerName.ifEmpty { "-" }}</div>
              <div>${data.shipToAddress.ifEmpty { "-" }}</div>
              <div class="party-title" style="margin-top: 3px;">Reference:</div>
              <div class="ref-row">
                <span>Order ID: ${data.orderReference.ifEmpty { "-" }}</span>
                <span>${if (data.orderReference.isNotEmpty()) data.orderReference else "-"}</span>
              </div>
            </div>
          </div>

          <!-- ITEMS TABLE -->
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

          <!-- BOTTOM SECTION: PAYMENT & TOTALS -->
          <div class="bottom-section">
            <div class="payment-col">
              <div class="payment-box">
                <div class="payment-method-header">
                  <span class="lbl-bold">Payment Method:</span> ${data.paymentMethod.ifEmpty { "-" }}
                </div>
                <div class="bank-content-row">
                  <div class="bank-details-text">
                    <div class="bank-title">Bank Details:</div>
                    <div><span class="lbl-bold">Bank Name:</span> ${data.bankName.ifEmpty { "-" }}</div>
                    <div><span class="lbl-bold">Acc0unt No:</span> ${data.accountNo.ifEmpty { "-" }}</div>
                    <div><span class="lbl-bold">IFSC/SWIFT:</span> ${data.ifscSwift.ifEmpty { "-" }}</div>
                  </div>
                  <div class="qr-wrap">
                    <div class="qr-code" id="qrCode"></div>
                    <div class="qr-caption">UPIQR /<br>Payment QR<br>Code</div>
                  </div>
                </div>
              </div>
            </div>

            <div class="totals-col">
              <div class="totals-row">
                <span>Subtotal:</span>
                <span>${money(data.subtotal)}</span>
              </div>
              <div class="totals-row">
                <span>Discount:</span>
                <span>-${money(data.discountAmount)}</span>
              </div>
              <div class="totals-row tax-highlight">
                <span>$taxLabel</span>
                <span>${money(data.taxAmount)}</span>
              </div>
              <div class="totals-row">
                <span>Shipping/Handling:</span>
                <span>${money(data.shippingAmount)}</span>
              </div>
              <div class="totals-row grand-total">
                <span>Grand Total:</span>
                <span>${money(data.totalAmount)}</span>
              </div>
            </div>
          </div>

          <!-- TERMS & CONDITIONS -->
          <div class="terms-block">
            <div class="terms-header">Terms & Conditions:</div>
            <div class="terms-body">${data.termsAndConditions}</div>
          </div>

          <!-- SIGNATURE BLOCK -->
          <div class="signature-wrapper">
            <div class="signature-box">
              <div class="signature-script">Signature</div>
              <div class="signature-line"></div>
              <div class="signature-caption">Authorized Signature</div>
            </div>
          </div>

          <!-- FOOTER -->
          <div class="footer-brand">
            <span>Created with cuso invoice</span>
            $footerLogoTag
          </div>

        </div>

        <script>
          const qr = document.getElementById('qrCode');
          if (qr) {
            const pattern = [
              1,1,1,0,1,1,
              1,0,1,1,0,1,
              1,1,0,0,1,1,
              0,1,1,0,1,0,
              1,0,1,1,0,1,
              1,1,0,1,1,1
            ];
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