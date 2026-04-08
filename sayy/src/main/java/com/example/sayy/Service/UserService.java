package com.example.sayy.Service;

import com.example.sayy.Mapper.EmployeeMapper;
import com.example.sayy.Mapper.UserMapper;
import com.example.sayy.Entity.EmployeeEntity;
import com.example.sayy.Entity.EmploymentType;
import com.example.sayy.Entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;
    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void createAdminIfMissing(String adminUsername, String adminRawPassword) {
        if (userMapper.existsByUsername(adminUsername) == 1) {
            return;
        }

        UserEntity admin = UserEntity.builder()
                .username(adminUsername)
                .pwd(passwordEncoder.encode(adminRawPassword))
                .role("ADMIN")
                .enabled(true)
                .employeeNo(null)
                .build();

        userMapper.insert(admin);
    }

    @Transactional
    public IssuedAccount issueAccountForEmployee(String empNo) {
        EmployeeEntity employee = employeeMapper.selectWithUserByEmpNo(empNo);
        if (employee == null) {
            throw new IllegalArgumentException("Employee not found: " + empNo);
        }

        if (employee.getEmploymentType() != EmploymentType.REGULAR) {
            throw new IllegalStateException("계정 발급은 정규직만 가능합니다.");
        }

        if (employee.getUser() != null && employee.getUser().getUsername() != null) {
            throw new IllegalStateException("Account already issued for empNo=" + empNo);
        }
        String usernameToUse = (employee.getLoginUsername() != null && !employee.getLoginUsername().isBlank())
                ? employee.getLoginUsername().trim()
                : empNo;
        if (userMapper.existsByUsername(usernameToUse) == 1) {
            throw new IllegalStateException("Username already exists: " + usernameToUse);
        }

        String rawPassword = generateInitialPassword(10);
        UserEntity user = UserEntity.builder()
                .username(usernameToUse) // employees.login_username가 있으면 우선 사용, 없으면 empNo 사용
                .pwd(passwordEncoder.encode(rawPassword))
                .role("USER")
                .enabled(true)
                .employeeNo(empNo)
                .build();

        userMapper.insert(user);

        return new IssuedAccount(usernameToUse, rawPassword);
    }

    /**
     * 엑셀 업로드 등 "지정한 username"으로 계정을 생성합니다.
     * - employee_no(사번)와 연결하여 users.employee_no FK/UNIQUE 정책을 따릅니다.
     */
    @Transactional
    public IssuedAccount createAccountForEmployee(String username, String empNo) {
        if (empNo == null || empNo.isBlank()) throw new IllegalArgumentException("empNo is required");
        if (username == null || username.isBlank()) throw new IllegalArgumentException("username is required");

        String normalizedEmpNo = empNo.trim();
        String normalizedUsername = username.trim();

        EmployeeEntity employee = employeeMapper.selectWithUserByEmpNo(normalizedEmpNo);
        if (employee == null) {
            throw new IllegalArgumentException("Employee not found: " + normalizedEmpNo);
        }
        if (employee.getUser() != null && employee.getUser().getUsername() != null) {
            throw new IllegalStateException("Account already issued for empNo=" + normalizedEmpNo);
        }
        if (userMapper.existsByUsername(normalizedUsername) == 1) {
            throw new IllegalStateException("Username already exists: " + normalizedUsername);
        }

        String rawPassword = generateInitialPassword(10);
        UserEntity user = UserEntity.builder()
                .username(normalizedUsername)
                .pwd(passwordEncoder.encode(rawPassword))
                .role("USER")
                .enabled(true)
                .employeeNo(normalizedEmpNo)
                .build();

        userMapper.insert(user);
        return new IssuedAccount(normalizedUsername, rawPassword);
    }

    private String generateInitialPassword(int length) {
        final String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
    }
        return sb.toString();
    }

    public record IssuedAccount(String username, String initialPassword) {}
}