package com.tripfriend.global.util

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.IOException
import java.util.*

@Component
class ImageUtil {
    @Value("\${cloud.aws.credentials.access-key}")
    private lateinit var accessKey: String

    @Value("\${cloud.aws.credentials.secret-key}")
    private lateinit var secretKey: String

    @Value("\${cloud.aws.region.static}")
    private lateinit var region: String

    @Value("\${cloud.aws.s3.bucket}")
    private lateinit var bucketName: String

    @Value("\${cloud.aws.s3.base-url}")
    private lateinit var baseUrl: String

    // S3 클라이언트 초기화
    private fun getS3Client(): S3Client {
        val credentials = AwsBasicCredentials.create(accessKey, secretKey)
        return S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .build()
    }

    @Throws(IOException::class)
    fun saveImage(imageFile: MultipartFile?): String? {
        if (imageFile == null || imageFile.isEmpty) {
            return null // 이미지가 없으면 null 반환
        }

        try {
            // 고유한 파일명 생성 및 정규화 (알파벳, 숫자, 밑줄, 하이픈, 점만 허용)
            val originalName = imageFile.originalFilename ?: "unknown"
            val sanitizedOriginalName = originalName.replace(Regex("[^a-zA-Z0-9._-]"), "")
            val fileName = "images/${UUID.randomUUID()}_$sanitizedOriginalName"

            // S3 클라이언트 생성
            val s3Client = getS3Client()

            // S3에 업로드
            val request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(imageFile.contentType)
                .build()

            s3Client.putObject(request, RequestBody.fromInputStream(
                imageFile.inputStream,
                imageFile.size
            ))

            // S3 객체 URL 반환
            return "$baseUrl/$fileName"

        } catch (e: Exception) {
            throw IOException("S3 업로드 실패: ${e.message}", e)
        }
    }

    @Throws(IOException::class)
    fun deleteImage(imagePath: String?) {
        if (imagePath.isNullOrBlank()) {
            return
        }

        try {
            // S3 URL에서 키 추출
            // 예: https://bucket-name.s3.region.amazonaws.com/images/uuid_filename.jpg
            // 또는: https://custom-domain.com/images/uuid_filename.jpg
            val key = if (imagePath.contains(baseUrl)) {
                imagePath.substringAfter(baseUrl).removePrefix("/")
            } else {
                // URL 형식이 다른 경우 images/ 디렉토리를 기준으로 처리
                val startIndex = imagePath.indexOf("images/")
                if (startIndex != -1) {
                    imagePath.substring(startIndex)
                } else {
                    throw IOException("유효하지 않은 이미지 경로: $imagePath")
                }
            }

            // S3 클라이언트 생성
            val s3Client = getS3Client()

            // S3에서 객체 삭제
            val request = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build()

            s3Client.deleteObject(request)

        } catch (e: Exception) {
            throw IOException("S3 객체 삭제 실패: ${e.message}", e)
        }
    }
}
