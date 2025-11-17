//package nus.iss.se.product.controller;
//
//import static org.mockito.Mockito.*;
//import static org.junit.jupiter.api.Assertions.*;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import nus.iss.se.common.Result;
//import nus.iss.se.common.constant.ResultStatus;
//import nus.iss.se.product.dto.MagicBagCreateDto;
//import nus.iss.se.product.dto.MagicBagDto;
//import nus.iss.se.product.dto.MagicBagListResponse;
//import nus.iss.se.product.dto.MagicBagUpdateDto;
//import nus.iss.se.product.service.IMagicBagService;
//import nus.iss.se.product.service.S3StorageService;
//
//import java.math.BigDecimal;
//import java.time.LocalTime;
//import java.util.Date;
//
//class MagicBagControllerTest {
//
//    @InjectMocks
//    private MagicBagController magicBagController;
//
//    @Mock
//    private IMagicBagService magicBagService;
//
//    @Mock
//    private nus.iss.se.product.common.UserContextHolder userContextHolder;
//
//    @Mock
//    private S3StorageService s3StorageService;
//
//    @BeforeEach
//    void setup() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    void testGetMagicBagById_Success() {
//        MagicBagDto bag = new MagicBagDto();
//        bag.setId(1);
//        bag.setTitle("测试盲盒");
//        bag.setPrice(new BigDecimal("25.00"));
//        when(magicBagService.getMagicBagById(1)).thenReturn(bag);
//
//        Result<MagicBagDto> result = magicBagController.getMagicBagById(1);
//
//        assertNotNull(result);
//        assertEquals(ResultStatus.SUCCESS.getCode(), result.getCode());
//        assertNotNull(result.getData());
//        assertEquals(1, result.getData().getId());
//        assertEquals("测试盲盒", result.getData().getTitle());
//        verify(magicBagService, times(1)).getMagicBagById(1);
//    }
//
//    @Test
//    void testGetMagicBagById_NotFound() {
//        when(magicBagService.getMagicBagById(999)).thenReturn(null);
//
//        Result<MagicBagDto> result = magicBagController.getMagicBagById(999);
//
//        assertNotNull(result);
//        assertNotEquals(ResultStatus.SUCCESS.getCode(), result.getCode());
//        verify(magicBagService, times(1)).getMagicBagById(999);
//    }
//
//    @Test
//    void testGetAllMagicBags_Success() {
//        MagicBagListResponse response = new MagicBagListResponse();
//        response.setTotalItems(10L);
//        response.setCurrentPage(1);
//        response.setPageSize(10);
//        when(magicBagService.getAllMagicBags(1, 10)).thenReturn(response);
//
//        Result<MagicBagListResponse> result = magicBagController.getAllMagicBags(1, 10);
//
//        assertNotNull(result);
//        assertEquals(ResultStatus.SUCCESS.getCode(), result.getCode());
//        assertNotNull(result.getData());
//        assertEquals(10L, result.getData().getTotalItems());
//        verify(magicBagService, times(1)).getAllMagicBags(1, 10);
//    }
//
//    @Test
//    void testCreateMagicBag_Success() {
//        MagicBagCreateDto dto = new MagicBagCreateDto();
//        dto.setMerchantId(1);
//        dto.setTitle("新盲盒");
//        dto.setPrice(25.0f);
//        dto.setQuantity(10);
//        dto.setPickupStartTime(LocalTime.of(9, 0));
//        dto.setPickupEndTime(LocalTime.of(18, 0));
//        dto.setAvailableDate(new Date());
//
//        MagicBagDto created = new MagicBagDto();
//        created.setId(1);
//        created.setTitle("新盲盒");
//        when(magicBagService.createMagicBag(any(MagicBagCreateDto.class))).thenReturn(created);
//
//        Result<MagicBagDto> result = magicBagController.createMagicBag(dto);
//
//        assertNotNull(result);
//        assertEquals(ResultStatus.SUCCESS.getCode(), result.getCode());
//        assertNotNull(result.getData());
//        assertEquals(1, result.getData().getId());
//        verify(magicBagService, times(1)).createMagicBag(any(MagicBagCreateDto.class));
//    }
//
//    @Test
//    void testUpdateMagicBag_Success() {
//        MagicBagUpdateDto dto = new MagicBagUpdateDto();
//        dto.setTitle("更新后的标题");
//        dto.setPrice(30.0f);
//
//        MagicBagDto updated = new MagicBagDto();
//        updated.setId(1);
//        updated.setTitle("更新后的标题");
//        when(magicBagService.updateMagicBag(eq(1), any(MagicBagUpdateDto.class))).thenReturn(updated);
//
//        Result<MagicBagDto> result = magicBagController.updateMagicBag(1, dto);
//
//        assertNotNull(result);
//        assertEquals(ResultStatus.SUCCESS.getCode(), result.getCode());
//        assertNotNull(result.getData());
//        assertEquals("更新后的标题", result.getData().getTitle());
//        verify(magicBagService, times(1)).updateMagicBag(eq(1), any(MagicBagUpdateDto.class));
//    }
//
//    @Test
//    void testDeleteMagicBag_Success() {
//        when(magicBagService.deleteMagicBag(1)).thenReturn(true);
//
//        Result<Void> result = magicBagController.deleteMagicBag(1);
//
//        assertNotNull(result);
//        assertEquals(ResultStatus.SUCCESS.getCode(), result.getCode());
//        verify(magicBagService, times(1)).deleteMagicBag(1);
//    }
//
//    @Test
//    void testDeleteMagicBag_Failed() {
//        when(magicBagService.deleteMagicBag(999)).thenReturn(false);
//
//        Result<Void> result = magicBagController.deleteMagicBag(999);
//
//        assertNotNull(result);
//        assertNotEquals(ResultStatus.SUCCESS.getCode(), result.getCode());
//        verify(magicBagService, times(1)).deleteMagicBag(999);
//    }
//}
//
