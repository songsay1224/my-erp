package com.example.sayy.Controller;

import com.example.sayy.Service.ClientService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping("/admin/clients")
    public String list(@RequestParam(required = false) String status,
                       @RequestParam(required = false) String keyword,
                       Model model) {
        model.addAttribute("clients", clientService.getClients(status, keyword));
        model.addAttribute("selectedStatus", status);
        model.addAttribute("keyword", keyword);
        return "admin-clients";
    }

    @GetMapping("/admin/clients/new")
    public String newForm() {
        return "admin-client-new";
    }

    @PostMapping("/admin/clients")
    public String create(@RequestParam String name,
                         @RequestParam(required = false) String businessRegNo,
                         @RequestParam(required = false) String corporateRegNo,
                         @RequestParam(required = false) String ceoName,
                         @RequestParam(required = false) String industry,
                         @RequestParam(required = false) String phone,
                         @RequestParam(required = false) String email,
                         @RequestParam(required = false) String zipCode,
                         @RequestParam(required = false) String address,
                         @RequestParam(required = false) String addressDetail,
                         @RequestParam(required = false) String status,
                         @RequestParam(required = false) String memo,
                         RedirectAttributes ra) {
        try {
            Long id = clientService.createClient(name, businessRegNo, corporateRegNo, ceoName,
                    industry, phone, email, zipCode, address, addressDetail, status, memo);
            ra.addFlashAttribute("success", "거래처가 등록되었습니다.");
            return "redirect:/admin/clients/" + id;
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/clients/new";
        }
    }

    @GetMapping("/admin/clients/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("client", clientService.getClientOrThrow(id));
        return "admin-client-detail";
    }

    @GetMapping("/admin/clients/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("client", clientService.getClientOrThrow(id));
        return "admin-client-edit";
    }

    @PostMapping("/admin/clients/{id}")
    public String update(@PathVariable Long id,
                         @RequestParam String name,
                         @RequestParam(required = false) String businessRegNo,
                         @RequestParam(required = false) String corporateRegNo,
                         @RequestParam(required = false) String ceoName,
                         @RequestParam(required = false) String industry,
                         @RequestParam(required = false) String phone,
                         @RequestParam(required = false) String email,
                         @RequestParam(required = false) String zipCode,
                         @RequestParam(required = false) String address,
                         @RequestParam(required = false) String addressDetail,
                         @RequestParam(required = false) String status,
                         @RequestParam(required = false) String memo,
                         RedirectAttributes ra) {
        try {
            clientService.updateClient(id, name, businessRegNo, corporateRegNo, ceoName,
                    industry, phone, email, zipCode, address, addressDetail, status, memo);
            ra.addFlashAttribute("success", "거래처 정보가 수정되었습니다.");
            return "redirect:/admin/clients/" + id;
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/clients/" + id + "/edit";
        }
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

    // ===== 담당자 =====

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
        return "redirect:/admin/clients/" + id;
    }

    @PostMapping("/admin/clients/{id}/contacts/{contactId}/delete")
    public String deleteContact(@PathVariable Long id, @PathVariable Long contactId, RedirectAttributes ra) {
        clientService.deleteContact(id, contactId);
        ra.addFlashAttribute("success", "담당자가 삭제되었습니다.");
        return "redirect:/admin/clients/" + id;
    }
}
