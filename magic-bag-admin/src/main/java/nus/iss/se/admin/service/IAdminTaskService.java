package nus.iss.se.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import nus.iss.se.admin.dto.TaskQo;
import nus.iss.se.admin.entity.AdminTask;
import nus.iss.se.admin.kafka.event.MerchantRegisterEvent;

import java.util.List;

public interface IAdminTaskService extends IService<AdminTask> {
    IPage<AdminTask> getTasks(TaskQo qo);

    void claimTask(Long taskId);
    void approveTask(Long taskId);
    void rejectTask(Long taskId,String comment);

}
