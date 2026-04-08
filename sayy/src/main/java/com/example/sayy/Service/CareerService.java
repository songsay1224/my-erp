package com.example.sayy.Service;

import com.example.sayy.Entity.CareerEntity;
import com.example.sayy.Entity.CertificateEntity;
import com.example.sayy.Entity.EducationEntity;
import com.example.sayy.Mapper.CareerMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class CareerService {

    private final CareerMapper careerMapper;

    public CareerService(CareerMapper careerMapper) {
        this.careerMapper = careerMapper;
    }

    // ===== 경력 =====

    public List<CareerEntity> getCareers(String empNo) {
        return careerMapper.selectCareersByEmpNo(empNo);
    }

    @Transactional
    public void addCareer(String empNo, String companyName, String department, String position,
                          LocalDate startDate, LocalDate endDate, String description) {
        if (empNo == null || empNo.isBlank()) throw new IllegalArgumentException("empNo is required");
        if (companyName == null || companyName.isBlank()) throw new IllegalArgumentException("회사명은 필수입니다.");
        if (startDate == null) throw new IllegalArgumentException("입사일은 필수입니다.");
        CareerEntity e = new CareerEntity();
        e.setEmpNo(empNo.trim());
        e.setCompanyName(companyName.trim());
        e.setDepartment(trim(department));
        e.setPosition(trim(position));
        e.setStartDate(startDate);
        e.setEndDate(endDate);
        e.setDescription(trim(description));
        e.setSortOrder(0);
        careerMapper.insertCareer(e);
    }

    @Transactional
    public void deleteCareer(String empNo, Long id) {
        careerMapper.deleteCareer(id, empNo);
    }

    // ===== 학력 =====

    public List<EducationEntity> getEducations(String empNo) {
        return careerMapper.selectEducationsByEmpNo(empNo);
    }

    @Transactional
    public void addEducation(String empNo, String schoolName, String major, String degree,
                             LocalDate startDate, LocalDate endDate, String graduationStatus) {
        if (empNo == null || empNo.isBlank()) throw new IllegalArgumentException("empNo is required");
        if (schoolName == null || schoolName.isBlank()) throw new IllegalArgumentException("학교명은 필수입니다.");
        EducationEntity e = new EducationEntity();
        e.setEmpNo(empNo.trim());
        e.setSchoolName(schoolName.trim());
        e.setMajor(trim(major));
        e.setDegree(trim(degree));
        e.setStartDate(startDate);
        e.setEndDate(endDate);
        e.setGraduationStatus(trim(graduationStatus));
        e.setSortOrder(0);
        careerMapper.insertEducation(e);
    }

    @Transactional
    public void deleteEducation(String empNo, Long id) {
        careerMapper.deleteEducation(id, empNo);
    }

    // ===== 자격증 =====

    public List<CertificateEntity> getCertificates(String empNo) {
        return careerMapper.selectCertificatesByEmpNo(empNo);
    }

    @Transactional
    public void addCertificate(String empNo, String certName, String issuer,
                               LocalDate acquiredDate, LocalDate expiryDate) {
        if (empNo == null || empNo.isBlank()) throw new IllegalArgumentException("empNo is required");
        if (certName == null || certName.isBlank()) throw new IllegalArgumentException("자격증명은 필수입니다.");
        CertificateEntity e = new CertificateEntity();
        e.setEmpNo(empNo.trim());
        e.setCertName(certName.trim());
        e.setIssuer(trim(issuer));
        e.setAcquiredDate(acquiredDate);
        e.setExpiryDate(expiryDate);
        e.setSortOrder(0);
        careerMapper.insertCertificate(e);
    }

    @Transactional
    public void deleteCertificate(String empNo, Long id) {
        careerMapper.deleteCertificate(id, empNo);
    }

    private static String trim(String v) {
        if (v == null) return null;
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }
}
