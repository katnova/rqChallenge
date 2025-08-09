package com.reliaquest.api.service;

import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.ServerResponse;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.springframework.stereotype.Service;

/**
 * Service responsible for caching employee data to reduce load on the backend API.
 * Uses Cache2k to store employee information with a configured expiration time.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeCacheService {

    private final EmployeeRestClient employeeRestClient;
    private Cache<UUID, Employee> employeeCache;

    /**
     * Initializes the employee cache.
     * This method is automatically called after dependency injection is complete.
     * Sets up a Cache2k cache for Employee objects with UUID keys and a 91-second expiration time.
     */
    @PostConstruct
    public void initCache() {
        log.info("Initializing employee cache");
        employeeCache = Cache2kBuilder.of(UUID.class, Employee.class)
                .expireAfterWrite(91, TimeUnit.SECONDS)
                .build();
        log.debug("Employee cache initialized with 91 second expiration time");
    }

    /**
     * Retrieves all employees, either from cache or from the backend API.
     *
     * If the cache contains employee data, returns the cached data.
     * If the cache is empty, fetches data from the API via the EmployeeRestClient,
     * stores each employee in the cache, and then returns the response.
     *
     * @return A ServerResponse containing a list of all employees and a status
     */
    public ServerResponse<List<Employee>> getAllEmployees() {
        // if cache is not empty, return from cache
        if (!employeeCache.asMap().isEmpty()) {
            log.debug("Cache hit: Returning all employees from cache");
            List<Employee> employees = new ArrayList<>(employeeCache.asMap().values());

            ServerResponse<List<Employee>> response = new ServerResponse<>();
            response.setData(employees);
            response.setStatus("SUCCESS");
            return response;
        }

        // cache is empty, fetch from API and cache individuals
        log.debug("Cache miss: Fetching all employees from API");
        ServerResponse<List<Employee>> response = employeeRestClient.getAllEmployees();

        if (response.getData() != null) {
            log.debug("Caching {} employees", response.getData().size());
            response.getData().forEach(employee -> {
                if (employee.getId() != null) {
                    employeeCache.put(employee.getId(), employee);
                }
            });
        }

        return response;
    }

    /**
     * Retrieves an employee by ID, either from cache or from the backend API.
     *
     * If the employee with the specified ID is in the cache, returns the cached data.
     * If not in cache, fetches the employee from the API via the EmployeeRestClient,
     * stores the employee in the cache if found, and then returns the response.
     *
     * @param id The UUID of the employee to retrieve
     * @return A ServerResponse containing the employee data and a status
     */
    public ServerResponse<Employee> getEmployeeById(final UUID id) {
        Employee cached = employeeCache.get(id);
        if (cached != null) {
            log.debug("Cache hit: Found employee with id: {}", id);
            ServerResponse<Employee> response = new ServerResponse<>();
            response.setData(cached);
            response.setStatus("SUCCESS");
            return response;
        }

        log.debug("Cache miss: Fetching employee with id: {} from API", id);
        ServerResponse<Employee> response = employeeRestClient.getEmployeeById(id);
        if (response.getData() != null) {
            log.debug("Caching employee with id: {}", id);
            employeeCache.put(id, response.getData());
        }
        return response;
    }

    /**
     * Adds or updates an employee in the cache.
     *
     * This method stores the provided employee object in the cache,
     * using the employee's ID as the key. If an employee with the same ID
     * already exists in the cache, it will be replaced with the new data.
     *
     * @param employee The employee object to store in the cache
     */
    public void putEmployee(final Employee employee) {
        log.debug("Adding/updating employee with id: {} in cache", employee.getId());
        employeeCache.put(employee.getId(), employee);
    }

    /**
     * Removes an employee from the cache.
     *
     * This method removes the employee with the specified ID from the cache.
     * If no employee with the given ID exists in the cache, this operation has no effect.
     *
     * @param id The UUID of the employee to remove from the cache
     */
    public void evictEmployee(final UUID id) {
        log.debug("Removing employee with id: {} from cache", id);
        employeeCache.remove(id);
    }
}
