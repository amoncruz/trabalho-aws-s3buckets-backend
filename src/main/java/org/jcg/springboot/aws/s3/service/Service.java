package org.jcg.springboot.aws.s3.service;

import com.amazonaws.services.s3.model.Bucket;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface Service {

	void uploadFile(MultipartFile multipartFile);
	void createBucket(String name);
	void deleteBucket(String name);
	List<Bucket> getAllBuckets();
	void deleteFile(String name,String key);
	List<String> listBucketFiles(String bucketName);
}
