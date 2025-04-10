package com.AIT.Optimanage.Models;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.Sort;

@Setter
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Search {
    private Integer page;
    private Integer pageSize;
    private String sort;
    private Sort.Direction order;
}
