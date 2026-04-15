package com.example.sayy.Controller;

import com.example.sayy.Entity.*;
import com.example.sayy.Mapper.*;
import com.example.sayy.Service.ClientService;
import com.example.sayy.Service.EmployeeService;
import com.example.sayy.Service.OrgUnitService;
import com.example.sayy.Service.ProjectService;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
public class ContractListController {

    private final ProjectMapper projectMapper;
    private final ProjectService projectService;
    private final ContractSalesMapper salesMapper;
    private final ContractPerformanceMapper performanceMapper;
    private final ContractPurchaseMapper purchaseMapper;
    private final ClientService clientService;
    private final EmployeeService employeeService;
    private final OrgUnitService orgUnitService;

    public ContractListController(ProjectMapper projectMapper,
                                  ProjectService projectService,
                                  ContractSalesMapper salesMapper,
                                  ContractPerformanceMapper performanceMapper,
                                  ContractPurchaseMapper purchaseMapper,
                                  ClientService clientService,
                                  EmployeeService employeeService,
                                  OrgUnitService orgUnitService) {
        this.projectMapper = projectMapper;
        this.projectService = projectService;
        this.salesMapper = salesMapper;
        this.performanceMapper = performanceMapper;
        this.purchaseMapper = purchaseMapper;
        this.clientService = clientService;
        this.employeeService = employeeService;
        this.orgUnitService = orgUnitService;
    }

