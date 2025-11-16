package nus.iss.se.merchant.controller;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.baomidou.mybatisplus.core.metadata.IPage;
import nus.iss.se.common.Result;
import nus.iss.se.common.constant.ResultStatus;
import nus.iss.se.common.cache.UserContext;
import nus.iss.se.common.exception.BusinessException;
import nus.iss.se.merchant.common.UserContextHolder;
import nus.iss.se.merchant.dto.MerchantDto;
import nus.iss.se.merchant.dto.MerchantLocationDto;
import nus.iss.se.merchant.dto.MerchantUpdateDto;
import nus.iss.se.merchant.service.IMerchantService;
import nus.iss.se.merchant.service.MerchantLocationService;

import java.util.Arrays;
import java.util.List;

class MerchantControllerTest {

    @InjectMocks
    private MerchantController merchantController;

    @Mock
    private IMerchantService merchantService;

    @Mock
    private UserContextHolder userContextHolder;

    @Mock
    private MerchantLocationService merchantLocationService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetMerchantById_Success() {
        MerchantDto merchant = new MerchantDto();
        merchant.setId(1);
        merchant.setName("测试商家");
        merchant.setStatus("approved");
        when(merchantService.getMerchantById(1)).thenReturn(merchant);

        Result<MerchantDto> result = merchantController.getMerchantById(1);

        assertNotNull(result);
        assertEquals(ResultStatus.SUCCESS.getCode(), result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().getId());
        assertEquals("测试商家", result.getData().getName());
        verify(merchantService, times(1)).getMerchantById(1);
    }

    @Test
    void testGetMerchantById_NotFound() {
        when(merchantService.getMerchantById(999)).thenReturn(null);

        Result<MerchantDto> result = merchantController.getMerchantById(999);

        assertNotNull(result);
        assertNotEquals(ResultStatus.SUCCESS.getCode(), result.getCode());
        verify(merchantService, times(1)).getMerchantById(999);
    }

    @Test
    void testGetAllMerchants_Success() {
        MerchantDto merchant1 = new MerchantDto();
        merchant1.setId(1);
        merchant1.setName("商家1");

        MerchantDto merchant2 = new MerchantDto();
        merchant2.setId(2);
        merchant2.setName("商家2");

        when(merchantService.getAllMerchants()).thenReturn(Arrays.asList(merchant1, merchant2));

        Result<List<MerchantDto>> result = merchantController.getAllMerchants();

        assertNotNull(result);
        assertEquals(ResultStatus.SUCCESS.getCode(), result.getCode());
        assertNotNull(result.getData());
        assertEquals(2, result.getData().size());
        verify(merchantService, times(1)).getAllMerchants();
    }

    @Test
    void testGetMyMerchantProfile_Success() {
        UserContext userContext = new UserContext();
        userContext.setId(1);
        userContext.setRole("MERCHANT");

        MerchantDto merchant = new MerchantDto();
        merchant.setId(1);
        merchant.setName("我的商家");

        when(userContextHolder.getCurrentUser()).thenReturn(userContext);
        when(merchantService.findByUserId(1)).thenReturn(merchant);

        Result<MerchantDto> result = merchantController.getMyMerchantProfile();

        assertNotNull(result);
        assertEquals(ResultStatus.SUCCESS.getCode(), result.getCode());
        assertNotNull(result.getData());
        assertEquals("我的商家", result.getData().getName());
        verify(userContextHolder, times(1)).getCurrentUser();
        verify(merchantService, times(1)).findByUserId(1);
    }

    @Test
    void testGetMyMerchantProfile_UserNotLoggedIn() {
        when(userContextHolder.getCurrentUser()).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
            () -> merchantController.getMyMerchantProfile());

