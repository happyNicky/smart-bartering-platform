package com.finalyear.liwatch.Cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }


    public String upload(MultipartFile file,String folderName) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap("resource_type", "auto",
                        "folder",folderName
                        ));
        return uploadResult.get("secure_url").toString();
    }

    public List<String> uploadMultiple(List<MultipartFile> files,String folderName) throws IOException {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            String url = upload(file,folderName);  // reuse single-file upload method
            urls.add(url);
        }
        return urls;
    }
}