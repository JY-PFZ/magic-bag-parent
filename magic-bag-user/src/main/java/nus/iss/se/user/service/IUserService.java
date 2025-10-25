package nus.iss.se.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import nus.iss.se.user.dto.RegisterReq;
import nus.iss.se.user.dto.UserDto;
import nus.iss.se.user.entity.User;

public interface IUserService extends IService<User> {
    User findByUsername(String username);

    void register(RegisterReq req);

    User updateUserInfo(User user);

    void editUser(UserDto dto);

    UserDto getUserProfile(String username);

    void activateUser(String username);

    UserDto getUserById(Integer id);
    
    IPage<User> getUserList(int pageNum, int pageSize, String role);
}
