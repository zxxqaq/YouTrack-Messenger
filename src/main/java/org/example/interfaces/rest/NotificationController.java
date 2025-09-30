package org.example.interfaces.rest;

import jakarta.validation.constraints.Min;
import org.example.application.service.NotifyIssueService;
import org.example.infrastructure.scheduler.SchedulerProperties;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotifyIssueService notifyIssueService;
    private final SchedulerProperties schedulerProperties;

    public NotificationController(NotifyIssueService notifyIssueService, SchedulerProperties schedulerProperties) {
        this.notifyIssueService = notifyIssueService;
        this.schedulerProperties = schedulerProperties;
    }

    @PostMapping("/broadcast")
    public void broadcast(@RequestParam(name = "top", required = false) @Min(1) Integer top) throws Exception {
        int actualTop = (top != null) ? top : schedulerProperties.getTop();
        notifyIssueService.sendAllToPm(actualTop);
    }
}


