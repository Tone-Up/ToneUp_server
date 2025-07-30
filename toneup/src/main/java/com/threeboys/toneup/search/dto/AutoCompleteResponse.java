package com.threeboys.toneup.search.dto;

import com.redislabs.lettusearch.Suggestion;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class AutoCompleteResponse {
    private List<AutoCompleteDetailResponse> autoCompleteList;

    protected AutoCompleteResponse(List<AutoCompleteDetailResponse> autoCompleteList) {
        this.autoCompleteList = autoCompleteList;
    }

    public static AutoCompleteResponse toDto(List<Suggestion<String>> suggestionList) {
        List<AutoCompleteDetailResponse> autoCompleteDetailResponseList = new ArrayList<>();
        suggestionList.forEach(stringSuggestion -> {
            autoCompleteDetailResponseList.add(new AutoCompleteDetailResponse(stringSuggestion.getString()));
        });
        return new AutoCompleteResponse(autoCompleteDetailResponseList);
    }

}
