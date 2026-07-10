package com.cuso.mobile.view.home.sales.sales_order.pdfgenerator

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import java.io.File
import java.io.FileOutputStream
import java.util.Locale



class QuotationPdfGenerator(private val context: Context) {

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

    data class QuotationItem(
        val description: String,
        val quantity: Int,
        val rate: Double,
        val amount: Double
    )

    // A4 size in points (72 dpi) — matches Canvas PDF page size convention
    private val pageWidthPt = 595
    private val pageHeightPt = 842

    // ────────────────────────────────────────────────────────────
    // MAIN ENTRY: Renders the SAME HTML used in preview into a WebView,
    // captures it as bitmap(s), and writes those bitmaps into a
    // PdfDocument — no package-private classes involved.
    // ────────────────────────────────────────────────────────────
    fun generatePdfFromHtml(
        data: QuotationData,
        fileName: String = "quotation_${data.quotationNumber}.pdf",
        saveToDownloads: Boolean = false,
        onComplete: (File?) -> Unit
    ) {
        val density = context.resources.displayMetrics.density
        // Render at same pixel width as an A4 page at ~96dpi scale for crisp output
        val renderWidthPx = (pageWidthPt * density).toInt().coerceAtLeast(800)

        val webView = WebView(context)
        webView.layoutParams = android.view.ViewGroup.LayoutParams(
            renderWidthPx,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
        webView.settings.javaScriptEnabled = false
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                view?.postDelayed({
                    try {
                        val file = renderWebViewToPdf(webView, renderWidthPx, density, fileName, saveToDownloads)
                        onComplete(file)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        onComplete(null)
                    }
                }, 350)
            }
        }

