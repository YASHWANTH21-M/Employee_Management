package com.example.employeemanagement.service;

import com.example.employeemanagement.dto.EmployeeRequest;
import com.example.employeemanagement.dto.EmployeeResponse;
import com.example.employeemanagement.entity.Employee;
import com.example.employeemanagement.exception.DuplicateEmailException;
import com.example.employeemanagement.exception.EmployeeNotFoundException;
import com.example.employeemanagement.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Override
    public EmployeeResponse createEmployee(EmployeeRequest request) {

    	log.info("Creating employee with email: {}", request.email());
        if (employeeRepository.existsByEmail(request.email())) {
        	log.warn("Duplicate email detected: {}", request.email());
            throw new DuplicateEmailException(
                    "Employee already exists with email: " + request.email()
            );
        }

        Employee employee = Employee.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .phoneNumber(request.phoneNumber())
                .department(request.department())
                .jobTitle(request.jobTitle())
                .salary(request.salary())
                .joiningDate(request.joiningDate())
                .build();

        Employee savedEmployee = employeeRepository.save(employee);

        return mapToResponse(savedEmployee);
    }

    @Override
    public EmployeeResponse updateEmployee(
            Long id,
            EmployeeRequest request) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() ->
                        new EmployeeNotFoundException(
                                "Employee not found with id: " + id
                        ));

        if (!employee.getEmail().equals(request.email())
                && employeeRepository.existsByEmail(request.email())) {

            throw new DuplicateEmailException(
                    "Email already exists: " + request.email()
            );
        }

        employee.setFirstName(request.firstName());
        employee.setLastName(request.lastName());
        employee.setEmail(request.email());
        employee.setPhoneNumber(request.phoneNumber());
        employee.setDepartment(request.department());
        employee.setJobTitle(request.jobTitle());
        employee.setSalary(request.salary());
        employee.setJoiningDate(request.joiningDate());

        Employee updatedEmployee = employeeRepository.save(employee);

        return mapToResponse(updatedEmployee);
    }

    @Override
    public List<EmployeeResponse> getAllEmployees() {

        return employeeRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public EmployeeResponse getEmployeeById(Long id) {
    	log.info("Fetching employee with id: {}", id);

    	return employeeRepository.findById(id)
    	        .map(this::mapToResponse)
    	        .orElseThrow(() -> {
    	            log.warn("Employee not found with id: {}", id);
    	            return new EmployeeNotFoundException(
    	                    "Employee not found with id: " + id
    	            );
    	        });
    }

    @Override
    public void deleteEmployee(Long id) {
    	log.info("Deleting employee with id: {}", id);
        if (!employeeRepository.existsById(id)) {
            throw new EmployeeNotFoundException(
                    "Employee not found with id: " + id
            );
        }

        employeeRepository.deleteById(id);
    }

    private EmployeeResponse mapToResponse(Employee employee) {

        return new EmployeeResponse(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getPhoneNumber(),
                employee.getDepartment(),
                employee.getJobTitle(),
                employee.getSalary(),
                employee.getJoiningDate()
        );
    }
}