package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Book {
	private Integer id; //序號
	private String name; //書名
	private Double price;
	private Integer amount; //數量
	private Boolean pub; //出刊 或 停刊
	
}
