package com.threeboys.toneup.common.repository;

import com.threeboys.toneup.security.jwt.RefreshToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenRepository extends CrudRepository<RefreshToken, Long> {
}
