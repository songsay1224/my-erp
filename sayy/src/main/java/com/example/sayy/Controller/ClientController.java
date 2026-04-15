package com.example.sayy.Controller;

import com.example.sayy.DTO.ClientImportRowDTO;
import com.example.sayy.Entity.ClientEntity;
import com.example.sayy.Service.ClientBulkImportService;
import com.example.sayy.Service.ClientExcelExportService;
import com.example.sayy.Service.ClientExcelImportService;
import com.example.sayy.Service.ClientService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class ClientController {

    private final ClientService clientService;
    private final ClientExcelExportService clientExcelExportService;
    private final ClientExcelImportService clientExcelImportService;
    private final ClientBulkImportService clientBulkImportService;

    public ClientController(ClientService clientService,
                            ClientExcelExportService clientExcelExportService,
                            ClientExcelImportService clientExcelImportService,
                            ClientBulkImportService clientBulkImportService) {
        this.clientService = clientService;
        this.clientExcelExportService = clientExcelExportService;
        this.clientExcelImportService = clientExcelImportService;
        this.clientBulkImportService = clientBulkImportService;
    }

    @GetMapping("/admin/clients")
    public String list(@RequestParam(required = false) String status,
                       @RequestParam(required = false) String clientType,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "20") int pageSize,
                       Model model) {
        long total = clientService.countClients(status, clientType, keyword);
        int totalPages = (int) Math.ceil((double) total / pageSize);
        if (page < 1) page = 1;
        if (page > totalPages && totalPages > 0) page = totalPages;

        model.addAttribute("clients", clientService.getClients(status, clientType, keyword, page, pageSize));
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedClientType", clientType);
        model.addAttribute("keyword", keyword);
        model.addAttribute("page", page);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("total", total);
        return "admin-clients";
    }

    @GetMapping("/admin/clients/export")
    public ResponseEntity<byte[]> export(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String clientType,
            @RequestParam(required = false) String keyword) {
        try {
            List<ClientEntity> clients = clientService.getClients(status, clientType, keyword, 1, Integer.MAX_VALUE);
            byte[] data = clientExcelExportService.export(clients);
            String filename = "거래처목록_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename(filename, StandardCharsets.UTF_8).build());
            return ResponseEntity.ok().headers(headers).body(data);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/admin/clients/import")
    public String importForm() { return "admin-client-import"; }

    @GetMapping("/admin/clients/import/template")
    public ResponseEntity<byte[]> importTemplate() {
        try {
            byte[] data = clientExcelImportService.generateTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename("거래처_일괄등록_템플릿.xlsx", StandardCharsets.UTF_8).build());
            return ResponseEntity.ok().headers(headers).body(data);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/admin/clients/import/preview")
    public String importPreview(@RequestParam("file") MultipartFile file,
                                jakarta.servlet.http.HttpSession session,
                                Model model, RedirectAttributes ra) {
        try {
            List<ClientImportRowDTO> rows = clientExcelImportService.parse(file);
            session.setAttribute("clientImportRows", rows);
            model.addAttribute("rows", rows);
            model.addAttribute("totalCount", rows.size());
            model.addAttribute("errorCount", rows.stream().filter(ClientImportRowDTO::hasError).count());
            return "admin-client-import";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "파일 파싱 오류: " + e.getMessage());
            return "redirect:/admin/clients/import";
        }
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/admin/clients/import/commit")
    public String importCommit(jakarta.servlet.http.HttpSession session, RedirectAttributes ra) {
        try {
            List<ClientImportRowDTO> rows = (List<ClientImportRowDTO>) session.getAttribute("clientImportRows");
            if (rows == null || rows.isEmpty()) {
                ra.addFlashAttribute("error", "미리보기 데이터가 없습니다. 파일을 다시 업로드하세요.");
                return "redirect:/admin/clients/import";
            }
            int count = clientBulkImportService.importAll(rows);
            session.removeAttribute("clientImportRows");
            ra.addFlashAttribute("success", count + "개 거래처가 등록되었습니다.");
            return "redirect:/admin/clients";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "일괄 등록 오류: " + e.getMessage());
            return "redirect:/admin/clients/import";
        }
    }

    @GetMapping("/admin/clients/new")
    public String newForm() { return "admin-client-new"; }

    @PostMapping("/admin/clients")
    public String create(@RequestParam(required = false) String clientType,
                         @RequestParam String name,
                         @RequestParam(required = false) String businessRegNo,
                         @RequestParam(required = false) String corporateRegNo,
                         @RequestParam(required = false) String ceoName,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate openedDate,
                         @RequestParam(required = false) String businessType,
                         @RequestParam(required = false) String businessItem,
                         @RequestParam(required = false) String industry,
                         @RequestParam(required = false) String phone,
                         @RequestParam(required = false) String homepage,
                         @RequestParam(required = false) String email,
                         @RequestParam(required = false) String zipCode,
                         @RequestParam(required = false) String address,
                         @RequestParam(required = false) String addressDetail,
                         @RequestParam(required = false) String region,
                         @RequestParam(required = false) String status,
                         @RequestParam(required = false) String bankName,
                         @RequestParam(required = false) String bankAccount,
                         @RequestParam(required = false) String bankHolder,
                         @RequestParam(required = false) String memo,
                         RedirectAttributes ra) {
        try {
            ClientEntity c = new ClientEntity();
            c.setClientType(clientType); c.setName(name);
            c.setBusinessRegNo(businessRegNo); c.setCorporateRegNo(corporateRegNo);
            c.setCeoName(ceoName); c.setOpenedDate(openedDate);
            c.setBusinessType(businessType); c.setBusinessItem(businessItem);
            c.setIndustry(industry); c.setPhone(phone); c.setHomepage(homepage);
            c.setEmail(email); c.setZipCode(zipCode); c.setAddress(address);
            c.setAddressDetail(addressDetail); c.setRegion(region); c.setStatus(status);
            c.setBankName(bankName); c.setBankAccount(bankAccount); c.setBankHolder(bankHolder);
            c.setMemo(memo);
            Long id = clientService.createClient(c);
            ra.addFlashAttribute("success", "거래처가 등록되었습니다.");
            return "redirect:/admin/clients/" + id;
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/clients/new";
        }
    }

    @GetMapping("/admin/clients/{id}")
    public String detail(@PathVariable Long id,
                         @RequestParam(required = false) String tab,
                         Model model) {
        model.addAttribute("client", clientService.getClientOrThrow(id));
        model.addAttribute("tab", tab);
        return "admin-client-detail";
    }

    @GetMapping("/admin/clients/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("client", clientService.getClientOrThrow(id));
        return "admin-client-edit";
    }

    @PostMapping("/admin/clients/{id}")
    public String update(@PathVariable Long id,
                         @RequestParam(required = false) String clientType,
                         @RequestParam String name,
                         @RequestParam(required = false) String businessRegNo,
                         @RequestParam(required = false) String corporateRegNo,
                         @RequestParam(required = false) String ceoName,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate openedDate,
                         @RequestParam(required = false) String businessType,
                         @RequestParam(required = false) String businessItem,
                         @RequestParam(required = false) String industry,
                         @RequestParam(required = false) String phone,
                         @RequestParam(required = false) String homepage,
                         @RequestParam(required = false) String email,
                         @RequestParam(required = false) String zipCode,
                         @RequestParam(required = false) String address,
                         @RequestParam(required = false) String addressDetail,
                         @RequestParam(required = false) String region,
                         @RequestParam(required = false) String status,
                         @RequestParam(required = false) String bankName,
                         @RequestParam(required = false) String bankAccount,
                         @RequestParam(required = false) String bankHolder,
                         @RequestParam(required = false) String memo,
                         RedirectAttributes ra) {
        try {
            ClientEntity c = new ClientEntity();
            c.setClientType(clientType); c.setName(name);
            c.setBusinessRegNo(businessRegNo); c.setCorporateRegNo(corporateRegNo);
            c.setCeoName(ceoName); c.setOpenedDate(openedDate);
            c.setBusinessType(businessType); c.setBusinessItem(businessItem);
            c.setIndustry(industry); c.setPhone(phone); c.setHomepage(homepage);
            c.setEmail(email); c.setZipCode(zipCode); c.setAddress(address);
            c.setAddressDetail(addressDetail); c.setRegion(region); c.setStatus(status);
            c.setBankName(bankName); c.setBankAccount(bankAccount); c.setBankHolder(bankHolder);
            c.setMemo(memo);
            clientService.updateClient(id, c);
            ra.addFlashAttribute("success", "거래처 정보가 수정되었습니다.");
            return "redirect:/admin/clients/" + id;
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/clients/" + id + "/edit";
        }
    }

    @PostMapping("/admin/clients/delete-selected")
    public String deleteSelected(@RequestParam(required = false) List<Long> ids, RedirectAttributes ra) {
        if (ids != null && !ids.isEmpty()) {
            clientService.deleteClients(ids);
            ra.addFlashAttribute("success", ids.size() + "개 거래처가 삭제되었습니다.");
        }
        return "redirect:/admin/clients";
    }

    @PostMapping("/admin/clients/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            clientService.deleteClient(id);
            ra.addFlashAttribute("success", "거래처가 삭제되었습니다.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/clients";
    }

    @PostMapping("/admin/clients/{id}/contacts")
    public String addContact(@PathVariable Long id,
                             @RequestParam String name,
                             @RequestParam(required = false) String department,
                             @RequestParam(required = false) String position,
                             @RequestParam(required = false) String phone,
                             @RequestParam(required = false) String email,
                             @RequestParam(defaultValue = "false") boolean isPrimary,
                             RedirectAttributes ra) {
        try {
            clientService.addContact(id, name, department, position, phone, email, isPrimary);
            ra.addFlashAttribute("success", "담당자가 추가되었습니다.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/clients/" + id + "?tab=contacts";
    }

    @PostMapping("/admin/clients/{id}/contacts/{contactId}/delete")
    public String deleteContact(@PathVariable Long id, @PathVariable Long contactId, RedirectAttributes ra) {
        clientService.deleteContact(id, contactId);
        ra.addFlashAttribute("success", "담당자가 삭제되었습니다.");
        return "redirect:/admin/clients/" + id + "?tab=contacts";
    }
}
