package org.example.interfaces.rest;

import jakarta.validation.constraints.Min;
import org.example.application.service.NotifyIssueService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotifyIssueService notifyIssueService;

    public NotificationController(NotifyIssueService notifyIssueService) {
        this.notifyIssueService = notifyIssueService;
    }

    @PostMapping("/broadcast")
    public void broadcast(@RequestParam(name = "top", defaultValue = "20") @Min(1) int top) throws Exception {
        notifyIssueService.sendAllToGroup(top);
    }
}


