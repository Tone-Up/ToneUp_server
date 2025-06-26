package com.threeboys.toneup.common.controller;

import com.threeboys.toneup.common.request.FileNamesDTO;
import com.threeboys.toneup.common.response.PresignedUrlListResponseDTO;
import com.threeboys.toneup.common.response.PresignedUrlResponseDTO;
import com.threeboys.toneup.common.response.StandardResponse;
import com.threeboys.toneup.common.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class FileController {
    private final FileService fileService;

    @PostMapping("/uploads/presigned-urls")
    public ResponseEntity<?> getPresignedUrl(@RequestBody FileNamesDTO fileNames){
        List<PresignedUrlResponseDTO> files = fileService.getPreSignedUrl("images", fileNames);
        return ResponseEntity.ok(new StandardResponse<>(true, 0, "ok",new PresignedUrlListResponseDTO(files)));
    }


}
