package com.threeboys.toneup.common.repository;

import com.threeboys.toneup.common.domain.ImageType;
import com.threeboys.toneup.common.domain.Images;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Images, Long> {

//    List<Images> findByRefIdOrderByImageOrderAsc(Long refId);

    @Modifying
    @Query("DELETE FROM Images i WHERE i.type = :type AND i.refId = :refId")
    void deleteByTypeAndRefId(@Param("type") ImageType type, @Param("refId") Long refId);

//    @Modifying
    @Query("SELECT i FROM Images i WHERE i.type = :type AND i.refId = :refId")
    List<Images> findByTypeAndRefId(@Param("type") ImageType type, @Param("refId") Long refId);


}
