package com.reliaquest.api.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.reliaquest.api.exception.EmployeeCreationException;
import com.reliaquest.api.model.CreateEmployeeInput;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.ServerResponse;
import com.reliaquest.api.service.EmployeeService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class EmployeeControllerTest {

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeController employeeController;

    private EmployeeControllerAdvice controllerAdvice;

    private UUID testId;
    private Employee testEmployee;
    private List<Employee> testEmployees;
    private ServerResponse<Employee> testEmployeeResponse;
    private ServerResponse<List<Employee>> testEmployeesResponse;

    @BeforeEach
    void setUp() {
        // Initialize controller advice
        controllerAdvice = new EmployeeControllerAdvice();

        // Initialize test data
        testId = UUID.randomUUID();

        testEmployee = Employee.builder()
                .id(testId)
                .name("John Doe")
                .salary(100000)
                .age(30)
                .title("Software Engineer")
                .email("john.doe@example.com")
                .build();

        Employee employee2 = Employee.builder()
                .id(UUID.randomUUID())
                .name("Jane Smith")
                .salary(120000)
                .age(35)
                .title("Senior Engineer")
                .email("jane.smith@example.com")
                .build();

        testEmployees = Arrays.asList(testEmployee, employee2);

        testEmployeeResponse = new ServerResponse<>();
        testEmployeeResponse.setData(testEmployee);
        testEmployeeResponse.setStatus("success");

        testEmployeesResponse = new ServerResponse<>();
        testEmployeesResponse.setData(testEmployees);
        testEmployeesResponse.setStatus("success");
    }

    @Test
    @DisplayName("Get all employees should return all employees")
    void getAllEmployees_ShouldReturnAllEmployees() {
        System.out.println("[DEBUG_LOG] Starting test: getAllEmployees_ShouldReturnAllEmployees");

        // Arrange
        when(employeeService.getAllEmployees()).thenReturn(testEmployeesResponse);
        System.out.println("[DEBUG_LOG] Arranged: Mock service to return " + testEmployees.size() + " employees");

        // Act
        ResponseEntity<List<Employee>> response = employeeController.getAllEmployees();
        System.out.println("[DEBUG_LOG] Action: Called getAllEmployees(), received response with status "
                + response.getStatusCode());

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testEmployees, response.getBody());
        verify(employeeService, times(1)).getAllEmployees();
        System.out.println("[DEBUG_LOG] Assertion passed: Response status is OK and contains "
                + response.getBody().size() + " employees");
    }

    @Test
    @DisplayName("Get employees by name search should return matching employees")
    void getEmployeesByNameSearch_ShouldReturnMatchingEmployees() {
        System.out.println("[DEBUG_LOG] Starting test: getEmployeesByNameSearch_ShouldReturnMatchingEmployees");

        // Arrange
        String searchString = "John";
        List<Employee> filteredEmployees = Collections.singletonList(testEmployee);
        when(employeeService.getEmployeesByNameSearch(searchString)).thenReturn(filteredEmployees);
        System.out.println("[DEBUG_LOG] Arranged: Mock service to return " + filteredEmployees.size()
                + " employees for search string '" + searchString + "'");

        // Act
        ResponseEntity<List<Employee>> response = employeeController.getEmployeesByNameSearch(searchString);
        System.out.println("[DEBUG_LOG] Action: Called getEmployeesByNameSearch('" + searchString
                + "'), received response with status " + response.getStatusCode());

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(filteredEmployees, response.getBody());
        verify(employeeService, times(1)).getEmployeesByNameSearch(searchString);
        System.out.println("[DEBUG_LOG] Assertion passed: Response status is OK and contains "
                + response.getBody().size() + " employees matching '" + searchString + "'");
    }

    @Test
    @DisplayName("Get employee by valid ID should return the employee")
    void getEmployeeById_WithValidId_ShouldReturnEmployee() {
        System.out.println("[DEBUG_LOG] Starting test: getEmployeeById_WithValidId_ShouldReturnEmployee");

        // Arrange
        String idString = testId.toString();
        when(employeeService.getEmployeeById(testId)).thenReturn(testEmployeeResponse);
        System.out.println("[DEBUG_LOG] Arranged: Mock service to return employee for ID " + idString);

        // Act
        ResponseEntity<Employee> response = employeeController.getEmployeeById(idString);
        System.out.println("[DEBUG_LOG] Action: Called getEmployeeById('" + idString
                + "'), received response with status " + response.getStatusCode());

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testEmployee, response.getBody());
        verify(employeeService, times(1)).getEmployeeById(testId);
        System.out.println("[DEBUG_LOG] Assertion passed: Response status is OK and contains employee with name '"
                + response.getBody().getName() + "'");
    }

    @Test
    @DisplayName("Get employee by invalid ID should return bad request")
    void getEmployeeById_WithInvalidId_ShouldReturnBadRequest() {
        System.out.println("[DEBUG_LOG] Starting test: getEmployeeById_WithInvalidId_ShouldReturnBadRequest");

        // Arrange
        String invalidId = "invalid-uuid";
        System.out.println("[DEBUG_LOG] Arranged: Using invalid ID '" + invalidId + "'");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            employeeController.getEmployeeById(invalidId);
        });
        System.out.println("[DEBUG_LOG] Action: Called getEmployeeById('" + invalidId
                + "'), caught IllegalArgumentException: " + exception.getMessage());

        // Handle the exception with controller advice
        ResponseEntity<?> response = controllerAdvice.handleIllegalArgumentException(exception);
        System.out.println("[DEBUG_LOG] ControllerAdvice handled the exception, received response with status "
                + response.getStatusCode());

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid UUID format.", response.getBody());
        assertEquals(
                "Invalid UUID format: " + exception.getMessage(),
                response.getHeaders().getFirst("X-Error-Message"));
        verify(employeeService, never()).getEmployeeById(any(UUID.class));
        System.out.println(
                "[DEBUG_LOG] Assertion passed: Response status is BAD_REQUEST, body contains error message, and service method was never called");
    }

    @Test
    @DisplayName("Get highest salary of employees should return the highest salary")
    void getHighestSalaryOfEmployees_ShouldReturnHighestSalary() {
        System.out.println("[DEBUG_LOG] Starting test: getHighestSalaryOfEmployees_ShouldReturnHighestSalary");

        // Arrange
        Integer highestSalary = 120000;
        when(employeeService.getHighestSalaryOfEmployees()).thenReturn(highestSalary);
        System.out.println("[DEBUG_LOG] Arranged: Mock service to return highest salary of " + highestSalary);

        // Act
        ResponseEntity<Integer> response = employeeController.getHighestSalaryOfEmployees();
        System.out.println("[DEBUG_LOG] Action: Called getHighestSalaryOfEmployees(), received response with status "
                + response.getStatusCode());

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(highestSalary, response.getBody());
        verify(employeeService, times(1)).getHighestSalaryOfEmployees();
        System.out.println(
                "[DEBUG_LOG] Assertion passed: Response status is OK and highest salary is " + response.getBody());
    }

    @Test
    @DisplayName("Get top ten highest earning employee names should return the top employee names")
    void getTopTenHighestEarningEmployeeNames_ShouldReturnTopEmployeeNames() {
        System.out.println(
                "[DEBUG_LOG] Starting test: getTopTenHighestEarningEmployeeNames_ShouldReturnTopEmployeeNames");

        // Arrange
        List<String> topEmployeeNames = Arrays.asList("Jane Smith", "John Doe");
        when(employeeService.getTopTenHighestEarningEmployeeNames()).thenReturn(topEmployeeNames);
        System.out.println(
                "[DEBUG_LOG] Arranged: Mock service to return " + topEmployeeNames.size() + " top employee names");

        // Act
        ResponseEntity<List<String>> response = employeeController.getTopTenHighestEarningEmployeeNames();
        System.out.println(
                "[DEBUG_LOG] Action: Called getTopTenHighestEarningEmployeeNames(), received response with status "
                        + response.getStatusCode());

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(topEmployeeNames, response.getBody());
        verify(employeeService, times(1)).getTopTenHighestEarningEmployeeNames();
        System.out.println("[DEBUG_LOG] Assertion passed: Response status is OK and contains "
                + response.getBody().size() + " top employee names: " + String.join(", ", response.getBody()));
    }

    @Test
    @DisplayName("Create employee with valid input should return created employee")
    void createEmployee_WithValidInput_ShouldReturnCreatedEmployee() {
        System.out.println("[DEBUG_LOG] Starting test: createEmployee_WithValidInput_ShouldReturnCreatedEmployee");

        // Arrange
        CreateEmployeeInput input = new CreateEmployeeInput();
        input.setName("John Doe");
        input.setSalary(100000);
        input.setAge(30);
        input.setTitle("Software Engineer");

        when(employeeService.createEmployee(input)).thenReturn(testEmployee);
        System.out.println(
                "[DEBUG_LOG] Arranged: Mock service to return employee for input with name '" + input.getName() + "'");

        // Act
        ResponseEntity<Employee> response = employeeController.createEmployee(input);
        System.out.println("[DEBUG_LOG] Action: Called createEmployee(), received response with status "
                + response.getStatusCode());

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testEmployee, response.getBody());
        verify(employeeService, times(1)).createEmployee(input);
        System.out.println("[DEBUG_LOG] Assertion passed: Response status is OK and contains employee with name '"
                + response.getBody().getName() + "'");
    }

    @Test
    @DisplayName("Create employee with service exception should return bad request with error message in header")
    void createEmployee_WithServiceException_ShouldReturnBadRequest() {
        System.out.println("[DEBUG_LOG] Starting test: createEmployee_WithServiceException_ShouldReturnBadRequest");

        // Arrange
        CreateEmployeeInput input = new CreateEmployeeInput();
        input.setName("John Doe");
        String errorMessage = "Service error";

        when(employeeService.createEmployee(input)).thenThrow(new RuntimeException(errorMessage));
        System.out.println(
                "[DEBUG_LOG] Arranged: Mock service to throw RuntimeException with message '" + errorMessage + "'");

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            employeeController.createEmployee(input);
        });
        System.out.println(
                "[DEBUG_LOG] Action: Called createEmployee(), caught RuntimeException: " + exception.getMessage());

        // Handle the exception with the controller advice
        ResponseEntity<?> response = controllerAdvice.handleException(exception);
        System.out.println("[DEBUG_LOG] ControllerAdvice handled the exception, received response with status "
                + response.getStatusCode());

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        assertEquals(
                "Error processing request: " + errorMessage,
                response.getHeaders().getFirst("X-Error-Message"));
        verify(employeeService, times(1)).createEmployee(input);
        System.out.println(
                "[DEBUG_LOG] Assertion passed: Response status is BAD_REQUEST, body is null, and header contains error message: '"
                        + response.getHeaders().getFirst("X-Error-Message") + "'");
    }

    @Test
    @DisplayName("Create employee with null response should throw EmployeeCreationException")
    void createEmployee_WithNullResponse_ShouldReturnBadRequest() {
        System.out.println("[DEBUG_LOG] Starting test: createEmployee_WithNullResponse_ShouldReturnBadRequest");

        // Arrange
        CreateEmployeeInput input = new CreateEmployeeInput();
        input.setName("John Doe");

        when(employeeService.createEmployee(input)).thenReturn(null);
        System.out.println("[DEBUG_LOG] Arranged: Mock service to return null");

        // Act & Assert
        EmployeeCreationException exception = assertThrows(EmployeeCreationException.class, () -> {
            employeeController.createEmployee(input);
        });
        System.out.println("[DEBUG_LOG] Action: Called createEmployee(), caught EmployeeCreationException: "
                + exception.getMessage());

        // Handle the exception with the controller advice
        ResponseEntity<?> response = controllerAdvice.handleEmployeeCreationException(exception);
        System.out.println("[DEBUG_LOG] ControllerAdvice handled the exception, received response with status "
                + response.getStatusCode());

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(
                "Failed to create employee: " + exception.getMessage(),
                response.getHeaders().getFirst("X-Error-Message"));
        verify(employeeService, times(1)).createEmployee(input);
        System.out.println(
                "[DEBUG_LOG] Assertion passed: Response status is BAD_REQUEST, body is null, and header contains error message: '"
                        + response.getHeaders().getFirst("X-Error-Message") + "'");
    }

    @Test
    @DisplayName("Delete employee by ID with successful deletion should return employee name")
    void deleteEmployeeById_WithSuccessfulDeletion_ShouldReturnSuccessMessage() {
        System.out.println(
                "[DEBUG_LOG] Starting test: deleteEmployeeById_WithSuccessfulDeletion_ShouldReturnSuccessMessage");

        // Arrange
        String idString = testId.toString();
        String employeeName = "John Doe";
        when(employeeService.deleteEmployeeById(any(UUID.class))).thenReturn(employeeName);
        System.out.println("[DEBUG_LOG] Arranged: Mock service to return employee name for ID " + idString);

        // Act
        ResponseEntity<String> response = employeeController.deleteEmployeeById(idString);
        System.out.println("[DEBUG_LOG] Action: Called deleteEmployeeById('" + idString
                + "'), received response with status " + response.getStatusCode());

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(employeeName, response.getBody());
        verify(employeeService, times(1)).deleteEmployeeById(any(UUID.class));
        System.out.println("[DEBUG_LOG] Assertion passed: Response status is OK and contains employee name: '"
                + response.getBody() + "'");
    }

    @Test
    @DisplayName("Delete employee by ID with failed deletion should return bad request with error message")
    void deleteEmployeeById_WithFailedDeletion_ShouldReturnBadRequest() {
        System.out.println("[DEBUG_LOG] Starting test: deleteEmployeeById_WithFailedDeletion_ShouldReturnBadRequest");

        // Arrange
        String idString = testId.toString();
        String failureMessage = "Failed to delete employee.";
        when(employeeService.deleteEmployeeById(any(UUID.class))).thenReturn(failureMessage);
        System.out.println("[DEBUG_LOG] Arranged: Mock service to return failure message for ID " + idString);

        // Act
        ResponseEntity<String> response = employeeController.deleteEmployeeById(idString);
        System.out.println("[DEBUG_LOG] Action: Called deleteEmployeeById('" + idString
                + "'), received response with status " + response.getStatusCode());

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(failureMessage, response.getBody());
        verify(employeeService, times(1)).deleteEmployeeById(any(UUID.class));
        System.out.println(
                "[DEBUG_LOG] Assertion passed: Response status is BAD_REQUEST and body contains error message: '"
                        + response.getBody() + "'");
    }

    @Test
    @DisplayName("Delete employee by ID with invalid UUID should return bad request with error message")
    void deleteEmployeeById_WithInvalidUUID_ShouldReturnBadRequest() {
        System.out.println("[DEBUG_LOG] Starting test: deleteEmployeeById_WithInvalidUUID_ShouldReturnBadRequest");

        // Arrange
        String invalidId = "invalid-uuid";
        System.out.println("[DEBUG_LOG] Arranged: Using invalid UUID: " + invalidId);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            employeeController.deleteEmployeeById(invalidId);
        });
        System.out.println("[DEBUG_LOG] Action: Called deleteEmployeeById('" + invalidId
                + "'), caught IllegalArgumentException: " + exception.getMessage());

        // Handle the exception with the controller advice
        ResponseEntity<?> response = controllerAdvice.handleIllegalArgumentException(exception);
        System.out.println("[DEBUG_LOG] ControllerAdvice handled the exception, received response with status "
                + response.getStatusCode());

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid UUID format.", response.getBody());
        assertEquals(
                "Invalid UUID format: " + exception.getMessage(),
                response.getHeaders().getFirst("X-Error-Message"));
        verify(employeeService, never()).deleteEmployeeById(any(UUID.class));
        System.out.println(
                "[DEBUG_LOG] Assertion passed: Response status is BAD_REQUEST, body contains error message: '"
                        + response.getBody() + "', and service method was never called");
    }
}
