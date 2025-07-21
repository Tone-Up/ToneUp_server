package com.threeboys.toneup.personalColor.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class PersonalColor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "type", unique = true)
    @Enumerated(EnumType.STRING)
    private PersonalColorType personalColorType;

    @Builder
    public PersonalColor(PersonalColorType personalColorType) {
        this.personalColorType = personalColorType;
    }
}
