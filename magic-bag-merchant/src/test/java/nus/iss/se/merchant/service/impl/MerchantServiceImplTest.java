package nus.iss.se.merchant.service.impl;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import nus.iss.se.common.constant.ResultStatus;
import nus.iss.se.common.exception.BusinessException;
import nus.iss.se.kafka.event.EventEnvelope;
import nus.iss.se.kafka.publisher.KafkaEventPublisher;
import nus.iss.se.merchant.common.UserContextHolder;
import nus.iss.se.merchant.dto.MerchantDto;
import nus.iss.se.merchant.dto.MerchantUpdateDto;
import nus.iss.se.merchant.entity.Merchant;
import nus.iss.se.merchant.mapper.MerchantMapper;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

class MerchantServiceImplTest {

    @InjectMocks
    private MerchantServiceImpl merchantService;

    @Mock
    private MerchantMapper merchantMapper;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private KafkaEventPublisher eventPublisher;

    @Mock
    private UserContextHolder userContextHolder;

    @BeforeEach
    void setup() throws Exception {
        MockitoAnnotations.openMocks(this);
        Field baseMapperField = merchantService.getClass().getSuperclass().getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(merchantService, merchantMapper);
    }

    @Test
    void testGetMerchantById_Success() {
        Merchant merchant = new Merchant();
        merchant.setId(1);
        merchant.setName("测试商家");
        merchant.setStatus("approved");
        when(merchantMapper.selectById(1)).thenReturn(merchant);

        MerchantDto result = merchantService.getMerchantById(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("测试商家", result.getName());
        verify(merchantMapper, times(1)).selectById(1);
    }

    @Test
    void testGetMerchantById_NotFound() {
        when(merchantMapper.selectById(999)).thenReturn(null);
        MerchantDto result = merchantService.getMerchantById(999);
        assertNull(result);
        verify(merchantMapper, times(1)).selectById(999);
    }

    @Test
    void testGetAllMerchants_Success() {
        Merchant merchant1 = new Merchant();
        merchant1.setId(1);
        merchant1.setName("商家1");
        merchant1.setStatus("approved");

        Merchant merchant2 = new Merchant();
        merchant2.setId(2);
        merchant2.setName("商家2");
        merchant2.setStatus("approved");

        when(merchantMapper.findAllApprovedMerchants()).thenReturn(Arrays.asList(merchant1, merchant2));

        List<MerchantDto> result = merchantService.getAllMerchants();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("商家1", result.get(0).getName());
        verify(merchantMapper, times(1)).findAllApprovedMerchants();
    }

    @Test
    void testFindByUserId_Success() {
        Merchant merchant = new Merchant();
        merchant.setId(1);
        merchant.setUserId(100);
        merchant.setName("测试商家");
        when(merchantMapper.findByUserId(100)).thenReturn(merchant);

        MerchantDto result = merchantService.findByUserId(100);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(100, merchant.getUserId());
        verify(merchantMapper, times(1)).findByUserId(100);
    }

    @Test
    void testFindByUserId_NotFound() {
        when(merchantMapper.findByUserId(999)).thenReturn(null);
        MerchantDto result = merchantService.findByUserId(999);
        assertNull(result);
        verify(merchantMapper, times(1)).findByUserId(999);
    }

    @Test
    void testRegisterMerchant_UserAlreadyRegistered() {
        MerchantUpdateDto dto = new MerchantUpdateDto();
        dto.setName("测试商家");
        dto.setPhone("81234567");
        dto.setAddress("测试地址");
        dto.setBusinessLicense("LIC123");

        when(userContextHolder.userId()).thenReturn(1);
        Merchant existingMerchant = new Merchant();
        existingMerchant.setId(1);
        when(merchantMapper.findByUserId(1)).thenReturn(existingMerchant);

        BusinessException exception = assertThrows(BusinessException.class,
            () -> merchantService.registerMerchant(dto));

        assertEquals(ResultStatus.FAIL, exception.getErrInfo());
        assertEquals("用户已经注册过商户", exception.getSupplementMessage());
        verify(merchantMapper, times(1)).findByUserId(1);
        verify(merchantMapper, never()).insert(any());
    }

    @Test
    void testRegisterMerchant_Success() throws Exception {
        MerchantUpdateDto dto = new MerchantUpdateDto();
        dto.setName("测试商家");
        dto.setPhone("81234567");
        dto.setAddress("测试地址");
        dto.setBusinessLicense("LIC123");

        when(userContextHolder.userId()).thenReturn(1);
        when(merchantMapper.findByUserId(1)).thenReturn(null);
        when(merchantMapper.insert(any(Merchant.class))).thenAnswer(invocation -> {
            Merchant m = invocation.getArgument(0);
            m.setId(1);
            return 1;
        });
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"userId\":1,\"merchantId\":1,\"shopName\":\"测试商家\"}");
        doNothing().when(eventPublisher).publish(any(EventEnvelope.class));

        assertDoesNotThrow(() -> merchantService.registerMerchant(dto));

        verify(merchantMapper, times(1)).findByUserId(1);
        verify(merchantMapper, times(1)).insert(any(Merchant.class));
        verify(eventPublisher, times(1)).publish(any(EventEnvelope.class));
    }

    @Test
    void testUpdateMerchantProfile_Success() {
        MerchantUpdateDto dto = new MerchantUpdateDto();
        dto.setName("更新后的商家名");
        dto.setPhone("87654321");
        dto.setAddress("新地址");
        dto.setBusinessLicense("LIC456");

        Merchant existingMerchant = new Merchant();
        existingMerchant.setId(1);
        existingMerchant.setUserId(100);
        existingMerchant.setName("旧商家名");

        when(merchantMapper.selectById(1)).thenReturn(existingMerchant);
        when(merchantMapper.updateById(any(Merchant.class))).thenReturn(1);

        // Mock getMerchantIdByUserId to return merchantId
        when(merchantMapper.selectById(100)).thenReturn(existingMerchant);

        assertDoesNotThrow(() -> merchantService.updateMerchantProfile(dto, 100));

        verify(merchantMapper, times(1)).selectById(100);
        verify(merchantMapper, times(1)).updateById(any(Merchant.class));
    }

    @Test
    void testUpdateMerchantProfile_MerchantNotFound() {
        MerchantUpdateDto dto = new MerchantUpdateDto();
        dto.setName("测试商家");

        when(merchantMapper.selectById(999)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
            () -> merchantService.updateMerchantProfile(dto, 999));

        assertEquals(ResultStatus.MERCHANT_NOT_FOUND, exception.getErrInfo());
        verify(merchantMapper, never()).updateById(any());
    }

    @Test
    void testSortedMerchantsByScore_Success() {
        Merchant merchant1 = new Merchant();
        merchant1.setId(1);
        merchant1.setScore(4.5);
        merchant1.setStatus("approved");

        Merchant merchant2 = new Merchant();
        merchant2.setId(2);
        merchant2.setScore(4.8);
        merchant2.setStatus("approved");

        Page<Merchant> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(merchant1, merchant2));
        page.setTotal(2);
        page.setPages(1);

        when(merchantMapper.findMerchantsByScore(any(Page.class), eq(4.0))).thenReturn(page);

        IPage<MerchantDto> result = merchantService.sortedMerchantsByScore(1, 10, 4);

        assertNotNull(result);
        assertEquals(2, result.getRecords().size());
        verify(merchantMapper, times(1)).findMerchantsByScore(any(Page.class), eq(4.0));
    }
}

