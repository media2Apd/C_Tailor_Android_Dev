package com.cuso.tailor.view.home.pdfgenerator

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.cuso.tailor.model.inventory.ProcurementBillDetailData
import com.cuso.tailor.view.home.formatIndianNumber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class ProcurementBillPdfGenerator(private val context: Context) {

    private var activeWebView: WebView? = null

    data class SavedPdf(
        val uri: Uri?,
        val displayName: String,
        val file: File? = null,
        val sizeBytes: Long = 0L
    )

    // Standard A4 Portrait in PDF points (72 DPI)
    private val pageWidthPt = 595
    private val pageHeightPt = 842

    // Standard A4 Portrait in CSS pixels (96 DPI): 794 x 1123
    private val renderWidthPx = 794
    private val pageHeightInRenderPx = 1123

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
            Log.e("ProcurementBillPdfGen", "attachToWindow failed", e)
        }
    }

    private fun detachFromWindow(webView: WebView) {
        try {
            (webView.parent as? ViewGroup)?.removeView(webView)
        } catch (e: Exception) {
            Log.e("ProcurementBillPdfGen", "detachFromWindow failed", e)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun generatePdfFromHtml(
        htmlContent: String,
        fileName: String,
        saveToDownloads: Boolean = true,
        onComplete: (SavedPdf?) -> Unit
    ) {
        val webView = WebView(context).apply {
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false
            scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
            overScrollMode = View.OVER_SCROLL_NEVER

            layoutParams = ViewGroup.LayoutParams(
                renderWidthPx,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            settings.apply {
                javaScriptEnabled = true
                textZoom = 100
                useWideViewPort = false
                loadWithOverviewMode = false
                defaultFontSize = 14
                minimumFontSize = 1
                minimumLogicalFontSize = 1
            }
        }

        activeWebView = webView

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
                    val result = renderWebViewToPdf(webView, fileName, saveToDownloads)
                    finish(result)
                } catch (e: Exception) {
                    Log.e("ProcurementBillPdfGen", "Render timeout fallback failed", e)
                    finish(null)
                }
            }
        }, 8000)

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                view?.postDelayed({
                    if (finished) return@postDelayed
                    try {
                        val result = renderWebViewToPdf(webView, fileName, saveToDownloads)
                        finish(result)
                    } catch (e: Exception) {
                        Log.e("ProcurementBillPdfGen", "Render onPageFinished failed", e)
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
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }

    private fun renderWebViewToPdf(
        webView: WebView,
        fileName: String,
        saveToDownloads: Boolean
    ): SavedPdf? {
        webView.measure(
            View.MeasureSpec.makeMeasureSpec(renderWidthPx, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )

        val measuredHeightPx = webView.measuredHeight
        webView.layout(0, 0, renderWidthPx, maxOf(measuredHeightPx, pageHeightInRenderPx))

        // Fixed ratio: 595 / 794
        val scale = pageWidthPt.toFloat() / renderWidthPx.toFloat()

        // Calculate pages only if content overflows standard A4 height
        val totalPages = maxOf(1, (measuredHeightPx + pageHeightInRenderPx - 1) / pageHeightInRenderPx)

        val pdfDocument = PdfDocument()

        for (pageIndex in 0 until totalPages) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidthPt, pageHeightPt, pageIndex + 1).create()
            val page = pdfDocument.startPage(pageInfo)

            page.canvas.save()
            page.canvas.scale(scale, scale)
            page.canvas.translate(0f, -(pageIndex * pageHeightInRenderPx).toFloat())
            webView.draw(page.canvas)
            page.canvas.restore()

            pdfDocument.finishPage(page)
        }

        val outputStream = ByteArrayOutputStream()
        pdfDocument.writeTo(outputStream)
        pdfDocument.close()

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
            Log.e("ProcurementBillPdfGen", "writeBytesToDownloads failed for '$fileName'", e)
            null
        }
    }

    fun downloadBillPdf(detail: ProcurementBillDetailData, onComplete: ((SavedPdf?) -> Unit)? = null) {
        val cleanBillNo = detail.billNumber.ifBlank { "Bill" }.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val fileName = "${cleanBillNo}_${System.currentTimeMillis()}.pdf"

        Toast.makeText(context, "Generating PDF...", Toast.LENGTH_SHORT).show()

        val html = buildBillHtml(detail)
        generatePdfFromHtml(
            htmlContent = html,
            fileName = fileName,
            saveToDownloads = true,
            onComplete = { saved ->
                if (saved != null) {
                    Toast.makeText(context, "PDF saved to Downloads: $fileName", Toast.LENGTH_LONG).show()
                    onComplete?.invoke(saved)
                } else {
                    Toast.makeText(context, "Failed to download PDF", Toast.LENGTH_SHORT).show()
                    onComplete?.invoke(null)
                }
            }
        )
    }

    private fun resolveStatusPillColors(status: String): Pair<String, String> {
        return when (status.lowercase()) {
            "paid", "completed", "approved" -> "#DCFCE7" to "#15803D"
            "sent", "pending", "open" -> "#EEECFC" to "#2F27CE"
            "void", "cancelled", "rejected" -> "#FEE2E2" to "#DC2626"
            "draft" -> "#F1F5F9" to "#475569"
            else -> "#F1F5F9" to "#475569"
        }
    }

    private fun buildBillHtml(detail: ProcurementBillDetailData): String {
        val billNumber = detail.billNumber.ifBlank { "-" }
        val status = detail.status.ifBlank { "-" }.uppercase()
        val (statusBg, statusTextColor) = resolveStatusPillColors(detail.status)

        // Bill To
        val billTo = detail.supplierSnapshot
        val vendorName = billTo?.name?.ifBlank { null }
            ?: detail.supplierId?.name?.ifBlank { null }
            ?: "-"
        val billingAddr = billTo?.billingAddress
        val billAddrLine = listOfNotNull(
            billingAddr?.flatNo?.ifBlank { null },
            billingAddr?.street?.ifBlank { null },
            billingAddr?.city?.ifBlank { null },
            billingAddr?.state?.ifBlank { null },
            billingAddr?.pincode?.ifBlank { null }
        ).joinToString(", ").ifBlank { "-" }
        val billCountry = billingAddr?.country?.ifBlank { null } ?: "-"
        val vendorPhone = billTo?.phone?.ifBlank { null } ?: "-"

        // Ship To
        val warehouseName = detail.warehouseId?.name?.ifBlank { null }
            ?: detail.companySnapshot?.name?.ifBlank { null }
            ?: "-"
        val shipAddr = detail.supplierSnapshot?.shippingAddress
        val shipAddrLine = listOfNotNull(
            shipAddr?.flatNo?.ifBlank { null },
            shipAddr?.street?.ifBlank { null },
            shipAddr?.city?.ifBlank { null },
            shipAddr?.state?.ifBlank { null },
            shipAddr?.pincode?.ifBlank { null }
        ).let { if (it.isNotEmpty()) it.joinToString(", ") else detail.companySnapshot?.address?.ifBlank { "-" } ?: "-" }
        val shipCountry = shipAddr?.country?.ifBlank { null } ?: "-"

        // Dates & Contacts
        val billDate = formatPdfDate(detail.billDate)
        val dueDate = formatPdfDate(detail.dueDate)
        val companyEmail = detail.companySnapshot?.email?.ifBlank { null } ?: "-"
        val companyPhone = detail.companySnapshot?.phone?.ifBlank { null } ?: "-"

        // Table Rows
        val itemRows = if (detail.lines.isEmpty()) {
            "<tr><td colspan=\"5\" style=\"text-align: left; color: #6b7280; padding: 14px 6px;\">-</td></tr>"
        } else {
            detail.lines.joinToString("\n") { line ->
                val itemName = line.item?.name?.ifBlank { null } ?: line.itemDescription?.ifBlank { null } ?: "-"
                val sku = line.item?.sku?.ifBlank { null }
                val skuHtml = if (!sku.isNullOrBlank()) "<div class=\"item-sku\">SKU: $sku</div>" else ""

                val taxPercentageText = if (line.taxBreakdown.isNotEmpty()) {
                    val sumRate = line.taxBreakdown.sumOf { it.rate }
                    if (sumRate % 1.0 == 0.0) "${sumRate.toInt()}%" else "$sumRate%"
                } else if (line.taxableAmount > 0.0 && line.totalTax > 0.0) {
                    val calcRate = (line.totalTax / line.taxableAmount) * 100.0
                    if (calcRate % 1.0 == 0.0) "${calcRate.toInt()}%" else String.format(Locale.US, "%.1f%%", calcRate)
                } else {
                    "-"
                }

                val qtyStr = if (line.quantity % 1.0 == 0.0) line.quantity.toInt().toString() else line.quantity.toString()

                """
                <tr>
                    <td class="col-desc">
                        <div class="item-name">$itemName</div>
                        $skuHtml
                    </td>
                    <td class="col-price">&#8377;${formatIndianNumber(line.rate)}</td>
                    <td class="col-qty">$qtyStr</td>
                    <td class="col-tax">$taxPercentageText</td>
                    <td class="col-total">&#8377;${formatIndianNumber(line.lineTotal)}</td>
                </tr>
                """.trimIndent()
            }
        }

        val subtotal = formatIndianNumber(detail.subtotal)
        val tax = formatIndianNumber(detail.totalTax)
        val due = formatIndianNumber(detail.balanceDue)
        val grandTotal = formatIndianNumber(detail.grandTotal)

        return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=794, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <title>Bill $billNumber</title>
            <style>
                * {
                    margin: 0;
                    padding: 0;
                    box-sizing: border-box;
                    -webkit-print-color-adjust: exact;
                }
                html, body {
                    width: 794px;
                    margin: 0;
                    padding: 0;
                    background: #ffffff;
                    color: #111827;
                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
                    overflow: hidden;
                }
                .page-container {
                    width: 794px;
                    padding: 42px 48px;
                    box-sizing: border-box;
                }
                .header-row {
                    display: flex;
                    justify-content: space-between;
                    align-items: flex-start;
                    margin-bottom: 26px;
                }
                .bill-title {
                    font-size: 26px;
                    font-weight: 500;
                    color: #111827;
                }
                .header-right {
                    text-align: right;
                }
                .status-badge {
                    display: inline-block;
                    padding: 3px 12px;
                    border-radius: 9999px;
                    font-size: 11px;
                    font-weight: 700;
                    background-color: $statusBg;
                    color: $statusTextColor;
                    margin-bottom: 6px;
                    letter-spacing: 0.5px;
                }
                .bill-number-label {
                    font-size: 11px;
                    color: #6B7280;
                    margin-bottom: 2px;
                }
                .bill-number-val {
                    font-size: 13px;
                    font-weight: 700;
                    color: #111827;
                }
                .two-col-grid {
                    display: flex;
                    justify-content: space-between;
                    margin-bottom: 22px;
                }
                .two-col-grid > div {
                    width: 48%;
                }
                .col-heading {
                    font-size: 11px;
                    color: #6B7280;
                    margin-bottom: 5px;
                }
                .entity-name {
                    font-size: 13px;
                    font-weight: 600;
                    color: #111827;
                    margin-bottom: 2px;
                }
                .address-line {
                    font-size: 11.5px;
                    color: #374151;
                    line-height: 1.4;
                    margin-bottom: 2px;
                }
                .meta-table {
                    width: 100%;
                    border-collapse: collapse;
                    margin-bottom: 24px;
                }
                .meta-table td {
                    padding: 4px 0;
                    font-size: 12px;
                }
                .meta-label {
                    color: #6B7280;
                }
                .meta-val {
                    color: #111827;
                    font-weight: 400;
                }
                .items-title {
                    font-size: 13.5px;
                    font-weight: 700;
                    color: #111827;
                    margin-bottom: 10px;
                }
                .items-table {
                    width: 100%;
                    border-collapse: collapse;
                }
                .items-table thead tr {
                    background-color: #F9FAFB;
                    border-top: 1px solid #E5E7EB;
                    border-bottom: 1px solid #E5E7EB;
                }
                .items-table th {
                    font-size: 11px;
                    font-weight: 500;
                    color: #6B7280;
                    padding: 8px 6px;
                }
                .items-table tbody tr {
                    border-bottom: 1px solid #F3F4F6;
                }
                .items-table td {
                    font-size: 12px;
                    color: #111827;
                    padding: 12px 6px;
                    vertical-align: top;
                }
                .col-desc { width: 44%; text-align: left; }
                .col-price { width: 16%; text-align: right; }
                .col-qty { width: 10%; text-align: center; }
                .col-tax { width: 12%; text-align: center; }
                .col-total { width: 18%; text-align: right; font-weight: 700; }
                
                .item-name {
                    font-weight: 500;
                    color: #111827;
                    margin-bottom: 2px;
                }
                .item-sku {
                    font-size: 10.5px;
                    color: #9CA3AF;
                }
                .summary-container {
                    display: flex;
                    justify-content: space-between;
                    align-items: flex-end;
                    margin-top: 24px;
                }
                .summary-left {
                    width: 220px;
                }
                .summary-row {
                    display: flex;
                    justify-content: space-between;
                    font-size: 12.5px;
                    margin-bottom: 6px;
                }
                .summary-row .label {
                    color: #6B7280;
                }
                .summary-row .val {
                    color: #111827;
                    font-weight: 400;
                }
                .summary-right {
                    text-align: right;
                }
                .grand-total-label {
                    font-size: 11px;
                    color: #6B7280;
                    margin-bottom: 4px;
                }
                .grand-total-val {
                    font-size: 24px;
                    font-weight: 700;
                    color: #111827;
                }
            </style>
        </head>
        <body>
            <div class="page-container">
                <div class="header-row">
                    <div class="bill-title">Bill</div>
                    <div class="header-right">
                        <div class="status-badge">$status</div>
                        <div class="bill-number-label">Bill number</div>
                        <div class="bill-number-val">$billNumber</div>
                    </div>
                </div>

                <div class="two-col-grid">
                    <div>
                        <div class="col-heading">Bill to</div>
                        <div class="entity-name">$vendorName</div>
                        <div class="address-line">$billAddrLine</div>
                        <div class="address-line">$billCountry</div>
                        <div class="address-line">Phone: $vendorPhone</div>
                    </div>
                    <div>
                        <div class="col-heading">Ship to</div>
                        <div class="entity-name">$warehouseName</div>
                        <div class="address-line">$shipAddrLine</div>
                        <div class="address-line">$shipCountry</div>
                    </div>
                </div>

                <table class="meta-table">
                    <tr>
                        <td style="width: 14%;" class="meta-label">Bill date</td>
                        <td style="width: 36%;" class="meta-val">$billDate</td>
                        <td style="width: 14%; text-align: right;" class="meta-label">Email</td>
                        <td style="width: 36%; text-align: right;" class="meta-val">$companyEmail</td>
                    </tr>
                    <tr>
                        <td style="width: 14%;" class="meta-label">Due date</td>
                        <td style="width: 36%;" class="meta-val">$dueDate</td>
                        <td style="width: 14%; text-align: right;" class="meta-label">Phone</td>
                        <td style="width: 36%; text-align: right;" class="meta-val">$companyPhone</td>
                    </tr>
                </table>

                <div class="items-title">Items</div>
                <table class="items-table">
                    <thead>
                        <tr>
                            <th class="col-desc">Item / Description</th>
                            <th class="col-price">Unit Price</th>
                            <th class="col-qty">Qty</th>
                            <th class="col-tax">Tax %</th>
                            <th class="col-total">Total</th>
                        </tr>
                    </thead>
                    <tbody>
                        $itemRows
                    </tbody>
                </table>

                <div class="summary-container">
                    <div class="summary-left">
                        <div class="summary-row">
                            <span class="label">Subtotal</span>
                            <span class="val">&#8377;$subtotal</span>
                        </div>
                        <div class="summary-row">
                            <span class="label">Tax</span>
                            <span class="val">&#8377;$tax</span>
                        </div>
                        <div class="summary-row">
                            <span class="label">Due</span>
                            <span class="val">&#8377;$due</span>
                        </div>
                    </div>
                    <div class="summary-right">
                        <div class="grand-total-label">Grand total</div>
                        <div class="grand-total-val">&#8377;$grandTotal</div>
                    </div>
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    private fun formatPdfDate(isoDate: String?): String {
        if (isoDate.isNullOrBlank()) return "-"
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
            val parsed = inputFormat.parse(isoDate)
            parsed?.let { outputFormat.format(it) } ?: isoDate.take(10)
        } catch (_: Exception) {
            isoDate.take(10)
        }
    }
}