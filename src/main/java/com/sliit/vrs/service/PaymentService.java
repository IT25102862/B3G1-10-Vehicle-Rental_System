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

    public Payment recordPayment(Reservation reservation, String method) {
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
