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
import androidx.core.graphics.createBitmap
import com.cuso.tailor.model.finance.CreditNoteAllocationItem
import com.cuso.tailor.model.finance.CreditNoteItemDto
import com.cuso.tailor.utils.currencySymbol
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

class CreditNotePdfGenerator(private val context: Context) {

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
            Log.e("CreditNotePdfGen", "attachToWindow failed", e)
        }
    }

    private fun detachFromWindow(webView: WebView) {
        try {
            (webView.parent as? ViewGroup)?.removeView(webView)
        } catch (e: Exception) {
            Log.e("CreditNotePdfGen", "detachFromWindow failed", e)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun printCreditNote(
        creditNote: CreditNoteItemDto,
        allocations: List<CreditNoteAllocationItem>
    ) {
        val webView = WebView(context)
        activeWebView = webView
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                val jobName = "CreditNote_${creditNote.creditNoteNumber}"
                val printAdapter = webView.createPrintDocumentAdapter(jobName)
                val printAttributes = PrintAttributes.Builder()
                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                    .setResolution(PrintAttributes.Resolution("id", "print", 300, 300))
                    .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                    .build()
                printManager?.print(jobName, printAdapter, printAttributes)
            }
        }
        webView.loadDataWithBaseURL(null, buildCreditNoteHtml(creditNote, allocations), "text/html", "UTF-8", null)
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun downloadCreditNotePdf(
        creditNote: CreditNoteItemDto,
        allocations: List<CreditNoteAllocationItem>,
        onComplete: (SavedPdf?) -> Unit
    ) {
        val fileName = "CreditNote_${creditNote.creditNoteNumber}_${System.currentTimeMillis()}.pdf"
        val density = context.resources.displayMetrics.density
        val renderWidthPx = (pageWidthPt * density).toInt().coerceAtLeast(800)

        val webView = WebView(context)
        activeWebView = webView

        webView.layoutParams = ViewGroup.LayoutParams(renderWidthPx, ViewGroup.LayoutParams.WRAP_CONTENT)
        webView.settings.javaScriptEnabled = true

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

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                finish(null)
            }
        }

        attachToWindow(webView)
        webView.loadDataWithBaseURL(null, buildCreditNoteHtml(creditNote, allocations), "text/html", "UTF-8", null)
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
            val dstRect = Rect(0, 0, pageWidthPt, (pageHeightPt.toFloat() * sliceHeight / pageHeightPx).toInt())
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

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues) ?: return null
                resolver.openOutputStream(uri)?.use { it.write(bytes) }
                SavedPdf(uri = uri, displayName = fileName, sizeBytes = bytes.size.toLong())
            } else {
                @Suppress("DEPRECATION")
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!dir.exists()) dir.mkdirs()
                val file = File(dir, fileName)
                FileOutputStream(file).use { it.write(bytes) }
                SavedPdf(uri = Uri.fromFile(file), displayName = fileName, file = file, sizeBytes = bytes.size.toLong())
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun buildCreditNoteHtml(
        creditNote: CreditNoteItemDto,
        allocations: List<CreditNoteAllocationItem>
    ): String {
        fun esc(v: String): String = v.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
        fun money(v: Double): String = "${currencySymbol()}" + String.format(Locale.US, if (v % 1.0 == 0.0) "%,.0f" else "%,.2f", v)

        fun formatDate(raw: String): String {
            return try {
                val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                val formatter = SimpleDateFormat("dd MMM yyyy", Locale.US)
                parser.parse(raw)?.let { formatter.format(it) } ?: raw.take(10)
            } catch (_: Exception) {
                raw.take(10)
            }
        }

        val companyName = creditNote.companySnapshot?.name ?: "NEXUS BOUTIQUE"
        val companyAddress = creditNote.companySnapshot?.address ?: "Udhayam Colony, Anna Nagar, Chennai, Tamil Nadu - 600040"
        val branchName = creditNote.branchId?.name ?: "Nexus CC - Main Branch"
        val logoHtml = if (!creditNote.companySnapshot?.logoUrl.isNullOrEmpty()) {
            """<img src="${creditNote.companySnapshot.logoUrl}" class="brand-logo"/>"""
        } else {
            """<div class="logo-box">NEXUS</div>"""
        }

        val customerName = creditNote.customerSnapshot?.name ?: creditNote.customerId?.fullName ?: "Ranjana"
        val customerAddr = creditNote.customerSnapshot?.billingAddress?.let {
            listOfNotNull(it.flatNo, it.street, it.city, it.pincode).filter { s -> s.isNotBlank() }.joinToString(", ")
        } ?: ""
        val customerPhone = creditNote.customerSnapshot?.phone ?: ""

        val allocationsHtml = if (allocations.isNotEmpty()) {
            allocations.joinToString("\n") { alloc ->
                """
                <tr>
                    <td class="bold">${esc(alloc.invoiceId?.invoiceNumber ?: "-")}</td>
                    <td>${formatDate(alloc.appliedDate)}</td>
                    <td class="r">${money(alloc.invoiceId?.grandTotal ?: alloc.amountApplied)}</td>
                    <td class="r bold">${money(alloc.amountApplied)}</td>
                </tr>
                """.trimIndent()
            }
        } else {
            """
            <tr>
                <td class="bold">-</td>
                <td>${formatDate(creditNote.creditNoteDate)}</td>
                <td class="r">${money(creditNote.grandTotal)}</td>
                <td class="r bold">${money(creditNote.amountAllocated)}</td>
            </tr>
            """.trimIndent()
        }

        val linesHtml = creditNote.lines.mapIndexed { idx, line ->
            """
            <tr>
                <td class="c idx">${idx + 1}</td>
                <td class="bold">${esc(line.itemName)}</td>
                <td class="c">—</td>
                <td class="c">${line.quantity.toInt()} ${esc(line.unit ?: "Pieces")}</td>
                <td class="r">${money(line.rate)}</td>
                <td class="r bold">${money(line.lineTotal)}</td>
            </tr>
            """.trimIndent()
        }.joinToString("\n")

        val remarks = creditNote.notes?.ifBlank { "test" } ?: "test"

        return """
        <!DOCTYPE html>
        <html>
        <head>
        <meta charset="UTF-8">
        <title>Credit Note</title>
        <style>
            * { margin: 0; padding: 0; box-sizing: border-box; }
            body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Arial, sans-serif; background: #fff; color: #111; padding: 24px; }
            .container { width: 100%; max-width: 720px; margin: 0 auto; }
            
            .header { display: flex; justify-content: space-between; align-items: flex-start; padding-bottom: 16px; border-bottom: 2px solid #111; }
            .logo-box { width: 44px; height: 44px; background: #000; color: #fff; font-size: 9px; font-weight: bold; display: flex; align-items: center; justify-content: center; letter-spacing: 1px; margin-bottom: 8px; border-radius: 4px; }
            .brand-logo { max-height: 48px; margin-bottom: 8px; }
            .company-name { font-size: 18px; font-weight: 800; text-transform: uppercase; letter-spacing: 0.5px; }
            .sub-branch { font-size: 13px; font-weight: 600; color: #374151; margin-top: 2px; }
            .address { font-size: 11px; color: #6B7280; margin-top: 2px; }

            .cn-title { font-size: 32px; font-weight: 900; color: #E5E7EB; text-align: right; letter-spacing: 1.5px; margin-bottom: 6px; }
            .meta-line { font-size: 12px; text-align: right; margin-top: 2px; color: #4B5563; }
            .meta-line b { color: #111; }

            .box-card { background: #F9FAFB; border: 1px solid #E5E7EB; border-radius: 6px; padding: 14px; margin-top: 18px; }
            .box-label { font-size: 10px; font-weight: 700; color: #9CA3AF; letter-spacing: 0.8px; text-transform: uppercase; margin-bottom: 4px; }
            .cust-name { font-size: 15px; font-weight: 700; color: #111; }
            .cust-meta { font-size: 12px; color: #4B5563; margin-top: 2px; }

            .sec-title { font-size: 11px; font-weight: 800; color: #6B7280; letter-spacing: 0.8px; text-transform: uppercase; margin: 22px 0 8px; }
            
            table { width: 100%; border-collapse: collapse; margin-top: 6px; }
            th { background: #F9FAFB; font-size: 11px; font-weight: 700; color: #4B5563; padding: 8px 10px; text-align: left; border-top: 1px solid #E5E7EB; border-bottom: 1px solid #E5E7EB; }
            td { font-size: 12px; color: #374151; padding: 10px; border-bottom: 1px solid #F3F4F6; }
            th.r, td.r { text-align: right; }
            th.c, td.c { text-align: center; }
            .bold { font-weight: 700; color: #111; }
            .idx { color: #9CA3AF; width: 6%; }

            .summary-section { display: flex; justify-content: flex-end; margin-top: 14px; }
            .summary-table { width: 280px; }
            .summary-row { display: flex; justify-content: space-between; font-size: 12px; color: #4B5563; padding: 4px 0; }
            .summary-row b { color: #111; }
            .total-credit-row { display: flex; justify-content: space-between; font-size: 15px; font-weight: 900; color: #111; border-top: 2px solid #111; padding-top: 6px; margin-top: 4px; }
            
            .remaining-box { background: #FFFBEB; border: 1px solid #FEF3C7; padding: 6px 10px; border-radius: 4px; display: flex; justify-content: space-between; font-size: 12px; font-weight: 800; color: #B45309; margin-top: 8px; }

            .footer-row { display: flex; justify-content: space-between; align-items: flex-end; margin-top: 40px; padding-top: 20px; }
            .signature-box { width: 200px; text-align: center; border-top: 1px solid #9CA3AF; padding-top: 6px; font-size: 12px; font-weight: 600; color: #111; }
        </style>
        </head>
        <body>
        <div class="container">
            <div class="header">
                <div>
                    $logoHtml
                    <div class="company-name">${esc(companyName)}</div>
                    <div class="sub-branch">${esc(branchName)}</div>
                    <div class="address">${esc(companyAddress)}</div>
                </div>
                <div>
                    <div class="cn-title">CREDIT NOTE</div>
                    <div class="meta-line">Credit Note No: <b>${esc(creditNote.creditNoteNumber)}</b></div>
                    <div class="meta-line">Date: <b>${formatDate(creditNote.creditNoteDate)}</b></div>
                    <div class="meta-line">Status: <b>${esc(creditNote.status)}</b></div>
                </div>
            </div>

            <div class="box-card">
                <div class="box-label">CUSTOMER / BENEFICIARY</div>
                <div class="cust-name">${esc(customerName)}</div>
                <div class="cust-meta">${esc(customerAddr)}</div>
                <div class="cust-meta">Phone: ${esc(customerPhone)}</div>
            </div>

            <div class="sec-title">APPLIED INVOICES &amp; CREDIT OFFSETS</div>
            <table>
                <thead>
                    <tr>
                        <th>Invoice No</th>
                        <th>Offset Date</th>
                        <th class="r">Invoice Total</th>
                        <th class="r">Credit Offset</th>
                    </tr>
                </thead>
                <tbody>
                    $allocationsHtml
                </tbody>
            </table>

            <table style="margin-top: 14px;">
                <thead>
                    <tr>
                        <th class="c">#</th>
                        <th>Description / Service</th>
                        <th class="c">HSN / SAC</th>
                        <th class="c">Qty</th>
                        <th class="r">Rate</th>
                        <th class="r">Amount</th>
                    </tr>
                </thead>
                <tbody>
                    $linesHtml
                </tbody>
            </table>

            <div class="summary-section">
                <div class="summary-table">
                    <div class="summary-row"><span>Subtotal:</span><span>${money(creditNote.subtotal)}</span></div>
                    <div class="summary-row"><span>Reversal Tax:</span><span>+${money(creditNote.totalTax)}</span></div>
                    <div class="total-credit-row"><span>TOTAL CREDIT:</span><span>${money(creditNote.grandTotal)}</span></div>
                    <div class="summary-row" style="margin-top: 4px;"><span>Total Applied:</span><b>${money(creditNote.amountAllocated)}</b></div>
                    <div class="remaining-box"><span>REMAINING CREDIT:</span><span>${money(creditNote.remainingBalance)}</span></div>
                </div>
            </div>

            <div class="footer-row">
                <div>
                    <div class="box-label">REASON / REMARKS</div>
                    <div style="font-size: 12px; color: #4B5563; margin-top: 4px;">${esc(remarks)}</div>
                </div>
                <div class="signature-box">
                    Authorized Signature
                </div>
            </div>
        </div>
        </body>
        </html>
        """.trimIndent()
    }
}