package com.elm.system.service;

import com.elm.system.entity.*;
import com.elm.system.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DataInitializationService implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializationService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    @Autowired
    private LeavePolicyRepository leavePolicyRepository;

    @Autowired
    private LeaveBalanceRepository leaveBalanceRepository;

    @Autowired
    private LeaveApplicationRepository leaveApplicationRepository;

    @Autowired
    private LeaveStatusHistoryRepository leaveStatusHistoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            logger.info("Database already contains data. Skipping initial seeding.");
            return;
        }

        logger.info("Initializing sample data for Employee Leave Management System...");

        String passwordHash = passwordEncoder.encode("admin123");

        // 1. Users
        User adminUser = userRepository.save(new User("admin@company.com", passwordHash, "ROLE_ADMIN", true));
        User managerUser = userRepository.save(new User("manager@company.com", passwordHash, "ROLE_MANAGER", true));
        User empUser1 = userRepository.save(new User("john.doe@company.com", passwordHash, "ROLE_EMPLOYEE", true));
        User empUser2 = userRepository.save(new User("jane.smith@company.com", passwordHash, "ROLE_EMPLOYEE", true));
        User empUser3 = userRepository.save(new User("alex.jones@company.com", passwordHash, "ROLE_EMPLOYEE", true));

        // 2. Departments
        Department deptEng = departmentRepository.save(new Department("Engineering", "Software Development, QA, and Infrastructure", null));
        Department deptHr = departmentRepository.save(new Department("Human Resources", "Recruitment and People Operations", null));
        Department deptFin = departmentRepository.save(new Department("Finance & Operations", "Accounting and Budget Planning", null));

        // 3. Employees
        Employee adminEmp = new Employee();
        adminEmp.setEmployeeCode("EMP001");
        adminEmp.setUser(adminUser);
        adminEmp.setFullName("System Administrator");
        adminEmp.setPhone("+1-555-0101");
        adminEmp.setDepartment(deptHr);
        adminEmp.setDesignation("HR Administrator");
        adminEmp.setJoiningDate(LocalDate.of(2023, 1, 15));
        adminEmp.setEmploymentStatus("ACTIVE");
        adminEmp = employeeRepository.save(adminEmp);

        Employee managerEmp = new Employee();
        managerEmp.setEmployeeCode("EMP002");
        managerEmp.setUser(managerUser);
        managerEmp.setFullName("Robert Taylor");
        managerEmp.setPhone("+1-555-0102");
        managerEmp.setDepartment(deptEng);
        managerEmp.setDesignation("Engineering Manager");
        managerEmp.setJoiningDate(LocalDate.of(2023, 3, 1));
        managerEmp.setEmploymentStatus("ACTIVE");
        managerEmp = employeeRepository.save(managerEmp);

        // Assign managers to departments
        deptEng.setManagerId(managerEmp.getId());
        departmentRepository.save(deptEng);
        deptHr.setManagerId(adminEmp.getId());
        departmentRepository.save(deptHr);

        // Employees reporting to manager Robert Taylor
        Employee emp1 = new Employee();
        emp1.setEmployeeCode("EMP003");
        emp1.setUser(empUser1);
        emp1.setFullName("John Doe");
        emp1.setPhone("+1-555-0103");
        emp1.setDepartment(deptEng);
        emp1.setManager(managerEmp);
        emp1.setDesignation("Senior Software Engineer");
        emp1.setJoiningDate(LocalDate.of(2023, 6, 15));
        emp1.setEmploymentStatus("ACTIVE");
        emp1 = employeeRepository.save(emp1);

        Employee emp2 = new Employee();
        emp2.setEmployeeCode("EMP004");
        emp2.setUser(empUser2);
        emp2.setFullName("Jane Smith");
        emp2.setPhone("+1-555-0104");
        emp2.setDepartment(deptEng);
        emp2.setManager(managerEmp);
        emp2.setDesignation("QA Automation Engineer");
        emp2.setJoiningDate(LocalDate.of(2024, 1, 10));
        emp2.setEmploymentStatus("ACTIVE");
        emp2 = employeeRepository.save(emp2);

        Employee emp3 = new Employee();
        emp3.setEmployeeCode("EMP005");
        emp3.setUser(empUser3);
        emp3.setFullName("Alex Jones");
        emp3.setPhone("+1-555-0105");
        emp3.setDepartment(deptEng);
        emp3.setManager(managerEmp);
        emp3.setDesignation("Frontend Developer");
        emp3.setJoiningDate(LocalDate.of(2024, 4, 1));
        emp3.setEmploymentStatus("ACTIVE");
        emp3 = employeeRepository.save(emp3);

        // 4. Leave Types
        LeaveType casualLeave = leaveTypeRepository.save(new LeaveType("Casual Leave", "Short leave for personal errands or emergencies", 12, true, true));
        LeaveType sickLeave = leaveTypeRepository.save(new LeaveType("Sick Leave", "Medical rest and doctor appointments", 10, true, true));
        LeaveType earnedLeave = leaveTypeRepository.save(new LeaveType("Earned Leave", "Paid privilege leave accrued annually", 15, true, true));
        LeaveType parentalLeave = leaveTypeRepository.save(new LeaveType("Maternity/Paternity Leave", "Parental leave for newborn child care", 30, true, true));
        LeaveType unpaidLeave = leaveTypeRepository.save(new LeaveType("Unpaid Leave (LWP)", "Leave without pay for extended absence", 30, false, true));

        // 5. Leave Policies
        createPolicy(casualLeave, "ANNUAL", false, 0, true);
        createPolicy(sickLeave, "ANNUAL", false, 0, true);
        createPolicy(earnedLeave, "ANNUAL", true, 5, true);
        createPolicy(parentalLeave, "ANNUAL", false, 0, true);
        createPolicy(unpaidLeave, "ANNUAL", false, 0, true);

        // 6. Balances for 2026
        String period = "2026";
        Employee[] allEmps = {adminEmp, managerEmp, emp1, emp2, emp3};
        for (Employee e : allEmps) {
            leaveBalanceRepository.save(new LeaveBalance(e, casualLeave, period, 12.0, (e == emp1 ? 3.0 : (e == emp2 ? 1.0 : 0.0)), 0.0));
            leaveBalanceRepository.save(new LeaveBalance(e, sickLeave, period, 10.0, (e == emp1 ? 1.0 : 0.0), 0.0));
            leaveBalanceRepository.save(new LeaveBalance(e, earnedLeave, period, 15.0, (e == emp1 ? 5.0 : 0.0), (e == emp1 ? 2.0 : 0.0)));
            leaveBalanceRepository.save(new LeaveBalance(e, parentalLeave, period, 30.0, 0.0, 0.0));
            leaveBalanceRepository.save(new LeaveBalance(e, unpaidLeave, period, 30.0, 0.0, 0.0));
        }

        // 7. Leave Applications
        // John Doe: Approved Sick Leave
        LeaveApplication app1 = new LeaveApplication();
        app1.setEmployee(emp1);
        app1.setLeaveType(sickLeave);
        app1.setStartDate(LocalDate.now().minusDays(15));
        app1.setEndDate(LocalDate.now().minusDays(15));
        app1.setLeaveDays(1.0);
        app1.setReason("Doctor consultation and recovery");
        app1.setStatus("APPROVED");
        app1.setSubmittedAt(LocalDateTime.now().minusDays(18));
        app1.setDecisionAt(LocalDateTime.now().minusDays(17));
        app1.setApprover(managerEmp);
        app1.setApproverComments("Approved. Take rest.");
        app1 = leaveApplicationRepository.save(app1);
        leaveStatusHistoryRepository.save(new LeaveStatusHistory(app1, null, "PENDING", emp1.getUser().getEmail(), "Submitted"));
        leaveStatusHistoryRepository.save(new LeaveStatusHistory(app1, "PENDING", "APPROVED", managerEmp.getUser().getEmail(), "Approved. Take rest."));

        // John Doe: Approved Casual Leave
        LeaveApplication app2 = new LeaveApplication();
        app2.setEmployee(emp1);
        app2.setLeaveType(casualLeave);
        app2.setStartDate(LocalDate.now().minusDays(5));
        app2.setEndDate(LocalDate.now().minusDays(3));
        app2.setLeaveDays(3.0);
        app2.setReason("Family wedding function");
        app2.setStatus("APPROVED");
        app2.setSubmittedAt(LocalDateTime.now().minusDays(10));
        app2.setDecisionAt(LocalDateTime.now().minusDays(9));
        app2.setApprover(managerEmp);
        app2.setApproverComments("Approved. Have fun.");
        app2 = leaveApplicationRepository.save(app2);
        leaveStatusHistoryRepository.save(new LeaveStatusHistory(app2, null, "PENDING", emp1.getUser().getEmail(), "Submitted"));
        leaveStatusHistoryRepository.save(new LeaveStatusHistory(app2, "PENDING", "APPROVED", managerEmp.getUser().getEmail(), "Approved. Have fun."));

        // Jane Smith: Approved Casual Leave
        LeaveApplication app3 = new LeaveApplication();
        app3.setEmployee(emp2);
        app3.setLeaveType(casualLeave);
        app3.setStartDate(LocalDate.now().minusDays(2));
        app3.setEndDate(LocalDate.now().minusDays(2));
        app3.setLeaveDays(1.0);
        app3.setReason("Personal bank appointment");
        app3.setStatus("APPROVED");
        app3.setSubmittedAt(LocalDateTime.now().minusDays(4));
        app3.setDecisionAt(LocalDateTime.now().minusDays(3));
        app3.setApprover(managerEmp);
        app3.setApproverComments("Approved.");
        app3 = leaveApplicationRepository.save(app3);
        leaveStatusHistoryRepository.save(new LeaveStatusHistory(app3, null, "PENDING", emp2.getUser().getEmail(), "Submitted"));
        leaveStatusHistoryRepository.save(new LeaveStatusHistory(app3, "PENDING", "APPROVED", managerEmp.getUser().getEmail(), "Approved."));

        // John Doe: Pending upcoming leave
        LeaveApplication app4 = new LeaveApplication();
        app4.setEmployee(emp1);
        app4.setLeaveType(earnedLeave);
        app4.setStartDate(LocalDate.now().plusDays(10));
        app4.setEndDate(LocalDate.now().plusDays(14));
        app4.setLeaveDays(5.0);
        app4.setReason("Planned vacation trip with family");
        app4.setStatus("PENDING");
        app4.setSubmittedAt(LocalDateTime.now().minusDays(1));
        app4 = leaveApplicationRepository.save(app4);
        leaveStatusHistoryRepository.save(new LeaveStatusHistory(app4, null, "PENDING", emp1.getUser().getEmail(), "Submitted"));

        // Jane Smith: Pending upcoming leave
        LeaveApplication app5 = new LeaveApplication();
        app5.setEmployee(emp2);
        app5.setLeaveType(casualLeave);
        app5.setStartDate(LocalDate.now().plusDays(5));
        app5.setEndDate(LocalDate.now().plusDays(6));
        app5.setLeaveDays(2.0);
        app5.setReason("Attending graduation ceremony");
        app5.setStatus("PENDING");
        app5.setSubmittedAt(LocalDateTime.now());
        app5 = leaveApplicationRepository.save(app5);
        leaveStatusHistoryRepository.save(new LeaveStatusHistory(app5, null, "PENDING", emp2.getUser().getEmail(), "Submitted"));

        // Alex Jones: Rejected leave
        LeaveApplication app6 = new LeaveApplication();
        app6.setEmployee(emp3);
        app6.setLeaveType(casualLeave);
        app6.setStartDate(LocalDate.now().minusDays(8));
        app6.setEndDate(LocalDate.now().minusDays(5));
        app6.setLeaveDays(4.0);
        app6.setReason("Vacation during sprint release");
        app6.setStatus("REJECTED");
        app6.setSubmittedAt(LocalDateTime.now().minusDays(12));
        app6.setDecisionAt(LocalDateTime.now().minusDays(11));
        app6.setApprover(managerEmp);
        app6.setApproverComments("Sprint release week. Please reschedule after release.");
        app6 = leaveApplicationRepository.save(app6);
        leaveStatusHistoryRepository.save(new LeaveStatusHistory(app6, null, "PENDING", emp3.getUser().getEmail(), "Submitted"));
        leaveStatusHistoryRepository.save(new LeaveStatusHistory(app6, "PENDING", "REJECTED", managerEmp.getUser().getEmail(), "Sprint release week. Please reschedule after release."));

        logger.info("Sample data initialization completed successfully.");
    }

    private void createPolicy(LeaveType type, String period, boolean carryForward, int maxCarryForward, boolean approvalRequired) {
        LeavePolicy p = new LeavePolicy();
        p.setLeaveType(type);
        p.setAllocationPeriod(period);
        p.setCarryForwardAllowed(carryForward);
        p.setMaximumCarryForward(maxCarryForward);
        p.setApprovalRequired(approvalRequired);
        p.setActive(true);
        leavePolicyRepository.save(p);
    }
}
