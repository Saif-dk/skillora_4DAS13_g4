package tn.esprit.skillora.payment.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import tn.esprit.skillora.payment.domain.Invoice;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.math.RoundingMode;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class InvoicePdfGenerator {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm 'UTC'").withZone(ZoneOffset.UTC);

    public byte[] generate(Invoice invoice) {
        Font title = new Font(Font.HELVETICA, 24, Font.BOLD);
        Font bold = new Font(Font.HELVETICA, 11, Font.BOLD);
        Font normal = new Font(Font.HELVETICA, 11, Font.NORMAL);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            document.add(new Paragraph("FACTURE", title));
            document.add(new Paragraph("Skillora - Plateforme e-learning", normal));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Num\u00e9ro : " + invoice.getInvoiceNumber(), bold));
            document.add(new Paragraph("Date d'\u00e9mission : " + DATE_FORMAT.format(invoice.getIssueDate()), normal));
            document.add(new Paragraph("Statut : " + invoice.getStatus(), normal));
            String studentName = invoice.getPayment().getStudentName();
            document.add(new Paragraph("\u00c9tudiant : "
                    + (studentName != null && !studentName.isBlank() ? studentName : invoice.getStudentId()), normal));
            document.add(new Paragraph("R\u00e9f\u00e9rence de paiement : "
                    + invoice.getPayment().getTransactionReference(), normal));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(new float[] {4f, 1.5f});
            table.setWidthPercentage(100);
            table.addCell(cell("Description", bold, Element.ALIGN_LEFT));
            table.addCell(cell("Montant", bold, Element.ALIGN_RIGHT));
            table.addCell(cell(invoice.getDescription(), normal, Element.ALIGN_LEFT));
            table.addCell(cell(format(invoice), normal, Element.ALIGN_RIGHT));
            table.addCell(cell("Total", bold, Element.ALIGN_LEFT));
            table.addCell(cell(format(invoice), bold, Element.ALIGN_RIGHT));
            document.add(table);

            document.close();
        } catch (DocumentException e) {
            throw new IllegalStateException("Cannot generate invoice PDF", e);
        }
        return out.toByteArray();
    }

    private static String format(Invoice invoice) {
        return invoice.getAmount().setScale(2, RoundingMode.HALF_UP).toPlainString() + " " + invoice.getCurrency();
    }

    private static PdfPCell cell(String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text == null ? "" : text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(6f);
        cell.setBorder(Rectangle.BOTTOM);
        return cell;
    }
}
