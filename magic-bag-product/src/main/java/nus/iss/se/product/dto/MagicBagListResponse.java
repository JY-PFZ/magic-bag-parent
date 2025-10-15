package nus.iss.se.product.dto;

import lombok.Data;
import java.util.List;

/**
 * MagicBag 分页响应DTO
 * 用于分页查询响应
 */
@Data
public class MagicBagListResponse {
    private List<MagicBagDto> magicBags;
    private Long totalItems;
    private Integer currentPage;
    private Integer pageSize;
    private Integer totalPages;
}


