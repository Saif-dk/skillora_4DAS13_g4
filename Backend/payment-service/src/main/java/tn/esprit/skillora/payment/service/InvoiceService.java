package tn.esprit.skillora.payment.service;

import tn.esprit.skillora.payment.domain.Invoice;
import tn.esprit.skillora.payment.domain.InvoiceStatus;
import tn.esprit.skillora.payment.domain.Payment;
import tn.esprit.skillora.payment.dto.InvoiceFile;
import tn.esprit.skillora.payment.exception.ResourceNotFoundException;
import tn.esprit.skillora.payment.repository.InvoiceRepository;
import tn.esprit.skillora.payment.security.CurrentUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.Year;
import java.time.ZoneOffset;

@Service
public class InvoiceService {

    private final InvoiceRepository invoices;
    private final InvoicePdfGenerator pdfGenerator;

    public InvoiceService(InvoiceRepository invoices, InvoicePdfGenerator pdfGenerator) {
        this.invoices = invoices;
        this.pdfGenerator = pdfGenerator;
    }

    /** Creates the invoice of a successful payment (same transaction as the status change). */
    @Transactional(propagation = Propagation.MANDATORY)
    public Invoice issueFor(Payment payment) {
        long sequence = invoices.nextInvoiceSequence();
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(String.format("INV-%d-%06d", Year.now(ZoneOffset.UTC).getValue(), sequence));
        invoice.setPayment(payment);
        invoice.setStudentId(payment.getStudentId());
        invoice.setDescription("Course: " + payment.getCourseTitle());
        invoice.setAmount(payment.getAmount());
        invoice.setCurrency(payment.getCurrency());
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setIssueDate(Instant.now());
        return invoices.save(invoice);
    }

    @Transactional(readOnly = true)
    public InvoiceFile download(CurrentUser user, Long invoiceId) {
        Invoice invoice = invoices.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice " + invoiceId + " not found"));
        if (!user.admin() && !invoice.getStudentId().equals(user.id())) {
            throw new AccessDeniedException("You are not allowed to access this invoice");
        }
        return new InvoiceFile(invoice.getInvoiceNumber() + ".pdf", pdfGenerator.generate(invoice));
    }
}