        assertEquals(ResultStatus.FAIL, exception.getErrInfo());
        verify(merchantService, never()).findByUserId(any());
    }

    @Test
    void testGetMyMerchantProfile_NotMerchantRole() {
        UserContext userContext = new UserContext();
        userContext.setId(1);
        userContext.setRole("CUSTOMER");

        when(userContextHolder.getCurrentUser()).thenReturn(userContext);

        BusinessException exception = assertThrows(BusinessException.class,
            () -> merchantController.getMyMerchantProfile());

        assertEquals(ResultStatus.FAIL, exception.getErrInfo());
        verify(merchantService, never()).findByUserId(any());
    }

    @Test
    void testRegisterMerchantProfile_Success() {
        MerchantUpdateDto dto = new MerchantUpdateDto();
        dto.setName("测试商家");
        dto.setPhone("81234567");
        dto.setAddress("测试地址");
        dto.setBusinessLicense("LIC123");

        doNothing().when(merchantService).registerMerchant(any(MerchantUpdateDto.class));

        Result<Void> result = merchantController.registerMerchantProfile(dto);

        assertNotNull(result);
        assertEquals(ResultStatus.SUCCESS.getCode(), result.getCode());
        verify(merchantService, times(1)).registerMerchant(any(MerchantUpdateDto.class));
    }

    @Test
    void testUpdateMerchantProfile_Success() {
        MerchantUpdateDto dto = new MerchantUpdateDto();
        dto.setName("更新后的商家名");
        dto.setPhone("87654321");
        dto.setAddress("新地址");
        dto.setBusinessLicense("LIC456");

        UserContext userContext = new UserContext();
        userContext.setId(1);
        when(userContextHolder.getCurrentUser()).thenReturn(userContext);
        doNothing().when(merchantService).updateMerchantProfile(any(MerchantUpdateDto.class), eq(1));

        Result<Void> result = merchantController.updateMerchantProfile(dto);

        assertNotNull(result);
        assertEquals(ResultStatus.SUCCESS.getCode(), result.getCode());
        verify(merchantService, times(1)).updateMerchantProfile(any(MerchantUpdateDto.class), eq(1));
    }

    @Test
    void testUpdateMerchantProfile_UserNotLoggedIn() {
        MerchantUpdateDto dto = new MerchantUpdateDto();
        dto.setName("测试商家");

        when(userContextHolder.getCurrentUser()).thenReturn(null);

        Result<Void> result = merchantController.updateMerchantProfile(dto);

        assertNotNull(result);
        assertNotEquals(ResultStatus.SUCCESS.getCode(), result.getCode());
        verify(merchantService, never()).updateMerchantProfile(any(), any());
    }

    @Test
    void testGetNearby_Success() {
        MerchantLocationDto location1 = new MerchantLocationDto();
        location1.setId("1");
        location1.setName("附近商家1");

        MerchantLocationDto location2 = new MerchantLocationDto();
        location2.setId("2");
        location2.setName("附近商家2");

        when(merchantLocationService.getNearbyMerchants(103.8, 1.3, 1.0))
            .thenReturn(Arrays.asList(location1, location2));

        Result<List<MerchantLocationDto>> result = merchantController.getNearby(1.3, 103.8, 1.0);

        assertNotNull(result);
        assertEquals(ResultStatus.SUCCESS.getCode(), result.getCode());
        assertNotNull(result.getData());
        assertEquals(2, result.getData().size());
        verify(merchantLocationService, times(1)).getNearbyMerchants(103.8, 1.3, 1.0);
    }

    @Test
    void testSortedByScore_Success() {
        IPage<MerchantDto> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10);
        when(merchantService.sortedMerchantsByScore(1, 10, 4)).thenReturn(page);

        Result<IPage<MerchantDto>> result = merchantController.sortedByScore(1, 10, 4);

        assertNotNull(result);
        assertEquals(ResultStatus.SUCCESS.getCode(), result.getCode());
        assertNotNull(result.getData());
        verify(merchantService, times(1)).sortedMerchantsByScore(1, 10, 4);
    }
}

