package com.tripjoy.api.utils;

import org.hibernate.annotations.processing.SQL;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.stream.Collectors;

public class PageableUtils {

     // Pageable: camelCase (Java) -> snake_case (SQL)
     //For sql Native Query
    public static Pageable toSnakeCase(Pageable pageable) {
        if (pageable == null || !pageable.getSort().isSorted()) {
            return pageable;
        }

        List<Sort.Order> orders = pageable.getSort().stream()
                .map(order -> new Sort.Order(
                        order.getDirection(),
                        camelToSnake(order.getProperty()) // Hàm convert
                ))
                .collect(Collectors.toList());

        return PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(orders)
        );
    }

    private static String camelToSnake(String str) {
        return str.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
    }
}