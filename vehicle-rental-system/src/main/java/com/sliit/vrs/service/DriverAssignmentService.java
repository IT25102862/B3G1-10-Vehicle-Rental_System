package com.sliit.vrs.service;

import com.sliit.vrs.entity.DriverAssignment;
import com.sliit.vrs.entity.Employee;
import com.sliit.vrs.repository.DriverAssignmentRepository;
import com.sliit.vrs.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

// MEMBER 6 - Maintenance Operations Management System (Driver Allocation)
@Service
public class DriverAssignmentService {

    @Autowired
    private DriverAssignmentRepository assignmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    public List<DriverAssignment> getAllAssignments() {
        return assignmentRepository.findAll();
    }

    public DriverAssignment assignDriver(DriverAssignment assignment) {
        assignment.setAssignedDate(LocalDate.now());
        assignment.setStatus(DriverAssignment.AssignmentStatus.ASSIGNED);

        // Update the driver's own availability record too (not just the assignment row).
        Employee driver = assignment.getDriver();
        driver.setDriverStatus(Employee.DriverStatus.ON_SHIFT);
        employeeRepository.save(driver);

        return assignmentRepository.save(assignment);
    }

    public void updateStatus(Long id, DriverAssignment.AssignmentStatus status) {
        DriverAssignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found with id: " + id));
        assignment.setStatus(status);
        assignmentRepository.save(assignment);
    }
}
