package nus.iss.se.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import nus.iss.se.admin.dto.TaskQo;
import nus.iss.se.admin.entity.AdminTask;
import nus.iss.se.admin.service.IAdminTaskService;
import nus.iss.se.common.Result;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class ApiController {
    private final IAdminTaskService adminTaskService;

    @GetMapping("/task")
    public Result<?> getTasks(TaskQo qo){
        IPage<AdminTask> tasks = adminTaskService.getTasks(qo);
        return Result.success(tasks);
    }

    @PostMapping("/task/{taskId}/claim")
    public Result<?> claimTask(@PathVariable Long taskId) {
        adminTaskService.claimTask(taskId);
        return Result.success();
    }

    @PostMapping("/task/{taskId}/approve")
    public Result<?> approveTask(@PathVariable Long taskId) {
        adminTaskService.approveTask(taskId);
        return Result.success();
    }

    @PostMapping("/task/{taskId}/reject")
    public Result<?> rejectTask(@PathVariable Long taskId, @RequestParam String comment) {
        adminTaskService.rejectTask(taskId,comment);
        return Result.success();
    }
}
