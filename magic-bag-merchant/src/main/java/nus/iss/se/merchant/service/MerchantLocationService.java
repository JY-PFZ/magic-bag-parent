package nus.iss.se.merchant.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.iss.se.common.constant.RedisPrefix;
import nus.iss.se.merchant.dto.MerchantLocationDto;
import nus.iss.se.common.util.RedisUtil;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 商户地理位置服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MerchantLocationService {
    private final RedisUtil redisUtil;
    private final StringRedisTemplate redisTemplate;

    // 初始化：将店铺数据加载到 Redis GEO（可从数据库同步）
    @PostConstruct
    public void init() {
        Map<String, Point> stores = new HashMap<>();
        stores.put("S001", new Point(116.4074, 39.9042)); // 注意：先经度，后纬度
        stores.put("S002", new Point(116.3284, 39.9447));
        stores.put("S003", new Point(121.4737, 31.2304));
        stores.put("S004", new Point(114.0579, 22.5431));
        stores.put("S005", new Point(104.0667, 30.6555));

        GeoOperations<String, String> geoOps = redisTemplate.opsForGeo();

        stores.forEach((name, point) -> geoOps.add(RedisPrefix.MERCHANT_LOCATION.getCode(), point, name));
        log.info("Merchant location data initialized with {} stores", stores.size());
    }

    /**
     * 获取指定位置附近 N 公里内的店铺，按距离排序
     *
     * @param userLon 用户经度
     * @param userLat 用户纬度
     * @param radius  半径（公里）
     * @return 按距离升序排列的店铺列表
     */
    public List<MerchantLocationDto> getNearbyMerchants(double userLon, double userLat, double radius) {
        GeoOperations<String, String> geoOps = redisTemplate.opsForGeo();

        Circle circle = new Circle(userLon, userLat, Metrics.KILOMETERS.getMultiplier() * radius);

        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                .includeDistance()  // 包含距离
                .includeCoordinates() // 包含坐标
                .sortAscending()  // 按距离升序排序
                ;

        GeoResults<RedisGeoCommands.GeoLocation<String>> results = geoOps.radius(RedisPrefix.MERCHANT_LOCATION.getCode(), circle, args);

        List<MerchantLocationDto> ans = new ArrayList<>();
        if (results != null){
            ans = results.getContent().stream().map(geoLocation -> {
                String name = geoLocation.getContent().getName(); // 格式: "S001:朝阳店"
                Distance distance = geoLocation.getDistance();    // 距离
                Point coord = geoLocation.getContent().getPoint(); // 坐标

                String[] parts = name.split(":", 2);
                String id = parts[0];
                String storeName = parts.length > 1 ? parts[1] : "";

                MerchantLocationDto dto = new MerchantLocationDto();
                dto.setId(id);
                dto.setName(storeName);
                dto.setLatitude(BigDecimal.valueOf(coord.getY())); // Redis 返回的是 lat/lon，但 Point 是 x=lon, y=lat
                dto.setLongitude(BigDecimal.valueOf(coord.getX()));
                dto.setUnit(distance.getMetric().getAbbreviation());
                dto.setDistance(BigDecimal.valueOf(distance.getValue()).setScale(2, RoundingMode.HALF_UP).doubleValue());
                return dto;
            }).toList();
        }

        log.info("Found {} nearby merchants within {} km radius", ans.size(), radius);
        return ans;
    }
}
