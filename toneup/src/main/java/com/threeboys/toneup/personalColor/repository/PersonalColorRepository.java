package com.threeboys.toneup.personalColor.repository;

import com.threeboys.toneup.personalColor.domain.PersonalColor;
import com.threeboys.toneup.personalColor.domain.PersonalColorType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonalColorRepository extends JpaRepository<PersonalColor, Integer> {

    Optional<PersonalColor> findByPersonalColorType(PersonalColorType personalColorType);
}
