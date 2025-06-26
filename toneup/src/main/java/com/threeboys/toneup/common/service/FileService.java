package com.threeboys.toneup.common.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.threeboys.toneup.common.request.FileNamesDTO;
import com.threeboys.toneup.common.response.PresignedUrlResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final AmazonS3 amazonS3;

    /**
     * presigned url 발급
     * @param prefix 버킷 디렉토리 이름
     * @param fileNames 클라이언트가 전달한 파일명 리스트 파라미터
     * @return presigned url
     */
    public List<PresignedUrlResponseDTO> getPreSignedUrl(String prefix, FileNamesDTO fileNames) {
        List<PresignedUrlResponseDTO> files = new ArrayList<>();
        for(String fileName : fileNames.getFileNames()){
            if(prefix!=null && !prefix.isBlank()) {
                fileName = createPath(prefix, fileName);
            }

            GeneratePresignedUrlRequest generatePresignedUrlRequest = getGeneratePreSignedUrlRequest(bucket, fileName);
            URL url = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);
            PresignedUrlResponseDTO fileResponseDTO =  PresignedUrlResponseDTO.builder()
                    .fileName(fileName)
                    .uploadUrl(url.toString())
                    .build();
            files.add(fileResponseDTO);
        }
//
//        List<PresignedUrlResponseDTO> files = fileNames.getFileNames().stream()
//                .map(fileName -> {
//                    if (prefix != null && !prefix.isBlank()) {
//                        fileName = createPath(prefix, fileName);
//                    }
//                    GeneratePresignedUrlRequest request = getGeneratePreSignedUrlRequest(bucket, fileName);
//                    URL url = amazonS3.generatePresignedUrl(request);
//                    return PresignedUrlResponseDTO.builder()
//                            .fileName(fileName)
//                            .uploadUrl(url.toString())
//                            .build();
//                })
//                .collect(Collectors.toList());

        return files;
    }

    /**
     * 파일 업로드용(PUT) presigned url 생성
     * @param bucket 버킷 이름
     * @param fileName S3 업로드용 파일 이름
     * @return presigned url
     */
    private GeneratePresignedUrlRequest getGeneratePreSignedUrlRequest(String bucket, String fileName) {
        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucket, fileName)
                        .withMethod(HttpMethod.PUT)
                        .withExpiration(getPreSignedUrlExpiration());
//        generatePresignedUrlRequest.addRequestParameter(
//                Headers.S3_CANNED_ACL,
//                CannedAccessControlList.PublicRead.toString());
        return generatePresignedUrlRequest;
    }

    /**
     * presigned url 유효 기간 설정
     * @return 유효기간
     */
    private Date getPreSignedUrlExpiration() {
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000 * 60 * 2;
        expiration.setTime(expTimeMillis);
        return expiration;
    }

    /**
     * 파일 고유 ID를 생성
     * @return 36자리의 UUID
     */
    private String createFileId() {
        return UUID.randomUUID().toString();
    }

    /**
     * 파일의 전체 경로를 생성
     * @param prefix 디렉토리 경로
     * @return 파일의 전체 경로
     */
    private String createPath(String prefix, String fileName) {
        String fileId = createFileId();
        return String.format("%s/%s", prefix, fileId + fileName);
    }
}
