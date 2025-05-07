package com.example.demo.controller;

import java.util.Comparator;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.BMI;
import com.example.demo.responce.ApiResponse;

//@RestController免去撰寫 @ResponseBody 但若要透過JSP 渲染 則不可用
//@RequestMapping {"/api/home" , "/api/"} 變成 {"/home" , "/"}  統一省略 url前綴
@RestController
@RequestMapping("/api") //統一 url前綴
public class ApiController {
	/*
	 * 1.首頁
	 * 路徑 /api/home
	 * 路徑 /api/
	 * 網址 http://localhost:8080/api/home
	 * 網址 http://localhost:8080/api/
	 */
	@GetMapping(value = {"/home" , "/"})
	public String home() {		
		return "我是首頁";
	}
	
	/*
	 * 2. ?帶參數
	 * 路徑: /greet?name=Jogn&age=18
	 * 路徑: /greet?name=Mery
	 * 網址 http://localhost:8080/api/greet?name=Jogn&age=18
	 * 結果 Hi John , 18 (成年)
	 * 網址 http://localhost:8080/api/greet?name=Mery
	 * 結果 Hi Mery , 0 (未成年)
	 * 限制:name 必填 age可選 有初始值0
	 * 
	 * required = true 代表一定要有
	 */
	@GetMapping("/greet")
	public String greet(@RequestParam(value = "name", required = true) String username,
			            @RequestParam(value = "age", required = false,defaultValue = "0") Integer userage) {	
		String result = String.format("Hi %s %d (%s)",
				username,userage , userage > 18 ? "成年" : "未成年");
		return result;
	}
	
	//精簡寫法 方法名與請求參數名 相同
	@GetMapping("/greet2")
	public String greet2(@RequestParam String name,
			             @RequestParam(defaultValue = "0") Integer age) {	
		String result = String.format("Hi %s %d (%s)",
				name,age , age > 18 ? "成年" : "未成年");
		return result;
	}
	
	//練習
	
	/*
	 * 體重(公斤) / 身高2(公尺2)
	 */
	
	@GetMapping("/bmi")
	public String bmi(@RequestParam Integer h,
			          @RequestParam Integer w) {
	    double bmi = w / (Math.pow(h / 100.0,2) );
	    return String.format("BMI = %.2f", bmi);
	}
	//你chrom 有安裝jsonview 可以宣告這個 produces = "application/json;charset=utf-8" 會比較好看
	@GetMapping(value = "/bmi2", produces = "application/json;charset=utf-8")
	public ResponseEntity<ApiResponse<BMI>> calcBmi(@RequestParam(required = false) Double h,
	          			  @RequestParam(required = false) Double w) {
		if(h == null || w == null) {
			return ResponseEntity.badRequest().body(ApiResponse.error("請提供身高(h)或體重(w)"));
		}
		double bmi = w / Math.pow(h/100, 2);		
		return ResponseEntity.ok(ApiResponse.success("BMI 計算成功", new BMI(h, w, bmi)));
	}
	//--------------------------------------------------
	/**
	 * 5. 同名多筆資料
	 * 路徑: /age?age=17&age=21&age=20
	 * 網址: http://localhost:8080/api/age?age=17&age=21&age=20
	 * 請計算出平均年齡
	 * */
	@GetMapping(value = "/age", produces = "application/json;charset=utf-8")
	public ResponseEntity<ApiResponse<Object>> getAverage(@RequestParam("age") List<String> ages){
		//orElseGet(()->0 代表沒找到時 沒給任何數 即是0
		if(ages == null || ages.size() == 0) {
			return ResponseEntity.badRequest().body(ApiResponse.error("請輸入年齡"));
		}
		double avg = ages.stream().mapToInt(Integer::parseInt).average().orElseGet(()->0);
		Object map = Map.of("年齡",ages, "平均年齡",String.format("%.1f",avg));
		return ResponseEntity.ok(ApiResponse.success("計算成功", map));
	}
	/*
	 * 6. Lab 練習: 得到多筆 score 資料
	 * 路徑: "/exam?score=80&score=100&score=50&score=70&score=30"
	 * 網址: http://localhost:8080/api/exam?score=80&score=100&score=50&score=70&score=30
	 * 請自行設計一個方法，此方法可以
	 * 印出: 最高分=?、最低分=?、平均=?、總分=?、及格分數列出=?、不及格分數列出=?
	 */
	@GetMapping(value = "/exam", produces = "application/json;charset=utf-8")
	public ResponseEntity<ApiResponse<Object>> getExamInfo(@RequestParam(name = "score", required = false) List<Integer> scores){
		//orElseGet(()->0 代表沒找到時 沒給任何數 即是0
		if(scores == null || scores.size() == 0) {
			return ResponseEntity.badRequest().body(ApiResponse.error("請輸入分數"));
		}
		//統計物件 此物件只接收基本型別 所以需要mapToInt(Integer::intValue)轉換
		IntSummaryStatistics stat = scores.stream().mapToInt(Integer::intValue).summaryStatistics();
		//利用 Collectors.partitioningBy 分組
		// Key=true 及格分數 | key = false 不及格
		Map<Boolean, List<Integer>> resultMap = scores.stream()
				.collect(Collectors.partitioningBy(score -> score >= 60));
		Object data = Map.of(
				"最高分",stat.getMax(),
				"最低分",stat.getMin(),
				"平均分",stat.getAverage(),
				"總分",stat.getSum(),
				"及格",resultMap.get(true),
				"不及格",resultMap.get(false));
				
		return ResponseEntity.ok(ApiResponse.success("計算成功", data));
	}
}
