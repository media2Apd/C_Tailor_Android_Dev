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
        val unit: String = "",
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
        val logoUrl: String? = null,
        val signatoryLabel: String = "Authorized Signatory"
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
        // PDF export uses the plain device layout so the responsive invoice lays out
// at the A4 CSS width. The on-screen preview keeps its own WebView settings.
        webView.settings.loadWithOverviewMode = false
        webView.settings.useWideViewPort = false

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
        fun esc(value: String): String = value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")

        // Whole amounts are shown without decimals, like the reference design
        fun money(v: Double): String {
            val pattern = if (v % 1.0 == 0.0) "%,.0f" else "%,.2f"
            return "₹" + String.format(Locale.US, pattern, v)
        }

        // Strip the branch suffix such as "- Main Branch"
        val companyName = data.companyName
            .substringBefore(" - ")
            .substringBefore(" – ")
            .trim()
            .ifEmpty { "Company" }

        val logoBlockHtml = if (!data.logoUrl.isNullOrEmpty()) {
            """<img src="${data.logoUrl}" class="brand-logo" alt="Logo"/>"""
        } else {
            """<div class="brand-fallback">${esc(companyName)}</div>"""
        }

        val companyMetaHtml = buildString {
            if (data.companyAddress.isNotBlank()) {
                append("""<div class="addr">${esc(data.companyAddress)}</div>""")
            }
            if (data.companyEmail.isNotBlank()) {
                append("""<div>Email: ${esc(data.companyEmail)}</div>""")
            }
            if (data.companyPhone.isNotBlank()) {
                append("""<div>Phone: ${esc(data.companyPhone)}</div>""")
            }
            if (data.companyGst.isNotBlank()) {
                append("""<div>GSTIN: ${esc(data.companyGst)}</div>""")
            }
        }

        val billToPhoneHtml = if (data.billToPhone.isNotBlank()) {
            """<div class="p-extra">Phone: ${esc(data.billToPhone)}</div>"""
        } else ""

        val referenceHtml = if (data.orderReference.isNotBlank()) {
            """<div class="p-extra">Order: ${esc(data.orderReference)}</div>"""
        } else ""

        val itemsHtml = data.items.mapIndexed { index, item ->
            val discountCell = if (item.discount.isBlank() || item.discount == "-") {
                """<span class="neg">&mdash;</span>"""
            } else {
                esc(item.discount)
            }
            val hsnCell = if (item.hsnSku.isBlank() || item.hsnSku == "-") "&ndash;" else esc(item.hsnSku)

            """
        <tr>
          <td class="idx">${index + 1}</td>
          <td class="desc">${esc(item.description.ifBlank { "-" })}</td>
          <td class="hsn">$hsnCell</td>
          <td class="qty"><div class="qty-n">${item.quantity}</div><div class="qty-u">${esc(item.unit)}</div></td>
          <td class="num rate">${money(item.unitPrice)}</td>
          <td class="num">$discountCell</td>
          <td class="num total">${money(item.total)}</td>
        </tr>
        """.trimIndent()
        }.joinToString("\n")

        val discountRowHtml = if (data.discountAmount > 0.0) {
            """<div class="t-row"><span>Discount:</span><span class="neg">-${money(data.discountAmount)}</span></div>"""
        } else ""

        val shippingRowHtml = if (data.shippingAmount > 0.0) {
            """<div class="t-row"><span>Shipping:</span><span>${money(data.shippingAmount)}</span></div>"""
        } else ""

        // Placeholder bank details are shown when the API does not send bankDetails
        val bankName = data.bankName.ifBlank { "Corporate Bank" }
        val accountNo = data.accountNo.ifBlank { "XXXXXXXXXXXX" }
        val ifscSwift = data.ifscSwift.ifBlank { "XXXX0000000" }
        val paymentMethod = data.paymentMethod.ifBlank { "Bank Transfer / UPI / Cheque" }

        return """
    <!DOCTYPE html>
    <html lang="en">
    <head>
    <meta charset="UTF-8">
    <!-- No initial-scale here so overview mode fits the 760px layout to the screen width -->
    <meta name="viewport" content="width=760, user-scalable=yes">
    <title>Invoice</title>
    <style>
      * { margin: 0; padding: 0; box-sizing: border-box; }

      body {
        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
        background: #ffffff;
        color: #111827;
        padding: 8px;
      }

      .container {
        width: 100%;
        max-width: 760px;
        margin: 0 auto;
        padding: 34px 38px;
        background: #ffffff;
        border: 1px solid #E5E7EB;
        border-radius: 16px;
      }

      /* Header */
      .head { display: flex; justify-content: space-between; align-items: flex-start; }
      .head-left { flex: 1; padding-right: 16px; }
      .head-right { flex-shrink: 0; min-width: 250px; }

      .brand-logo { height: 46px; max-width: 150px; object-fit: contain; display: block; margin-bottom: 14px; }
      .brand-fallback {
        display: inline-block; background: #111111; color: #D9B86C;
        font-size: 13px; font-weight: 700; letter-spacing: 1px;
        padding: 14px 12px; margin-bottom: 14px; border-radius: 3px;
      }

      .company-meta { font-size: 12.5px; color: #4B5563; line-height: 1.6; }
      .company-meta .addr { margin-bottom: 10px; }

      .invoice-title {
        font-size: 36px; font-weight: 900; letter-spacing: 1px;
        color: #E5E7EB; text-align: right; line-height: 1; margin-bottom: 22px;
      }

      .meta-row { display: flex; justify-content: space-between; font-size: 13px; padding: 3px 0; }
      .meta-row .k { color: #6B7280; font-weight: 600; }
      .meta-row .v { color: #111827; }
      .meta-row .mono { font-family: "Courier New", monospace; font-weight: 700; font-size: 12px; }
      .meta-row .due { color: #2222D6; font-weight: 700; }

      .divider { border: none; border-top: 1px solid #E5E7EB; margin: 20px 0; }

      /* Bill to and ship to */
      .parties {
        display: flex; gap: 24px;
        background: #F9FAFB; border: 1px solid #E5E7EB; border-radius: 14px;
        padding: 18px 22px;
      }
      .party { flex: 1; }
      .p-label { font-size: 10px; font-weight: 700; letter-spacing: 1px; color: #9CA3AF; text-transform: uppercase; margin-bottom: 8px; }
      .p-name { font-size: 16px; font-weight: 800; color: #000000; margin-bottom: 6px; }
      .p-addr { font-size: 13px; color: #4B5563; line-height: 1.6; }
      .p-extra { font-size: 13px; color: #4B5563; margin-top: 6px; }

      /* Items table */
      table.items { width: 100%; border-collapse: collapse; margin-top: 22px; }
      table.items thead th {
        background: #F3F4F6; color: #4B5563; font-size: 13px; font-weight: 600;
        text-align: left; padding: 12px 8px;
        border-top: 1px solid #D1D5DB; border-bottom: 1px solid #D1D5DB;
      }
      table.items thead th.c { text-align: center; }
      table.items thead th.r { text-align: right; }
      table.items tbody td { font-size: 13px; color: #4B5563; padding: 16px 8px; vertical-align: middle; }
      td.idx { color: #9CA3AF; font-weight: 600; width: 6%; }
      td.desc { color: #000000; font-weight: 700; }
      td.hsn { width: 14%; }
      td.qty { text-align: center; width: 11%; }
      .qty-n { color: #000000; font-weight: 700; }
      .qty-u { color: #000000; font-weight: 600; font-size: 12px; }
      td.num { text-align: right; width: 13%; }
      td.rate { color: #4B5563; }
      td.total { color: #000000; font-weight: 800; font-size: 14px; }
      .neg { color: #DC2626; }

      /* Payment and totals */
      .bottom { display: flex; gap: 28px; align-items: flex-start; margin-top: 8px; padding-top: 22px; border-top: 1px solid #E5E7EB; }
      .pay-col { flex: 1; }
      .pay-box {
        display: flex; justify-content: space-between; align-items: center; gap: 12px;
        background: #F9FAFB; border: 1px solid #E5E7EB; border-radius: 14px; padding: 16px 18px;
      }
      .pay-title { font-size: 10.5px; font-weight: 800; letter-spacing: 1px; color: #111827; text-transform: uppercase; margin-bottom: 6px; }
      .pay-method { font-size: 13px; color: #4B5563; margin-bottom: 10px; }
      .pay-line { font-size: 11.5px; color: #374151; line-height: 1.7; }
      .pay-line b { color: #111827; }
      .qr-box {
        width: 64px; height: 64px; flex-shrink: 0; background: #ffffff;
        border: 1px solid #E5E7EB; border-radius: 10px;
        display: flex; align-items: center; justify-content: center;
      }

      .totals-col { flex: 1; }
      .t-row { display: flex; justify-content: space-between; font-size: 13px; color: #4B5563; padding: 4px 0; }
      .t-row .amt { color: #111827; font-weight: 600; }
      .t-row.tax, .t-row.tax .amt { color: #047857; }
      .grand {
        display: flex; justify-content: space-between; align-items: center;
        border-top: 2px solid #111827; margin-top: 10px; padding-top: 12px;
        font-size: 19px; font-weight: 900; color: #000000; letter-spacing: 0.3px;
      }
      .paid { display: flex; justify-content: space-between; color: #047857; font-weight: 700; font-size: 14px; padding: 10px 0 8px; }
      .balance {
        display: flex; justify-content: space-between; align-items: center;
        background: #FFFBEB; border: 1px solid #FDE68A; border-radius: 8px;
        padding: 10px 14px; color: #78350F; font-weight: 800; font-size: 14px;
      }

      /* Terms and signature */
      .foot { display: flex; justify-content: space-between; align-items: flex-end; gap: 20px; margin-top: 26px; padding-top: 20px; border-top: 1px solid #E5E7EB; }
      .terms-h { font-size: 10.5px; font-weight: 800; letter-spacing: 1px; color: #111827; text-transform: uppercase; margin-bottom: 6px; }
      .terms-b { font-size: 12px; color: #4B5563; line-height: 1.5; }
      .sign { min-width: 190px; text-align: center; border-top: 1px solid #D1D5DB; padding-top: 6px; font-size: 12px; font-weight: 700; color: #111827; }
    </style>
    </head>
    <body>
    <div class="container">

      <div class="head">
        <div class="head-left">
          $logoBlockHtml
          <div class="company-meta">$companyMetaHtml</div>
        </div>
        <div class="head-right">
          <div class="invoice-title">INVOICE</div>
          <div class="meta-row"><span class="k">Invoice No:</span><span class="v mono">${esc(data.invoiceNumber.ifEmpty { "-" })}</span></div>
          <div class="meta-row"><span class="k">Invoice Date:</span><span class="v">${esc(data.invoiceDate.ifEmpty { "-" })}</span></div>
          <div class="meta-row"><span class="k">Due Date:</span><span class="due">${esc(data.dueDate.ifEmpty { "-" })}</span></div>
        </div>
      </div>

      <hr class="divider"/>

      <div class="parties">
        <div class="party">
          <div class="p-label">Bill to:</div>
          <div class="p-name">${esc(data.customerName.ifEmpty { "-" })}</div>
          <div class="p-addr">${esc(data.billToAddress.ifEmpty { "-" })}</div>
          $billToPhoneHtml
        </div>
        <div class="party">
          <div class="p-label">Ship to:</div>
          <div class="p-name">${esc(data.customerName.ifEmpty { "-" })}</div>
          <div class="p-addr">${esc(data.shipToAddress.ifEmpty { "-" })}</div>
          $referenceHtml
        </div>
      </div>

      <table class="items">
        <thead>
          <tr>
            <th>#</th>
            <th>Item &amp; Description</th>
            <th>HSN / SAC</th>
            <th class="c">Qty</th>
            <th class="r">Rate</th>
            <th class="r">Discount</th>
            <th class="r">Total</th>
          </tr>
        </thead>
        <tbody>
          $itemsHtml
        </tbody>
      </table>

      <div class="bottom">
        <div class="pay-col">
          <div class="pay-box">
            <div>
              <div class="pay-title">Payment instructions</div>
              <div class="pay-method">${esc(paymentMethod)}</div>
              <div class="pay-line">Bank: <b>${esc(bankName)}</b></div>
              <div class="pay-line">Account No: <b>${esc(accountNo)}</b></div>
              <div class="pay-line">IFSC: <b>${esc(ifscSwift)}</b></div>
            </div>
            <div class="qr-box">
              <svg viewBox="0 0 24 24" width="30" height="30" fill="none" stroke="#9CA3AF" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="3" width="7" height="7" rx="1"/>
                <rect x="14" y="3" width="7" height="7" rx="1"/>
                <rect x="3" y="14" width="7" height="7" rx="1"/>
                <path d="M14 14h3v3h-3zM20 14v.01M14 20h.01M17 20h4M20 17v3"/>
              </svg>
            </div>
          </div>
        </div>

        <div class="totals-col">
          <div class="t-row"><span>Subtotal:</span><span class="amt">${money(data.subtotal)}</span></div>
          $discountRowHtml
          <div class="t-row tax"><span>Total Tax:</span><span class="amt">+${money(data.taxAmount)}</span></div>
          $shippingRowHtml
          <div class="grand"><span>GRAND TOTAL:</span><span>${money(data.totalAmount)}</span></div>
          <div class="paid"><span>Amount Paid:</span><span>${money(data.paidAmount)}</span></div>
          <div class="balance"><span>Balance Due:</span><span>${money(data.balanceAmount)}</span></div>
        </div>
      </div>

      <div class="foot">
        <div>
          <div class="terms-h">Terms &amp; conditions</div>
          <div class="terms-b">${esc(data.termsAndConditions)}</div>
        </div>
        <div class="sign">${esc(data.signatoryLabel)}</div>
      </div>

    </div>
    </body>
    </html>
    """.trimIndent()
    }
}