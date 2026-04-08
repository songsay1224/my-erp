package com.example.sayy.Controller;

import com.example.sayy.Entity.EmploymentStatus;
import com.example.sayy.Entity.EmploymentType;
import com.example.sayy.Entity.TaxScheme;
import com.example.sayy.Service.CareerService;
import com.example.sayy.Service.CompanyService;
import com.example.sayy.Service.DaysOffService;
import com.example.sayy.Service.EmployeeBulkImportService;
import com.example.sayy.Service.EmployeeDocumentService;
import com.example.sayy.Service.EmployeeService;
import com.example.sayy.Service.EmployeeExcelImportService;
import com.example.sayy.Service.EmployeeImportStore;
import com.example.sayy.Service.OrgUnitService;
import com.example.sayy.Service.UserService;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class AdminController {
    private final EmployeeService employeeService;
    private final UserService userService;
    private final DaysOffService daysOffService;
    private final CompanyService companyService;
    private final OrgUnitService orgUnitService;
    private final EmployeeExcelImportService employeeExcelImportService;
    private final EmployeeImportStore employeeImportStore;
    private final EmployeeBulkImportService employeeBulkImportService;
    private final CareerService careerService;
    private final EmployeeDocumentService documentService;

    public AdminController(EmployeeService employeeService,
                           UserService userService,
                           DaysOffService daysOffService,
                           CompanyService companyService,
                           OrgUnitService orgUnitService,
                           EmployeeExcelImportService employeeExcelImportService,
                           EmployeeImportStore employeeImportStore,
                           EmployeeBulkImportService employeeBulkImportService,
                           CareerService careerService,
                           EmployeeDocumentService documentService) {
        this.employeeService = employeeService;
        this.userService = userService;
        this.daysOffService = daysOffService;
        this.companyService = companyService;
        this.orgUnitService = orgUnitService;
        this.employeeExcelImportService = employeeExcelImportService;
        this.employeeImportStore = employeeImportStore;
        this.employeeBulkImportService = employeeBulkImportService;
        this.careerService = careerService;
        this.documentService = documentService;
    }

    @GetMapping("/admin")
    public String adminRoot() {
        return "redirect:/admin/home";
    }

    @GetMapping("/admin/home")
    public String adminHome() {
        return "admin-home";
    }

    @GetMapping("/admin/calendar")
    public String adminCalendar() {
        return "admin-calendar";
    }

    @GetMapping("/admin/company")
    public String companyInfo(Model model) {
        model.addAttribute("company", companyService.getCompanyInfoOrEmpty());
        return "admin-company";
    }

    @GetMapping("/admin/settings")
    public String adminSettings(@RequestParam(value = "drawer", required = false) String drawer,
                                @RequestParam(value = "year", required = false) Integer year,
                                Model model) {
        if ("days-off".equals(drawer)) {
            int y = (year == null) ? Year.now().getValue() : year;
            model.addAttribute("daysOffDrawerOpen", true);
            model.addAttribute("daysOffYear", y);
            model.addAttribute("daysOffHolidays", daysOffService.getHolidays(y));
            model.addAttribute("daysOffCustom", daysOffService.getCustomDaysOff(y));
        }
        if ("company".equals(drawer)) {
            model.addAttribute("companyDrawerOpen", true);
            model.addAttribute("company", companyService.getCompanyInfoOrEmpty());
        }
        if ("company-seal".equals(drawer)) {
            model.addAttribute("companySealDrawerOpen", true);
            model.addAttribute("company", companyService.getCompanyInfoOrEmpty());
        }
        if ("org".equals(drawer)) {
            model.addAttribute("orgDrawerOpen", true);
            model.addAttribute("orgRows", orgUnitService.listRows());
        }
        return "admin-dashboard";
    }

    @PostMapping("/admin/settings/org")
    public String createOrgUnit(@RequestParam String name,
                                @RequestParam(required = false) Long parentId,
                                RedirectAttributes redirectAttributes) {
        try {
            orgUnitService.create(name, parentId);
            redirectAttributes.addFlashAttribute("success", "조직이 저장되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/settings?drawer=org";
    }

    @PostMapping("/admin/settings/org/{id}/rename")
    public String renameOrgUnit(@PathVariable long id,
                                @RequestParam String name,
                                RedirectAttributes redirectAttributes) {
        try {
            orgUnitService.rename(id, name);
            redirectAttributes.addFlashAttribute("success", "조직명이 변경되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/settings?drawer=org";
    }

    @PostMapping("/admin/settings/org/{id}/delete")
    public String deleteOrgUnit(@PathVariable long id,
                                RedirectAttributes redirectAttributes) {
        try {
            orgUnitService.delete(id);
            redirectAttributes.addFlashAttribute("success", "조직이 삭제되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/settings?drawer=org";
    }

    @PostMapping("/admin/settings/org/{id}/move")
    public String moveOrgUnit(@PathVariable long id,
                              @RequestParam(required = false) Long parentId,
                              @RequestParam(required = false) Long beforeId,
                              @RequestParam(required = false) Long afterId,
                              RedirectAttributes redirectAttributes) {
        try {
            orgUnitService.move(id, parentId, beforeId, afterId);
            redirectAttributes.addFlashAttribute("success", "조직이 이동되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/settings?drawer=org";
    }

    @PostMapping("/admin/settings/company")
    public String saveCompanyInfo(@RequestParam String companyName,
                                  @RequestParam(required = false) String ceoName,
                                  @RequestParam(required = false) String phone,
                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate foundedDate,
                                  @RequestParam(required = false) String zipCode,
                                  @RequestParam(required = false) String address,
                                  @RequestParam(required = false) String addressDetail,
                                  @RequestParam(required = false) String businessRegNo,
                                  @RequestParam(required = false) String corporateRegNo,
                                  RedirectAttributes redirectAttributes) {
        try {
            companyService.saveCompanyInfo(companyName, ceoName, phone, foundedDate, zipCode, address, addressDetail, businessRegNo, corporateRegNo);
            redirectAttributes.addFlashAttribute("success", "회사 정보가 저장되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/settings?drawer=company";
    }

    @PostMapping("/admin/settings/company/logo")
    public String saveCompanyLogo(@RequestParam(required = false) MultipartFile logoFile,
                                  @RequestParam(defaultValue = "false") boolean removeLogo,
                                  RedirectAttributes redirectAttributes) {
        try {
            companyService.saveCompanyLogo(logoFile, removeLogo);
            redirectAttributes.addFlashAttribute("success", "회사 로고가 저장되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/settings?drawer=company";
    }

    @PostMapping("/admin/settings/company/seal")
    public String saveCompanySeal(@RequestParam(required = false) MultipartFile sealFile,
                                  @RequestParam(defaultValue = "false") boolean removeSeal,
                                  RedirectAttributes redirectAttributes) {
        try {
            companyService.saveCompanySeal(sealFile, removeSeal);
            redirectAttributes.addFlashAttribute("success", "회사 직인이 저장되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/settings?drawer=company-seal";
    }

    @PostMapping("/admin/settings/days-off/custom")
    public String upsertCustomDayOff(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                     @RequestParam String name,
                                     @RequestParam(defaultValue = "false") boolean repeatAnnually,
                                     RedirectAttributes redirectAttributes) {
        try {
            daysOffService.upsertCustom(date, name, repeatAnnually);
            redirectAttributes.addFlashAttribute("success", "쉬는 날이 저장되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/settings?drawer=days-off&year=" + date.getYear();
    }

    @PostMapping("/admin/settings/days-off/custom/{id}/delete")
    public String deleteCustomDayOff(@PathVariable long id,
                                     @RequestParam int year,
                                     RedirectAttributes redirectAttributes) {
        try {
            daysOffService.deleteCustom(id);
            redirectAttributes.addFlashAttribute("success", "쉬는 날이 삭제되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/settings?drawer=days-off&year=" + year;
    }

    @GetMapping("/admin/employees")
    public String employeeList(@RequestParam(value = "type", required = false) String type,
                               @RequestParam(value = "status", required = false) String status,
                               @RequestParam(value = "q", required = false) String q,
                               @RequestParam(value = "sort", required = false) String sort,
                               Model model) {
        EmploymentType selectedType = null;
        if (type != null && !type.isBlank() && !"ALL".equalsIgnoreCase(type)) {
            selectedType = EmploymentType.valueOf(type.toUpperCase());
        }
        EmploymentStatus selectedStatus = null;
        if (status == null || status.isBlank()) {
            selectedStatus = EmploymentStatus.ACTIVE;
        } else if (!"ALL".equalsIgnoreCase(status)) {
            selectedStatus = EmploymentStatus.valueOf(status.toUpperCase());
        }

        model.addAttribute("selectedType", selectedType == null ? "ALL" : selectedType.name());
        model.addAttribute("selectedStatus", selectedStatus == null ? "ALL" : selectedStatus.name());
        model.addAttribute("keyword", q == null ? "" : q.trim());
        model.addAttribute("selectedSort", (sort == null || sort.isBlank()) ? "createdAtDesc" : sort.trim());
        model.addAttribute("totalCount", employeeService.countAll());
        model.addAttribute("regularCount", employeeService.countByEmploymentType(EmploymentType.REGULAR));
        model.addAttribute("contractCount", employeeService.countByEmploymentType(EmploymentType.CONTRACT));
        model.addAttribute("freelancerCount", employeeService.countByEmploymentType(EmploymentType.FREELANCER));
        model.addAttribute("activeCount", employeeService.countByEmploymentStatus(EmploymentStatus.ACTIVE));
        model.addAttribute("leaveCount", employeeService.countByEmploymentStatus(EmploymentStatus.LEAVE));
        model.addAttribute("terminatedCount", employeeService.countByEmploymentStatus(EmploymentStatus.TERMINATED));
        model.addAttribute("employees", employeeService.findByFilters(selectedType, selectedStatus, q, sort));
        return "admin-employees";
    }

    @GetMapping("/admin/employees/new")
    public String employeeNew() {
        return "admin-employees-new";
    }

    @GetMapping("/admin/employees/{empNo}")
    public String employeeDetail(@PathVariable String empNo, Model model) {
        var employee = employeeService.findOneWithUserOrThrow(empNo);
        var assignments = employeeService.listOrgAssignments(employee.getEmpNo());
        var orgRows = orgUnitService.listRows();
        model.addAttribute("employee", employee);
        model.addAttribute("assignments", assignments);
        model.addAttribute("orgRows", orgRows);
        model.addAttribute("orgNameById", toOrgNameMap(orgRows));
        model.addAttribute("activeProfileTab", "hr");
        return "admin-employee-detail";
    }

    @GetMapping("/admin/employees/{empNo}/hr/edit")
    public String employeeHrEdit(@PathVariable String empNo, Model model) {
        var employee = employeeService.findOneWithUserOrThrow(empNo);
        var assignments = employeeService.listOrgAssignments(employee.getEmpNo());
        var orgRows = orgUnitService.listRows();

        model.addAttribute("employee", employee);
        model.addAttribute("assignments", assignments.isEmpty() ? List.of(new com.example.sayy.Entity.EmployeeOrgAssignmentEntity()) : assignments);
        model.addAttribute("orgRows", orgRows);

        // MVP: 항목 설정(직무/직위/직책) 화면은 추후 확장. 현재는 자주 쓰는 항목만 제공.
        model.addAttribute("positionOptions", List.of("담당", "대리", "과장", "차장", "부장", "임원"));
        model.addAttribute("titleOptions", List.of("팀원", "팀장", "파트장", "실장", "본부장"));
        model.addAttribute("jobOptions", List.of("개발", "기획", "디자인", "마케팅", "영업", "인사", "재무", "운영"));
        return "admin-employee-hr-edit";
    }

    @PostMapping("/admin/employees/{empNo}/hr")
    public String employeeHrUpdate(@PathVariable String empNo,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hireDate,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate groupHireDate,
                                   @RequestParam(required = false) String positionName,
                                   @RequestParam(required = false) String jobNames,
                                   @RequestParam(required = false) String hrMemo,
                                   @RequestParam(required = false, name = "orgUnitIds") List<String> orgUnitIds,
                                   @RequestParam(required = false, name = "titleNames") List<String> titleNames,
                                   @RequestParam(required = false, name = "orgLeaders") List<String> orgLeaders,
                                   RedirectAttributes redirectAttributes) {
        try {
            employeeService.updateHrProfile(
                    empNo,
                    hireDate,
                    groupHireDate,
                    positionName,
                    jobNames,
                    hrMemo,
                    parseLongList(orgUnitIds),
                    titleNames,
                    parseBooleanList(orgLeaders)
            );
            redirectAttributes.addFlashAttribute("success", "인사정보가 저장되었습니다.");
            return "redirect:/admin/employees/" + empNo;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/employees/" + empNo + "/hr/edit";
        }
    }

    // ===== 경력·학력 =====

    @GetMapping("/admin/employees/{empNo}/career")
    public String employeeCareer(@PathVariable String empNo, Model model) {
        model.addAttribute("employee", employeeService.findOneWithUserOrThrow(empNo));
        model.addAttribute("careers", careerService.getCareers(empNo));
        model.addAttribute("educations", careerService.getEducations(empNo));
        model.addAttribute("certificates", careerService.getCertificates(empNo));
        return "admin-employee-career";
    }

    @PostMapping("/admin/employees/{empNo}/career/add")
    public String addCareer(@PathVariable String empNo,
                            @RequestParam String companyName,
                            @RequestParam(required = false) String department,
                            @RequestParam(required = false) String position,
                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                            @RequestParam(required = false) String description,
                            RedirectAttributes ra) {
        try {
            careerService.addCareer(empNo, companyName, department, position, startDate, endDate, description);
            ra.addFlashAttribute("success", "경력이 추가되었습니다.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/employees/" + empNo + "/career";
    }

    @PostMapping("/admin/employees/{empNo}/career/{id}/delete")
    public String deleteCareer(@PathVariable String empNo, @PathVariable Long id, RedirectAttributes ra) {
        careerService.deleteCareer(empNo, id);
        ra.addFlashAttribute("success", "경력이 삭제되었습니다.");
        return "redirect:/admin/employees/" + empNo + "/career";
    }

    @PostMapping("/admin/employees/{empNo}/education/add")
    public String addEducation(@PathVariable String empNo,
                               @RequestParam String schoolName,
                               @RequestParam(required = false) String major,
                               @RequestParam(required = false) String degree,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                               @RequestParam(required = false) String graduationStatus,
                               RedirectAttributes ra) {
        try {
            careerService.addEducation(empNo, schoolName, major, degree, startDate, endDate, graduationStatus);
            ra.addFlashAttribute("success", "학력이 추가되었습니다.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/employees/" + empNo + "/career";
    }

    @PostMapping("/admin/employees/{empNo}/education/{id}/delete")
    public String deleteEducation(@PathVariable String empNo, @PathVariable Long id, RedirectAttributes ra) {
        careerService.deleteEducation(empNo, id);
        ra.addFlashAttribute("success", "학력이 삭제되었습니다.");
        return "redirect:/admin/employees/" + empNo + "/career";
    }

    @PostMapping("/admin/employees/{empNo}/certificate/add")
    public String addCertificate(@PathVariable String empNo,
                                 @RequestParam String certName,
                                 @RequestParam(required = false) String issuer,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate acquiredDate,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiryDate,
                                 RedirectAttributes ra) {
        try {
            careerService.addCertificate(empNo, certName, issuer, acquiredDate, expiryDate);
            ra.addFlashAttribute("success", "자격증이 추가되었습니다.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/employees/" + empNo + "/career";
    }

    @PostMapping("/admin/employees/{empNo}/certificate/{id}/delete")
    public String deleteCertificate(@PathVariable String empNo, @PathVariable Long id, RedirectAttributes ra) {
        careerService.deleteCertificate(empNo, id);
        ra.addFlashAttribute("success", "자격증이 삭제되었습니다.");
        return "redirect:/admin/employees/" + empNo + "/career";
    }

    // ===== 문서 =====

    /** 서비스에서 관리하는 정해진 문서 종류 목록 */
    private static final List<String> DOCUMENT_CATEGORIES = List.of(
            "주민등록등본", "연봉계약서", "근로계약서", "이력서", "졸업증명서", "자격증"
    );

    @GetMapping("/admin/employees/{empNo}/documents")
    public String employeeDocuments(@PathVariable String empNo, Model model) {
        model.addAttribute("employee", employeeService.findOneWithUserOrThrow(empNo));
        var allDocs = documentService.getDocuments(empNo);
        model.addAttribute("documents", allDocs);
        // 카테고리별 전체 파일 목록 맵 (category -> List<doc>)
        Map<String, List<com.example.sayy.Entity.EmployeeDocumentEntity>> docsByCategory = new HashMap<>();
        for (var doc : allDocs) {
            String cat = doc.getCategory() != null ? doc.getCategory() : "기타";
            docsByCategory.computeIfAbsent(cat, k -> new java.util.ArrayList<>()).add(doc);
        }
        model.addAttribute("docsByCategory", docsByCategory);
        model.addAttribute("documentCategories", DOCUMENT_CATEGORIES);
        return "admin-employee-documents";
    }

    @PostMapping("/admin/employees/{empNo}/documents/upload")
    public String uploadDocument(@PathVariable String empNo,
                                 @RequestParam("file") MultipartFile file,
                                 @RequestParam(required = false) String category,
                                 RedirectAttributes ra,
                                 java.security.Principal principal) {
        try {
            String uploader = principal != null ? principal.getName() : "admin";
            documentService.upload(empNo, file, category, uploader);
            ra.addFlashAttribute("success", "문서가 업로드되었습니다.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/employees/" + empNo + "/documents";
    }

    @GetMapping("/admin/employees/{empNo}/documents/{id}/download")
    @ResponseBody
    public ResponseEntity<Resource> downloadDocument(@PathVariable String empNo,
                                                     @PathVariable Long id) {
        try {
            Resource resource = documentService.download(empNo, id);
            var doc = documentService.findById(id);
            String contentType = doc != null && doc.getContentType() != null
                    ? doc.getContentType() : "application/octet-stream";
            String filename = doc != null ? doc.getOriginalName() : resource.getFilename();
            String encodedFilename = java.net.URLEncoder.encode(filename, java.nio.charset.StandardCharsets.UTF_8)
                    .replace("+", "%20");
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/admin/employees/{empNo}/documents/{id}/delete")
    public String deleteDocument(@PathVariable String empNo, @PathVariable Long id, RedirectAttributes ra) {
        documentService.delete(empNo, id);
        ra.addFlashAttribute("success", "문서가 삭제되었습니다.");
        return "redirect:/admin/employees/" + empNo + "/documents";
    }

    @GetMapping("/admin/employees/{empNo}/contract")
    public String employeeContract(@PathVariable String empNo, Model model) {
        model.addAttribute("employee", employeeService.findOneWithUserOrThrow(empNo));
        return "admin-employee-contract";
    }

    @GetMapping("/admin/employees/{empNo}/contract/edit")
    public String employeeContractEdit(@PathVariable String empNo, Model model) {
        model.addAttribute("employee", employeeService.findOneWithUserOrThrow(empNo));
        return "admin-employee-contract-edit";
    }

    @PostMapping("/admin/employees/{empNo}/contract")
    public String employeeContractUpdate(@PathVariable String empNo,
                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate laborContractStart,
                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate laborContractEnd,
                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate probationStart,
                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate probationEnd,
                                         @RequestParam(required = false) Integer probationPayRate,
                                         @RequestParam(required = false) String incomeType,
                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate wageContractStart,
                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate wageContractEnd,
                                         @RequestParam(required = false) Long monthlySalary,
                                         RedirectAttributes redirectAttributes) {
        try {
            employeeService.updateContractInfo(empNo, laborContractStart, laborContractEnd,
                    probationStart, probationEnd, probationPayRate,
                    incomeType, wageContractStart, wageContractEnd, monthlySalary);
            redirectAttributes.addFlashAttribute("success", "계약정보가 저장되었습니다.");
            return "redirect:/admin/employees/" + empNo + "/contract";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/employees/" + empNo + "/contract/edit";
        }
    }

    @GetMapping("/admin/employees/{empNo}/personal")
    public String employeePersonal(@PathVariable String empNo, Model model) {
        var employee = employeeService.findOneWithUserOrThrow(empNo);
        model.addAttribute("employee", employee);
        model.addAttribute("activeProfileTab", "personal");
        return "admin-employee-personal";
    }

    @GetMapping("/admin/employees/{empNo}/personal/edit")
    public String employeePersonalEdit(@PathVariable String empNo, Model model) {
        var employee = employeeService.findOneWithUserOrThrow(empNo);
        model.addAttribute("employee", employee);
        return "admin-employee-personal-edit";
    }

    @PostMapping("/admin/employees/{empNo}/personal")
    public String employeePersonalUpdate(@PathVariable String empNo,
                                         @RequestParam(required = false) String nameEn,
                                         @RequestParam(required = false) String gender,
                                         @RequestParam(required = false) String nationality,
                                         @RequestParam(required = false) String phone,
                                         @RequestParam(required = false) String bankName,
                                         @RequestParam(required = false) String bankAccount,
                                         @RequestParam(required = false) String bankHolder,
                                         RedirectAttributes redirectAttributes) {
        try {
            employeeService.updatePersonalInfo(empNo, nameEn, gender, nationality, phone, bankName, bankAccount, bankHolder);
            redirectAttributes.addFlashAttribute("success", "개인정보가 저장되었습니다.");
            return "redirect:/admin/employees/" + empNo + "/personal";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/employees/" + empNo + "/personal/edit";
        }
    }

    @GetMapping("/admin/employees/import")
    public String employeeImportPage() {
        return "admin-employees-import";
    }

    @GetMapping("/admin/employees/import/template")
    public org.springframework.http.ResponseEntity<byte[]> downloadEmployeeImportTemplate() {
        byte[] bytes = employeeExcelImportService.generateTemplateXlsx();
        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"인사기록카드.xlsx\"")
                .contentType(org.springframework.http.MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    @PostMapping("/admin/employees/import/preview")
    public String employeeImportPreview(@RequestParam("file") MultipartFile file,
                                        Model model,
                                        RedirectAttributes redirectAttributes) {
        try {
            var rows = employeeExcelImportService.parseAndValidate(file);
            String token = employeeImportStore.putPreview(rows);

            long total = rows.size();
            long invalid = rows.stream().filter(r -> !r.isValid()).count();
            long valid = total - invalid;

            model.addAttribute("previewToken", token);
            model.addAttribute("previewRows", rows);
            model.addAttribute("previewTotal", total);
            model.addAttribute("previewValid", valid);
            model.addAttribute("previewInvalid", invalid);
            model.addAttribute("previewCanCommit", invalid == 0);
            return "admin-employees-import";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/employees/import";
        }
    }

    @PostMapping("/admin/employees/import/commit")
    public String employeeImportCommit(@RequestParam("token") String token,
                                       RedirectAttributes redirectAttributes) {
        try {
            var payload = employeeImportStore.removePreview(token);
            if (payload == null || payload.rows() == null) {
                throw new IllegalArgumentException("미리보기 토큰이 만료되었거나 유효하지 않습니다. 다시 업로드하세요.");
            }

            int imported = employeeBulkImportService.importEmployeesOnlyOrThrow(payload.rows());
            redirectAttributes.addFlashAttribute("success",
                    "업로드 반영 완료: " + imported + "명 등록되었습니다. (계정/초기비번 발급은 구성원 목록에서 진행하세요)");
            return "redirect:/admin/employees";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/employees/import";
        }
    }

    @PostMapping("/admin/employees")
    public String createEmployee(@RequestParam String empNo,
                                 @RequestParam String name,
                                 @RequestParam EmploymentType employmentType,
                                 @RequestParam(required = false) Boolean fourInsured,
                                 @RequestParam(required = false) TaxScheme taxScheme,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate contractEndDate,
                                 RedirectAttributes redirectAttributes) {
        try {
            employeeService.create(empNo, name, employmentType, fourInsured, taxScheme, contractEndDate);
            redirectAttributes.addFlashAttribute("success", "인사정보가 등록되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/employees/new";
    }

    @PostMapping("/admin/employees/{empNo}/issue-account")
    public String issueAccount(@PathVariable String empNo, RedirectAttributes redirectAttributes) {
        try {
            UserService.IssuedAccount issued = userService.issueAccountForEmployee(empNo);
            redirectAttributes.addFlashAttribute("issuedUsername", issued.username());
            redirectAttributes.addFlashAttribute("issuedPassword", issued.initialPassword());
            redirectAttributes.addFlashAttribute("success", "계정이 발급되었습니다. (초기 비밀번호는 1회만 표시됩니다)");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/employees";
    }

    @PostMapping("/admin/employees/{empNo}/delete")
    public String deleteEmployee(@PathVariable String empNo, RedirectAttributes redirectAttributes) {
        try {
            employeeService.deleteEmployee(empNo, false);
            redirectAttributes.addFlashAttribute("success", "인사정보가 삭제되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/employees";
    }

    @PostMapping("/admin/employees/{empNo}/delete-with-account")
    public String deleteEmployeeWithAccount(@PathVariable String empNo, RedirectAttributes redirectAttributes) {
        try {
            employeeService.deleteEmployee(empNo, true);
            redirectAttributes.addFlashAttribute("success", "인사정보/계정이 삭제되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/employees";
    }

    private static Map<Long, String> toOrgNameMap(List<com.example.sayy.DTO.OrgUnitRowDTO> rows) {
        Map<Long, String> map = new HashMap<>();
        if (rows == null) return map;
        for (var r : rows) {
            if (r == null || r.id() == null) continue;
            map.put(r.id(), r.name());
        }
        return map;
    }

    private static List<Long> parseLongList(List<String> raw) {
        if (raw == null || raw.isEmpty()) return List.of();
        return raw.stream()
                .map(s -> (s == null) ? null : s.trim())
                .map(s -> (s == null || s.isEmpty()) ? null : s)
                .map(s -> {
                    try {
                        return (s == null) ? null : Long.parseLong(s);
                    } catch (Exception ignored) {
                        return null;
                    }
                })
                .toList();
    }

    private static List<Boolean> parseBooleanList(List<String> raw) {
        if (raw == null || raw.isEmpty()) return List.of();
        return raw.stream()
                .map(s -> s != null && ("true".equalsIgnoreCase(s.trim()) || "1".equals(s.trim()) || "on".equalsIgnoreCase(s.trim())))
                .toList();
    }
}