package org.jcg.springboot.aws.s3.controller;

import org.jcg.springboot.aws.s3.model.Bucket;
import org.jcg.springboot.aws.s3.service.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping(value= "/s3")
public class S3Controller {

	@Autowired
	private Service service;

	@Value("${aws.s3.endpoint}")
	private String endpoint;

	@Value("${aws.s3.bucket}")
	private String bucketName;

	@PostMapping(value= "/upload")
	public ResponseEntity<String> uploadFile(@RequestPart(value= "file") final MultipartFile multipartFile) {
		service.uploadFile(multipartFile);
		String fileUrl = "";
		fileUrl = endpoint + "/" + bucketName + "/" + multipartFile.getName();
		return new ResponseEntity<>(fileUrl, HttpStatus.OK);
	}

	@PostMapping(value="/create-bucket")
	public void createBucket(@RequestBody Bucket bucket) {
		service.createBucket(bucket.getName());
	}

	@DeleteMapping(value="/delete-bucket/{name}")
	public void deleteBucket(@PathVariable("name") String name) {
		service.deleteBucket(name);
	}

	@DeleteMapping(value="/delete-file/bucket/{name}/file/{key}")
	public void deleteFile(@PathVariable("name") String name,@PathVariable("key") String key){
		service.deleteFile(name,key);
	}

	@GetMapping(value="/list-bucket/{name}/files")
	public List<String> listBucketFiles(@PathVariable("name") String name){
		return service.listBucketFiles(name);
	}

	@GetMapping (value ="/list-buckets")
	public List<com.amazonaws.services.s3.model.Bucket> getAllBuckets(){
		return service.getAllBuckets();
	}

}
