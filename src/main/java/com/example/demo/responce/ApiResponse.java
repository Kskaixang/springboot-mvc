package com.example.demo.responce;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//建立server 與 Client 在傳遞資料上的統一結構與標準(含錯誤)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
	//private Integer status; //狀態 例如 200(成功) 400(參數錯誤)
	private String message; //訊息
	T data; // payload 實際資料
	
	// 成功回應
	public static <T> ApiResponse<T> success(String message, T data) {
		return new ApiResponse<T>(message, data);
	}
	
	// 失敗回應
	public static <T> ApiResponse<T> error(String message) {
		return new ApiResponse<T>(message, null);
	}
}
