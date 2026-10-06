package com.sliit.vrs.service;

import com.sliit.vrs.entity.Payment;
import com.sliit.vrs.entity.Reservation;
import com.sliit.vrs.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

// MEMBER 2 - Booking & Reservation Management (Payment sub-feature).
// No external payment gateway is used (per the proposal's "Zero External
// Dependencies" constraint) - payments are simply recorded internally.
@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    // Only these payment methods are offered on the payment form.
    private static final List<String> ALLOWED_METHODS = List.of("Card", "Bank Transfer", "Cash");

    public boolean isPaid(Reservation reservation) {
        return reservation != null && paymentRepository.existsByReservation(reservation);
    }

    public Payment recordPayment(Reservation reservation, String method) {
        if (reservation == null) {
            throw new IllegalArgumentException("Booking not found.");
        }
        if (method == null || !ALLOWED_METHODS.contains(method)) {
            throw new IllegalArgumentException("Please choose a valid payment method.");
        }
        if (reservation.getStatus() != Reservation.ReservationStatus.PENDING_APPROVAL
                && reservation.getStatus() != Reservation.ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("A " + reservation.getStatus() + " booking cannot be paid for.");
        }
        if (reservation.getTotalAmount() == null || reservation.getTotalAmount() <= 0) {
            throw new IllegalStateException("This booking has an invalid total amount and cannot be paid.");
        }
        // Payment has a one-to-one link to Reservation, so a second payment
        // would also break the database's unique constraint.
        if (isPaid(reservation)) {
            throw new IllegalStateException("This booking has already been paid.");
        }

        Payment payment = new Payment();
        payment.setReservation(reservation);
        payment.setAmount(reservation.getTotalAmount());
        payment.setPaymentDate(LocalDate.now());
        payment.setPaymentMethod(method);
        payment.setPaymentStatus(Payment.PaymentStatus.PAID);
        payment.setTransactionReference("TXN-" + System.currentTimeMillis());
        return paymentRepository.save(payment);
    }
}
