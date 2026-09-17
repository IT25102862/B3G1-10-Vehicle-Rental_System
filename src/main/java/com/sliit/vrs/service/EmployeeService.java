package com.sliit.vrs.service;

import com.sliit.vrs.entity.Employee;
import com.sliit.vrs.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

// Shared helper service used by Member 3 (assigning technicians) and
// Member 6 (driver allocation / maintenance staff records).
@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Employee getById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
    }

    public Employee save(Employee employee) {
        return employeeRepository.save(employee);
    }
}
