package com.elm.system;

import com.elm.system.dto.*;
import com.elm.system.entity.Employee;
import com.elm.system.entity.LeaveBalance;
import com.elm.system.entity.LeaveType;
import com.elm.system.exception.BadRequestException;
import com.elm.system.repository.EmployeeRepository;
import com.elm.system.repository.LeaveTypeRepository;
import com.elm.system.security.JwtUtils;
import com.elm.system.service.AuthService;
import com.elm.system.service.EmployeeService;
import com.elm.system.service.LeaveApplicationService;
import com.elm.system.service.LeaveBalanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class ApplicationTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private LeaveApplicationService leaveApplicationService;

    @Autowired
    private LeaveBalanceService leaveBalanceService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    @Autowired
    private UserDetailsService userDetailsService;

    private void authenticateUser(String email) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        UsernamePasswordAuthenticationToken auth = 
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void testContextLoads() {
        assertNotNull(authService);
        assertNotNull(leaveApplicationService);
    }

    @Test
    void testSuccessfulLogin() {
        LoginRequest req = new LoginRequest("admin@company.com", "admin123");
        AuthResponse res = authService.login(req);
        assertNotNull(res.getToken());
        assertEquals("ROLE_ADMIN", res.getRole());
        assertEquals("admin@company.com", res.getEmail());
    }

    @Test
    void testInvalidPasswordThrowsException() {
        LoginRequest req = new LoginRequest("admin@company.com", "wrongpassword");
        assertThrows(BadCredentialsException.class, () -> authService.login(req));
    }

    @Test
    void testEmployeeLeaveBalanceAndApplication() {
        authenticateUser("john.doe@company.com");
        Employee john = employeeRepository.findByUserEmail("john.doe@company.com").orElseThrow();
        LeaveType casual = leaveTypeRepository.findByName("Casual Leave").orElseThrow();

        // Check John has balances
        List<LeaveBalanceResponse> balances = leaveBalanceService.getBalancesForEmployee(john.getId());
        assertFalse(balances.isEmpty());

        // Apply for 2 days leave
        LocalDate start = LocalDate.now().plusDays(20);
        LocalDate end = LocalDate.now().plusDays(21);

        LeaveApplicationRequest req = new LeaveApplicationRequest();
        req.setLeaveTypeId(casual.getId());
        req.setStartDate(start);
        req.setEndDate(end);
        req.setReason("Doctor checkup");

        LeaveApplicationResponse app = leaveApplicationService.applyForLeave(req);
        assertNotNull(app.getId());
        assertEquals("PENDING", app.getStatus());

        // Overlapping leave test: submitting same dates should fail
        LeaveApplicationRequest overlapReq = new LeaveApplicationRequest();
        overlapReq.setLeaveTypeId(casual.getId());
        overlapReq.setStartDate(start);
        overlapReq.setEndDate(end);
        overlapReq.setReason("Duplicate dates");

        assertThrows(BadRequestException.class, () -> leaveApplicationService.applyForLeave(overlapReq));
    }

    @Test
    void testEmployeeCannotApproveOwnLeave() {
        authenticateUser("john.doe@company.com");
        Employee john = employeeRepository.findByUserEmail("john.doe@company.com").orElseThrow();
        LeaveType casual = leaveTypeRepository.findByName("Casual Leave").orElseThrow();

        LeaveApplicationRequest req = new LeaveApplicationRequest();
        req.setLeaveTypeId(casual.getId());
        req.setStartDate(LocalDate.now().plusDays(25));
        req.setEndDate(LocalDate.now().plusDays(26));
        req.setReason("Personal trip");

        LeaveApplicationResponse app = leaveApplicationService.applyForLeave(req);

        // Try to approve as John Doe
        assertThrows(BadRequestException.class, () -> 
                leaveApplicationService.approveLeave(app.getId(), new LeaveApprovalRequest("Self approving")));
    }

    @Test
    void testManagerCanApproveTeamLeaveAndDeductBalance() {
        authenticateUser("jane.smith@company.com");
        Employee jane = employeeRepository.findByUserEmail("jane.smith@company.com").orElseThrow();
        LeaveType sick = leaveTypeRepository.findByName("Sick Leave").orElseThrow();

        LeaveApplicationRequest req = new LeaveApplicationRequest();
        req.setLeaveTypeId(sick.getId());
        req.setStartDate(LocalDate.now().plusDays(30));
        req.setEndDate(LocalDate.now().plusDays(31));
        req.setReason("Dental surgery");

        LeaveApplicationResponse app = leaveApplicationService.applyForLeave(req);
        assertEquals("PENDING", app.getStatus());

        LeaveBalance beforeBalance = leaveBalanceService.getBalanceEntity(jane.getId(), sick.getId());
        double usedBefore = beforeBalance.getUsedDays();

        // Switch user to manager Robert Taylor
        authenticateUser("manager@company.com");
        LeaveApplicationResponse approved = leaveApplicationService.approveLeave(app.getId(), new LeaveApprovalRequest("Get well soon"));
        assertEquals("APPROVED", approved.getStatus());

        LeaveBalance afterBalance = leaveBalanceService.getBalanceEntity(jane.getId(), sick.getId());
        assertEquals(usedBefore + approved.getLeaveDays(), afterBalance.getUsedDays());
    }

    @Test
    void testCancelApprovedLeaveRestoresBalance() {
        authenticateUser("alex.jones@company.com");
        Employee alex = employeeRepository.findByUserEmail("alex.jones@company.com").orElseThrow();
        LeaveType casual = leaveTypeRepository.findByName("Casual Leave").orElseThrow();

        LeaveApplicationRequest req = new LeaveApplicationRequest();
        req.setLeaveTypeId(casual.getId());
        req.setStartDate(LocalDate.now().plusDays(40));
        req.setEndDate(LocalDate.now().plusDays(41));
        req.setReason("Family event");

        LeaveApplicationResponse app = leaveApplicationService.applyForLeave(req);

        // Approve as manager
        authenticateUser("manager@company.com");
        leaveApplicationService.approveLeave(app.getId(), new LeaveApprovalRequest("Approved"));

        LeaveBalance balanceAfterApproval = leaveBalanceService.getBalanceEntity(alex.getId(), casual.getId());
        double usedAfterApproval = balanceAfterApproval.getUsedDays();

        // Cancel as Alex
        authenticateUser("alex.jones@company.com");
        LeaveApplicationResponse cancelled = leaveApplicationService.cancelApplication(app.getId());
        assertEquals("CANCELLED", cancelled.getStatus());

        LeaveBalance balanceAfterCancel = leaveBalanceService.getBalanceEntity(alex.getId(), casual.getId());
        assertEquals(usedAfterApproval - app.getLeaveDays(), balanceAfterCancel.getUsedDays());
    }
}