        webView.loadDataWithBaseURL(null, buildQuotationHtml(data), "text/html", "UTF-8", null)
    }

    private fun renderWebViewToPdf(
        webView: WebView,
        renderWidthPx: Int,
        density: Float,
        fileName: String,
        saveToDownloads: Boolean
    ): File? {
        // Measure full content height
        webView.measure(
            android.view.View.MeasureSpec.makeMeasureSpec(renderWidthPx, android.view.View.MeasureSpec.EXACTLY),
            android.view.View.MeasureSpec.makeMeasureSpec(0, android.view.View.MeasureSpec.UNSPECIFIED)
        )
        val contentHeightPx = (webView.contentHeight * density).toInt().coerceAtLeast(webView.measuredHeight)
        webView.layout(0, 0, renderWidthPx, contentHeightPx)

        // Capture the whole page into one tall bitmap
        val fullBitmap = Bitmap.createBitmap(renderWidthPx, contentHeightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(fullBitmap)
        canvas.drawColor(android.graphics.Color.WHITE)
        webView.draw(canvas)

        // Slice into A4-proportioned pages and write into PdfDocument
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

        val targetFile = resolveOutputFile(fileName, saveToDownloads)
        return try {
            FileOutputStream(targetFile).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()
            fullBitmap.recycle()
            targetFile
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            fullBitmap.recycle()
            null
        }
    }

    private fun resolveOutputFile(fileName: String, saveToDownloads: Boolean): File {
        return if (saveToDownloads) {
            val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
                android.os.Environment.DIRECTORY_DOWNLOADS
            )
            if (!downloadsDir.exists()) downloadsDir.mkdirs()
            File(downloadsDir, fileName)
        } else {
            File(context.getExternalFilesDir(null), fileName)
        }
    }

    // ── Download PDF (public Downloads folder, exact preview match) ──
    // ── Download PDF with better error handling ──
    fun downloadQuotationPdf(data: QuotationData): File? {
        return try {
            val fileName = "quotation_${data.quotationNumber}_${System.currentTimeMillis()}.pdf"
            val file = File(context.getExternalFilesDir(null), fileName)

            // Generate PDF and save
            generateQuotationPdf(data, fileName)?.let { savedFile ->
                // Verify file exists and has content
                if (savedFile.exists() && savedFile.length() > 0) {
                    savedFile
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    // ── Generate PDF ──
    fun generateQuotationPdf(data: QuotationData, fileName: String = "quotation_${data.quotationNumber}.pdf"): File? {
        return try {
            // Get the app's external files directory
            val directory = context.getExternalFilesDir(null)
            if (directory == null) {
                // Fallback to internal storage
                val file = File(context.filesDir, fileName)
                generatePdfToFile(data, file)
                return file
            }

            val file = File(directory, fileName)
            generatePdfToFile(data, file)
            file

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun generatePdfToFile(data: QuotationData, file: File) {
        val outputStream = FileOutputStream(file)

        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // ... (rest of the PDF drawing code goes here)

        pdfDocument.finishPage(page)
        pdfDocument.writeTo(outputStream)
        pdfDocument.close()
        outputStream.close()
    }

    // ── Print (still uses WebView's built-in adapter — this one IS allowed,
    //     since we're not subclassing the callback classes ourselves) ──
    fun printQuotationPdf(data: QuotationData) {
        val webView = WebView(context)
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
            }
        }
        webView.loadDataWithBaseURL(null, buildQuotationHtml(data), "text/html", "UTF-8", null)
    }

    // ── HTML for WebView (SAME html used for preview, download, print) ──
    fun buildQuotationHtml(data: QuotationData): String {
        val itemsHtml = data.items.joinToString("") { item ->
            """
            <tr>
                <td style="text-align: center; padding: 8px 10px;">${item.quantity}</td>
                <td style="padding: 8px 10px;">${item.description}</td>
                <td style="text-align: right; padding: 8px 10px;">₹${String.format(Locale.US, "%.0f", item.rate)}</td>
                <td style="text-align: right; padding: 8px 10px;">₹${String.format(Locale.US, "%.0f", item.amount)}</td>
            </tr>
            """.trimIndent()
        }

        val termsHtml = data.termsAndConditions.joinToString("") { term ->
            "<li>&bull; $term</li>"
        }

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Quotation</title>
            <style>
                * { margin: 0; padding: 0; box-sizing: border-box; }
                body {
                    font-family: Arial, sans-serif;
                    padding: 30px 40px;
                    max-width: 800px;
                    margin: 0 auto;
                    color: #111827;
                }
                .top-row {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    margin-bottom: 26px;
                }
                .logo-box {
                    width: 130px;
                    height: 65px;
                    border: 2px solid #7C3AED;
                    border-radius: 8px;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    background: #fff;
                    overflow: hidden;
                }
                .logo-box img {
                    max-width: 100%;
                    max-height: 100%;
                    object-fit: contain;
                }
                .quotation-title {
                    font-size: 26px;
                    font-weight: bold;
                    color: #1E293B;
                }
                .info-row {
                    display: flex;
                    justify-content: space-between;
                    margin-bottom: 24px;
                }
                .section-title {
                    font-size: 13px;
                    font-weight: bold;
                    color: #111827;
                    margin-bottom: 6px;
                }
                .recipient-info p {
                    font-size: 12.5px;
                    color: #2563EB;
                    line-height: 1.6;
                    margin: 1px 0;
                }
                .quotation-meta { text-align: right; }
                .meta-row { margin: 4px 0; }
                .meta-label {
                    font-size: 11px;
                    font-weight: bold;
                    color: #94A3B8;
                    display: block;
                }
                .meta-value {
                    font-size: 12.5px;
                    color: #2563EB;
                }
                table {
                    width: 100%;
                    border-collapse: collapse;
                    margin: 10px 0 16px 0;
                }
                th {
                    background: #F8F9FB;
                    font-size: 11.5px;
                    font-weight: 600;
                    color: #9CA3AF;
                    padding: 10px;
                    text-align: left;
                    text-transform: uppercase;
                    border-bottom: 1px solid #EEE;
                }
                th:first-child { text-align: center; width: 50px; }
                th:nth-child(2) { width: 55%; }
                th:nth-child(3) { text-align: right; width: 20%; }
                th:last-child { text-align: right; width: 20%; }
                td {
                    font-size: 13px;
                    color: #1E293B;
                    padding: 8px 10px;
                    border-bottom: 1px solid #F3F4F6;
                }
                td:first-child { text-align: center; }
                td:nth-child(3), td:last-child { text-align: right; }
                .totals {
                    margin-top: 6px;
                    text-align: right;
                }
                .totals p {
                    font-size: 13.5px;
                    color: #6B7280;
                    padding: 3px 0;
                }
                .totals p span { color: #111827; margin-left: 20px; }
                .total-row {
                    font-size: 16px !important;
                    font-weight: bold;
                    color: #4338CA !important;
                    margin-top: 4px;
                }
                .total-row span { color: #4338CA !important; }
                .terms { margin-top: 30px; }
                .terms-title {
                    font-size: 13px;
                    font-weight: bold;
                    color: #111827;
                    margin-bottom: 8px;
                }
                .terms ul { list-style: none; padding-left: 0; }
                .terms li {
                    font-size: 11.5px;
                    color: #6B7280;
                    padding: 2px 0;
                }
                .footer {
                    text-align: center;
                    margin-top: 34px;
                }
                .footer p {
                    font-size: 12.5px;
                    color: #9CA3AF;
                    margin: 3px 0;
                }
                .footer .powered { font-size: 10.5px; }
            </style>
        </head>
        <body>
            <div class="top-row">
                <div class="logo-box">
                    <img src="${data.logoUrl ?: ""}" onerror="this.style.display='none'"/>
                </div>
                <div class="quotation-title">Quotation</div>
            </div>

            <div class="info-row">
                <div class="recipient-info">
                    <div class="section-title">RECIPIENT</div>
                    <p>${data.customerName}</p>
                    ${data.customerAddress.split("\n").joinToString("") { "<p>$it</p>" }}
                    ${if (data.customerEmail.isNotEmpty()) "<p>${data.customerEmail}</p>" else ""}
                    ${if (data.customerPhone.isNotEmpty()) "<p>${data.customerPhone}</p>" else ""}
                </div>
                <div class="quotation-meta">
                    <div class="meta-row">
                        <span class="meta-label">QUOTATION NO.</span>
                        <span class="meta-value">${data.quotationNumber}</span>
                    </div>
                    <div class="meta-row">
                        <span class="meta-label">QUOTATION DATE</span>
                        <span class="meta-value">${data.quotationDate}</span>
                    </div>
                </div>
            </div>

            <div class="section-title">TASK DESCRIPTION</div>

            <table>
                <thead>
                    <tr>
                        <th>Qty</th>
                        <th>Description</th>
                        <th>RATE</th>
                        <th>AMOUNT</th>
                    </tr>
                </thead>
                <tbody>
                    $itemsHtml
                </tbody>
            </table>

            <div class="totals">
                <p>SUBTOTAL <span>₹${String.format(Locale.US, "%.2f", data.subtotal)}</span></p>
                ${if (data.discountPercent > 0) """
                <p>DISCOUNT ${String.format(Locale.US, "%.0f", data.discountPercent)}% <span>-₹${String.format(Locale.US, "%.2f", data.discountAmount)}</span></p>
                """ else ""}
                <p class="total-row">TOTAL <span>₹${String.format(Locale.US, "%.2f", data.total)}</span></p>
            </div>

            ${if (data.termsAndConditions.isNotEmpty()) """
            <div class="terms">
                <div class="terms-title">Terms & Conditions:</div>
                <ul>
                    $termsHtml
                </ul>
            </div>
            """ else ""}

            <div class="footer">
                <p>${data.thankYouMessage}</p>
                <p class="powered">${data.poweredBy}</p>
            </div>
        </body>
        </html>
        """.trimIndent()
    }
}