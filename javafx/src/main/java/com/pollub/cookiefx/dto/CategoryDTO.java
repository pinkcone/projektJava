package com.pollub.cookiefx.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
public class CategoryDTO {


    private Long id;

    private String nazwa;

    private String opis;
    private List<Long> produktyIds;


    @Override
    public String toString() {
        return "CategoryDTO{" +
                "id=" + id +
                ", nazwa='" + nazwa + '\'' +
                ", opis='" + opis + '\'' +
                ", produktyIds=" + produktyIds +
                '}';
    }
}
