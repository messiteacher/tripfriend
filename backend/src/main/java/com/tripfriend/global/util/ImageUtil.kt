package com.tripfriend.global.util

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*

@Component
class ImageUtil {
    @Value("\${file.upload-dir}") // 이미지 저장 경로
    private lateinit var uploadDir: String // uploadDir non-null 보장

    @Throws(IOException::class)
    fun saveImage(imageFile: MultipartFile?): String? {
        if (imageFile == null || imageFile.isEmpty) {
            return null // 이미지가 없으면 null 반환
        }

        // uploadDir 값 검증
        if (uploadDir.isBlank()) {
            throw IOException("업로드 디렉토리가 설정되어 있지 않습니다.")
        }

        // 디렉토리 생성 (없을 경우)
        val directory = File(uploadDir)
        if (!directory.exists() && !directory.mkdirs()) {
            throw IOException("업로드 디렉토리 생성 실패")
        }

        // 고유한 파일명 생성 및 정규화 (알파벳, 숫자, 밑줄, 하이픈, 점만 허용)
        val originalName = imageFile.originalFilename ?: "unknown"
        val sanitizedOriginalName = originalName.replace(Regex("[^a-zA-Z0-9._-]"), "")
        val fileName = UUID.randomUUID().toString() + "_" + sanitizedOriginalName

        // 파일 경로 생성 후 절대 경로 검증 (업로드 디렉토리 내에 존재하는지 확인)
        val filePath: Path = Paths.get(uploadDir, fileName).toAbsolutePath()
        if (!filePath.startsWith(Paths.get(uploadDir).toAbsolutePath())) {
            throw IOException("잘못된 파일 경로입니다.")
        }

        // 이미지 저장
        Files.copy(imageFile.inputStream, filePath, StandardCopyOption.REPLACE_EXISTING)

        return "/images/$fileName" // 저장된 파일의 상대 경로 반환
    }

    @Throws(IOException::class)
    fun deleteImage(imagePath: String?) {
        if (imagePath.isNullOrBlank()) {
            return
        }

        // "/images/" 로 시작하는 상대 경로에서 실제 파일명만 추출
        val fileName = imagePath.substring(imagePath.lastIndexOf("/") + 1)

        // 실제 파일 경로 생성
        val fullPath = Paths.get(uploadDir, fileName).toAbsolutePath()
        if (!fullPath.startsWith(Paths.get(uploadDir).toAbsolutePath())) {
            throw IOException("잘못된 파일 경로입니다.")
        }

        val file = fullPath.toFile()
        if (file.exists() && !file.delete()) {
            throw IOException("이미지 파일 삭제 실패: $fullPath")
        }
    }
}
