package com.example.sayy.Controller;

import com.example.sayy.Entity.ProjectEntity;
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

@Controller
public class ProjectController {

    private final ProjectService projectService;
    private final ClientService clientService;
    private final EmployeeService employeeService;
    private final OrgUnitService orgUnitService;

    public ProjectController(ProjectService projectService,
                             ClientService clientService,
                             EmployeeService employeeService,
                             OrgUnitService orgUnitService) {
        this.projectService = projectService;
        this.clientService = clientService;
        this.employeeService = employeeService;
        this.orgUnitService = orgUnitService;
    }

    @GetMapping("/admin/projects")
    public String list(@RequestParam(required = false) String status,
                       @RequestParam(required = false) String keyword,
                       Model model) {
        model.addAttribute("projects", projectService.getProjects(status, keyword));
        model.addAttribute("selectedStatus", status);
        model.addAttribute("keyword", keyword);
        return "admin-projects";
    }

    @GetMapping("/admin/projects/new")
    public String newForm(Model model) {
        model.addAttribute("clients", clientService.getClients(null, null));
        model.addAttribute("allEmployees", employeeService.findAllActive());
        model.addAttribute("orgUnits", orgUnitService.getAll());
        return "admin-project-new";
    }

    @PostMapping("/admin/projects")
    public String create(@ModelAttribute ProjectEntity p,
                         RedirectAttributes ra) {
        try {
            Long id = projectService.createProject(p);
            ra.addFlashAttribute("success", "프로젝트가 등록되었습니다.");
            return "redirect:/admin/projects/" + id;
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/projects/new";
        }
    }

    @GetMapping("/admin/projects/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("project", projectService.getProjectOrThrow(id));
        return "admin-project-detail";
    }

    @GetMapping("/admin/projects/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("project", projectService.getProjectOrThrow(id));
        model.addAttribute("clients", clientService.getClients(null, null));
        model.addAttribute("allEmployees", employeeService.findAllActive());
        model.addAttribute("orgUnits", orgUnitService.getAll());
        return "admin-project-edit";
    }

    @PostMapping("/admin/projects/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute ProjectEntity p,
                         RedirectAttributes ra) {
        try {
            projectService.updateProject(id, p);
            ra.addFlashAttribute("success", "프로젝트가 수정되었습니다.");
            return "redirect:/admin/projects/" + id;
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/projects/" + id + "/edit";
        }
    }

    @PostMapping("/admin/projects/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            projectService.deleteProject(id);
            ra.addFlashAttribute("success", "프로젝트가 삭제되었습니다.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/projects";
    }

    // ===== 계약서 =====

    @PostMapping("/admin/projects/{id}/contracts")
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
        return "redirect:/admin/projects/" + id;
    }

    @PostMapping("/admin/projects/{id}/contracts/{contractId}/status")
    public String updateContractStatus(@PathVariable Long id,
                                       @PathVariable Long contractId,
                                       @RequestParam String status,
                                       @RequestParam(required = false) String memo,
                                       RedirectAttributes ra) {
        try {
            projectService.updateContractStatus(id, contractId, status, memo);
            ra.addFlashAttribute("success", "계약서 상태가 변경되었습니다.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/projects/" + id;
    }

    @GetMapping("/admin/projects/{id}/contracts/{contractId}/download")
    @ResponseBody
    public ResponseEntity<Resource> downloadContract(@PathVariable Long id,
                                                     @PathVariable Long contractId) {
        try {
            Resource resource = projectService.downloadContract(id, contractId);
            var contract = projectService.getContractOrThrow(contractId);
            String contentType = contract.getContentType() != null ? contract.getContentType() : "application/octet-stream";
            String filename = contract.getOriginalName() != null ? contract.getOriginalName() : resource.getFilename();
            String encoded = java.net.URLEncoder.encode(filename, java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20");
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/admin/projects/{id}/contracts/{contractId}/delete")
    public String deleteContract(@PathVariable Long id, @PathVariable Long contractId, RedirectAttributes ra) {
        try {
            projectService.deleteContract(id, contractId);
            ra.addFlashAttribute("success", "계약서가 삭제되었습니다.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/projects/" + id;
    }
}
