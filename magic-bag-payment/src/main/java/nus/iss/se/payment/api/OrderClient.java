package nus.iss.se.payment.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import nus.iss.se.common.Result;
import nus.iss.se.payment.dto.OrderDto;

@FeignClient("magic-bag-order")
public interface OrderClient {
    
    @GetMapping("/order/{id}")
    Result<OrderDto> getOrderById(@PathVariable Integer id);
    
    @PutMapping("/order/{id}/status/internal")
    Result<Void> updateOrderStatus(
        @PathVariable Integer id, 
        @RequestParam String status
    );
}