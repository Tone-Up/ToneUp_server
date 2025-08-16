//package com.threeboys.toneup.file;
//
//import com.amazonaws.services.s3.AmazonS3;
//import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.threeboys.toneup.common.request.FileNamesDTO;
//import com.threeboys.toneup.common.service.FileService;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.net.URI;
//import java.net.URL;
//import java.util.List;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//public class FileControllerTest {
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockitoBean
//    private AmazonS3 amazonS3; // S3 호출만 mock
//
//    @Autowired
//    private FileService fileService; // 실제 FileService 빈 사용
//
//    @Test
//    @WithMockUser(username = "testuser", roles = {"ROLE_USER"})
//    void generatePreSignedUrl() throws Exception {
//        // given
//        List<String> fileNames = List.of("image1.png", "image2.jpg");
//        FileNamesDTO requestDto = new FileNamesDTO();
//        requestDto.setFileNames(fileNames);
//
//        // amazonS3.generatePresignedUrl() mock
//        when(amazonS3.generatePresignedUrl(any(GeneratePresignedUrlRequest.class)))
//                .thenAnswer(invocation -> {
//                    GeneratePresignedUrlRequest req = invocation.getArgument(0);
//                    String key = req != null && req.getKey() != null ? req.getKey() : "default";
//                    return new URL("https://mock-s3/" + key);
//                });
//
//        // when & then
//        mockMvc.perform(post("/api/uploads/presigned-urls")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(new ObjectMapper().writeValueAsString(requestDto)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.data.files[0].fileName").value("image1.png"))
//                .andExpect(jsonPath("$.data.files[0].uploadUrl").value("https://mock-s3/images/image1.png"))
//                .andExpect(jsonPath("$.data.files[1].fileName").value("image2.jpg"))
//                .andExpect(jsonPath("$.data.files[1].uploadUrl").value("https://mock-s3/images/image2.jpg"));
//
//
//        // verify 호출 확인
//        verify(amazonS3, times(2)).generatePresignedUrl(any(GeneratePresignedUrlRequest.class));
//    }
//}
