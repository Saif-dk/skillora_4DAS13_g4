package tn.esprit.skillora.payment.repository;

import tn.esprit.skillora.payment.domain.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundRepository extends JpaRepository<Refund, Long> {
}
