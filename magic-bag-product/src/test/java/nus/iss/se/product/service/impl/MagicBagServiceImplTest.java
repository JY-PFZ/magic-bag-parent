//package nus.iss.se.product.service.impl;
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
//import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
//import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
//import nus.iss.se.product.dto.MagicBagCreateDto;
//import nus.iss.se.product.dto.MagicBagDto;
//import nus.iss.se.product.dto.MagicBagListResponse;
//import nus.iss.se.product.dto.MagicBagUpdateDto;
//import nus.iss.se.product.entity.MagicBag;
//import nus.iss.se.product.mapper.MagicBagMapper;
//
//import java.lang.reflect.Field;
//import java.time.LocalTime;
//import java.util.Arrays;
//import java.util.Date;
//import java.util.List;
//
//class MagicBagServiceImplTest {
//
//    @InjectMocks
//    private MagicBagServiceImpl magicBagService;
//
//    @Mock
//    private MagicBagMapper magicBagMapper;
//
//    @BeforeEach
//    void setup() throws Exception {
//        MockitoAnnotations.openMocks(this);
//        Field baseMapperField = magicBagService.getClass().getSuperclass().getDeclaredField("baseMapper");
//        baseMapperField.setAccessible(true);
//        baseMapperField.set(magicBagService, magicBagMapper);
//    }
//
//    @Test
//    void testGetMagicBagById_Success() {
//        MagicBag bag = new MagicBag();
//        bag.setId(1);
//        bag.setTitle("测试盲盒");
//        bag.setPrice(25.0f);
//        bag.setActive(true);
//        bag.setMerchantId(1);
//        when(magicBagMapper.selectOne(any(QueryWrapper.class))).thenReturn(bag);
//
//        MagicBagDto result = magicBagService.getMagicBagById(1);
//
//        assertNotNull(result);
//        assertEquals(1, result.getId());
//        assertEquals("测试盲盒", result.getTitle());
//        verify(magicBagMapper, times(1)).selectOne(any(QueryWrapper.class));
//    }
//
//    @Test
//    void testGetMagicBagById_NotFound() {
//        when(magicBagMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);
//        MagicBagDto result = magicBagService.getMagicBagById(999);
//        assertNull(result);
//        verify(magicBagMapper, times(1)).selectOne(any(QueryWrapper.class));
//    }
//
//    @Test
//    void testGetAllMagicBags_Success() {
//        MagicBag bag1 = new MagicBag();
//        bag1.setId(1);
//        bag1.setTitle("盲盒1");
//        bag1.setPrice(20.0f);
//        bag1.setActive(true);
//
//        MagicBag bag2 = new MagicBag();
//        bag2.setId(2);
//        bag2.setTitle("盲盒2");
//        bag2.setPrice(30.0f);
//        bag2.setActive(true);
//
//        Page<MagicBag> page = new Page<>(1, 10);
//        page.setRecords(Arrays.asList(bag1, bag2));
//        page.setTotal(2);
//        page.setPages(1);
//
//        when(magicBagMapper.selectPage(any(Page.class), any(QueryWrapper.class))).thenReturn(page);
//
//        MagicBagListResponse result = magicBagService.getAllMagicBags(1, 10);
//
//        assertNotNull(result);
//        assertEquals(2, result.getTotalItems());
//        assertEquals(1, result.getCurrentPage());
//        assertEquals(10, result.getPageSize());
//        assertNotNull(result.getMagicBags());
//        assertEquals(2, result.getMagicBags().size());
//    }
//
//    @Test
//    void testGetMagicBagsByCategory_Success() {
//        MagicBag bag = new MagicBag();
//        bag.setId(1);
//        bag.setTitle("面包盲盒");
//        bag.setCategory("面包");
//        bag.setPrice(20.0f);
//        bag.setActive(true);
//
//        when(magicBagMapper.findByCategory("面包")).thenReturn(Arrays.asList(bag));
//
//        List<MagicBagDto> result = magicBagService.getMagicBagsByCategory("面包");
//
//        assertNotNull(result);
//        assertEquals(1, result.size());
//        assertEquals("面包盲盒", result.get(0).getTitle());
//        verify(magicBagMapper, times(1)).findByCategory("面包");
//    }
//
//    @Test
//    void testGetMagicBagsByMerchantId_Success() {
//        MagicBag bag = new MagicBag();
//        bag.setId(1);
//        bag.setTitle("商户盲盒");
//        bag.setMerchantId(1);
//        bag.setPrice(25.0f);
//        bag.setActive(true);
//
//        when(magicBagMapper.findByMerchantId(1)).thenReturn(Arrays.asList(bag));
//
//        List<MagicBagDto> result = magicBagService.getMagicBagsByMerchantId(1);
//
//        assertNotNull(result);
//        assertEquals(1, result.size());
//        assertEquals(1, result.get(0).getMerchantId());
//        verify(magicBagMapper, times(1)).findByMerchantId(1);
//    }
//
//    @Test
//    void testCreateMagicBag_Success() {
//        MagicBagCreateDto dto = new MagicBagCreateDto();
//        dto.setMerchantId(1);
//        dto.setTitle("新盲盒");
//        dto.setPrice(30.0f);
//        dto.setQuantity(10);
//        dto.setPickupStartTime(LocalTime.of(9, 0));
//        dto.setPickupEndTime(LocalTime.of(18, 0));
//        dto.setAvailableDate(new Date());
//
//        when(magicBagMapper.insert(any(MagicBag.class))).thenAnswer(invocation -> {
//            MagicBag bag = invocation.getArgument(0);
//            bag.setId(1);
//            return 1;
//        });
//
//        MagicBagDto result = magicBagService.createMagicBag(dto);
//
//        assertNotNull(result);
//        assertEquals(1, result.getId());
//        assertEquals("新盲盒", result.getTitle());
//        verify(magicBagMapper, times(1)).insert(any(MagicBag.class));
//    }
//
//    @Test
//    void testUpdateMagicBag_Success() {
//        MagicBag existingBag = new MagicBag();
//        existingBag.setId(1);
//        existingBag.setTitle("旧标题");
//        existingBag.setPrice(20.0f);
//        existingBag.setActive(true);
//
//        MagicBagUpdateDto updateDto = new MagicBagUpdateDto();
//        updateDto.setTitle("新标题");
//        updateDto.setPrice(25.0f);
//
//        when(magicBagMapper.selectOne(any(QueryWrapper.class))).thenReturn(existingBag);
//        when(magicBagMapper.updateById(any(MagicBag.class))).thenReturn(1);
//
//        MagicBagDto result = magicBagService.updateMagicBag(1, updateDto);
//
//        assertNotNull(result);
//        assertEquals(1, result.getId());
//        verify(magicBagMapper, times(1)).selectOne(any(QueryWrapper.class));
//        verify(magicBagMapper, times(1)).updateById(any(MagicBag.class));
//    }
//
//    @Test
//    void testUpdateMagicBag_NotFound() {
//        MagicBagUpdateDto updateDto = new MagicBagUpdateDto();
//        updateDto.setTitle("新标题");
//
//        when(magicBagMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);
//
//        RuntimeException exception = assertThrows(RuntimeException.class,
//            () -> magicBagService.updateMagicBag(999, updateDto));
//
//        assertEquals("盲盒不存在或已被删除", exception.getMessage());
//        verify(magicBagMapper, times(1)).selectOne(any(QueryWrapper.class));
//        verify(magicBagMapper, never()).updateById(any());
//    }
//
//    @Test
//    void testDeleteMagicBag_Success() {
//        MagicBag bag = new MagicBag();
//        bag.setId(1);
//        bag.setActive(true);
//        when(magicBagMapper.selectById(1)).thenReturn(bag);
//        when(magicBagMapper.updateById(any(MagicBag.class))).thenReturn(1);
//
//        boolean result = magicBagService.deleteMagicBag(1);
//
//        assertTrue(result);
//        verify(magicBagMapper, times(1)).selectById(1);
//        verify(magicBagMapper, times(1)).updateById(argThat(m -> !m.isActive()));
//    }
//
//    @Test
//    void testDeleteMagicBag_NotFound() {
//        when(magicBagMapper.selectById(999)).thenReturn(null);
//
//        boolean result = magicBagService.deleteMagicBag(999);
//
//        assertFalse(result);
//        verify(magicBagMapper, times(1)).selectById(999);
//        verify(magicBagMapper, never()).updateById(any());
//    }
//}
//
