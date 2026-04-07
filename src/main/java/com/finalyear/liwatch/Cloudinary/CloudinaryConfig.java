package com.finalyear.liwatch.Cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dta9p2gd1",
                "api_key", "865995491742914",
                "api_secret", "pD08X-ga8qnV0aQ8PbNDP3T8TvY",
                "secure", true
        ));
    }
}