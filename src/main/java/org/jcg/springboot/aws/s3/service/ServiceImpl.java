package org.jcg.springboot.aws.s3.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;

@org.springframework.stereotype.Service
public class ServiceImpl implements Service {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceImpl.class);

	@Autowired
	private AmazonS3 amazonS3;
	@Value("${aws.s3.bucket}")
	private String bucketName;

	@Value("${aws.s3.endpoint}")
	private String endpoint;

	@Value("${aws.s3.region}")
	private String region;

	@Override
	// @Async annotation ensures that the method is executed in a different background thread 
	// but not consume the main thread.
	@Async
	public void uploadFile(final MultipartFile multipartFile) {
		LOGGER.info("File upload in progress.");

		try {
			final File file = convertMultiPartFileToFile(multipartFile);
			uploadFileToS3Bucket(bucketName, file);
			LOGGER.info("File upload is completed.");

			file.delete();	// To remove the file locally created in the project folder.
		} catch (final AmazonServiceException ex) {
			LOGGER.info("File upload is failed.");
			LOGGER.error("Error= {} while uploading file.", ex.getMessage());
		}
	}

	@Override
	public void createBucket(String bucket_name) {
		final AmazonS3 s3 = amazonS3;
		Bucket b = null;
		if (s3.doesBucketExistV2(bucket_name)) {
			System.out.format("Bucket %s already exists.\n", bucket_name);
			b = getBucket(bucket_name);
		} else {
			try {
				b = s3.createBucket(bucket_name);
			} catch (AmazonS3Exception e) {
				System.err.println(e.getErrorMessage());
			}
		}
	}

	@Override
	public void deleteBucket(String bucket_name) {

		System.out.println("Deleting S3 bucket: " + bucket_name);
		final AmazonS3 s3 = amazonS3;
		try {
			System.out.println(" - removing objects from bucket");
			ObjectListing object_listing = s3.listObjects(bucket_name);
			while (true) {
				for (Iterator<?> iterator =
					 object_listing.getObjectSummaries().iterator();
					 iterator.hasNext(); ) {
					S3ObjectSummary summary = (S3ObjectSummary) iterator.next();
					s3.deleteObject(bucket_name, summary.getKey());
				}

				// more object_listing to retrieve?
				if (object_listing.isTruncated()) {
					object_listing = s3.listNextBatchOfObjects(object_listing);
				} else {
					break;
				}
			}

			System.out.println(" - removing versions from bucket");
			VersionListing version_listing = s3.listVersions(
					new ListVersionsRequest().withBucketName(bucket_name));
			while (true) {
				for (Iterator<?> iterator =
					 version_listing.getVersionSummaries().iterator();
					 iterator.hasNext(); ) {
					S3VersionSummary vs = (S3VersionSummary) iterator.next();
					s3.deleteVersion(
							bucket_name, vs.getKey(), vs.getVersionId());
				}

				if (version_listing.isTruncated()) {
					version_listing = s3.listNextBatchOfVersions(
							version_listing);
				} else {
					break;
				}
			}

			System.out.println(" OK, bucket ready to delete!");
			s3.deleteBucket(bucket_name);
		} catch (AmazonServiceException e) {
			System.err.println(e.getErrorMessage());
			System.exit(1);
		}
		System.out.println("Done!");
	}

	@Override
	public List<Bucket> getAllBuckets() {
		final AmazonS3 s3 = amazonS3;
		List<Bucket> buckets = s3.listBuckets();
		return buckets;
	}

	@Override
	public void deleteFile(String bucket, String keyName) {
		final DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucket, keyName);
		amazonS3.deleteObject(deleteObjectRequest);
		LOGGER.info("File deleted successfully.");

	}

	@Override
	public List<String> listBucketFiles(String bucket) {
		ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(bucket)/*.withMaxKeys(2)*/;
		ListObjectsV2Result result;

			result = amazonS3.listObjectsV2(req);
			List<String> keys = new ArrayList<>();
			for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
				System.out.printf(" - %s (size: %d)\n", objectSummary.getKey(), objectSummary.getSize());
				keys.add(objectSummary.getKey());
			}
			// If there are more than maxKeys keys in the bucket, get a continuation token
			// and list the next objects.
			String token = result.getNextContinuationToken();
			System.out.println("Next Continuation Token: " + token);
			req.setContinuationToken(token);
			return keys;
	}

	public static Bucket getBucket(String bucket_name) {
		final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.DEFAULT_REGION).build();
		Bucket named_bucket = null;
		List<Bucket> buckets = s3.listBuckets();
		for (Bucket b : buckets) {
			if (b.getName().equals(bucket_name)) {
				named_bucket = b;
			}
		}
		return named_bucket;
	}

	private File convertMultiPartFileToFile(final MultipartFile multipartFile) {
		final File file = new File(multipartFile.getOriginalFilename());
		try (final FileOutputStream outputStream = new FileOutputStream(file)) {
			outputStream.write(multipartFile.getBytes());
		} catch (final IOException ex) {
			LOGGER.error("Error converting the multi-part file to file= ", ex.getMessage());
		}
		return file;
	}

	private void uploadFileToS3Bucket(final String bucketName, final File file) {
		final String uniqueFileName = LocalDateTime.now() + "_" + file.getName();
		LOGGER.info("Uploading file with name= " + uniqueFileName);
		final PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, uniqueFileName, file);
		amazonS3.putObject(putObjectRequest);
	}
}
