package com.ombits.leavemanagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ombits.leavemanagement.config.JwtAuthFilter;
import com.ombits.leavemanagement.config.SecurityConfig;
import com.ombits.leavemanagement.controller.EmployeeController;
import com.ombits.leavemanagement.dto.CreateEmployeeRequest;
import com.ombits.leavemanagement.dto.EmployeeDto;
import com.ombits.leavemanagement.security.CustomUserDetailsService;
import com.ombits.leavemanagement.security.JwtTokenProvider;
import com.ombits.leavemanagement.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = EmployeeController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class},
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {SecurityConfig.class, JwtAuthFilter.class})
)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void testCreateEmployee_Success() throws Exception {
        CreateEmployeeRequest request = new CreateEmployeeRequest("Alice Wonderland", "alice@ombits.com", "QA");
        EmployeeDto dto = new EmployeeDto(1L, "Alice Wonderland", "alice@ombits.com", "QA", LocalDateTime.now());

        when(employeeService.createEmployee(any(CreateEmployeeRequest.class))).thenReturn(dto);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Alice Wonderland"))
                .andExpect(jsonPath("$.email").value("alice@ombits.com"))
                .andExpect(jsonPath("$.department").value("QA"));
    }

    @Test
    void testCreateEmployee_InvalidEmail_ReturnsBadRequest() throws Exception {
        CreateEmployeeRequest invalidRequest = new CreateEmployeeRequest("Alice Wonderland", "invalid-email-address", "QA");

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.email").exists());
    }

    @Test
    void testCreateEmployee_BlankName_ReturnsBadRequest() throws Exception {
        CreateEmployeeRequest invalidRequest = new CreateEmployeeRequest("", "alice@ombits.com", "QA");

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.name").exists());
    }

    @Test
    void testGetAllEmployees_Success() throws Exception {
        List<EmployeeDto> list = List.of(
                new EmployeeDto(1L, "Alice", "alice@ombits.com", "QA", LocalDateTime.now()),
                new EmployeeDto(2L, "Bob", "bob@ombits.com", "Dev", LocalDateTime.now())
        );

        when(employeeService.getAllEmployees()).thenReturn(list);

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Alice"))
                .andExpect(jsonPath("$[1].name").value("Bob"));
    }
}
