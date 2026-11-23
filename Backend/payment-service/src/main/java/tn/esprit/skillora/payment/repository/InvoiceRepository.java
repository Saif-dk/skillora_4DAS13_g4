package tn.esprit.skillora.payment.repository;

import tn.esprit.skillora.payment.domain.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByPaymentId(Long paymentId);

    List<Invoice> findByPaymentIdIn(Collection<Long> paymentIds);

    @Query(value = "select nextval('invoice_number_seq')", nativeQuery = true)
    Long nextInvoiceSequence();
}
