package com.example.employeemanagement.serviceImpltest;

import com.example.employeemanagement.dto.EmployeeRequest;
import com.example.employeemanagement.dto.EmployeeResponse;
import com.example.employeemanagement.entity.Employee;
import com.example.employeemanagement.exception.EmployeeNotFoundException;
import com.example.employeemanagement.repository.EmployeeRepository;
import com.example.employeemanagement.service.EmployeeServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private EmployeeRequest request;
    private Employee employee;

    @BeforeEach
    void setUp() {

        request = new EmployeeRequest(
                "Yashwanth",
                "Kumar",
                "yashwanth@gmail.com",
                "9876543210",
                "IT",
                "Java Developer",
                new BigDecimal("75000"),
                LocalDate.of(2026, 9, 7)
        );

        employee = Employee.builder()
                .id(1L)
                .firstName("Yashwanth")
                .lastName("Kumar")
                .email("yashwanth@gmail.com")
                .phoneNumber("9876543210")
                .department("IT")
                .jobTitle("Java Developer")
                .salary(new BigDecimal("75000"))
                .joiningDate(LocalDate.of(2026, 9, 7))
                .build();
    }

    @Test
    void createEmployee_shouldCreateEmployeeSuccessfully() {

        when(employeeRepository.existsByEmail(request.email()))
                .thenReturn(false);

        when(employeeRepository.save(any(Employee.class)))
                .thenReturn(employee);

        EmployeeResponse response =
                employeeService.createEmployee(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Yashwanth", response.firstName());
        assertEquals("Kumar", response.lastName());
        assertEquals("yashwanth@gmail.com", response.email());
        assertEquals(new BigDecimal("75000"), response.salary());

        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void createEmployee_shouldThrowExceptionWhenEmailExists() {

        when(employeeRepository.existsByEmail(request.email()))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.createEmployee(request)
        );

        verify(employeeRepository, never())
                .save(any(Employee.class));
    }

    @Test
    void getEmployeeById_shouldReturnEmployee() {

        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(employee));

        EmployeeResponse response =
                employeeService.getEmployeeById(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Yashwanth", response.firstName());
        assertEquals("IT", response.department());
    }

    @Test
    void getEmployeeById_shouldThrowExceptionWhenNotFound() {

        when(employeeRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                EmployeeNotFoundException.class,
                () -> employeeService.getEmployeeById(1L)
        );
    }

    @Test
    void getAllEmployees_shouldReturnEmployees() {

        Employee employee2 = Employee.builder()
                .id(2L)
                .firstName("Rahul")
                .lastName("Kumar")
                .email("rahul@gmail.com")
                .phoneNumber("9876543211")
                .department("HR")
                .jobTitle("HR Manager")
                .salary(new BigDecimal("85000"))
                .joiningDate(LocalDate.of(2026, 9, 7))
                .build();

        when(employeeRepository.findAll())
                .thenReturn(List.of(employee, employee2));

        List<EmployeeResponse> response =
                employeeService.getAllEmployees();

        assertEquals(2, response.size());
        assertEquals("Yashwanth", response.get(0).firstName());
        assertEquals("Rahul", response.get(1).firstName());

        verify(employeeRepository).findAll();
    }

    @Test
    void updateEmployee_shouldUpdateSuccessfully() {

        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(employee));

        when(employeeRepository.save(any(Employee.class)))
                .thenReturn(employee);

        EmployeeResponse response =
                employeeService.updateEmployee(1L, request);

        assertNotNull(response);
        assertEquals(1L, response.id());

        verify(employeeRepository).save(employee);
    }

    @Test
    void updateEmployee_shouldThrowExceptionWhenEmployeeNotFound() {

        when(employeeRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                EmployeeNotFoundException.class,
                () -> employeeService.updateEmployee(1L, request)
        );

        verify(employeeRepository, never())
                .save(any(Employee.class));
    }

    @Test
    void updateEmployee_shouldThrowExceptionWhenEmailAlreadyExists() {

        EmployeeRequest updateRequest = new EmployeeRequest(
                "Yashwanth",
                "Kumar",
                "newemail@gmail.com",
                "9876543210",
                "IT",
                "Senior Java Developer",
                new BigDecimal("90000"),
                LocalDate.of(2026, 9, 7)
        );

        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(employee));

        when(employeeRepository.existsByEmail("newemail@gmail.com"))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.updateEmployee(1L, updateRequest)
        );

        verify(employeeRepository, never())
                .save(any(Employee.class));
    }

    @Test
    void deleteEmployee_shouldDeleteSuccessfully() {

        when(employeeRepository.existsById(1L))
                .thenReturn(true);

        employeeService.deleteEmployee(1L);

        verify(employeeRepository)
                .deleteById(1L);
    }

    @Test
    void deleteEmployee_shouldThrowExceptionWhenNotFound() {

        when(employeeRepository.existsById(1L))
                .thenReturn(false);

        assertThrows(
                EmployeeNotFoundException.class,
                () -> employeeService.deleteEmployee(1L)
        );

        verify(employeeRepository, never())
                .deleteById(anyLong());
    }
}