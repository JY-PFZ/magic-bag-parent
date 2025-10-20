package nus.iss.se.order.dto;


import lombok.Data;
import java.util.List;

@Data
public class OrderDetailResponse {
    private OrderDto order;
    private MagicBagInfo magicBag;
    private UserInfo user;
    private MerchantInfo merchant;
    private List<OrderVerificationDto> verifications;
    
    @Data
    public static class MagicBagInfo {
        private Integer id;
        private String title;
        private String description;
        private String category;
        private String imageUrl;
    }
    
    @Data
    public static class UserInfo {
        private Integer id;
        private String nickname;
        private String phone;
    }
    
    @Data
    public static class MerchantInfo {
        private Integer id;
        private String name;
        private String phone;
        private String address;
    }
}
