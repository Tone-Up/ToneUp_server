package com.threeboys.toneup.chatbot.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
@Component
@RequiredArgsConstructor
public class ChatbotInitialization implements CommandLineRunner {
    private final VectorStore vectorStore;

    private static final String EMBEDDING_FILE = "qna_embeddings.json";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Document> loadDocumentsFromFile() {
        try {
            ClassPathResource resource = new ClassPathResource(EMBEDDING_FILE);
            File file = resource.getFile();

            if (!file.exists()) {
                System.out.println(file.toPath() + ":  file PAth");
                throw new RuntimeException("임베딩 파일이 존재하지 않습니다: " + EMBEDDING_FILE);
            }

            // 파일 읽기
            String json = Files.readString(file.toPath());

            // JSON -> List<Document>
            List<Document> documents = objectMapper.readValue(
                    json, new TypeReference<List<Document>>() {}
            );

            return documents;

        } catch (Exception e) {
            throw new RuntimeException("Document 파일 로드 실패", e);
        }
    }

    //chatbot QnA 관련 문서 임베딩 후 벡터 스토어 저장 과정
    @Override
    public void run(String... args) throws Exception {
        List<Document> documents = loadDocumentsFromFile();
//        System.out.println("Document Example : !!!!!!!!!!!!!!!!!!!!!!!! : "+ documents);
        vectorStore.add(documents);
    }
}