    /* ===== 목록 ===== */
    @GetMapping("/admin/contract-list")
    public String list(@RequestParam(required = false) Integer managementYear,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "20") int pageSize,
                       Model model) {

        long total = projectMapper.countContractList(managementYear, blank(keyword) ? null : keyword.trim());
        int totalPages = (int) Math.ceil((double) total / pageSize);
        if (page < 1) page = 1;
        if (totalPages > 0 && page > totalPages) page = totalPages;

        int offset = (page - 1) * pageSize;
        List<ProjectEntity> rows = projectMapper.selectContractList(
                managementYear, blank(keyword) ? null : keyword.trim(), pageSize, offset);
        List<Integer> years = projectMapper.selectDistinctManagementYears();

        model.addAttribute("rows", rows);
        model.addAttribute("years", years);
        model.addAttribute("selectedYear", managementYear);
        model.addAttribute("keyword", keyword);
        model.addAttribute("page", page);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("total", total);
        return "admin-contract-list";
    }

    /* ===== 상세 ===== */
    @GetMapping("/admin/contract-list/{id}")
    public String detail(@PathVariable Long id,
                         @RequestParam(required = false) String tab,
                         Model model) {
        ProjectEntity project = projectService.getProjectOrThrow(id);
        model.addAttribute("project", project);
        model.addAttribute("tab", tab);

        // 모든 탭 변수를 항상 초기화 (Thymeleaf NPE 방지)
        model.addAttribute("salesList",
                "sales".equals(tab) ? salesMapper.selectByProjectId(id) : java.util.Collections.emptyList());
        model.addAttribute("performanceList",
                "performance".equals(tab) ? performanceMapper.selectByProjectId(id) : java.util.Collections.emptyList());
        model.addAttribute("purchaseList",
                "purchase".equals(tab) ? purchaseMapper.selectByProjectId(id) : java.util.Collections.emptyList());

        return "admin-contract-detail";
    }

    /* ===== 계약서 파일 등록 ===== */
    @PostMapping("/admin/contract-list/{id}/contracts")
    public String addContract(@PathVariable Long id,
                              @RequestParam String title,
                              @RequestParam(required = false) String contractType,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate contractDate,
                              @RequestParam(required = false) Long amount,
                              @RequestParam(required = false) String status,
                              @RequestParam(required = false) MultipartFile file,
                              @RequestParam(required = false) String memo,
                              RedirectAttributes ra) {
        try {
            projectService.addContract(id, title, contractType, contractDate, amount, status, file, memo);
            ra.addFlashAttribute("success", "계약서가 등록되었습니다.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/contract-list/" + id + "?tab=contract";
    }

    /* ===== 계약서 파일 삭제 ===== */
    @PostMapping("/admin/contract-list/{id}/contracts/{contractId}/delete")
    public String deleteContract(@PathVariable Long id,
                                 @PathVariable Long contractId,
                                 RedirectAttributes ra) {
        try {
            projectService.deleteContract(id, contractId);
            ra.addFlashAttribute("success", "계약서가 삭제되었습니다.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/contract-list/" + id + "?tab=contract";
    }

    /* ===== 계약서 파일 다운로드 ===== */
    @GetMapping("/admin/contract-list/{id}/contracts/{contractId}/download")
    @ResponseBody
    public ResponseEntity<Resource> downloadContract(@PathVariable Long id,
                                                     @PathVariable Long contractId) {
        try {
            Resource resource = projectService.downloadContract(id, contractId);
            ContractEntity contract = projectService.getContractOrThrow(contractId);
            String contentType = contract.getContentType() != null
                    ? contract.getContentType() : "application/octet-stream";
            String filename = contract.getOriginalName() != null
                    ? contract.getOriginalName() : resource.getFilename();
            String encoded = java.net.URLEncoder
                    .encode(filename, java.nio.charset.StandardCharsets.UTF_8)
                    .replace("+", "%20");
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /* ===== 매출 정보 등록 ===== */
    @PostMapping("/admin/contract-list/{id}/sales")
    public String addSales(@PathVariable Long id,
                           @RequestParam(required = false) String contractName,
                           @RequestParam(required = false) String customerAgency,
                           @RequestParam(required = false) Long contractAmount,
                           @RequestParam(required = false) Long accumulatedProgress,
                           @RequestParam(required = false) String memo,
                           RedirectAttributes ra) {
        try {
            ContractSalesEntity e = new ContractSalesEntity();
            e.setProjectId(id);
            e.setContractName(contractName);
            e.setCustomerAgency(customerAgency);
            e.setContractAmount(contractAmount);
            e.setAccumulatedProgress(accumulatedProgress);
            e.setMemo(memo);
            salesMapper.insert(e);
            ra.addFlashAttribute("success", "매출 정보가 등록되었습니다.");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/contract-list/" + id + "?tab=sales";
    }

    /* ===== 매출 정보 삭제 ===== */
    @PostMapping("/admin/contract-list/{id}/sales/{salesId}/delete")
    public String deleteSales(@PathVariable Long id, @PathVariable Long salesId, RedirectAttributes ra) {
        try {
            salesMapper.deleteById(salesId, id);
            ra.addFlashAttribute("success", "매출 정보가 삭제되었습니다.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/contract-list/" + id + "?tab=sales";
    }

    /* ===== 실적 정보 등록 ===== */
    @PostMapping("/admin/contract-list/{id}/performance")
    public String addPerformance(@PathVariable Long id,
                                 @RequestParam(required = false) String customerAgency,
                                 @RequestParam(required = false) String contractName,
                                 @RequestParam(required = false) Long businessAmount,
                                 @RequestParam(required = false) String supplyType,
                                 @RequestParam(required = false) BigDecimal paymentRate,
                                 @RequestParam(required = false) Long contractAmount,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate completionDate,
                                 @RequestParam(required = false) String businessPeriod,
                                 @RequestParam(required = false) String categoryL1,
                                 @RequestParam(required = false) String categoryL2,
                                 @RequestParam(required = false) String categoryL3,
                                 @RequestParam(required = false) String memo,
                                 RedirectAttributes ra) {
        try {
            ContractPerformanceEntity e = new ContractPerformanceEntity();
            e.setProjectId(id);
            e.setCustomerAgency(customerAgency);
            e.setContractName(contractName);
            e.setBusinessAmount(businessAmount);
            e.setSupplyType(supplyType);
            e.setPaymentRate(paymentRate);
            e.setContractAmount(contractAmount);
            e.setCompletionDate(completionDate);
            e.setBusinessPeriod(businessPeriod);
            e.setCategoryL1(categoryL1);
            e.setCategoryL2(categoryL2);
            e.setCategoryL3(categoryL3);
            e.setMemo(memo);
            performanceMapper.insert(e);
            ra.addFlashAttribute("success", "실적 정보가 등록되었습니다.");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/contract-list/" + id + "?tab=performance";
    }

    /* ===== 실적 정보 삭제 ===== */
    @PostMapping("/admin/contract-list/{id}/performance/{perfId}/delete")
    public String deletePerformance(@PathVariable Long id, @PathVariable Long perfId, RedirectAttributes ra) {
        try {
            performanceMapper.deleteById(perfId, id);
            ra.addFlashAttribute("success", "실적 정보가 삭제되었습니다.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/contract-list/" + id + "?tab=performance";
    }

    /* ===== 구매 정보 등록 ===== */
    @PostMapping("/admin/contract-list/{id}/purchase")
    public String addPurchase(@PathVariable Long id,
                              @RequestParam(required = false) String purchaseType,
                              @RequestParam(required = false) String projectName,
                              @RequestParam(required = false) String subcontractName,
                              @RequestParam(required = false) String vendorName,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate contractDate,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate contractStart,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate contractEnd,
                              @RequestParam(required = false) Long supplyAmount,
                              @RequestParam(required = false) Long vatAmount,
                              @RequestParam(required = false) Long totalAmount,
                              @RequestParam(required = false) String memo,
                              RedirectAttributes ra) {
        try {
            ContractPurchaseEntity e = new ContractPurchaseEntity();
            e.setProjectId(id);
            e.setPurchaseType(purchaseType);
            e.setProjectName(projectName);
            e.setSubcontractName(subcontractName);
            e.setVendorName(vendorName);
            e.setContractDate(contractDate);
            e.setContractStart(contractStart);
            e.setContractEnd(contractEnd);
            e.setSupplyAmount(supplyAmount);
            e.setVatAmount(vatAmount);
            e.setTotalAmount(totalAmount);
            e.setMemo(memo);
            purchaseMapper.insert(e);
            ra.addFlashAttribute("success", "구매 정보가 등록되었습니다.");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/contract-list/" + id + "?tab=purchase";
    }

    /* ===== 구매 정보 삭제 ===== */
    @PostMapping("/admin/contract-list/{id}/purchase/{purchaseId}/delete")
    public String deletePurchase(@PathVariable Long id, @PathVariable Long purchaseId, RedirectAttributes ra) {
        try {
            purchaseMapper.deleteById(purchaseId, id);
            ra.addFlashAttribute("success", "구매 정보가 삭제되었습니다.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/contract-list/" + id + "?tab=purchase";
    }

    /* ===== 수정 폼 ===== */
    @GetMapping("/admin/contract-list/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("project", projectService.getProjectOrThrow(id));
        model.addAttribute("clients", clientService.getClients(null, null, null, 1, Integer.MAX_VALUE));
        model.addAttribute("allEmployees", employeeService.findAllActive());
        model.addAttribute("orgUnits", orgUnitService.getAll());
        return "admin-contract-edit";
    }

    /* ===== 수정 처리 ===== */
    @PostMapping("/admin/contract-list/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute ProjectEntity p,
                         RedirectAttributes ra) {
        try {
            projectService.updateProject(id, p);
            ra.addFlashAttribute("success", "수정되었습니다.");
            return "redirect:/admin/contract-list/" + id;
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/contract-list/" + id + "/edit";
        }
    }

    /* ===== 삭제 ===== */
    @PostMapping("/admin/contract-list/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            projectService.deleteProject(id);
            ra.addFlashAttribute("success", "삭제되었습니다.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/contract-list";
    }

    /* ===== 선택 삭제 ===== */
    @PostMapping("/admin/contract-list/delete-selected")
    public String deleteSelected(@RequestParam(required = false) List<Long> ids, RedirectAttributes ra) {
        if (ids != null && !ids.isEmpty()) {
            projectMapper.deleteByIds(ids);
            ra.addFlashAttribute("success", ids.size() + "개 계약이 삭제되었습니다.");
        }
        return "redirect:/admin/contract-list";
    }

    private boolean blank(String v) { return v == null || v.isBlank(); }
}
